package cn.xgpjun.cypay.order;


import cn.xgpjun.cypay.CYPay;
import cn.xgpjun.cypay.Config;
import cn.xgpjun.cypay.Message;
import cn.xgpjun.cypay.qrcode.MapAndView;
import cn.xgpjun.cypay.qrcode.QRCodeRenderer;
import cn.xgpjun.cypay.qrcode.SignUtil;
import cn.xgpjun.cypay.listener.PayListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Order {

    private final UUID player;
    private final String orderNo;
    private String goodsInfo;
    private final String money;
    private final int points;
    private final OrderType type;
    private final List<String> commands;


    public Order(UUID player, String goodsInfo, String money, int points, OrderType orderType, List<String> commands){
        this.player = player;
        this.goodsInfo = goodsInfo;
        this.money = money;
        this.points = points;
        this.type = orderType;
        this.commands = commands;
        orderNo = generateOrdersNo(player);
    }

    public void submit(){
        switch (Config.getSite()){
            case "B":{
                submitB();
                break;
            }
            case "A":
            default: submitA();break;
        }
    }


    private void submitA(){
        Bukkit.getScheduler().runTaskAsynchronously(CYPay.getInstance(), () -> {
            String url = "https://pay.oi4.cn/mapi.php";

            // 请求参数
            Map<String, String> parameters = new HashMap<>();
            parameters.put("pid", Config.getId());
            parameters.put("type", type.type);
            parameters.put("out_trade_no", orderNo);
            parameters.put("notify_url", "http://www.pay.com/notify_url.php");
            parameters.put("return_url", "http://www.pay.com/return_url.php");
            parameters.put("name", goodsInfo);
            parameters.put("money", money);
            parameters.put("clientip", "35.206.212.168");
            parameters.put("device", "pc");
            parameters.put("param", "");
            String sign = SignUtil.generateSignature(parameters);
            parameters.put("sign", sign);
            parameters.put("sign_type", "MD5");

            try {
                URL obj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    if (postData.length() != 0) {
                        postData.append('&');
                    }
                    postData.append(param.getKey());
                    postData.append('=');
                    postData.append(param.getValue());
                }
                try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
                    wr.write(postDataBytes);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        // 解析JSON响应
                        String jsonResponse = response.toString();
                        JsonObject json = new JsonParser().parse(jsonResponse).getAsJsonObject();


                        String code = json.get("code").toString();
                        //成功
                        if (code.equals("1")) {
                            String text = String.valueOf(json.get("qrcode")).replace("\"", ""); // 要生成二维码的 URL
                            int width = 128; // 图像宽度
                            int height = 128; // 图像高度

                            // 设置二维码参数
                            Map<EncodeHintType, Object> hints = new HashMap<>();
                            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                            hints.put(EncodeHintType.MARGIN, 1); // 设置白边大小

                            try {
                                // 生成 BitMatrix 对象
                                BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);

                                MapAndView map = new QRCodeRenderer(bitMatrix).getMap(getPlayer());
                                //给与地图
                                sendOrderMessage(getPlayer(), type, money, orderNo);
                                Bukkit.getScheduler().runTask(CYPay.getInstance(), () -> {
                                    PayListener.sendMap(getPlayer(), map, orderNo, points, commands);
                                });
                            } catch (Exception e) {
                                CYPay.log("生成二维码时出现错误: " + e.getMessage());
                            }
                        } else {
                            String msg = String.valueOf(json.get("msg"));
                            if (!msg.equals("金额不合法")) {
                                CYPay.log(getPlayer().getDisplayName() + "订单生成失败！返回码：" + responseCode + "。插件正确配置了吗？");
                                CYPay.log("返回信息:" + msg);

                            }

                            getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.error + ChatColor.GREEN + msg));
                        }
                    }
                } else {
                    getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.error + ChatColor.GREEN + "请求失败，状态码：" + responseCode + "。可能：商品名有屏蔽词/服务器网站出现了问题。"));
                    CYPay.log(getPlayer().getDisplayName() + "的支付请求失败，状态码：" + responseCode + "可能：商品名有屏蔽词/服务器网站出现了问题。");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void submitB(){
        Bukkit.getScheduler().runTaskAsynchronously(CYPay.getInstance(),() -> {

            String url = "https://mpay.oi4.cn/pay/apisubmit";

            // 请求参数
            Map<String, String> parameters = new HashMap<>();
            parameters.put("pid", Config.getId());
            parameters.put("type", type.type);
            parameters.put("out_trade_no", orderNo);
            parameters.put("notify_url", "http://mpay.oi4.cn/notify_url.php");
            parameters.put("return_url", "http://mpay.oi4.cn/return_url.php");
            parameters.put("name", goodsInfo);
            parameters.put("money", money);
            String sign = SignUtil.generateSignature(parameters);

            try {
                goodsInfo = URLEncoder.encode(goodsInfo, "UTF-8");
                parameters.put("name", goodsInfo);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            parameters.put("sign", sign);
            parameters.put("sign_type", "MD5");

            try {
                URL obj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    if (postData.length() != 0) {
                        postData.append('&');
                    }
                    postData.append(param.getKey());
                    postData.append('=');
                    postData.append(param.getValue());
                }
                try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
                    wr.write(postDataBytes);
                }
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        // 解析JSON响应
                        String jsonResponse = response.toString();
                        JsonObject json = new JsonParser().parse(jsonResponse).getAsJsonObject();


                        String code = json.get("code").toString();
                        //成功
                        if(code.equals("200")){
                            String text = String.valueOf(json.get("qrcode")).replace("\"",""); // 要生成二维码的 URL
                            try {
                                text = URLDecoder.decode(text, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            int width = 128; // 图像宽度
                            int height = 128; // 图像高度

                            // 设置二维码参数
                            java.util.Map<EncodeHintType, Object> hints = new java.util.HashMap<>();
                            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                            hints.put(EncodeHintType.MARGIN, 1); // 设置白边大小

                            try {
                                // 生成 BitMatrix 对象
                                BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
                                MapAndView map = new QRCodeRenderer(bitMatrix).getMap(getPlayer());
                                //给与地图
                                String money = json.get("money").toString();
                                sendOrderMessage(getPlayer(),type,money,orderNo);
                                Bukkit.getScheduler().runTask(CYPay.getInstance(),()->{
                                    PayListener.sendMap(getPlayer(),map,orderNo,points,commands);
                                });
                            } catch (Exception e) {
                                CYPay.log("生成二维码时出现错误: " + e.getMessage());
                            }
                        }else {
                            String msg = String.valueOf(json.get("msg"));
                            if(!msg.equals("金额不合法")){
                                CYPay.log(getPlayer().getDisplayName()+"订单生成失败！返回码："+ responseCode +"。插件正确配置了吗？");
                                CYPay.log("返回信息:"+msg);

                            }
                            getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.error+ChatColor.GREEN+msg));

                        }
                    }
                } else {
                    getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.error+ChatColor.GREEN+"请求失败，状态码：" + responseCode +"。可能：商品名有屏蔽词/服务器网站出现了问题。") );
                    CYPay.log(getPlayer().getDisplayName()+"的支付请求失败，状态码："+ responseCode +"可能：商品名有屏蔽词/服务器网站出现了问题。");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Player getPlayer(){
        return Bukkit.getPlayer(player);
    }
    private static String generateOrdersNo(UUID uuid){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");
        String no = now.format(formatter);
        CYPay.log(Bukkit.getPlayer(uuid).getDisplayName()+"生成了订单:"+no);
        return no;
    }
    private static void sendOrderMessage(Player player,OrderType orderType,String money,String orderNo){
        switch (orderType){
            case WX:{
                for(String s: Message.wx){
                    s = s.replace("%money%",money).replace("%NO%",orderNo);
                    s = ChatColor.translateAlternateColorCodes('&',s);
                    player.sendMessage(s);
                }
                break;
            }
            case ZFB:{
                for(String s: Message.zfb){
                    s = s.replace("%money%",money).replace("%NO%",orderNo);
                    s = ChatColor.translateAlternateColorCodes('&',s);
                    player.sendMessage(s);
                }
                break;
            }
            case QQ:{
                for(String s: Message.qq){
                    s = s.replace("%money%",money).replace("%NO%",orderNo);
                    s = ChatColor.translateAlternateColorCodes('&',s);
                    player.sendMessage(s);
                }
                break;
            }
        }
    }
}

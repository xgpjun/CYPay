package cn.xgpjun.cypay.listener;

import cn.xgpjun.cypay.CYPay;
import cn.xgpjun.cypay.Config;
import cn.xgpjun.cypay.Message;
import cn.xgpjun.cypay.common.VersionAdapterUtil;
import cn.xgpjun.cypay.qrcode.MapAndView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PayListener implements Listener {
    //缓存所有订单
    public static Map<UUID,PayListener> listeners = new HashMap<>();
    final UUID uuid;
    final ItemStack rowItem;
    final MapAndView map;
    final BukkitTask cancelTask;
    final BukkitTask statusTask;
    final BukkitTask sendMap;
    final String orderNo;
    final int points;
    final List<String> commands;


    public static void sendMap(Player player, MapAndView map, String orderNo, int points, List<String> commands){
        if(listeners.containsKey(player.getUniqueId())){
            return;
        }
        new PayListener(player.getUniqueId(),VersionAdapterUtil.getItemInMainHand(player),map,orderNo,points,commands);

    }
    public PayListener(UUID uuid,ItemStack rowItem,MapAndView map,String orderNo,int points,List<String> commands){
        this.uuid = uuid;
        this.rowItem = rowItem;
        this.map = map;
        this.orderNo = orderNo;
        this.points = points;
        this.commands = commands;
        listeners.put(uuid,this);
        VersionAdapterUtil.setItemInMainHand(Bukkit.getPlayer(uuid),map.getItemStack());
        sendMap = Bukkit.getScheduler().runTaskTimer(CYPay.getInstance(),()->{
            Objects.requireNonNull(Bukkit.getPlayer(uuid)).sendMap(map.getMapView());
        },20L,20L);
        Bukkit.getPluginManager().registerEvents(this, CYPay.getInstance());
        cancelTask = Bukkit.getScheduler().runTaskLaterAsynchronously(CYPay.getInstance(),()-> cancel(), 20L *Config.getTimeout());

        statusTask = Bukkit.getScheduler().runTaskTimerAsynchronously(CYPay.getInstance(),()->{
            if(Config.getSite().equals("A")){
                try {
                    String apiUrl = "https://pay.oi4.cn/api.php?act=order&pid=" + Config.getId() + "&key=" + Config.getKey() + "&out_trade_no=" + orderNo;
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        String jsonResponse = response.toString();
                        JsonObject json = new JsonParser().parse(jsonResponse).getAsJsonObject();
                        if(String.valueOf(json.get("code")).equals("1")){
                            if(json.get("status").toString().replace("\"","").equals("1")){
                                //支付成功
                                CYPay.ppAPI.give(uuid,points);
                                Bukkit.getScheduler().runTask(CYPay.getInstance(),()->{
                                    for(String cmd:commands){
                                        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                                            cmd = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid),cmd);
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
                                    }
                                });
                                Bukkit.getScheduler().runTask(CYPay.getInstance(),()-> cancel());
                                Bukkit.getPlayer(uuid).sendMessage(ChatColor.translateAlternateColorCodes('&',Message.success));
                                CYPay.log("订单:"+orderNo+"支付成功");

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    String apiUrl = "https://mpay.oi4.cn/pay/chaorder?order_no="+orderNo+"&type=2";
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        String jsonResponse = response.toString();
                        JsonObject json = new JsonParser().parse(jsonResponse).getAsJsonObject();
                        if(String.valueOf(json.get("code")).equals("200")){
                            if(json.getAsJsonObject("data").get("status").toString().replace("\"","").equals("1")){
                                //支付成功
                                CYPay.ppAPI.give(uuid,points);
                                Bukkit.getScheduler().runTask(CYPay.getInstance(),()->{
                                    for(String cmd:commands){
                                        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                                            cmd = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid),cmd);
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
                                    }
                                });
                                Bukkit.getScheduler().runTask(CYPay.getInstance(),()-> cancel());
                                Bukkit.getPlayer(uuid).sendMessage(ChatColor.translateAlternateColorCodes('&',Message.success));
                                CYPay.log("订单:"+orderNo+"支付成功");

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        },40L,40L);

    }



    @EventHandler
    public void limit1(PlayerInteractEvent e){
        if(e.getPlayer().getUniqueId().equals(uuid)){
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.payMessage));
        }
    }

    @EventHandler
    public void limit2(InventoryClickEvent e){
        if(e.getWhoClicked().getUniqueId().equals(uuid)){
            e.setCancelled(true);
            e.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.payMessage));
        }
    }
    @EventHandler
    public void limit3(PlayerItemHeldEvent e){
        if(e.getPlayer().getUniqueId().equals(uuid)){
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.payMessage));
        }
    }
    @EventHandler
    public void limit4(PlayerSwapHandItemsEvent e){
        if(e.getPlayer().getUniqueId().equals(uuid)){
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.payMessage));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void cancel(PlayerDropItemEvent e){
        if(e.getPlayer().getUniqueId().equals(uuid)){
            e.getItemDrop().setItemStack(new ItemStack(Material.AIR));
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.cancelOrder));
            Bukkit.getScheduler().runTaskLater(CYPay.getInstance(),()->{
                cancel();
            },1L);
        }
    }
    @EventHandler
    public void quit(PlayerQuitEvent e){
        if(e.getPlayer().getUniqueId().equals(uuid)){
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Message.cancelOrder));
            cancel();
        }
    }

    public void cancel(){
        listeners.remove(uuid);
        HandlerList.unregisterAll(this);
        //删除物品
        Player player = Bukkit.getPlayer(uuid);
        VersionAdapterUtil.setItemInMainHand(player,new ItemStack(Material.AIR));
        if(rowItem!=null&&rowItem.getType()!=Material.AIR){
            VersionAdapterUtil.setItemInMainHand(player,rowItem);
        }

        cancelTask.cancel();
        statusTask.cancel();
        sendMap.cancel();
    }
}

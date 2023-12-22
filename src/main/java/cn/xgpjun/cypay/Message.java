package cn.xgpjun.cypay;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Message {
    public static List<String> ophelp;
    public static List<String> help;
    public static List<String> wx;
    public static List<String> zfb;
    public static List<String> qq;
    public static String itemName;
    public static String args;
    public static String argsErr;
    public static String cancelOrder;
    public static String error;
    public static String success;
    public static String noGoods;
    public static String payMessage;

    public static List<String> enableMessage;
    public static void init(){
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(new File(CYPay.getInstance().getDataFolder(), "message.yml"));
        ophelp = yml.getStringList("commands.ophelp");
        help = yml.getStringList("commands.help");
        wx = yml.getStringList("order.wx");
        zfb = yml.getStringList("order.zfb");
        qq = yml.getStringList("order.qq");
        itemName = yml.getString("itemName");
        args = yml.getString("args");
        argsErr = yml.getString("argsErr");
        cancelOrder = yml.getString("cancelOrder");
        error = yml.getString("error");
        success = yml.getString("success");
        noGoods = yml.getString("noGoods");
        payMessage = yml.getString("payMessage");

        enableMessage = Arrays.asList(
                "§l§d----------§aCYPay§d----------",
                "§6正在加载§l§aCYPay ",
                "§6版本: §a1.0.2",
                "§6Key状态: status*",
                "§l§d----------§aCYPay§d----------");
    }

}

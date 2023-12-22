package cn.xgpjun.cypay;

import lombok.Getter;
import org.bukkit.configuration.Configuration;

public class Config {
    @Getter
    private static String site;
    @Getter
    private static String id;
    @Getter
    private static String key;

    @Getter
    private static boolean vx;
    @Getter
    private static boolean zfb;
    @Getter
    private static boolean qq;
    @Getter
    private static int timeout;
    public static void loadConfig(){
        Configuration configuration = CYPay.getInstance().getConfig();
        site = configuration.getString("site","A");
        if(!site.equals("A")&&!site.equals("B")){
            site = "B";
        }
        timeout = configuration.getInt("timeout",120);
        id = configuration.getString("id");
        key = configuration.getString("key");
        vx = configuration.getBoolean("vx");
        zfb = configuration.getBoolean("zfb");
        qq = configuration.getBoolean("qq");

    }
}

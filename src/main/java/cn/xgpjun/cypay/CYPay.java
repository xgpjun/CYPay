package cn.xgpjun.cypay;

import cn.xgpjun.cypay.bstats.Metrics;
import cn.xgpjun.cypay.command.PayCommand;
import cn.xgpjun.cypay.listener.InventoryListener;
import cn.xgpjun.cypay.listener.PayListener;
import cn.xgpjun.cypay.shop.ShopLoader;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class CYPay extends JavaPlugin {
    private static CYPay instance;
    public static PlayerPointsAPI ppAPI;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;


        Metrics.enable();
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            try{
                Class<?> playerPointsClass = Class.forName("org.black_ixx.playerpoints.PlayerPoints");
                Method getInstanceMethod = playerPointsClass.getMethod("getInstance");
                ppAPI = PlayerPoints.getInstance().getAPI();
            }catch (Exception e){
                Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
                ppAPI = ((PlayerPoints) plugin).getAPI();
            }
        }
        Bukkit.getPluginCommand("cypay").setExecutor(new PayCommand());
        Bukkit.getPluginManager().registerEvents(new InventoryListener(),this);
        saveDefaultConfig();
        Config.loadConfig();
        boolean hasKey = Config.getKey().length()>5;

        if(!new File(getDataFolder(),"shop.yml").exists()){
            saveResource("shop.yml",false);
        }
        if(!new File(getDataFolder(),"message.yml").exists()){
            saveResource("message.yml",false);
        }
        Message.init();

        ShopLoader.init();
        for(String s:Message.enableMessage){
            Bukkit.getConsoleSender().sendMessage(s.replace("status*",hasKey?"§a已配置":"§c未配置"));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (PayListener payListener : PayListener.listeners.values()) {
            payListener.cancel();
        }
        HandlerList.unregisterAll(this);
        Bukkit.getPluginCommand("cypay").setExecutor(null);

    }

    public static CYPay getInstance() {
        return instance;
    }
    public static void log(String msg){
        getInstance().getLogger().info("[CYPay]"+msg);
        File log = new File(getInstance().getDataFolder(), "logs.yml");
        if (!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(log, true))) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日HH时mm分ss秒: ");
            String time = now.format(formatter);
            writer.write(time+msg);
            writer.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void reload(){
        Config.loadConfig();
        if(!new File(getDataFolder(),"shop.yml").exists()){
            saveResource("shop.yml",false);
        }
        if(!new File(getDataFolder(),"message.yml").exists()){
            saveResource("message.yml",false);
        }
        ShopLoader.init();
        Message.init();
    }
}

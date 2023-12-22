package cn.xgpjun.cypay.shop;

import cn.xgpjun.cypay.CYPay;
import cn.xgpjun.cypay.MyItem;
import cn.xgpjun.cypay.order.Order;
import cn.xgpjun.cypay.order.OrderType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopLoader {
    public static Map<String, Shop> shopMap = new HashMap<>();
    public static String title;
    public static void init(){
        shopMap.clear();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(new File(CYPay.getInstance().getDataFolder(),"shop.yml"));
        title = ChatColor.translateAlternateColorCodes('&', yml.getString("title",""));
        for(String s:yml.getKeys(false)){
            Material material = Material.valueOf(yml.getString(s+".item.material","CHEST").toUpperCase());

            MyItem item = new MyItem(material);
            item.setDisplayName(ChatColor.translateAlternateColorCodes('&',s));
            List<String> lore = yml.getStringList(s+".item.lore");
            lore.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
            item.setLore(lore);

            int points = yml.getInt(s+".points",0);
            String money = yml.getString(s+".money");
            List<String> commands = yml.getStringList(s+".command");
            if(money!=null){
                shopMap.put(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',s)),new Shop(points,money, ChatColor.translateAlternateColorCodes('&',s),commands,item.getItem()));
            }
        }
    }

    public static void buy(String name, Player player, OrderType orderType){
        if(!shopMap.containsKey(name)){
            return;
        }
        Shop shop =  shopMap.get(name);
        String goodsInfo = player.getDisplayName()+"购买"+shop.getName();
        new Order(player.getUniqueId(),goodsInfo,shop.getMoney(),shop.getPoints(),orderType,shop.getCommands()).submit();
    }
}

package cn.xgpjun.cypay.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;

public class VersionAdapterUtil {
    static String packet = Bukkit.getServer().getClass().getPackage().getName();
    static String version = packet.substring(packet.lastIndexOf('.') + 1);
    static int versionToInt = Integer.parseInt(version.split("_")[1]);

    public static void setItemInMainHand(Player player, ItemStack itemStack){
        if(versionToInt<9){
            player.setItemInHand(itemStack);
        }else {
            player.getInventory().setItemInMainHand(itemStack);
        }
    }

    public static ItemStack getItemInMainHand(Player player){
        if(versionToInt<9)
            return player.getItemInHand();
        else
            return player.getInventory().getItemInMainHand();
    }

}

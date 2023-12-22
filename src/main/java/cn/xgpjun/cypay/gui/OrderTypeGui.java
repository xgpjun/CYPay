package cn.xgpjun.cypay.gui;

import cn.xgpjun.cypay.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OrderTypeGui implements CYInventory{
    Inventory inv;
    static ItemStack[] glasses = new ItemStack[16];
    String preCommand;
    public OrderTypeGui(String preCommand){
        this.preCommand = preCommand;
    }

    /**
     * 数据值 0: 白色（White）
     * 数据值 1: 橙色（Orange）
     * 数据值 2: 品红色（Magenta）
     * 数据值 3: 淡蓝色（Light Blue）
     * 数据值 4: 黄色（Yellow）
     * 数据值 5: 黄绿色（Lime）
     * 数据值 6: 粉红色（Pink）
     * 数据值 7: 灰色（Gray）
     * 数据值 8: 淡灰色（Light Gray）
     * 数据值 9: 青色（Cyan）
     * 数据值 10: 紫色（Purple）
     * 数据值 11: 蓝色（Blue）
     * 数据值 12: 棕色（Brown）
     * 数据值 13: 绿色（Green）
     * 数据值 14: 红色（Red）
     * 数据值 15: 黑色（Black）
     */
    static {
        String packet = Bukkit.getServer().getClass().getPackage().getName();
        String version = packet.substring(packet.lastIndexOf('.') + 1);
        int versionToInt = Integer.parseInt(version.split("_")[1]);
        if(versionToInt<13){
            Material select = Material.valueOf("STAINED_GLASS_PANE");
            for(int i=0;i<16;i++){
                glasses[i] = new ItemStack(select,1,(byte)i);
            }
        }else {
            glasses[0] = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
            glasses[1] = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
            glasses[2] = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
            glasses[3] = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            glasses[4] = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            glasses[5] = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            glasses[6] = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
            glasses[7] = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            glasses[8] = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            glasses[9] = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
            glasses[10] = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
            glasses[11] = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
            glasses[12] = new ItemStack(Material.BROWN_STAINED_GLASS_PANE);
            glasses[13] = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            glasses[14] = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            glasses[15] = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        }
    }
    private void init(){
        inv = Bukkit.createInventory(this,27, ChatColor.GOLD+"选择支付方式");
        if(Config.isVx()){
            ItemStack vx = new ItemStack(glasses[13]);
            ItemMeta vxmt = vx.getItemMeta();
            vxmt.setDisplayName(ChatColor.GREEN+"使用微信充值");
            vx.setItemMeta(vxmt);
            inv.setItem(11,vx);
        }
        if(Config.isZfb()){
            ItemStack zfb = new ItemStack(glasses[3]);
            ItemMeta zfbmt = zfb.getItemMeta();
            zfbmt.setDisplayName(ChatColor.BLUE+"使用支付宝充值");
            zfb.setItemMeta(zfbmt);
            inv.setItem(13,zfb);
        }
        if(Config.isQq()){
            ItemStack qq = new ItemStack(glasses[11]);
            ItemMeta qqmt = qq.getItemMeta();
            qqmt.setDisplayName(ChatColor.AQUA+"使用qq充值");
            qq.setItemMeta(qqmt);
            inv.setItem(15,qq);
        }

    }
    @Override
    public void handleClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if(e.getInventory().getItem(slot)==null){
            return;
        }
        switch (slot){
            case 11:{
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),preCommand.replace("OrderType*","wx"));
                break;
            }
            case 13:{
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),preCommand.replace("OrderType*","zfb"));
                break;
            }
            case 15:{
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),preCommand.replace("OrderType*","qq"));
                break;
            }
        }
    }

    @Override
    public Inventory getInventory() {
        init();
        return inv;
    }
}

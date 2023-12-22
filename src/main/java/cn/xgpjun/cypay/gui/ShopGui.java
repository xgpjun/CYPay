package cn.xgpjun.cypay.gui;

import cn.xgpjun.cypay.shop.Shop;
import cn.xgpjun.cypay.shop.ShopLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.Iterator;

public class ShopGui implements CYInventory{
    Inventory inv;

    void init(){
        inv = Bukkit.createInventory(this,54, ShopLoader.title);
        Iterator<Shop> iterator = ShopLoader.shopMap.values().iterator();
        int index = 0;
        while (iterator.hasNext()){
            Shop shop = iterator.next();
            inv.setItem(index,shop.getDisplayItem());
            index++;
            if(index==54)
                break;
        }
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if(slot<inv.getSize()&&e.getCurrentItem()!=null){
            if(slot<ShopLoader.shopMap.size()){
                ItemStack i = inv.getItem(slot);
                if (i != null&&i.hasItemMeta()) {
                    Shop shop = ShopLoader.shopMap.get(ChatColor.stripColor(i.getItemMeta().getDisplayName()));
                    OrderTypeGui orderTypeGui = new OrderTypeGui("cypay buy name* OrderType* playerName*"
                            .replace("playerName*",e.getWhoClicked().getName())
                            .replace("name*",ChatColor.stripColor(shop.getName())));
                    e.getWhoClicked().openInventory(orderTypeGui.getInventory());
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        init();
        return inv;
    }
}

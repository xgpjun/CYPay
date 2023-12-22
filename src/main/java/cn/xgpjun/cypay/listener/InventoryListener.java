package cn.xgpjun.cypay.listener;

import cn.xgpjun.cypay.gui.CYInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void a(InventoryClickEvent e){
        if(e.getInventory().getHolder() instanceof CYInventory){
            e.setCancelled(true);
            ((CYInventory) e.getInventory().getHolder()).handleClick(e);
        }
    }
}

package cn.xgpjun.cypay.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface CYInventory extends InventoryHolder {

    void handleClick(InventoryClickEvent e);

}

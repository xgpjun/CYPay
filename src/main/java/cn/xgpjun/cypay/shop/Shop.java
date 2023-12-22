package cn.xgpjun.cypay.shop;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
@AllArgsConstructor
public class Shop {

    private int points;
    private String money;
    private String name;
    private List<String> commands;
    private ItemStack displayItem;
}

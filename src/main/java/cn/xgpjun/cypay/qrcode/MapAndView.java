package cn.xgpjun.cypay.qrcode;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

@AllArgsConstructor
@Data
public
class MapAndView {
    MapView mapView;
    ItemStack itemStack;

}


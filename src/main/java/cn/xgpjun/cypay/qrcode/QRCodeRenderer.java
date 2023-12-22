package cn.xgpjun.cypay.qrcode;

import cn.xgpjun.cypay.Message;
import cn.xgpjun.cypay.NMSUtil;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.server.v1_12_R1.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;


public class QRCodeRenderer extends MapRenderer {
    private final BitMatrix bitMatrix;
    private static Material map;
    private static boolean legacy;
    static {
        try {
            map = Material.FILLED_MAP;
            legacy =false;
        }catch (Error e){
            map = Material.valueOf("MAP");
            legacy = true;
        }

    }

    public QRCodeRenderer(BitMatrix bitMatrix) {
        this.bitMatrix = bitMatrix;
    }
    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (legacy){
            canvas.drawImage(0,0,MatrixToImageWriter.toBufferedImage(bitMatrix));

        }else {
            if (map.isLocked()) {
                return;
            }
            // 地图尺寸
            int mapWidth = 128;
            int mapHeight = 128;
            int matrixWidth = bitMatrix.getWidth();
            int matrixHeight = bitMatrix.getHeight();
            // 计算比例
            double scaleX = (double) matrixWidth / mapWidth;
            double scaleY = (double) matrixHeight / mapHeight;
            for (int x = 0; x < mapWidth; x++) {
                for (int y = 0; y < mapHeight; y++) {
                    // 使用比例将地图坐标映射到 BitMatrix 坐标
                    int bitX = (int) (x * scaleX);
                    int bitY = (int) (y * scaleY);
                    if (bitX >= 0 && bitX < matrixWidth && bitY >= 0 && bitY < matrixHeight) {
                        // 获取 BitMatrix 中的像素值
                        boolean bit = bitMatrix.get(bitX, bitY);
                        // 设置地图颜色
                        byte color = (bit) ? MapPalette.matchColor(Color.black) : MapPalette.matchColor(Color.white);
                        canvas.setPixel(x, y, color);
                    } else {
                        // 超出范围变绿色
                        canvas.setPixel(x, y, MapPalette.matchColor(Color.green));
                    }
                }
            }
            // 更新地图并锁定
            map.setLocked(true);

        }

    }

    public MapAndView getMap(Player player){
        if(legacy){
            ItemStack map = new ItemStack(QRCodeRenderer.map);
            MapMeta mapMeta = (MapMeta) map.getItemMeta();
            MapView mapView = Bukkit.createMap(player.getWorld());
            mapView.setUnlimitedTracking(false);
            for(MapRenderer renderer : mapView.getRenderers())
                mapView.removeRenderer(renderer);
            QRCodeRenderer qrCodeRenderer= this;

            mapView.addRenderer(qrCodeRenderer);

            mapMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Message.itemName));
            map.setItemMeta(mapMeta);
            map.setDurability(NMSUtil.getId(mapView));
            return new MapAndView(mapView,map);
        }else {
            ItemStack map = new ItemStack(QRCodeRenderer.map);
            MapMeta mapMeta = (MapMeta) map.getItemMeta();
            MapView mapView = Bukkit.createMap(player.getWorld());
            mapView.setTrackingPosition(false);
            for(MapRenderer renderer : mapView.getRenderers())
                mapView.removeRenderer(renderer);
            QRCodeRenderer qrCodeRenderer= this;
            mapView.addRenderer(qrCodeRenderer);
            mapMeta.setMapView(mapView);
            mapMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Message.itemName));
            map.setItemMeta(mapMeta);
            return new MapAndView(mapView,map);
        }
    }


}


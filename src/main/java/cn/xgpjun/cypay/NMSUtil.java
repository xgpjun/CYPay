package cn.xgpjun.cypay;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.map.MapView;

import java.lang.reflect.Method;

public class NMSUtil {
    private static Method getID;
    private static String OBC_PACKAGE = "";
    private final static String version;
    public static int versionToInt;

    static
    {
        String packet = Bukkit.getServer().getClass().getPackage().getName();
        version = packet.substring(packet.lastIndexOf('.') + 1);
        String nmsBaseHead = "net.minecraft.server.";
        versionToInt = Integer.parseInt(version.split("_")[1]);
        OBC_PACKAGE = "org.bukkit.craftbukkit." + version;
        try {
            getID = Class.forName(OBC_PACKAGE+".map.CraftMapView").getMethod("getId");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public static short getId(MapView mapView){
        try {
            return (short) getID.invoke(mapView);
        }catch (Exception e){
            return 0;
        }
    }
}

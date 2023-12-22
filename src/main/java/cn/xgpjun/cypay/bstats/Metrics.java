package cn.xgpjun.cypay.bstats;

import cn.xgpjun.cypay.CYPay;

public class Metrics {
    public static void enable() {
        int pluginId = 19775;
        org.bstats.bukkit.Metrics metrics = new org.bstats.bukkit.Metrics(CYPay.getInstance(), pluginId);
        metrics.addCustomChart(new org.bstats.bukkit.Metrics.SimplePie("chart_id", () -> "My value"));
    }
}

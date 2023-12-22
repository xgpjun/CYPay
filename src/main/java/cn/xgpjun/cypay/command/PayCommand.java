package cn.xgpjun.cypay.command;

import cn.xgpjun.cypay.CYPay;
import cn.xgpjun.cypay.Message;
import cn.xgpjun.cypay.shop.ShopLoader;
import cn.xgpjun.cypay.gui.ShopGui;
import cn.xgpjun.cypay.order.Order;
import cn.xgpjun.cypay.order.OrderType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PayCommand implements TabExecutor {
    /**
     * /cp gui
     * /cp points playerName OrderType money points goodsName
     * /cp buy name OrderType playerName
     * /cp reload
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int len = args.length;
        if(len < 1){
            help(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub){
            case "points":{
                if(!sender.isOp())
                    return true;
                if(len!=6){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',Message.args));
                    return true;
                }
                try{
                    Player player = Bukkit.getPlayer(args[1]);
                    OrderType type = OrderType.valueOf(args[2].toUpperCase());
                    String money = args[3];
                    int points = Integer.parseInt( args[4]);
                    String goodsName = args[5];
                    if (player != null) {
                        player.closeInventory();
                        new Order(player.getUniqueId(),player.getDisplayName()+"购买"+goodsName,money,points,type,new ArrayList<>()).submit();
                    }
                    return true;
                }catch (Exception e){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',Message.argsErr));                    return true;
                }
            }
            case "gui":{
                if(!(sender instanceof Player)){
                    return true;
                }
                Player player = (Player) sender;
                player.openInventory(new ShopGui().getInventory());
                return true;
            }
            case "buy":{
                if(!sender.isOp())
                    return true;
                if(len!=4){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',Message.args));
                    return true;
                }
                try{
                    String name = args[1];
                    OrderType type = OrderType.valueOf(args[2].toUpperCase());
                    Player player = Bukkit.getPlayer(args[3]);
                    if (player != null) {
                        if(!ShopLoader.shopMap.containsKey(name)){
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',Message.noGoods));
                            return true;
                        }
                        player.closeInventory();
                        ShopLoader.buy(name,player,type);
                        return true;
                    }
                    return true;
                }catch (Exception e){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',Message.argsErr));
                    return true;
                }
            }
            case "reload":{
                CYPay.getInstance().reload();
                sender.sendMessage(ChatColor.GOLD+ "[CYPay]"+ChatColor.GREEN +"重载成功");
                return true;
            }
            default: help(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        int len = args.length;
        if(sender.isOp()){
            switch (len){
                case 1:{
                    return filter(Arrays.asList("gui","points","buy","reload"),args);
                }
                case 2:{
                    if(args[0].equals("buy")){
                        return filter(new ArrayList<>(ShopLoader.shopMap.keySet()),args);
                    }
                    if(args[0].equals("points"))
                        return null;
                    break;
                }
                case 3:{
                    if(args[0].equals("points"))
                        return filter(Arrays.asList("wx","zfb","qq"),args);
                    if(args[0].equals("buy")){
                        return filter(Arrays.asList("wx","zfb","qq"),args);
                    }
                    break;
                }
                case 4:{
                    if(args[0].equals("points"))
                        return Arrays.asList("金额(CNY)");
                    if(args[0].equals("buy"))
                        return null;
                    break;
                }
                case 5:{
                    if(args[0].equals("points"))
                        return Arrays.asList("获得点券数量");
                    break;
                }
            }
        }else {
            switch (len){
                case 1:{
                    return filter(Arrays.asList("gui"),args);
                }
            }
        }

        return new ArrayList<>();
    }

    private void help(CommandSender sender){
        if(sender.isOp()){
            for(String s: Message.ophelp){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',s));
            }
        }else {
            for(String s: Message.help){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',s));
            }
        }
    }

    public static List<String> filter(List<String> list, String[] args) {
        String latest = null;
        if (args.length != 0) {
            latest = args[args.length - 1];
        }
        if (list.isEmpty() || latest == null)
            return list;
        String ll = latest.toLowerCase();
        List<String> filteredList = new ArrayList<>(list);
        filteredList.removeIf(k -> !k.toLowerCase().startsWith(ll));
        return filteredList;
    }
}

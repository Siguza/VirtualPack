// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.drgnome.virtualpack.util.*;

import static net.drgnome.virtualpack.util.Global.*;

public class VCommands implements CommandExecutor
{
    public static final HashMap<String, String> commandMap = new HashMap<String, String>();
    static
    {
        commandMap.put(VPlugin._components[0], "");
        commandMap.put("h", "help");
        commandMap.put("a", "admin");
        commandMap.put("v", "version");
        commandMap.put("s", "stats");
        commandMap.put("p", "price");
        commandMap.put("up", "update");
        commandMap.put("l", "link");
        commandMap.put("u", "unlink");
        commandMap.put("w", VPlugin._components[1]);
        commandMap.put("uc", VPlugin._components[2]);
        commandMap.put("c", VPlugin._components[3]);
        commandMap.put("f", VPlugin._components[4]);
        commandMap.put("b", VPlugin._components[5]);
        commandMap.put("e", VPlugin._components[6]);
        commandMap.put("b", VPlugin._components[7]);
    }
    
    public static String alias(String command)
    {
        for(String component : VPlugin._components)
        {
            List<String> list = Config.list("commands." + component);
            if(list.contains(command))
            {
                return component;
            }
        }
        return "";
    }
    
    public static String longname(String command)
    {
        command = command.toLowerCase();
        if(commandMap.containsKey(command))
        {
            return commandMap.get(command);
        }
        return command;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        cmd(sender, alias(label), args);
        return true;
    }
    
    private void cmd(CommandSender sender, String command, String[] args)
    {
        if(command.equals(VPlugin._components[0])) // Main
        {
            main(sender, args);
        }
        else if(!(sender instanceof Player))
        {
            sendMessage(sender, Lang.get("use.player"), ChatColor.RED);
            return;
        }
        else if(!Perm.has(sender, "vpack.use"))
        {
            sendMessage(sender, Lang.get("use.perm"), ChatColor.RED);
            return;
        }
        Player player = (Player)sender;
        if(!Config.bool(player.getWorld().getName(), "enabled"))
        {
            sendMessage(sender, Lang.get("world.disabled"), ChatColor.RED);
        }
        else if(command.equals(VPlugin._components[1])) // Workbench
        {
            
        }
        else if(command.equals(VPlugin._components[2])) // Uncrafter
        {
            
        }
        else if(command.equals(VPlugin._components[3])) // Chest
        {
            
        }
        else if(command.equals(VPlugin._components[4])) // Furnace
        {
            
        }
        else if(command.equals(VPlugin._components[5])) // Brewing Stand
        {
            
        }
        else if(command.equals(VPlugin._components[6])) // Enchanting Table
        {
            
        }
        else if(command.equals(VPlugin._components[7])) // Trash
        {
            
        }
        else // Unknown command
        {
            sendMessage(player, Lang.get("argument.unknown"), ChatColor.RED);
        }
    }
    
    private void main(CommandSender sender, String[] args)
    {
        if(args.length <= 0)
        {
            args = new String[]{"help"};
        }
        for(int i = 0; i < args.length; i++)
        {
            args[i] = args[i].toLowerCase();
        }
        String command = longname(args[0]);
        args = Util.copy(args, 1);
        if(command.equals("version"))
        {
            sendMessage(sender, Lang.get("version", VPlugin._version), ChatColor.BLUE);
        }
        else if(command.equals("update"))
        {
            update(sender);
        }
        else if(command.equals("help"))
        {
            help(sender, args);
        }
        else if(command.equals("admin"))
        {
            admin(sender, args);
        }
        else if(!(sender instanceof Player))
        {
            sendMessage(sender, Lang.get("use.player"), ChatColor.RED);
            return;
        }
        Player player = (Player)sender;
        if(!Config.bool(player.getWorld().getName(), "enabled"))
        {
            sendMessage(sender, Lang.get("world.disabled"), ChatColor.RED);
        }
        if(command.equals("stats"))
        {
            stats(player, args);
        }
        else if(command.equals("price"))
        {
            price(player);
        }
        else
        {
            cmd(player, command, args);
        }
    }
    
    private void update(CommandSender sender)
    {
        if(Perm.has(sender, "vpack.update"))
        {
            if(_plugin.checkUpdate())
            {
                sendMessage(sender, Lang.get("update.msg"), ChatColor.GREEN);
                sendMessage(sender, Lang.get("update.link"), ChatColor.GOLD);
            }
            else
            {
                sendMessage(sender, Lang.get("update.no"), ChatColor.GREEN);
            }
        }
        else
        {
            sendMessage(sender, Lang.get("update.perm"), ChatColor.RED);
        }
    }
    
    private void help(CommandSender sender, String[] args)
    {
        if(args.length <= 0)
        {
            args = new String[]{"1"};
        }
        if(args[0].equals("uncrafter"))
        {
            sendMessage(sender, Lang.get("help.uncrafter.title"), ChatColor.AQUA);
            sendMessage(sender, Lang.get("help.uncrafter.description"), ChatColor.AQUA);
        }
        else if(args[0].equals("link"))
        {
            sendMessage(sender, Lang.get("help.link.title"), ChatColor.AQUA);
            sendMessage(sender, Lang.get("help.link.description"), ChatColor.AQUA);
            sendMessage(sender, Lang.get("help.link.note"), ChatColor.RED);
        }
        else if(args[0].equals("commands"))
        {
            for(String component : VPlugin._components)
            {
                if(!component.equals(VPlugin._components[0]) && !Perm.has(sender, "vpack.use." + component))
                {
                    continue;
                }
                List<String> list = Config.list("commands." + component);
                if(list.size() <= 0)
                {
                    continue;
                }
                sendMessage(sender, component + ": " + Util.implode(", ", list.toArray(new String[0])), ChatColor.AQUA);
            }
        }
        else
        {
            int page = Util.tryParse(args[0], 1);
            List<String> list = Config.list("commands." + VPlugin._components[1]);
            String cmd = (list.size() <= 0) ? "" : list.get(0);
            sendMessage(sender, Lang.get("help.title"));
            sendMessage(sender, Lang.get("help.commands"));
            if(Perm.has(sender, "vpack.admin"))
            {
                sendMessage(sender, Lang.get("help.admin"), ChatColor.RED);
            }
            sendMessage(sender, Lang.get("help.version"));
            sendMessage(sender, Lang.get("help.stats"));
            sendMessage(sender, Lang.get("help.price"));
            if(Perm.has(sender, "vpack.use.workbench"))
            {
                sendMessage(sender, Lang.get("help.workbench.buy"));
                sendMessage(sender, Lang.get("help.workbench.use"));
            }
            if(Perm.has(sender, "vpack.use.uncrafter"))
            {
                sendMessage(sender, Lang.get("help.uncrafter.buy"));
                sendMessage(sender, Lang.get("help.uncrafter.use", "" + ChatColor.AQUA, "" + ChatColor.WHITE));
            }
            if(Perm.has(sender, "vpack.use.chest"))
            {
                sendMessage(sender, Lang.get("help.chest.buy"));
                sendMessage(sender, Lang.get("help.chest.use"));
                sendMessage(sender, Lang.get("help.chest.drop"));
                sendMessage(sender, Lang.get("help.chest.trash"));
            }
            if(Perm.has(sender, "vpack.use.furnace"))
            {
                sendMessage(sender, Lang.get("help.furnace.buy"));
                sendMessage(sender, Lang.get("help.furnace.use"));
                sendMessage(sender, Lang.get("help.furnace.link"));
                sendMessage(sender, Lang.get("help.furnace.unlink"));
                sendMessage(sender, Lang.get("help.link.info", "" + ChatColor.AQUA, "" + ChatColor.WHITE));
            }
            if(Perm.has(sender, "vpack.use.brewingstand"))
            {
                sendMessage(sender, Lang.get("help.brewingstand.buy"));
                sendMessage(sender, Lang.get("help.brewingstand.use"));
                sendMessage(sender, Lang.get("help.brewingstand.link"));
                sendMessage(sender, Lang.get("help.brewingstand.unlink"));
                sendMessage(sender, Lang.get("help.link.info", "" + ChatColor.AQUA, "" + ChatColor.WHITE));
            }
            if(Perm.has(sender, "vpack.use.enchanttable"))
            {
                sendMessage(sender, Lang.get("help.enchanttable.buy"));
                sendMessage(sender, Lang.get("help.enchanttable.use"));
                sendMessage(sender, Lang.get("help.enchanttable.book"));
            }
            if(Perm.has(sender, "vpack.use.trash"))
            {
                sendMessage(sender, Lang.get("help.trash"));
            }
            sendMessage(sender, Lang.get("help.more", ChatColor.GOLD + "http://dev.bukkit.org/server-mods/virtualpack/commands"));
        }
    }
    
    private void admin(CommandSender sender, String[] args)
    {
        // TODO
    }
    
    private void stats(Player player, String[] args)
    {
        // TODO
    }
    
    private void price(Player player)
    {
        sendMessage(player, lang("price.title"), ChatColor.AQUA);
        if(Money.world().enabled())
        {
            VPack pack = _plugin.getPack(player);
            final String y = "" + ChatColor.YELLOW;
            final String g = "" + ChatColor.GREEN;;
            sendMessage(player, Lang.get("price.workbench", y, g, "" + pack.priceWorkbenchBuy(), "" + pack.priceWorkbenchUse()));
            sendMessage(player, Lang.get("price.uncrafter", y, g, "" + pack.priceUncrafterBuy(), "" + pack.priceUncrafterUse()));
            sendMessage(player, Lang.get("price.enchanttable", y, g, "" + pack.priceEnchBuy(), "" + pack.priceEnchUse(), "" + pack.priceEnchBook(1)));
            sendMessage(player, Lang.get("price.chest", y, g, "" + pack.priceChestBuy(1), "" + pack.priceChestUse()));
            sendMessage(player, Lang.get("price.furnace", y, g, "" + pack.priceFurnaceBuy(1), "" + pack.priceFurnaceUse(), "" + pack.priceFurnaceLink()));
            sendMessage(player, Lang.get("price.brewingstand", y, g, "" + pack.priceBrewBuy(1), "" + pack.priceBrewUse(), "" + pack.priceBrewLink()));
        }
        else
        {
            sendMessage(player, lang("price.free"), ChatColor.AQUA);
        }
    }
}
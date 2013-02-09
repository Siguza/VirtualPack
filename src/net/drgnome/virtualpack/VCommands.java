// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftPlayer;
import net.drgnome.virtualpack.util.*;
import net.drgnome.virtualpack.item.ValuedItemStack;
import net.drgnome.virtualpack.data.TransmutationHelper;
import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.tmp.*; /** FUUU **/

import static net.drgnome.virtualpack.util.Global.*;

public class VCommands implements CommandExecutor
{
    public static final HashMap<String, String> commandMap = new HashMap<String, String>();
    static
    {
        commandMap.put(VPlugin._components[0], "");
        commandMap.put("h", "help");
        commandMap.put("ad", "admin");
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
        commandMap.put("t", VPlugin._components[7]);
        commandMap.put("a", VPlugin._components[9]);
        commandMap.put("m", VPlugin._components[10]);
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
        try
        {
            cmd(sender, alias(label), args);
        }
        catch(Throwable t)
        {
            sendMessage(sender, Lang.get("command.error"), ChatColor.RED);
            t.printStackTrace();
        }
        return true;
    }
    
    private void cmd(CommandSender sender, String command, String[] args)
    {
        if(sender instanceof Player)
        {
            _plugin.stopAnnoyingPlayer((Player)sender);
        }
        if(_plugin.isReloading())
        {
            sendMessage(sender, Lang.get("loading.single", Config.bool("load-multithreaded") ? Lang.get("loading.multi", _plugin.getLoadingProgress()) : ""), ChatColor.YELLOW);
            return;
        }
        else if(command.equals(VPlugin._components[0])) // Main
        {
            main(sender, args);
            return;
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
            sendMessage(player, Lang.get("world.disabled"), ChatColor.RED);
        }
        else
        {
            tools(player, _plugin.getPack(player), command, args, false);
        }
    }
    
    private void tools(Player player, VPack pack, String command, String args[], boolean admin)
    {
        /** An ugly workaround, to be replaced when VPack is fully using the Bukkit API **/
        if(!(player instanceof CraftPlayer))
        {
            Player p = Bukkit.getPlayer(player.getName());
            if((p == null) || !(p instanceof CraftPlayer))
            {
                sendMessage(player, Lang.get("vpack.nocraftplayer"), ChatColor.RED);
                return;
            }
            player = p;
        }
        if((player.getGameMode() == GameMode.CREATIVE) && (!Config.bool(player.getWorld().getName(), "allow-creative")) && (!admin) && (!Perm.has(player, "vpack.bypass.creative")))
        {
            sendMessage(player, Lang.get("vpack.nocreative"), ChatColor.RED);
        }
        else if(!Money.world(player.getWorld().getName()).enabled() && (args.length >= 1) && args[0].equalsIgnoreCase("buy"))
        {
            sendMessage(player, Lang.get("vpack.ecodisabled"), ChatColor.YELLOW);
        }
        else if(command.equals(VPlugin._components[1])) // Workbench
        {
            workbench(player, pack, args, admin);
        }
        else if(command.equals(VPlugin._components[2])) // Uncrafter
        {
            uncrafter(player, pack, args, admin);
        }
        else if(command.equals(VPlugin._components[3])) // Chest
        {
            chest(player, pack, args, admin);
        }
        else if(command.equals(VPlugin._components[4])) // Furnace
        {
            furnace(player, pack, args, admin);
        }
        else if(command.equals(VPlugin._components[5])) // Brewing Stand
        {
            brew(player, pack, args, admin);
        }
        else if(command.equals(VPlugin._components[6])) // Enchanting Table
        {
            ench(player, pack, args, admin);
        }
        else if(command.equals(VPlugin._components[7])) // Trash
        {
            trash(player, pack);
        }
        else if(command.equals(VPlugin._components[8]) && !admin) // Send
        {
            send(player, pack, args);
        }
        else if(command.equals(VPlugin._components[9])) // Anvil
        {
            anvil(player, pack, args, admin);
        }
        else if(command.equals(VPlugin._components[10])) // Materializer
        {
            matter(player, pack, args, admin);
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
        args = Util.cut(args, 1);
        if(command.equals("version"))
        {
            sendMessage(sender, Lang.get("version", VPlugin._version), ChatColor.BLUE);
            return;
        }
        else if(command.equals("update"))
        {
            update(sender);
            return;
        }
        else if(command.equals("help"))
        {
            help(sender, args);
            return;
        }
        else if(command.equals("admin"))
        {
            admin(sender, args);
            return;
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
        if(longname(args[0]).equals(VPlugin._components[2]))
        {
            sendMessage(sender, Lang.get("help.uncrafter.title"), ChatColor.AQUA);
            sendMessage(sender, Lang.get("help.uncrafter.description"), ChatColor.AQUA);
        }
        else if(longname(args[0]).equals(VPlugin._components[10]))
        {
            sendMessage(sender, Lang.get("help.matter.title"), ChatColor.AQUA);
            sendMessage(sender, Lang.get("help.matter.description"), ChatColor.AQUA);
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
                if((!component.equals(VPlugin._components[0]) && !Perm.has(sender, "vpack.use." + component)) || (component.equals(VPlugin._components[10]) && !Config.bool("transmutation.enabled")))
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
            List<String> list = Config.list("commands." + VPlugin._components[0]);
            String cmd = (list.size() <= 0) ? "" : list.get(0);
            sendMessage(sender, Lang.get("help.title"));
            sendMessage(sender, Lang.get("help.commands", cmd));
            if(Perm.has(sender, "vpack.admin"))
            {
                sendMessage(sender, Lang.get("help.admin", cmd), ChatColor.RED);
            }
            sendMessage(sender, Lang.get("help.version", cmd));
            sendMessage(sender, Lang.get("help.stats", cmd));
            sendMessage(sender, Lang.get("help.price", cmd));
            if(Perm.has(sender, "vpack.send"))
            {
                sendMessage(sender, Lang.get("help.send1", cmd), ChatColor.AQUA);
                sendMessage(sender, Lang.get("help.send2", cmd), ChatColor.AQUA);
            }
            if(Perm.has(sender, "vpack.send.copy"))
            {
                sendMessage(sender, Lang.get("help.send3", cmd), ChatColor.AQUA);
            }
            if(Perm.has(sender, "vpack.send.all"))
            {
                sendMessage(sender, Lang.get("help.send4", cmd), ChatColor.AQUA);
            }
            if(Perm.has(sender, "vpack.use.workbench"))
            {
                sendMessage(sender, Lang.get("help.workbench.buy", cmd));
                sendMessage(sender, Lang.get("help.workbench.use", cmd));
            }
            if(Perm.has(sender, "vpack.use.uncrafter"))
            {
                sendMessage(sender, Lang.get("help.uncrafter.buy", cmd));
                sendMessage(sender, Lang.get("help.uncrafter.use", cmd, "" + ChatColor.AQUA, "" + ChatColor.WHITE));
            }
            if(Perm.has(sender, "vpack.use.enchanttable"))
            {
                sendMessage(sender, Lang.get("help.enchanttable.buy", cmd));
                sendMessage(sender, Lang.get("help.enchanttable.use", cmd));
                sendMessage(sender, Lang.get("help.enchanttable.book", cmd));
            }
            if(Perm.has(sender, "vpack.use.anvil"))
            {
                sendMessage(sender, Lang.get("help.anvil.buy", cmd));
                sendMessage(sender, Lang.get("help.anvil.use", cmd));
            }
            if(Perm.has(sender, "vpack.use.materializer") && Config.bool("transmutation.enabled"))
            {
                sendMessage(sender, Lang.get("help.matter.buy", cmd));
                sendMessage(sender, Lang.get("help.matter.use", cmd, "" + ChatColor.AQUA, "" + ChatColor.WHITE));
                sendMessage(sender, Lang.get("help.matter.list", cmd));
            }
            if(Perm.has(sender, "vpack.use.chest"))
            {
                sendMessage(sender, Lang.get("help.chest.buy", cmd));
                sendMessage(sender, Lang.get("help.chest.use", cmd));
                sendMessage(sender, Lang.get("help.chest.drop", cmd));
                sendMessage(sender, Lang.get("help.chest.trash", cmd));
            }
            if(Perm.has(sender, "vpack.use.furnace"))
            {
                sendMessage(sender, Lang.get("help.furnace.buy", cmd));
                sendMessage(sender, Lang.get("help.furnace.use", cmd));
                sendMessage(sender, Lang.get("help.furnace.link", cmd));
                sendMessage(sender, Lang.get("help.furnace.unlink", cmd));
                sendMessage(sender, Lang.get("help.link.info", cmd, "" + ChatColor.AQUA, "" + ChatColor.WHITE));
            }
            if(Perm.has(sender, "vpack.use.brewingstand"))
            {
                sendMessage(sender, Lang.get("help.brewingstand.buy", cmd));
                sendMessage(sender, Lang.get("help.brewingstand.use", cmd));
                sendMessage(sender, Lang.get("help.brewingstand.link", cmd));
                sendMessage(sender, Lang.get("help.brewingstand.unlink", cmd));
                sendMessage(sender, Lang.get("help.link.info", cmd, "" + ChatColor.AQUA, "" + ChatColor.WHITE));
            }
            if(Perm.has(sender, "vpack.use.trash"))
            {
                sendMessage(sender, Lang.get("help.trash", cmd));
            }
            sendMessage(sender, Lang.get("help.more", ""));
            sender.sendMessage(ChatColor.GOLD + "http://dev.bukkit.org/server-mods/virtualpack/pages/commands"); // Thou shall not split my link! :P
        }
    }
    
    private void admin(CommandSender sender, String[] args)
    {
        if(!Perm.has(sender, "vpack.admin"))
        {
            sendMessage(sender, Lang.get("admin.perm"), ChatColor.RED);
            return;
        }
        if((args.length <= 0) || args[0].equalsIgnoreCase("help"))
        {
            List<String> list = Config.list("commands." + VPlugin._components[0]);
            String cmd = (list.size() <= 0) ? "" : list.get(0);
            sendMessage(sender, Lang.get("admin.help.title"), ChatColor.AQUA);
            sendMessage(sender, Lang.get("admin.help.reload", cmd), ChatColor.AQUA);
            sendMessage(sender, Lang.get("admin.help.reloaduser", cmd), ChatColor.AQUA);
            if(Config.bool("transmutation.enabled"))
            {
                sendMessage(sender, Lang.get("admin.help.listmatter", cmd), ChatColor.AQUA);
            }
            sendMessage(sender, Lang.get("admin.help.save", cmd), ChatColor.AQUA);
            sendMessage(sender, Lang.get("admin.help.savefile", cmd), ChatColor.AQUA);
            sendMessage(sender, Lang.get("admin.help.loadfile", cmd), ChatColor.AQUA);
            sendMessage(sender, Lang.get("admin.help.world", cmd), ChatColor.GOLD);
            sendMessage(sender, Lang.get("admin.help.use", cmd), ChatColor.AQUA);
            sendMessage(sender, Lang.get("admin.help.give", cmd), ChatColor.AQUA);
            sendMessage(sender, Lang.get("admin.help.take", cmd), ChatColor.AQUA);
            sendMessage(sender, Lang.get("admin.help.delete", cmd), ChatColor.AQUA);
            return;
        }
        args[0] = args[0].toLowerCase();
        if(args[0].equals("reload"))
        {
            if(args.length > 1)
            {
                for(String world : Config.worlds())
                {
                    if(_plugin.hasPack(world, args[1]))
                    {
                        _plugin.getPack(world, args[1]).recalculate();
                    }
                }
                sendMessage(sender, Lang.get("admin.reloadeduser", args[1]), ChatColor.YELLOW);
            }
            else
            {
                _plugin.saveUserData();
                _plugin.reloadConfig();
                _plugin.loadUserData();
                sendMessage(sender, Lang.get("admin.reloaded"), ChatColor.YELLOW);
            }
            return;
        }
        else if(args[0].equals("save"))
        {
            _plugin.saveUserData();
            sendMessage(sender, Lang.get("admin.saved"), ChatColor.YELLOW);
            return;
        }
        else if(args[0].equals("savefile"))
        {
            _plugin.saveUserData(true);
            sendMessage(sender, Lang.get("admin.saved"), ChatColor.YELLOW);
            return;
        }
        else if(args[0].equals("loadfile"))
        {
            if(!Config.bool("db.use"))
            {
                sendMessage(sender, Lang.get("admin.mysql"), ChatColor.YELLOW);
                return;
            }
            _plugin.saveUserData(true, "backup.db");
            _plugin.forceMysqlPort();
            _plugin.loadUserData();
            sendMessage(sender, Lang.get("admin.loaded"), ChatColor.YELLOW);
            return;
        }
        else if(args[0].equals("listmatter"))
        {
            if(Config.bool("transmutation.enabled"))
            {
                for(ValuedItemStack stack : TransmutationHelper.getAll())
                {
                    sendMessage(sender, stack.toString() + " = " + Util.formatDouble(stack.getValue()));
                }
            }
            else
            {
                sendMessage(sender, Lang.get("matter.disabled"), ChatColor.RED);
            }
            return;
        }
        else if(args.length < 2)
        {
            sendMessage(sender, Lang.get("argument.few"), ChatColor.RED);
            return;
        }
        String world;
        if(args[0].toLowerCase().startsWith("w:"))
        {
            world = args[0].substring(2);
            args = Util.cut(args, 1);
        }
        else
        {
            if(sender instanceof Player)
            {
                world = ((Player)sender).getWorld().getName();
            }
            else
            {
                sendMessage(sender, Lang.get("use.world"), ChatColor.RED);
                return;
            }
        }
        if(args[0].equals("give"))
        {
            give(world, sender, Util.cut(args, 1));
            return;
        }
        else if(args[0].equals("take"))
        {
            take(world, sender, Util.cut(args, 1));
            return;
        }
        else if(args[0].equals("delete"))
        {
            if(!Perm.has(sender, "vpack.admin.delete"))
            {
                sendMessage(sender, Lang.get("admin.perm"), ChatColor.RED);
                return;
            }
            if(!_plugin.hasPack(world, args[1]))
            {
                sendMessage(sender, Lang.get("vpack.none"), ChatColor.RED);
                return;
            }
            _plugin.setPack(world, args[1], null);
            sendMessage(sender, Lang.get("admin.delete", args[1]), ChatColor.GREEN);
        }
        else if(!(sender instanceof Player))
        {
            sendMessage(sender, Lang.get("use.player"), ChatColor.RED);
            return;
        }
        Player player = (Player)sender;
        if(args[0].equals("use"))
        {
            if(!Perm.has(player, "vpack.admin.use"))
            {
                sendMessage(player, Lang.get("admin.perm"), ChatColor.RED);
                return;
            }
            else if(args.length < 3)
            {
                sendMessage(player, Lang.get("argument.few"), ChatColor.RED);
                return;
            }
            tools(player, _plugin.getPack(player.getWorld().getName(), args[1]), longname(args[2]), Util.cut(args, 3), true);
        }
        else
        {
            sendMessage(sender, Lang.get("argument.unknown"), ChatColor.RED);
        }
    }
    
    private void give(String world, CommandSender sender, String[] args)
    {
        if(!Perm.has(sender, "vpack.admin.give"))
        {
            sendMessage(sender, Lang.get("admin.perm"), ChatColor.RED);
            return;
        }
        if(args.length < 2)
        {
            sendMessage(sender, Lang.get("argument.few"), ChatColor.RED);
            return;
        }
        if(!_plugin.hasPack(world, args[0]))
        {
            sendMessage(sender, Lang.get("vpack.none"), ChatColor.RED);
            return;
        }
        VPack pack = _plugin.getPack(world, args[0]);
        int amount = 1;
        if(args.length >= 3)
        {
            try
            {
                amount = Integer.parseInt(args[2]);
            }
            catch(Throwable t)
            {
                sendMessage(sender, Lang.get("argument.invalid"), ChatColor.RED);
            }
        }
        args[1] = longname(args[1]);
        if(args[1].equals("workbench"))
        {
            if(pack._hasWorkbench)
            {
                sendMessage(sender, Lang.get("admin.give.workbench.have"), ChatColor.RED);
            }
            else
            {
                pack._hasWorkbench = true;
                sendMessage(sender, Lang.get("admin.give.workbench.done", args[0]), ChatColor.GREEN);
            }
        }
        else if(args[1].equals("uncrafter"))
        {
            if(pack._hasUncrafter)
            {
                sendMessage(sender, Lang.get("admin.give.uncrafter.have"), ChatColor.RED);
            }
            else
            {
                pack._hasUncrafter = true;
                sendMessage(sender, Lang.get("admin.give.uncrafter.done", args[0]), ChatColor.GREEN);
            }
        }
        else if(args[1].equals("enchanttable"))
        {
            if(pack._hasEnchantTable)
            {
                sendMessage(sender, Lang.get("admin.give.enchanttable.have"), ChatColor.RED);
            }
            else
            {
                pack._hasEnchantTable = true;
                sendMessage(sender, Lang.get("admin.give.enchanttable.done", args[0]), ChatColor.GREEN);
            }
        }
        else if(args[1].equals("anvil"))
        {
            if(pack._hasAnvil)
            {
                sendMessage(sender, Lang.get("admin.give.anvil.have"), ChatColor.RED);
            }
            else
            {
                pack._hasAnvil = true;
                sendMessage(sender, Lang.get("admin.give.anvil.done", args[0]), ChatColor.GREEN);
            }
        }
        else if(args[1].equals("materializer"))
        {
            if(pack._matter == null)
            {
                /** FUUU **/
                // pack._matter = new MatterInv(pack.getWorld(), pack.getPlayer());
                pack._matter = new TmpMatterInv(pack.getWorld(), pack.getPlayer());
                sendMessage(sender, Lang.get("admin.give.matter.done", args[0]), ChatColor.GREEN);
            }
            else
            {
                sendMessage(sender, Lang.get("admin.give.matter.have"), ChatColor.RED);
            }
        }
        else if(args[1].equals("chest"))
        {
            for(int i = 0; i < amount; i++)
            {
                pack._chests.put((Integer)(pack._chests.size() + 1), new VInv(pack.getChestSize()));
            }
            sendMessage(sender, (amount == 1) ? Lang.get("admin.give.chest.one", args[0]) : Lang.get("admin.give.chest.many", args[0], "" + amount), ChatColor.GREEN);
        }
        else if(args[1].equals("furnace"))
        {
            for(int i = 0; i < amount; i++)
            {
                pack._furnaces.put((Integer)(pack._furnaces.size() + 1), new VTEFurnace(pack));
            }
            sendMessage(sender, (amount == 1) ? Lang.get("admin.give.furnace.one", args[0]) : Lang.get("admin.give.furnace.many", args[0], "" + amount), ChatColor.GREEN);
        }
        else if(args[1].equals("brewingstand"))
        {
            for(int i = 0; i < amount; i++)
            {
                pack._brews.put((Integer)(pack._brews.size() + 1), new VTEBrewingstand(pack));
            }
            sendMessage(sender, (amount == 1) ? Lang.get("admin.give.brewingstand.one", args[0]) : Lang.get("admin.give.brewingstand.many", args[0], "" + amount), ChatColor.GREEN);
        }
        else if(args[1].equals("book"))
        {
            int max = amount + pack._bookshelves > VPack._maxBookshelves ? VPack._maxBookshelves : amount + pack._bookshelves;
            pack._bookshelves += max;
            sendMessage(sender, (amount == 1) ? Lang.get("admin.give.book.one", args[0]) : Lang.get("admin.give.book.many", args[0], "" + max), ChatColor.GREEN);
        }
    }

    private void take(String world, CommandSender sender, String[] args)
    {
        if(!Perm.has(sender, "vpack.admin.take"))
        {
            sendMessage(sender, Lang.get("admin.perm"), ChatColor.RED);
            return;
        }
        if(args.length < 2)
        {
            sendMessage(sender, Lang.get("argument.few"), ChatColor.RED);
            return;
        }
        if(!_plugin.hasPack(world, args[0]))
        {
            sendMessage(sender, Lang.get("vpack.none"), ChatColor.RED);
        }
        VPack pack = _plugin.getPack(world, args[0]);
        int amount = 1;
        if(args.length >= 3)
        {
            try
            {
                amount = Integer.parseInt(args[2]);
            }
            catch(Throwable t)
            {
                sendMessage(sender, Lang.get("argument.invalid"), ChatColor.RED);
            }
        }
        args[1] = longname(args[1]);
        if(args[1].equals("workbench"))
        {
            if(!pack._hasWorkbench)
            {
                sendMessage(sender, Lang.get("admin.take.workbench.none"), ChatColor.RED);
            }
            else
            {
                pack._hasWorkbench = false;
                sendMessage(sender, Lang.get("admin.take.workbench.done", args[0]), ChatColor.GREEN);
            }
        }
        else if(args[1].equals("uncrafter"))
        {
            if(!pack._hasUncrafter)
            {
                sendMessage(sender, Lang.get("admin.take.uncrafter.none"), ChatColor.RED);
            }
            else
            {
                pack._hasUncrafter = false;
                sendMessage(sender, Lang.get("admin.take.uncrafter.done", args[0]), ChatColor.GREEN);
            }
        }
        else if(args[1].equals("enchanttable"))
        {
            if(!pack._hasEnchantTable)
            {
                sendMessage(sender, Lang.get("admin.take.enchanttable.none"), ChatColor.RED);
            }
            else
            {
                pack._hasEnchantTable = false;
                sendMessage(sender, Lang.get("admin.give.enchanttable.done", args[0]), ChatColor.GREEN);
            }
        }
        else if(args[1].equals("anvil"))
        {
            if(!pack._hasAnvil)
            {
                sendMessage(sender, Lang.get("admin.take.anvil.none"), ChatColor.RED);
            }
            else
            {
                pack._hasAnvil = false;
                sendMessage(sender, Lang.get("admin.take.anvil.done", args[0]), ChatColor.GREEN);
            }
        }
        else if(args[1].equals("materializer"))
        {
            if(pack._matter == null)
            {
                sendMessage(sender, Lang.get("admin.take.matter.none"), ChatColor.RED);
            }
            else
            {
                pack._matter = null;
                sendMessage(sender, Lang.get("admin.take.matter.done", args[0]), ChatColor.GREEN);
            }
        }
        else if(args[1].equals("chest"))
        {
            for(int i = 0; i < amount; i++)
            {
                if(pack._chests.containsKey((Integer)pack._chests.size()))
                {
                    pack._chests.remove((Integer)pack._chests.size());
                }
            }
            sendMessage(sender, (amount == 1) ? Lang.get("admin.take.chest.one", args[0]) : Lang.get("admin.take.chest.many", args[0], "" + amount), ChatColor.GREEN);
        }
        else if(args[1].equals("furnace"))
        {
            for(int i = 0; i < amount; i++)
            {
                if(pack._furnaces.containsKey((Integer)pack._furnaces.size()))
                {
                    pack._furnaces.remove((Integer)pack._furnaces.size());
                }
            }
            sendMessage(sender, (amount == 1) ? Lang.get("admin.take.furnace.one", args[0]) : Lang.get("admin.take.furnace.many", args[0], "" + amount), ChatColor.GREEN);
        }
        else if(args[1].equals("brewingstand"))
        {
            for(int i = 0; i < amount; i++)
            {
                if(pack._brews.containsKey((Integer)pack._brews.size()))
                {
                    pack._brews.remove((Integer)pack._brews.size());
                }
            }
            sendMessage(sender, (amount == 1) ? Lang.get("admin.take.brewingstand.one", args[0]) : Lang.get("admin.take.brewingstand.many", args[0], "" + amount), ChatColor.GREEN);
        }
        else if(args[1].equals("book"))
        {
            int max = pack._bookshelves - amount < 0 ? pack._bookshelves : amount;
            pack._bookshelves -= max;
            sendMessage(sender, (amount == 1) ? Lang.get("admin.take.book.one", args[0]) : Lang.get("admin.take.book.many", args[0], "" + max), ChatColor.GREEN);
        }
    }
    
    private void stats(Player player, String[] args)
    {
        if(args.length >= 1)
        {
            if(Perm.has(player, "vpack.stats"))
            {
                if(_plugin.hasPack(player.getWorld().getName(), args[0]))
                {
                    _plugin.getPack(player.getWorld().getName(), args[0]).printStats(player);
                }
                else
                {
                    sendMessage(player, Lang.get("stats.usernotfound"), ChatColor.RED);
                }
            }
            else
            {
                sendMessage(player, Lang.get("stats.allow"), ChatColor.RED);
            }
            return;
        }
        _plugin.getPack(player).printStats(player);
    }
    
    private void price(Player player)
    {
        sendMessage(player, Lang.get("price.title"), ChatColor.AQUA);
        if(Money.world(player.getWorld().getName()).enabled())
        {
            VPack pack = _plugin.getPack(player);
            final String y = ChatColor.YELLOW.toString();
            final String g = ChatColor.GREEN.toString();
            sendMessage(player, Lang.get("price.workbench", y, g, "" + pack.priceWorkbenchBuy(), "" + pack.priceWorkbenchUse()));
            sendMessage(player, Lang.get("price.uncrafter", y, g, "" + pack.priceUncrafterBuy(), "" + pack.priceUncrafterUse()));
            sendMessage(player, Lang.get("price.enchanttable", y, g, "" + pack.priceEnchBuy(), "" + pack.priceEnchUse(), "" + pack.priceEnchBook(1)));
            sendMessage(player, Lang.get("price.anvil", y, g, "" + pack.priceAnvilBuy(), "" + pack.priceAnvilUse()));
            sendMessage(player, Lang.get("price.matter", y, g, "" + pack.priceMatterBuy(), "" + pack.priceMatterUse()));
            sendMessage(player, Lang.get("price.chest", y, g, "" + pack.priceChestBuy(1), "" + pack.priceChestUse()));
            sendMessage(player, Lang.get("price.furnace", y, g, "" + pack.priceFurnaceBuy(1), "" + pack.priceFurnaceUse(), "" + pack.priceFurnaceLink()));
            sendMessage(player, Lang.get("price.brewingstand", y, g, "" + pack.priceBrewBuy(1), "" + pack.priceBrewUse(), "" + pack.priceBrewLink()));
        }
        else
        {
            sendMessage(player, Lang.get("price.free"), ChatColor.AQUA);
        }
    }
    
    private void workbench(Player player, VPack pack, String[] args, boolean admin)
    {
        if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.use.workbench"))
        {
            sendMessage(player, Lang.get("workbench.perm"), ChatColor.RED);
            return;
        }
        if((args.length >= 1) && (args[0].equalsIgnoreCase("buy")))
        {
            pack.buyWorkbench(player);
            return;
        }
        pack.openWorkbench(player, admin);
    }
    
    private void uncrafter(Player player, VPack pack, String[] args, boolean admin)
    {
        if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.use.uncrafter"))
        {
            sendMessage(player, Lang.get("uncrafter.perm"), ChatColor.RED);
            return;
        }
        if((args.length >= 1) && (args[0].equalsIgnoreCase("buy")))
        {
            pack.buyUncrafter(player);
            return;
        }
        pack.openUncrafter(player, admin);
    }
    
    private void chest(Player player, VPack pack, String[] args, boolean admin)
    {
        if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.use.chest"))
        {
            sendMessage(player, Lang.get("chest.perm"), ChatColor.RED);
            return;
        }
        int i = 1;
        if(args.length >= 1)
        {
            args[0] = args[0].toLowerCase();
            if(args[0].equals("buy") || args[0].equals("drop") || args[0].equals("trash"))
            {
                if(args.length >= 2)
                {
                    try
                    {
                        i = Integer.parseInt(args[1]);
                    }
                    catch(Throwable t)
                    {
                        sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                if(args[0].equals("buy"))
                {
                    pack.buyChest(player, i);
                }
                else if(args[0].equals("drop"))
                {
                    pack.dropChest(player, i);
                }
                else if(args[0].equals("trash"))
                {
                    pack.trashChest(player, i);
                }
                return;
            }
            else
            {
                try
                {
                    i = Integer.parseInt(args[0]);
                }
                catch(Throwable t)
                {
                    sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                    return;
                }
            }
        }
        pack.openChest(player, i, admin);
    }
    
    private void furnace(Player player, VPack pack, String[] args, boolean admin)
    {
        if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.use.furnace"))
        {
            sendMessage(player, Lang.get("furnace.perm"), ChatColor.RED);
            return;
        }
        int i = 1;
        if(args.length >= 1)
        {
            String par = longname(args[0]);
            if(par.equals("buy"))
            {
                if(args.length >= 2)
                {
                    try
                    {
                        i = Integer.parseInt(args[1]);
                    }
                    catch(Throwable t)
                    {
                        sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                pack.buyFurnace(player, i);
                return;
            }
            else if(par.equals("link"))
            {
                try
                {
                    pack.linkFurnace(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]), admin);
                }
                catch(Throwable t)
                {
                    sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                }
                return;
            }
            else if(par.equals("unlink"))
            {
                try
                {
                    pack.unlinkFurnace(player, Integer.parseInt(args[1]), admin);
                }
                catch(Throwable t)
                {
                    sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                }
                return;
            }
            else
            {
                try
                {
                    i = Integer.parseInt(args[0]);
                }
                catch(Throwable t)
                {
                    sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                    return;
                }
            }
        }
        pack.openFurnace(player, i, admin);
    }
    
    private void brew(Player player, VPack pack, String[] args, boolean admin)
    {
        if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.use.brewingstand"))
        {
            sendMessage(player, Lang.get("brewingstand.perm"), ChatColor.RED);
            return;
        }
        int i = 1;
        if(args.length >= 1)
        {
            String par = longname(args[0]);
            if(par.equals("buy"))
            {
                if(args.length >= 2)
                {
                    try
                    {
                        i = Integer.parseInt(args[1]);
                    }
                    catch(Throwable t)
                    {
                        sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                pack.buyBrewingstand(player, i);
                return;
            }
            else if(par.equals("link"))
            {
                try
                {
                    pack.linkBrewingstand(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]), admin);
                }
                catch(Throwable t)
                {
                    sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                }
                return;
            }
            else if(par.equals("unlink"))
            {
                try
                {
                    pack.unlinkBrewingstand(player, Integer.parseInt(args[1]), admin);
                }
                catch(Throwable t)
                {
                    sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                }
                return;
            }
            else
            {
                try
                {
                    i = Integer.parseInt(args[0]);
                }
                catch(Throwable t)
                {
                    sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                    return;
                }
            }
        }
        pack.openBrewingstand(player, i, admin);
    }
    
    private void ench(Player player, VPack pack, String[] args, boolean admin)
    {
        if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.use.enchanttable"))
        {
            sendMessage(player, Lang.get("enchanttable.perm"), ChatColor.RED);
            return;
        }
        if((args.length >= 1) && (args[0].equalsIgnoreCase("buy")))
        {
            if((args.length >= 2) && (args[1].equalsIgnoreCase("b")))
            {
                int amount = 1;
                if(args.length >= 3)
                {
                    try
                    {
                        amount = Integer.parseInt(args[2]);
                    }
                    catch(Throwable t)
                    {
                        sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                amount = amount <= 0 ? 1 : amount;
                pack.buyBookshelf(player, amount);
                return;
            }
            pack.buyEnchantTable(player);
            return;
        }
        pack.openEnchantTable(player, admin);
    }
    
    private void trash(Player player, VPack pack)
    {
        pack.openTrash(player);
    }
    
    private void send(Player player, VPack pack, String[] args)
    {
        if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.send"))
        {
            sendMessage(player, Lang.get("send.perm"), ChatColor.RED);
            return;
        }
        if(args.length <= 0)
        {
            sendMessage(player, Lang.get("argument.few"), ChatColor.RED);
            return;
        }
        boolean copy = false;
        if(args[0].equalsIgnoreCase("-copy"))
        {
            if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.send.copy"))
            {
                sendMessage(player, Lang.get("send.copy.perm"), ChatColor.RED);
                return;
            }
            args = Util.cut(args, 1);
            copy = true;
        }
        else if(args[0].equalsIgnoreCase("-all"))
        {
            if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.send.all"))
            {
                sendMessage(player, Lang.get("send.all.perm"), ChatColor.RED);
                return;
            }
            copy = true;
        }
        int i = 0;
        if(args.length >= 2)
        {
            try
            {
                i = Integer.parseInt(args[1]);
            }
            catch(Throwable t)
            {
                sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                return;
            }
        }
        pack.sendItem(player, args[0], i, copy);
    }
    
    private void anvil(Player player, VPack pack, String[] args, boolean admin)
    {
        if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.use.anvil"))
        {
            sendMessage(player, Lang.get("anvil.perm"), ChatColor.RED);
            return;
        }
        if((args.length >= 1) && (args[0].equalsIgnoreCase("buy")))
        {
            pack.buyAnvil(player);
            return;
        }
        pack.openAnvil(player, admin);
    }
    
    private void matter(Player player, VPack pack, String[] args, boolean admin)
    {
        if(!Config.bool("transmutation.enabled"))
        {
            sendMessage(player, Lang.get("matter.disabled"), ChatColor.RED);
            return;
        }
        if(!Perm.has(player.getWorld().getName(), pack.getPlayer(), "vpack.use.materializer"))
        {
            sendMessage(player, Lang.get("matter.perm"), ChatColor.RED);
            return;
        }
        if(args.length >= 1)
        {
            if(args[0].equalsIgnoreCase("buy"))
            {
                pack.buyMaterializer(player);
                return;
            }
            else if(args[0].equalsIgnoreCase("list"))
            {
                int page = 1;
                if(args.length >= 2)
                {
                    try
                    {
                        page = Integer.parseInt(args[1]);
                        if(page < 1)
                        {
                            page = 1;
                        }
                    }
                    catch(Throwable t)
                    {
                        sendMessage(player, Lang.get("argument.invalid"), ChatColor.RED);
                    }
                }
                for(ValuedItemStack stack : TransmutationHelper.getAll(page - 1))
                {
                    sendMessage(player, stack.toString() + " = " + Util.formatDouble(stack.getValue()));
                }
                return;
            }
        }
        pack.openMaterializer(player, admin);
    }
}
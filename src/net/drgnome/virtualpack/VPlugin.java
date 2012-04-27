// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import net.minecraft.server.*;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.configuration.file.*;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import static net.drgnome.virtualpack.Util.*;

public class VPlugin extends VPluginBase
{    
    protected void cmdHelp(CommandSender sender, String[] args)
    {
        int page = 1;
        if(args.length >= 2)
        {
            try
            {
                page = Integer.parseInt(args[1]);
            }
            catch(Exception e)
            {
                args[1] = longname(args[1]);
                if(args[1].equals("uncrafter"))
                {
                    sendMessage(sender, lang("help.uncrafter.title"), ChatColor.AQUA);
                    sendMessage(sender, lang("help.uncrafter.description"), ChatColor.AQUA);
                    return;
                }
                else if(args[1].equals("link"))
                {
                    sendMessage(sender, lang("help.link.title"), ChatColor.AQUA);
                    sendMessage(sender, lang("help.link.description"), ChatColor.AQUA);
                    sendMessage(sender, lang("help.link.note"), ChatColor.RED);
                    return;
                }
            }
        }
        sendMessage(sender, lang("help.title", new String[]{"" + page, "4"}));
        if(((sender instanceof Player) && (sender.hasPermission("vpack.admin"))) || !(sender instanceof Player))
        {
            sendMessage(sender, lang("help.admin"), ChatColor.RED);
        }
        sendMessage(sender, lang("help.args"), ChatColor.YELLOW);
        switch(page)
        {
            case 1:
                sendMessage(sender, lang("help.help"));
                sendMessage(sender, lang("help.version"));
                sendMessage(sender, lang("help.stats"));
                sendMessage(sender, lang("help.price"));
                sendMessage(sender, lang("help.workbench.buy"));
                sendMessage(sender, lang("help.workbench.use"));
                break;
            case 2:
                sendMessage(sender, lang("help.uncrafter.buy"));
                sendMessage(sender, lang("help.uncrafter.use", new String[]{"" + ChatColor.AQUA, "" + ChatColor.WHITE}));
                sendMessage(sender, lang("help.chest.buy"));
                sendMessage(sender, lang("help.chest.use"));
                sendMessage(sender, lang("help.chest.drop"));
                sendMessage(sender, lang("help.chest.trash"));
                break;
            case 3:
                sendMessage(sender, lang("help.furnace.buy"));
                sendMessage(sender, lang("help.furnace.use"));
                sendMessage(sender, lang("help.furnace.link"));
                sendMessage(sender, lang("help.furnace.unlink"));
                sendMessage(sender, lang("help.link.info", new String[]{"" + ChatColor.AQUA, "" + ChatColor.WHITE}));
                sendMessage(sender, lang("help.brewingstand"), ChatColor.GREEN);
                break;
            case 4:
                sendMessage(sender, lang("help.enchanttable.buy"));
                sendMessage(sender, lang("help.enchanttable.use"));
                sendMessage(sender, lang("help.enchanttable.book"));
                break;
            default:
                break;
        }
        sendMessage(sender, lang("help.more", new String[]{ChatColor.GOLD + "dev.bukkit.org/server-mods/virtualpack/commands"}));
    }
    
    protected void cmdConsoleStats(CommandSender sender, String[] args)
    {
        if(args.length >= 2)
        {
            if(hasPack(args[1]))
            {
                getPack(args[1]).printStats(sender);
            }
            else
            {
                sendMessage(sender, lang("stats.usernotfound"), ChatColor.RED);
            }
        }
        else
        {
            sendMessage(sender, lang("stats.wrong"), ChatColor.RED);
        }
    }
    
    protected void cmdAdmin(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("vpack.admin"))
        {
            sendMessage(sender, lang("admin.perm"), ChatColor.RED);
            return;
        }
        if(args.length < 2)
        {
            sendMessage(sender, lang("admin.help.title"), ChatColor.AQUA);
            sendMessage(sender, lang("admin.help.reload"), ChatColor.AQUA);
            sendMessage(sender, lang("admin.help.use"), ChatColor.AQUA);
            sendMessage(sender, lang("admin.help.give"), ChatColor.AQUA);
            sendMessage(sender, lang("admin.help.take"), ChatColor.AQUA);
            sendMessage(sender, lang("admin.help.delete"), ChatColor.AQUA);
            return;
        }
        args[1] = longname(args[1]);
        if(args[1].equals("reload"))
        {
            saveUserData();
            reloadConfig();
            loadUserData();
            sendMessage(sender, lang("admin.reloaded"), ChatColor.YELLOW);
            return;
        }
        else if(args[1].equals("give"))
        {
            cmdAdminGive(sender, args);
            return;
        }
        else if(args[1].equals("take"))
        {
            cmdAdminTake(sender, args);
            return;
        }
        else if(args[1].equals("delete"))
        {
            if(!sender.hasPermission("vpack.admin.delete"))
            {
                sendMessage(sender, lang("admin.perm"), ChatColor.RED);
                return;
            }
            if(!hasPack(args[2]))
            {
                sendMessage(sender, lang("vpack.none"), ChatColor.RED);
                return;
            }
            putPack(args[2], null);
            sendMessage(sender, lang("admin.delete", new String[]{args[2]}), ChatColor.GREEN);
            return;
        }
        if(!args[1].equals("use"))
        {
            sendMessage(sender, lang("argument.unknown"), ChatColor.RED);
        }
    }
    
    private void cmdAdminGive(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("vpack.admin.give"))
        {
            sendMessage(sender, lang("admin.perm"), ChatColor.RED);
            return;
        }
        if(args.length < 4)
        {
            sendMessage(sender, lang("argument.few"), ChatColor.RED);
            return;
        }
        if(!hasPack(args[2]))
        {
            sendMessage(sender, lang("vpack.none"), ChatColor.RED);
        }
        int amount = 1;
        if(args.length >= 5)
        {
            try
            {
                amount = Integer.parseInt(args[4]);
            }
            catch(Exception e)
            {
                sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
            }
        }
        args[3] = longname(args[3]);
        if(args[3].equals("workbench"))
        {
            if(getPack(args[2]).hasWorkbench)
            {
                sendMessage(sender, lang("admin.give.workbench.have"), ChatColor.RED);
            }
            else
            {
                getPack(args[2]).hasWorkbench = true;
                sendMessage(sender, lang("admin.give.workbench.done"), ChatColor.GREEN);
            }
        }
        if(args[3].equals("uncrafter"))
        {
            if(getPack(args[2]).hasUncrafter)
            {
                sendMessage(sender, lang("admin.give.uncrafter.have"), ChatColor.RED);
            }
            else
            {
                getPack(args[2]).hasUncrafter = true;
                sendMessage(sender, lang("admin.give.uncrafter.done"), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("enchanttable"))
        {
            if(getPack(args[2]).hasEnchantTable)
            {
                sendMessage(sender, lang("admin.give.enchanttable.have"), ChatColor.RED);
            }
            else
            {
                getPack(args[2]).hasEnchantTable = true;
                sendMessage(sender, lang("admin.give.enchanttable.done"), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("chest"))
        {
            VPack vpack = getPack(args[2]);
            for(int i = 0; i < amount; i++)
            {
                vpack.chests.put((Integer)(vpack.chests.size() + 1), new VInv(6 * 9));
            }
            if(amount == 1)
            {
                sendMessage(sender, lang("admin.give.chest.one", new String[]{args[2]}), ChatColor.GREEN);
            }
            else
            {
                sendMessage(sender, lang("admin.give.chest.many", new String[]{args[2], "" + amount}), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("furnace"))
        {
            VPack vpack = getPack(args[2]);
            for(int i = 0; i < amount; i++)
            {
                vpack.furnaces.put((Integer)(vpack.furnaces.size() + 1), new VTEFurnace(vpack));
            }
            if(amount == 1)
            {
                sendMessage(sender, lang("admin.give.furnace.one", new String[]{args[2]}), ChatColor.GREEN);
            }
            else
            {
                sendMessage(sender, lang("admin.give.furnace.many", new String[]{args[2], "" + amount}), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("brewingstand"))
        {
            VPack vpack = getPack(args[2]);
            for(int i = 0; i < amount; i++)
            {
                vpack.brewingstands.put((Integer)(vpack.brewingstands.size() + 1), new VTEBrewingstand(vpack));
            }
            if(amount == 1)
            {
                sendMessage(sender, lang("admin.give.brewingstand.one", new String[]{args[2]}), ChatColor.GREEN);
            }
            else
            {
                sendMessage(sender, lang("admin.give.brewingstand.many", new String[]{args[2], "" + amount}), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("book"))
        {
            VPack vpack = getPack(args[2]);
            int max = amount + vpack.bookshelves > 30 ? 30 : amount + vpack.bookshelves;
            vpack.bookshelves += max;
            if(amount == 1)
            {
                sendMessage(sender, lang("admin.give.book.one", new String[]{args[2]}), ChatColor.GREEN);
            }
            else
            {
                sendMessage(sender, lang("admin.give.book.many", new String[]{args[2], "" + amount}), ChatColor.GREEN);
            }
        }
    }
    
    private void cmdAdminTake(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("vpack.admin.take"))
        {
            sendMessage(sender, lang("admin.perm"), ChatColor.RED);
            return;
        }
        if(args.length < 4)
        {
            sendMessage(sender, lang("argument.few"), ChatColor.RED);
            return;
        }
        if(!hasPack(args[2]))
        {
            sendMessage(sender, lang("vpack.none"), ChatColor.RED);
        }
        int amount = 1;
        if(args.length >= 5)
        {
            try
            {
                amount = Integer.parseInt(args[4]);
            }
            catch(Exception e)
            {
                sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
            }
        }
        args[3] = longname(args[3]);
        if(args[3].equals("workbench"))
        {
            if(!getPack(args[2]).hasWorkbench)
            {
                sendMessage(sender, lang("admin.take.workbench.none"), ChatColor.RED);
            }
            else
            {
                getPack(args[2]).hasWorkbench = false;
                sendMessage(sender, lang("admin.take.workbench.done"), ChatColor.GREEN);
            }
        }
        if(args[3].equals("uncrafter"))
        {
            if(!getPack(args[2]).hasUncrafter)
            {
                sendMessage(sender, lang("admin.take.uncrafter.none"), ChatColor.RED);
            }
            else
            {
                getPack(args[2]).hasUncrafter = false;
                sendMessage(sender, lang("admin.take.uncrafter.done"), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("enchanttable"))
        {
            if(!getPack(args[2]).hasEnchantTable)
            {
                sendMessage(sender, lang("admin.take.enchanttable.none"), ChatColor.RED);
            }
            else
            {
                getPack(args[2]).hasEnchantTable = false;
                sendMessage(sender, lang("admin.give.enchanttable.done"), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("chest"))
        {
            VPack vpack = getPack(args[2]);
            for(int i = 0; i < amount; i++)
            {
                if(vpack.chests.containsKey((Integer)vpack.chests.size()))
                {
                    vpack.chests.remove((Integer)vpack.chests.size());
                }
            }
            if(amount == 1)
            {
                sendMessage(sender, lang("admin.take.chest.one", new String[]{args[2]}), ChatColor.GREEN);
            }
            else
            {
                sendMessage(sender, lang("admin.take.chest.many", new String[]{args[2], "" + amount}), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("furnace"))
        {
            VPack vpack = getPack(args[2]);
            for(int i = 0; i < amount; i++)
            {
                if(vpack.furnaces.containsKey((Integer)vpack.furnaces.size()))
                {
                    vpack.furnaces.remove((Integer)vpack.furnaces.size());
                }
            }
            if(amount == 1)
            {
                sendMessage(sender, lang("admin.take.furnace.one", new String[]{args[2]}), ChatColor.GREEN);
            }
            else
            {
                sendMessage(sender, lang("admin.take.furnace.many", new String[]{args[2], "" + amount}), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("brewingstand"))
        {
            VPack vpack = getPack(args[2]);
            for(int i = 0; i < amount; i++)
            {
                if(vpack.brewingstands.containsKey((Integer)vpack.brewingstands.size()))
                {
                    vpack.brewingstands.remove((Integer)vpack.brewingstands.size());
                }
            }
            if(amount == 1)
            {
                sendMessage(sender, lang("admin.take.brewingstand.one", new String[]{args[2]}), ChatColor.GREEN);
            }
            else
            {
                sendMessage(sender, lang("admin.take.brewingstand.many", new String[]{args[2], "" + amount}), ChatColor.GREEN);
            }
        }
        else if(args[3].equals("book"))
        {
            VPack vpack = getPack(args[2]);
            int max = vpack.bookshelves - amount < 0 ? vpack.bookshelves : amount;
            vpack.bookshelves -= max;
            if(amount == 1)
            {
                sendMessage(sender, lang("admin.take.book.one", new String[]{args[2]}), ChatColor.GREEN);
            }
            else
            {
                sendMessage(sender, lang("admin.take.book.many", new String[]{args[2], "" + amount}), ChatColor.GREEN);
            }
        }
    }
    
    protected void cmdAdminUse(CommandSender sender, String[] args)
    {
        if(args.length < 4)
        {
            sendMessage(sender, lang("argument.few"), ChatColor.RED);
            return;
        }
        if(!hasPack(args[2]))
        {
            sendMessage(sender, lang("vpack.none"), ChatColor.RED);
            return;
        }
        int amount = 1;
        if(args.length >= 5)
        {
            try
            {
                amount = Integer.parseInt(args[4]);
            }
            catch(Exception e)
            {
            }
        }
        args[3] = longname(args[3]);
        if(args[3].equals("workbench"))
        {
            getPack(args[2]).openWorkbench(sender, true);
        }
        if(args[3].equals("uncrafter"))
        {
            getPack(args[2]).openUncrafter(sender, true);
        }
        else if(args[3].equals("enchanttable"))
        {
            getPack(args[2]).openEnchantTable(sender, true);
        }
        else if(args[3].equals("chest"))
        {
            if(args.length >= 5)
            {
                if(args[4].equalsIgnoreCase("drop"))
                {
                    amount = 1;
                    if(args.length >= 6)
                    {
                        try
                        {
                            amount = Integer.parseInt(args[5]);
                        }
                        catch(Exception e)
                        {
                        }
                    }
                    getPack(args[2]).dropChest(sender, amount);
                    return;
                }
                else if(args[4].equalsIgnoreCase("trash"))
                {
                    amount = 1;
                    if(args.length >= 6)
                    {
                        try
                        {
                            amount = Integer.parseInt(args[5]);
                        }
                        catch(Exception e)
                        {
                        }
                    }
                    getPack(args[2]).trashChest(sender, amount);
                    return;
                }
            }
            getPack(args[2]).openChest(sender, amount, true);
        }
        else if(args[3].equals("furnace"))
        {
            if(args.length >= 5)
            {
                args[4] = longname(args[4]);
                if(args[4].equals("link"))
                {
                    if(args.length < 7)
                    {
                        sendMessage(sender, lang("argument.few"), ChatColor.RED);
                        return;
                    }
                    try
                    {
                        getPack(args[2]).linkFurnace(sender, Integer.parseInt(args[5]), Integer.parseInt(args[6]), true);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                    }
                    return;
                }
                if(args[4].equals("unlink"))
                {
                    if(args.length < 6)
                    {
                        sendMessage(sender, lang("argument.few"), ChatColor.RED);
                        return;
                    }
                    try
                    {
                        getPack(args[2]).unlinkFurnace(sender, Integer.parseInt(args[5]), true);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                    }
                    return;
                }
            }
            getPack(args[2]).openFurnace(sender, amount, true);
        }
        else if(args[3].equals("brewingstand"))
        {
            if(args.length >= 5)
            {
                args[4] = longname(args[4]);
                if(args[4].equals("link"))
                {
                    if(args.length < 7)
                    {
                        sendMessage(sender, lang("argument.few"), ChatColor.RED);
                        return;
                    }
                    try
                    {
                        getPack(args[2]).linkBrewingstand(sender, Integer.parseInt(args[5]), Integer.parseInt(args[6]), true);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                    }
                    return;
                }
                if(args[4].equals("unlink"))
                {
                    if(args.length < 6)
                    {
                        sendMessage(sender, lang("argument.few"), ChatColor.RED);
                        return;
                    }
                    try
                    {
                        getPack(args[2]).unlinkBrewingstand(sender, Integer.parseInt(args[5]), true);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                    }
                    return;
                }
            }
            getPack(args[2]).openBrewingstand(sender, amount, true);
        }
    }
    
    protected void cmdStats(CommandSender sender, String[] args)
    {
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(args.length >= 2)
        {
            if(sender.hasPermission("vpack.stats"))
            {
                if(hasPack(args[1]))
                {
                    getPack(args[1]).printStats(sender);
                }
                else
                {
                    sendMessage(sender, lang("stats.usernotfound"), ChatColor.RED);
                }
            }
            else
            {
                sendMessage(sender, lang("stats.allow"), ChatColor.RED);
            }
            return;
        }
        getPack(player.name).printStats(sender);
    }
    
    protected void cmdPrices(CommandSender sender, String[] args)
    {
        sendMessage(sender, lang("price.title"), ChatColor.AQUA);
        if(economyDisabled)
        {
            sendMessage(sender, lang("price.free"), ChatColor.AQUA);
        }
        else
        {
            EntityPlayer player = ((CraftPlayer)sender).getHandle();
            VPack vp = getPack(player.name);
            sendMessage(sender, lang("price.workbench", new String[]
                                     {
                                         "" + ChatColor.YELLOW,
                                         "" + ChatColor.GREEN,
                                         "" + getConfigDouble("workbench", "buy", sender, false, 2),
                                         "" + getConfigDouble("workbench", "use", sender, false, 2)
                                     }));
            
            sendMessage(sender, lang("price.uncrafter", new String[]
                                     {
                                         "" + ChatColor.YELLOW,
                                         "" + ChatColor.GREEN,
                                         "" + getConfigDouble("uncrafter", "buy", sender, false, 2),
                                         "" + getConfigDouble("uncrafter", "use", sender, false, 2)
                                     }));
            
            sendMessage(sender, lang("price.enchanttable", new String[]
                                     {
                                         "" + ChatColor.YELLOW,
                                         "" + ChatColor.GREEN,
                                         "" + getConfigDouble("enchanttable", "buy", sender, false, 2),
                                         "" + getConfigDouble("enchanttable", "use", sender, false, 2),
                                         smoothDouble(getConfigDouble("enchanttable", "book", sender, false) * Math.pow(getConfigDouble("enchanttable", "multiply", sender, false), vp.getBookshelves()), 2)
                                     }));
            sendMessage(sender, lang("price.chest", new String[]
                                     {
                                         "" + ChatColor.YELLOW,
                                         "" + ChatColor.GREEN,
                                         smoothDouble(getConfigDouble("chest", "buy", sender, false) * Math.pow(getConfigDouble("chest", "multiply", sender, false), vp.getChests() - getConfigInt("chest", "start", sender, true)), 2),
                                         "" + getConfigDouble("chest", "use", sender, false, 2)
                                     }));
            sendMessage(sender, lang("price.furnace", new String[]
                                     {
                                         "" + ChatColor.YELLOW,
                                         "" + ChatColor.GREEN,
                                         smoothDouble(getConfigDouble("furnace", "buy", sender, false) * Math.pow(getConfigDouble("furnace", "multiply", sender, false), vp.getFurnaces() - getConfigInt("furnace", "start", sender, true)), 2),
                                         "" + getConfigDouble("furnace", "use", sender, false, 2),
                                         "" + getConfigDouble("furnace", "link", sender, false, 2)
                                     }));
            sendMessage(sender, lang("price.brewingstand", new String[]
                                     {
                                         "" + ChatColor.YELLOW,
                                         "" + ChatColor.GREEN,
                                         smoothDouble(getConfigDouble("brewingstand", "buy", sender, false) * Math.pow(getConfigDouble("brewingstand", "multiply", sender, false), vp.getBrewingstands() - getConfigInt("brewingstand", "start", sender, true)), 2),
                                         "" + getConfigDouble("brewingstand", "use", sender, false),
                                         "" + getConfigDouble("brewingstand", "link", sender, false)
                                     }));
        }
    }
    
    protected void cmdWorkbench(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("vpack.use.workbench"))
        {
            sendMessage(sender, lang("workbench.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if((args.length >= 2) && (args[1].trim().equalsIgnoreCase("buy")))
        {
            getPack(player.name).buyWorkbench(sender);
            return;
        }
        getPack(player.name).openWorkbench(sender);
    }
    
    protected void cmdUncrafter(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("vpack.use.uncrafter"))
        {
            sendMessage(sender, lang("uncrafter.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if((args.length >= 2) && (args[1].trim().equalsIgnoreCase("buy")))
        {
            getPack(player.name).buyUncrafter(sender);
            return;
        }
        getPack(player.name).openUncrafter(sender);
    }
    
    protected void cmdEnchanttable(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("vpack.use.enchanttable"))
        {
            sendMessage(sender, lang("enchanttable.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if((args.length >= 2) && (args[1].trim().equalsIgnoreCase("buy")))
        {
            if((args.length >= 3) && (args[2].trim().equalsIgnoreCase("b")))
            {
                int amount = 1;
                if(args.length >= 4)
                {
                    try
                    {
                        amount = Integer.parseInt(args[3]);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                amount = amount <= 0 ? 1 : amount;
                getPack(player.name).buyBookshelf(sender, amount);
                return;
            }
            getPack(player.name).buyEnchantTable(sender);
            return;
        }
        getPack(player.name).openEnchantTable(sender);
    }
    
    protected void cmdChest(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("vpack.use.chest"))
        {
            sendMessage(sender, lang("chest.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        int nr = 1;
        if((args.length >= 2))
        {
            if(args[1].trim().equalsIgnoreCase("buy"))
            {
                int amount = 1;
                if(args.length >= 3)
                {
                    try
                    {
                        amount = Integer.parseInt(args[2]);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                amount = amount <= 0 ? 1 : amount;
                getPack(player.name).buyChest(sender, amount);
                return;
            }
            else if(args[1].trim().equalsIgnoreCase("drop"))
            {
                if(args.length >= 3)
                {
                    try
                    {
                        nr = Integer.parseInt(args[2]);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                getPack(player.name).dropChest(sender, nr);
                return;
            }
            else if(args[1].trim().equalsIgnoreCase("trash"))
            {
                if(args.length >= 3)
                {
                    try
                    {
                        nr = Integer.parseInt(args[2]);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                getPack(player.name).trashChest(sender, nr);
                return;
            }
            else
            {
                try
                {
                    nr = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                    return;
                }
            }
        }
        getPack(player.name).openChest(sender, nr);
    }
    
    protected void cmdFurnace(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("vpack.use.furnace"))
        {
            sendMessage(sender, lang("furnace.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        int nr = 1;
        if((args.length >= 2))
        {
            String par = longname(args[1]);
            if(par.equals("buy"))
            {
                int amount = 1;
                if(args.length >= 3)
                {
                    try
                    {
                        amount = Integer.parseInt(args[2]);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                amount = amount <= 0 ? 1 : amount;
                getPack(player.name).buyFurnace(sender, amount);
                return;
            }
            else if(par.equals("link"))
            {
                if(args.length >= 3)
                {
                    try
                    {
                        getPack(player.name).linkFurnace(sender, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                    }
                }
                return;
            }
            else if(par.equals("unlink"))
            {
                if(args.length >= 3)
                {
                    try
                    {
                        getPack(player.name).unlinkFurnace(sender, Integer.parseInt(args[2]));
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                    }
                }
                return;
            }
            else
            {
                try
                {
                    nr = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                    return;
                }
            }
        }
        getPack(player.name).openFurnace(sender, nr);
    }
    
    protected void cmdBrewingstand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("vpack.use.brewingstand"))
        {
            sendMessage(sender, lang("brewingstand.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        int nr = 1;
        if((args.length >= 2))
        {
            String par = longname(args[1]);
            if(par.equals("buy"))
            {
                int amount = 1;
                if(args.length >= 3)
                {
                    try
                    {
                        amount = Integer.parseInt(args[2]);
                    }
                    catch(Exception e)
                    {
                        sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                        return;
                    }
                }
                amount = amount <= 0 ? 1 : amount;
                getPack(player.name).buyBrewingstand(sender, amount);
                return;
            }
            else if(par.equals("link"))
            {
                try
                {
                    getPack(player.name).linkBrewingstand(sender, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                }
                catch(Exception e)
                {
                    sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                }
                return;
            }
            else if(par.equals("unlink"))
            {
                try
                {
                    getPack(player.name).unlinkBrewingstand(sender, Integer.parseInt(args[2]));
                }
                catch(Exception e)
                {
                    sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                }
                return;
            }
            else
            {
                try
                {
                    nr = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                    return;
                }
            }
        }
        getPack(player.name).openBrewingstand(sender, nr);
    }
}
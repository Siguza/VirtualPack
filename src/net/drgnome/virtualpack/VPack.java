// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;

import net.minecraft.server.*;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import net.milkbowl.vault.economy.Economy;

import static net.drgnome.virtualpack.Util.*;

public class VPack
{
    public boolean hasWorkbench;
    public boolean hasUncrafter;
    public boolean hasEnchantTable;
    public int bookshelves;
    private int flinks;
    private int blinks;
    public HashMap<Integer, VInv> chests;
    public HashMap<Integer, VTEFurnace> furnaces;
    public HashMap<Integer, VTEBrewingstand> brewingstands;
    
    public VPack(String username)
    {
        String groups[] = VPlugin.perms.getPlayerGroups((String)null, username);
        flinks = 0;
        blinks = 0;
        chests = new HashMap<Integer, VInv>();
        furnaces = new HashMap<Integer, VTEFurnace>();
        brewingstands = new HashMap<Integer, VTEBrewingstand>();
        if(VPlugin.economyDisabled)
        {
            hasWorkbench = true;
            hasUncrafter = true;
            hasEnchantTable = true;
            bookshelves = 30;
            for(int i = 1; i <= VPlugin.getConfigInt("chest", "max", groups, true); i++)
            {
                chests.put((Integer)i, new VInv(6 * 9));
            }
            for(int i = 1; i <= VPlugin.getConfigInt("furnace", "max", groups, true); i++)
            {
                furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = 1; i <= VPlugin.getConfigInt("brewingstand", "max", groups, true); i++)
            {
                brewingstands.put((Integer)i, new VTEBrewingstand(this));
            }
        }
        else
        {
            hasWorkbench = VPlugin.getConfigDouble("workbench", "buy", groups, false) == 0.0D;
            hasUncrafter = VPlugin.getConfigDouble("uncrafter", "buy", groups, false) == 0.0D;
            hasEnchantTable = VPlugin.getConfigDouble("enchanttable", "buy", groups, false) == 0.0D;
            bookshelves = VPlugin.getConfigDouble("enchanttable", "book", groups, false) == 0.0D ? 30 : 0;
            for(int i = 1; i <= VPlugin.getConfigInt("chest", "start", groups, true); i++)
            {
                chests.put((Integer)i, new VInv(6 * 9));
            }
            for(int i = 1; i <= VPlugin.getConfigInt("furnace", "start", groups, true); i++)
            {
                furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = 1; i <= VPlugin.getConfigInt("brewingstand", "start", groups, true); i++)
            {
                brewingstands.put((Integer)i, new VTEBrewingstand(this));
            }
        }
    }
    
    public VPack(String username, String data[])
    {
        this(username, data, 0);
    }
    
    public VPack(String username, String data[], int offset)
    {
        String groups[] = VPlugin.perms.getPlayerGroups((String)null, username);
        if(VPlugin.economyDisabled)
        {
            hasWorkbench = true;
            hasUncrafter = true;
            hasEnchantTable = true;
        }
        else
        {
            hasWorkbench = VPlugin.getConfigDouble("workbench", "buy", groups, false) == 0.0D;
            hasUncrafter = VPlugin.getConfigDouble("uncrafter", "buy", groups, false) == 0.0D;
            hasEnchantTable = VPlugin.getConfigDouble("enchanttable", "buy", groups, false) == 0.0D;
        }
        chests = new HashMap<Integer, VInv>();
        furnaces = new HashMap<Integer, VTEFurnace>();
        brewingstands = new HashMap<Integer, VTEBrewingstand>();
        String a[];
        for(; offset < data.length; offset++)
        {
            a = data[offset].split(separator[1]);
            if(a.length < 1)
            {
                continue;
            }
            a[0] = a[0].trim().toLowerCase();
            if(a[0].equals("w") && (a.length >= 2))
            {
                hasWorkbench = a[1].equals("1") || hasWorkbench;
            }
            else if(a[0].equals("u") && (a.length >= 2))
            {
                hasUncrafter = a[1].equals("1") || hasUncrafter;
            }
            else if(a[0].equals("e") && (a.length >= 3))
            {
                hasEnchantTable = a[1].equals("1") || hasEnchantTable;
                try
                {
                    bookshelves = Integer.parseInt(a[2]);
                }
                catch(Exception e)
                {
                }
            }
            else if(a[0].equals("c"))
            {
                chests.put((Integer)(chests.size() + 1), new VInv(6 * 9, a, 1));
            }
            else if(a[0].equals("f"))
            {
                furnaces.put((Integer)(furnaces.size() + 1), new VTEFurnace(this, a, 1));
            }
            else if(a[0].equals("b"))
            {
                brewingstands.put((Integer)(brewingstands.size() + 1), new VTEBrewingstand(this, a, 1));
            }
            else if(a[0].equals("fl"))
            {
                try
                {
                    flinks = Integer.parseInt(a[1]);
                }
                catch(Exception e)
                {
                }
            }
            else if(a[0].equals("bl"))
            {
                try
                {
                    blinks = Integer.parseInt(a[1]);
                }
                catch(Exception e)
                {
                }
            }
        }
        flinks = flinks < 0 ? 0 : flinks;
        blinks = blinks < 0 ? 0 : blinks;
        if(VPlugin.economyDisabled)
        {
            bookshelves = 30;
            for(int i = chests.size() + 1; i <= VPlugin.getConfigInt("chest", "max", groups, true); i++)
            {
                chests.put((Integer)i, new VInv(6 * 9));
            }
            for(int i = furnaces.size() + 1; i <= VPlugin.getConfigInt("furnace", "max", groups, true); i++)
            {
                furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = brewingstands.size() + 1; i <= VPlugin.getConfigInt("brewingstand", "max", groups, true); i++)
            {
                brewingstands.put((Integer)i, new VTEBrewingstand(this));
            }
        }
        else
        {
            int tmp = VPlugin.getConfigDouble("enchanttable", "book", groups, false) == 0.0D ? 30 : 0;
            bookshelves = tmp > bookshelves ? tmp : bookshelves;
            for(int i = chests.size() + 1; i <= VPlugin.getConfigInt("chest", "start", groups, true); i++)
            {
                chests.put((Integer)i, new VInv(6 * 9));
            }
            for(int i = furnaces.size() + 1; i <= VPlugin.getConfigInt("furnace", "start", groups, true); i++)
            {
                furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = brewingstands.size() + 1; i <= VPlugin.getConfigInt("brewingstand", "start", groups, true); i++)
            {
                brewingstands.put((Integer)i, new VTEBrewingstand(this));
            }
        }
    }
    
    public void tick()
    {
        VTEFurnace fur[] = furnaces.values().toArray(new VTEFurnace[0]);
        for(int i = 0; i < fur.length; i++)
        {
            fur[i].tick();
        }
        VTEBrewingstand brew[] = brewingstands.values().toArray(new VTEBrewingstand[0]);
        for(int i = 0; i < brew.length; i++)
        {
            brew[i].tick();
        }
    }
    
    public String[] save()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add("w" + separator[1] + (hasWorkbench ? "1" : "0"));
        list.add("u" + separator[1] + (hasUncrafter ? "1" : "0"));
        list.add("e" + separator[1] + (hasEnchantTable ? "1" : "0") + separator[1] + bookshelves);
        list.add("fl" + separator[1] + flinks);
        list.add("bl" + separator[1] + blinks);
        String string;
        String data[];
        VInv inv;
        for(int i = 1; i <= chests.size(); i++)
        {
            string = "c";
            inv = chests.get((Integer)i);
            if(inv == null)
            {
                continue;
            }
            data = inv.save();
            for(int j = 0; j < data.length; j++)
            {
                string += separator[1] + data[j];
            }
            list.add(string);
        }
        VTEFurnace fur;
        for(int i = 1; i <= furnaces.size(); i++)
        {
            string = "f";
            fur = furnaces.get((Integer)i);
            if(fur == null)
            {
                continue;
            }
            data = fur.save();
            for(int j = 0; j < data.length; j++)
            {
                string += separator[1] + data[j];
            }
            list.add(string);
        }
        VTEBrewingstand brew;
        for(int i = 1; i <= brewingstands.size(); i++)
        {
            string = "b";
            brew = brewingstands.get((Integer)i);
            if(brew == null)
            {
                continue;
            }
            data = brew.save();
            for(int j = 0; j < data.length; j++)
            {
                string += separator[1] + data[j];
            }
            list.add(string);
        }
        return list.toArray(new String[0]);
    }
    
    public void printStats(CommandSender sender)
    {
        if(VPlugin.economyDisabled)
        {
            sendMessage(sender, VPlugin.lang("stats.workbench", new String[]{"" + ChatColor.GREEN, VPlugin.lang("yes")}));
            sendMessage(sender, VPlugin.lang("stats.uncrafter", new String[]{"" + ChatColor.GREEN, VPlugin.lang("yes")}));
            sendMessage(sender, VPlugin.lang("stats.enchanttable", new String[]{"" + ChatColor.GREEN, VPlugin.lang("yes")}) + VPlugin.lang("stats.books", new String[]{"30"}));
            sendMessage(sender, VPlugin.lang("stats.chest", new String[]{"" + ChatColor.GREEN, "" + chests.size()}));
            sendMessage(sender, VPlugin.lang("stats.furnace", new String[]{"" + ChatColor.GREEN, "" + furnaces.size()}));
            sendMessage(sender, VPlugin.lang("stats.brewingstand", new String[]{"" + ChatColor.GREEN, "" + brewingstands.size()}));
        }
        else
        {
            sendMessage(sender, VPlugin.lang("stats.workbench", new String[]{"" + ChatColor.GREEN, hasWorkbench ? VPlugin.lang("yes") : VPlugin.lang("no")}));
            sendMessage(sender, VPlugin.lang("stats.uncrafter", new String[]{"" + ChatColor.GREEN, hasUncrafter ? VPlugin.lang("yes") : VPlugin.lang("no")}));
            sendMessage(sender, VPlugin.lang("stats.enchanttable", new String[]{"" + ChatColor.GREEN, hasEnchantTable ? VPlugin.lang("yes") : VPlugin.lang("no")}) + (hasEnchantTable ? VPlugin.lang("stats.books", new String[]{"" + bookshelves}): ""));
            int i = VPlugin.getConfigInt("chest", "max", sender, true);
            sendMessage(sender, VPlugin.lang("stats.chest", new String[]{"" + ChatColor.GREEN, "" + chests.size() + (i != -1 ? "/" + i : "")}));
            i = VPlugin.getConfigInt("furnace", "max", sender, true);
            sendMessage(sender, VPlugin.lang("stats.furnace", new String[]{"" + ChatColor.GREEN, "" + furnaces.size() + (i != -1 ? "/" + i : "")}) + (VPlugin.lang("stats.link", new String[]{"" + ChatColor.WHITE, "" + ChatColor.GREEN, "" + flinks})));
            i = VPlugin.getConfigInt("brewingstand", "max", sender, true);
            sendMessage(sender, VPlugin.lang("stats.brewingstand", new String[]{"" + ChatColor.GREEN, "" + brewingstands.size() + (i != -1 ? "/" + i : "")}) + (VPlugin.lang("stats.link", new String[]{"" + ChatColor.WHITE, "" + ChatColor.GREEN, "" + blinks})));
        }
    }
    
    public VInv getInv(int nr)
    {
        return chests.get((Integer)nr);
    }
    
    public int getChests()
    {
        return chests.size();
    }
    
    public int getFurnaces()
    {
        return furnaces.size();
    }
    
    public int getFLinks()
    {
        int l = 0;
        VTEFurnace f;
        for(int i = 1; i <= furnaces.size(); i++)
        {
            f = furnaces.get((Integer)i);
            l += ((f != null) && (f.link > 0)) ? 1 : 0;
        }
        return l;
    }
    
    public int getBrewingstands()
    {
        return furnaces.size();
    }
    
    public int getBLinks()
    {
        int l = 0;
        VTEBrewingstand b;
        for(int i = 1; i <= brewingstands.size(); i++)
        {
            b = brewingstands.get((Integer)i);
            l += ((b != null) && (b.link > 0)) ? 1 : 0;
        }
        return l;
    }
    
    public int getBookshelves()
    {
        return bookshelves;
    }
    
    public void openWorkbench(CommandSender sender)
    {
        openWorkbench(sender, VPlugin.economyDisabled);
    }
    
    public void openWorkbench(CommandSender sender, boolean forfree)
    {
        if(!hasWorkbench)
        {
            sendMessage(sender, VPlugin.lang("workbench.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree)
        {
            double price = VPlugin.getConfigDouble("workbench", "use", sender, false);
            if(!VPlugin.economy.has(player.name, price))
            {
                sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
                return;
            }
            VPlugin.economy.withdrawPlayer(player.name, price);
        }
        player.netServerHandler.sendPacket(new Packet100OpenWindow(1, 1, "", 9));
        VWorkbench container = new VWorkbench(player);
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyWorkbench(CommandSender sender)
    {
        if(VPlugin.economyDisabled)
        {
            sendMessage(sender, VPlugin.lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        if(hasWorkbench)
        {
            sendMessage(sender, VPlugin.lang("workbench.max"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = VPlugin.getConfigDouble("workbench", "buy", sender, false);
        if(!VPlugin.economy.has(player.name, price))
        {
            sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
            return;
        }
        VPlugin.economy.withdrawPlayer(player.name, price);
        hasWorkbench = true;
        sendMessage(sender, VPlugin.lang("workbench.bought"), ChatColor.GREEN);
    }
    
    public void openUncrafter(CommandSender sender)
    {
        openUncrafter(sender, VPlugin.economyDisabled);
    }
    
    public void openUncrafter(CommandSender sender, boolean forfree)
    {
        if(!hasUncrafter)
        {
            sendMessage(sender, VPlugin.lang("uncrafter.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree)
        {
            double price = VPlugin.getConfigDouble("uncrafter", "use", sender, false);
            if(!VPlugin.economy.has(player.name, price))
            {
                sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
                return;
            }
            VPlugin.economy.withdrawPlayer(player.name, price);
        }
        String guiname = VPlugin.lang("uncrafter.name");
        if(guiname.length() > 32)
        {
            guiname = guiname.substring(0, 32);
        }
        player.netServerHandler.sendPacket(new Packet100OpenWindow(1, 0, guiname, 2 * 9));
        VUncrafter container = new VUncrafter(player);
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyUncrafter(CommandSender sender)
    {
        if(VPlugin.economyDisabled)
        {
            sendMessage(sender, VPlugin.lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        if(hasUncrafter)
        {
            sendMessage(sender, VPlugin.lang("uncrafter.max"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = VPlugin.getConfigDouble("uncrafter", "buy", sender, false);
        if(!VPlugin.economy.has(player.name, price))
        {
            sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
            return;
        }
        VPlugin.economy.withdrawPlayer(player.name, price);
        hasUncrafter = true;
        sendMessage(sender, VPlugin.lang("uncrafter.bought"), ChatColor.GREEN);
    }
    
    public void openEnchantTable(CommandSender sender)
    {
        openEnchantTable(sender, VPlugin.economyDisabled);
    }
    
    public void openEnchantTable(CommandSender sender, boolean forfree)
    {
        if(!hasEnchantTable)
        {
            sendMessage(sender, VPlugin.lang("enchanttable.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree)
        {
            double price = VPlugin.getConfigDouble("enchanttable", "use", sender, false);
            if(!VPlugin.economy.has(player.name, price))
            {
                sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
                return;
            }
            VPlugin.economy.withdrawPlayer(player.name, price);
        }
        player.netServerHandler.sendPacket(new Packet100OpenWindow(1, 4, "", 9));
        VEnchantTable container = new VEnchantTable(player, bookshelves);
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyEnchantTable(CommandSender sender)
    {
        if(VPlugin.economyDisabled)
        {
            sendMessage(sender, VPlugin.lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        if(hasEnchantTable)
        {
            sendMessage(sender, VPlugin.lang("enchanttable.max"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = VPlugin.getConfigDouble("enchanttable", "buy", sender, false);
        if(!VPlugin.economy.has(player.name, price))
        {
            sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
            return;
        }
        VPlugin.economy.withdrawPlayer(player.name, price);
        hasEnchantTable = true;
        sendMessage(sender, VPlugin.lang("enchanttable.bought"), ChatColor.GREEN);
    }
    
    public void buyBookshelf(CommandSender sender, int amount)
    {
        if(VPlugin.economyDisabled)
        {
            sendMessage(sender, VPlugin.lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        if(bookshelves >= 30)
        {
            sendMessage(sender, VPlugin.lang("enchanttable.book.max"), ChatColor.RED);
            return;
        }
        if(amount > 30 - bookshelves)
        {
            amount = 30 - bookshelves;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = 0.0D;
        for(int i = 0; i < amount; i++)
        {
            price += VPlugin.getConfigDouble("enchanttable", "book", sender, false) * Math.pow(VPlugin.getConfigDouble("enchanttable", "multiply", sender, false), bookshelves + i);
        }
        if(!VPlugin.economy.has(player.name, price))
        {
            sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
            return;
        }
        VPlugin.economy.withdrawPlayer(player.name, price);
        bookshelves += amount;
        if(bookshelves == 1)
        {
            sendMessage(sender, VPlugin.lang("enchanttable.book.one"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, VPlugin.lang("enchanttable.book.many", new String[]{"" + bookshelves}), ChatColor.GREEN);
        }
    }
    
    public void openChest(CommandSender sender, int nr)
    {
        openChest(sender, nr, VPlugin.economyDisabled);
    }
    
    public void openChest(CommandSender sender, int nr, boolean forfree)
    {
        VInv inv = chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(sender, VPlugin.lang("chest.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree)
        {
            double price = VPlugin.getConfigDouble("chest", "use", sender, false);
            if(!VPlugin.economy.has(player.name, price))
            {
                sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
                return;
            }
            VPlugin.economy.withdrawPlayer(player.name, price);
        }
        VChest container = new VChest(player, inv);
        String chestname = VPlugin.lang("chest.name", new String[]{"" + nr});
        if(chestname.length() > 32)
        {
            chestname = chestname.substring(0, 32);
        }
        player.netServerHandler.sendPacket(new Packet100OpenWindow(1, 0, chestname, 6 * 9));
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyChest(CommandSender sender, int amount)
    {
        if(VPlugin.economyDisabled)
        {
            sendMessage(sender, VPlugin.lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        int max = VPlugin.getConfigInt("chest", "max", sender, true);
        if((chests.size() + amount > max) && (max != -1))
        {
            sendMessage(sender, VPlugin.lang("chest.max", new String[]{"" + max}), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = 0.0D;
        for(int i = chests.size(); i < chests.size() + amount; i++)
        {
            price += VPlugin.getConfigDouble("chest", "buy", sender, false) * Math.pow(VPlugin.getConfigDouble("chest", "multiply", sender, false), i);
        }
        if(!VPlugin.economy.has(player.name, price))
        {
            sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
            return;
        }
        VPlugin.economy.withdrawPlayer(player.name, price);
        int end = chests.size();
        for(int i = end; i < amount + end; i++)
        {
            chests.put((Integer)(i + 1), new VInv(6 * 9));
        }
        if(chests.size() == 1)
        {
            sendMessage(sender, VPlugin.lang("chest.bought.one"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, VPlugin.lang("chest.bought.many", new String[]{"" + chests.size()}), ChatColor.GREEN);
        }
    }
    
    public void dropChest(CommandSender sender, int nr)
    {
        VInv inv = chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(sender, VPlugin.lang("chest.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        ItemStack item;
        for(int i = 0; i < inv.getSize(); i++)
        {
            item = inv.getItem(i);
            if(item != null)
            {
                item = item.cloneItemStack();
                player.drop(item);
                inv.setItem(i, null);
            }
        }
    }
    
    public void trashChest(CommandSender sender, int nr)
    {
        VInv inv = chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(sender, VPlugin.lang("chest.none"), ChatColor.RED);
            return;
        }
        chests.put((Integer)nr, new VInv(6 * 9));
        sendMessage(sender, VPlugin.lang("chest.trashed", new String[]{"" + nr}), ChatColor.GREEN);
    }
    
    public void openFurnace(CommandSender sender, int nr)
    {
        openFurnace(sender, nr, VPlugin.economyDisabled);
    }
    
    public void openFurnace(CommandSender sender, int nr, boolean forfree)
    {
        VTEFurnace fur = furnaces.get((Integer)nr);
        if(fur == null)
        {
            sendMessage(sender, VPlugin.lang("furnace.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree)
        {
            double price = VPlugin.getConfigDouble("furnace", "use", sender, false);
            if(!VPlugin.economy.has(player.name, price))
            {
                sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
                return;
            }
            VPlugin.economy.withdrawPlayer(player.name, price);
        }
        VFurnace container = new VFurnace(player, fur);
        player.netServerHandler.sendPacket(new Packet100OpenWindow(1, 2, "", 3));
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyFurnace(CommandSender sender, int amount)
    {
        if(VPlugin.economyDisabled)
        {
            sendMessage(sender, VPlugin.lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        int max = VPlugin.getConfigInt("furnace", "max", sender, true);
        if((furnaces.size() + amount > max) && (max != -1))
        {
            sendMessage(sender, VPlugin.lang("furnace.max", new String[]{"" + max}), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = 0.0D;
        for(int i = furnaces.size(); i < furnaces.size() + amount; i++)
        {
            price += VPlugin.getConfigDouble("furnace", "buy", sender, false) * Math.pow(VPlugin.getConfigDouble("furnace", "multiply", sender, false), i);
        }
        if(!VPlugin.economy.has(player.name, price))
        {
            sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
            return;
        }
        VPlugin.economy.withdrawPlayer(player.name, price);
        int end = furnaces.size();
        for(int i = end; i < amount + end; i++)
        {
            furnaces.put((Integer)(i + 1), new VTEFurnace(this));
        }
        if(furnaces.size() == 1)
        {
            sendMessage(sender, VPlugin.lang("furnace.bought.one"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, VPlugin.lang("furnace.bought.many", new String[]{"" + furnaces.size()}), ChatColor.GREEN);
        }
    }
    
    public void linkFurnace(CommandSender sender, int furnaceNR, int chestNR)
    {
        linkFurnace(sender, furnaceNR, chestNR, VPlugin.economyDisabled);
    }
    
    public void linkFurnace(CommandSender sender, int furnaceNR, int chestNR, boolean forfree)
    {
        VTEFurnace fur = furnaces.get((Integer)furnaceNR);
        if(fur == null)
        {
            sendMessage(sender, VPlugin.lang("furnace.none"), ChatColor.RED);
            return;
        }
        if(chests.get((Integer)chestNR) == null)
        {
            sendMessage(sender, VPlugin.lang("chest.none"), ChatColor.RED);
            return;
        }
        if((!forfree) && (fur.link <= 0))
        {
            if(flinks <= 0)
            {
                EntityPlayer player = ((CraftPlayer)sender).getHandle();
                double price = VPlugin.getConfigDouble("furnace", "link", sender, false);
                if(!VPlugin.economy.has(player.name, price))
                {
                    sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
                    return;
                }
                VPlugin.economy.withdrawPlayer(player.name, price);
            }
            else
            {
                flinks--;
            }
        }
        fur.link = chestNR;
        sendMessage(sender, VPlugin.lang("furnace.linked", new String[]{"" + furnaceNR, "" + chestNR}), ChatColor.GREEN);
    }
    
    public void unlinkFurnace(CommandSender sender, int furnaceNR)
    {
        unlinkFurnace(sender, furnaceNR, VPlugin.economyDisabled);
    }
    
    public void unlinkFurnace(CommandSender sender, int furnaceNR, boolean forfree)
    {
        VTEFurnace fur = furnaces.get((Integer)furnaceNR);
        if(fur == null)
        {
            sendMessage(sender, VPlugin.lang("furnace.none"), ChatColor.RED);
            return;
        }
        if(fur.link <= 0)
        {
            sendMessage(sender, VPlugin.lang("furnace.nolink"), ChatColor.RED);
            return;
        }
        if(!forfree)
        {
            flinks++;
        }
        fur.link = 0;
        sendMessage(sender, VPlugin.lang("furnace.unlinked", new String[]{"" + furnaceNR}), ChatColor.GREEN);
    }
    
    public void openBrewingstand(CommandSender sender, int nr)
    {
        openBrewingstand(sender, nr, VPlugin.economyDisabled);
    }
    
    public void openBrewingstand(CommandSender sender, int nr, boolean forfree)
    {
        VTEBrewingstand brew = brewingstands.get((Integer)nr);
        if(brew == null)
        {
            sendMessage(sender, VPlugin.lang("brewingstand.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree)
        {
            double price = VPlugin.getConfigDouble("brewingstand", "use", sender, false);
            if(!VPlugin.economy.has(player.name, price))
            {
                sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
                return;
            }
            VPlugin.economy.withdrawPlayer(player.name, price);
        }
        VBrewingstand container = new VBrewingstand(player, brew);
        player.netServerHandler.sendPacket(new Packet100OpenWindow(1, 5, "", 4));
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyBrewingstand(CommandSender sender, int amount)
    {
        if(VPlugin.economyDisabled)
        {
            sendMessage(sender, VPlugin.lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        int max = VPlugin.getConfigInt("brewingstand", "max", sender, true);
        if((brewingstands.size() + amount > max) && (max != -1))
        {
            sendMessage(sender, VPlugin.lang("brewingstand.max", new String[]{"" + max}), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = 0.0D;
        for(int i = brewingstands.size(); i < brewingstands.size() + amount; i++)
        {
            price += VPlugin.getConfigDouble("brewingstand", "buy", sender, false) * Math.pow(VPlugin.getConfigDouble("brewingstand", "multiply", sender, false), i);
        }
        if(!VPlugin.economy.has(player.name, price))
        {
            sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
            return;
        }
        VPlugin.economy.withdrawPlayer(player.name, price);
        int end = brewingstands.size();
        for(int i = end; i < amount + end; i++)
        {
            brewingstands.put((Integer)(i + 1), new VTEBrewingstand(this));
        }
        if(brewingstands.size() == 1)
        {
            sendMessage(sender, VPlugin.lang("brewingstand.bought.one"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, VPlugin.lang("brewingstand.bought.many", new String[]{"" + brewingstands.size()}), ChatColor.GREEN);
        }
    }
    
    public void linkBrewingstand(CommandSender sender, int brewNR, int chestNR)
    {
        linkBrewingstand(sender, brewNR, chestNR, VPlugin.economyDisabled);
    }
    
    public void linkBrewingstand(CommandSender sender, int brewNR, int chestNR, boolean forfree)
    {
        VTEBrewingstand brew = brewingstands.get((Integer)brewNR);
        if(brew == null)
        {
            sendMessage(sender, VPlugin.lang("brewingstand.none"), ChatColor.RED);
            return;
        }
        if(chests.get((Integer)chestNR) == null)
        {
            sendMessage(sender, VPlugin.lang("chest.none"), ChatColor.RED);
            return;
        }
        if((!forfree) && (brew.link <= 0))
        {
            if(blinks <= 0)
            {
                EntityPlayer player = ((CraftPlayer)sender).getHandle();
                double price = VPlugin.getConfigDouble("brewingstand", "link", sender, false);
                if(!VPlugin.economy.has(player.name, price))
                {
                    sendMessage(sender, VPlugin.lang("money.toofew"), ChatColor.RED);
                    return;
                }
                VPlugin.economy.withdrawPlayer(player.name, price);
            }
            else
            {
                blinks--;
            }
        }
        brew.link = chestNR;
        sendMessage(sender, VPlugin.lang("brewingstand.linked", new String[]{"" + brewNR, "" + chestNR}), ChatColor.GREEN);
    }
    
    public void unlinkBrewingstand(CommandSender sender, int brewNR)
    {
        unlinkBrewingstand(sender, brewNR, VPlugin.economyDisabled);
    }
    
    public void unlinkBrewingstand(CommandSender sender, int brewNR, boolean forfree)
    {
        VTEBrewingstand brew = brewingstands.get((Integer)brewNR);
        if(brew == null)
        {
            sendMessage(sender, VPlugin.lang("brewingstand.none"), ChatColor.RED);
            return;
        }
        if(brew.link <= 0)
        {
            sendMessage(sender, VPlugin.lang("brewingstand.nolink"), ChatColor.RED);
            return;
        }
        if(!forfree)
        {
            blinks++;
        }
        brew.link = 0;
        sendMessage(sender, VPlugin.lang("brewingstand.unlinked", new String[]{"" + brewNR}), ChatColor.GREEN);
    }
}
// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;

import #PACKAGE_MINECRAFT#.*;

import org.bukkit.ChatColor;
import #PACKAGE_CRAFTBUKKIT#.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import static net.drgnome.virtualpack.Config.*;
import static net.drgnome.virtualpack.Lang.*;
import static net.drgnome.virtualpack.Util.*;

public class VPack
{
    private String groups[];
    private String owner;
    public boolean hasWorkbench;
    public boolean hasUncrafter;
    //public boolean hasInvGuard;
    public boolean hasEnchantTable;
    public int bookshelves;
    private int flinks;
    private int blinks;
    public HashMap<Integer, VInv> chests;
    public HashMap<Integer, VTEFurnace> furnaces;
    public HashMap<Integer, VTEBrewingstand> brewingstands;
    public PlayerInventory inv;
    
    public VPack(String username)
    {
        owner = username;
        groups = getPlayerGroups(username);
        flinks = 0;
        blinks = 0;
        chests = new HashMap<Integer, VInv>();
        furnaces = new HashMap<Integer, VTEFurnace>();
        brewingstands = new HashMap<Integer, VTEBrewingstand>();
        inv = null;
        if(economyDisabled)
        {
            hasWorkbench = true;
            hasUncrafter = true;
            //hasInvGuard = true;
            hasEnchantTable = true;
            bookshelves = 15;
            for(int i = 1; i <= getConfigInt("chest", "max", groups, true); i++)
            {
                chests.put((Integer)i, new VInv(getChestSize()));
            }
            for(int i = 1; i <= getConfigInt("furnace", "max", groups, true); i++)
            {
                furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = 1; i <= getConfigInt("brewingstand", "max", groups, true); i++)
            {
                brewingstands.put((Integer)i, new VTEBrewingstand(this));
            }
        }
        else
        {
            hasWorkbench = getConfigDouble("workbench", "buy", groups, false, username) == 0.0D;
            hasUncrafter = getConfigDouble("uncrafter", "buy", groups, false, username) == 0.0D;
            //hasInvGuard = getConfigDouble("invguard", "buy", groups, false, username) == 0.0D;
            hasEnchantTable = getConfigDouble("enchanttable", "buy", groups, false, username) == 0.0D;
            bookshelves = getConfigDouble("enchanttable", "book", groups, false, username) == 0.0D ? 15 : 0;
            for(int i = 1; i <= getConfigInt("chest", "start", groups, true); i++)
            {
                chests.put((Integer)i, new VInv(getChestSize()));
            }
            for(int i = 1; i <= getConfigInt("furnace", "start", groups, true); i++)
            {
                furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = 1; i <= getConfigInt("brewingstand", "start", groups, true); i++)
            {
                brewingstands.put((Integer)i, new VTEBrewingstand(this));
            }
        }
    }
    
    public VPack(String username, String data[]) throws Throwable
    {
        this(username, data, 0);
    }
    
    public VPack(String username, String data[], int offset) throws Throwable
    {
        owner = username;
        groups = getPlayerGroups(username);
        if(economyDisabled)
        {
            hasWorkbench = true;
            hasUncrafter = true;
            //hasInvGuard = true;
            hasEnchantTable = true;
        }
        else
        {
            hasWorkbench = getConfigDouble("workbench", "buy", groups, false, username) == 0.0D;
            hasUncrafter = getConfigDouble("uncrafter", "buy", groups, false, username) == 0.0D;
            //hasInvGuard = getConfigDouble("invguard", "buy", groups, false, username) == 0.0D;
            hasEnchantTable = getConfigDouble("enchanttable", "buy", groups, false, username) == 0.0D;
        }
        chests = new HashMap<Integer, VInv>();
        furnaces = new HashMap<Integer, VTEFurnace>();
        brewingstands = new HashMap<Integer, VTEBrewingstand>();
        inv = null;
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
            /*else if(a[0].equals("i") && (a.length >= 2))
            {
                hasInvGuard = a[1].equals("1") || hasInvGuard;
            }*/
            else if(a[0].equals("e") && (a.length >= 3))
            {
                hasEnchantTable = a[1].equals("1") || hasEnchantTable;
                try
                {
                    bookshelves = Integer.parseInt(a[2]);
                }
                catch(Throwable t)
                {
                }
            }
            else if(a[0].equals("c"))
            {
                chests.put((Integer)(chests.size() + 1), new VInv(getChestSize(), a, 1));
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
                catch(Throwable t)
                {
                }
            }
            else if(a[0].equals("bl"))
            {
                try
                {
                    blinks = Integer.parseInt(a[1]);
                }
                catch(Throwable t)
                {
                }
            }
            else if(a[0].equals("inv"))
            {
                inv = new PlayerInventory((EntityHuman)null);
                int max = inv.items.length > (a.length - 1) ? (a.length - 1) : inv.items.length;
                for(int i = 0; i < max; i++)
                {
                    inv.items[i] = stringToItemStack(a[i + 1]);
                }
                max = inv.armor.length > (a.length - (inv.items.length + 1)) ? (a.length - (inv.items.length + 1)) : inv.items.length;
                for(int i = 0; i < max; i++)
                {
                    inv.armor[i] = stringToItemStack(a[i + (inv.items.length + 1)]);
                }
            }
        }
        flinks = flinks < 0 ? 0 : flinks;
        blinks = blinks < 0 ? 0 : blinks;
        if(economyDisabled)
        {
            bookshelves = 15;
            for(int i = chests.size() + 1; i <= getConfigInt("chest", "max", groups, true); i++)
            {
                chests.put((Integer)i, new VInv(getChestSize()));
            }
            for(int i = furnaces.size() + 1; i <= getConfigInt("furnace", "max", groups, true); i++)
            {
                furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = brewingstands.size() + 1; i <= getConfigInt("brewingstand", "max", groups, true); i++)
            {
                brewingstands.put((Integer)i, new VTEBrewingstand(this));
            }
        }
        else
        {
            int tmp = getConfigDouble("enchanttable", "book", groups, false, username) == 0.0D ? 15 : 0;
            bookshelves = tmp > bookshelves ? tmp : bookshelves;
            for(int i = chests.size() + 1; i <= getConfigInt("chest", "start", groups, true); i++)
            {
                chests.put((Integer)i, new VInv(getChestSize()));
            }
            for(int i = furnaces.size() + 1; i <= getConfigInt("furnace", "start", groups, true); i++)
            {
                furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = brewingstands.size() + 1; i <= getConfigInt("brewingstand", "start", groups, true); i++)
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
    
    public void drop(CraftPlayer player)
    {
        EntityPlayer p = player.getHandle();
        ArrayList<IInventory> list = new ArrayList<IInventory>();
        if(!hasPermission(player, "vpack.keep.chest"))
        {
            list.addAll(chests.values());
        }
        if(!hasPermission(player, "vpack.keep.furnace"))
        {
            list.addAll(furnaces.values());
        }
        if(!hasPermission(player, "vpack.keep.brewingstand"))
        {
            list.addAll(brewingstands.values());
        }
        for(IInventory inv : list.toArray(new IInventory[0]))
        {
            for(ItemStack i : inv.getContents())
            {
                p.drop(i);
            }
        }
    }
    
    public void wipe(CraftPlayer player)
    {
        if(!hasPermission(player, "vpack.keep.enchanttable"))
        {
            bookshelves = 0;
        }
        if(!hasPermission(player, "vpack.keep.chest"))
        {
            Integer keys[] = chests.keySet().toArray(new Integer[0]);
            for(Integer i : keys)
            {
                chests.put(i, new VInv(getChestSize()));
            }
        }
        int l;
        Integer keys[];
        if(!hasPermission(player, "vpack.keep.furnace"))
        {
            keys = furnaces.keySet().toArray(new Integer[0]);
            VTEFurnace f;
            for(Integer i : keys)
            {
                l = furnaces.get(i).link;
                f = new VTEFurnace(this);
                f.link = l;
                furnaces.put(i, f);
            }
        }
        if(!hasPermission(player, "vpack.keep.brewingstand"))
        {
            keys = brewingstands.keySet().toArray(new Integer[0]);
            VTEBrewingstand b;
            for(Integer i : keys)
            {
                l = brewingstands.get(i).link;
                b = new VTEBrewingstand(this);
                b.link = l;
                brewingstands.put(i, b);
            }
        }
    }
    
    public void reset(CraftPlayer player)
    {
        if(!hasPermission(player, "vpack.keep.workbench"))
        {
            hasWorkbench = false;
        }
        if(!hasPermission(player, "vpack.keep.uncrafter"))
        {
            hasUncrafter = false;
        }
        if(!hasPermission(player, "vpack.keep.enchanttable"))
        {
            hasEnchantTable = false;
            bookshelves = 0;
        }
        if(!hasPermission(player, "vpack.keep.chest"))
        {
            chests = new HashMap<Integer, VInv>();
            if(hasPermission(player, "vpack.keep.furnace"))
            {
                VTEFurnace fs[] = furnaces.values().toArray(new VTEFurnace[0]);
                for(VTEFurnace f : fs)
                {
                    f.link = 0;
                }
            }
            if(hasPermission(player, "vpack.keep.brewingstand"))
            {
                VTEBrewingstand bs[] = brewingstands.values().toArray(new VTEBrewingstand[0]);
                for(VTEBrewingstand b : bs)
                {
                    b.link = 0;
                }
            }
        }
        if(!hasPermission(player, "vpack.keep.furnace"))
        {
            furnaces = new HashMap<Integer, VTEFurnace>();
            flinks = 0;
        }
        if(!hasPermission(player, "vpack.keep.brewingstand"))
        {
            brewingstands = new HashMap<Integer, VTEBrewingstand>();
            blinks = 0;
        }
    }
    
    public String[] save()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add("w" + separator[1] + (hasWorkbench ? "1" : "0"));
        list.add("u" + separator[1] + (hasUncrafter ? "1" : "0"));
        //list.add("i" + separator[1] + (hasInvGuard ? "1" : "0"));
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
        if(this.inv != null)
        {
            string = "inv";
            for(ItemStack i : this.inv.items)
            {
                string += separator[1] + itemStackToString(i);
            }
            for(ItemStack i : this.inv.armor)
            {
                string += separator[1] + itemStackToString(i);
            }
        }
        return list.toArray(new String[0]);
    }
    
    public void printStats(CommandSender sender)
    {
        if(economyDisabled)
        {
            sendMessage(sender, lang("stats.workbench", "" + ChatColor.GREEN, lang("yes")));
            sendMessage(sender, lang("stats.uncrafter", "" + ChatColor.GREEN, lang("yes")));
            //sendMessage(sender, lang("stats.invguard", "" + ChatColor.GREEN, lang("yes")));
            sendMessage(sender, lang("stats.enchanttable", "" + ChatColor.GREEN, lang("yes")) + lang("stats.books", "15"));
            sendMessage(sender, lang("stats.chest", "" + ChatColor.GREEN, "" + chests.size()));
            sendMessage(sender, lang("stats.furnace", "" + ChatColor.GREEN, "" + furnaces.size()));
            sendMessage(sender, lang("stats.brewingstand", "" + ChatColor.GREEN, "" + brewingstands.size()));
        }
        else
        {
            sendMessage(sender, lang("stats.workbench", "" + ChatColor.GREEN, hasWorkbench ? lang("yes") : lang("no")));
            sendMessage(sender, lang("stats.uncrafter", "" + ChatColor.GREEN, hasUncrafter ? lang("yes") : lang("no")));
            //sendMessage(sender, lang("stats.invguard", "" + ChatColor.GREEN, hasInvGuard ? lang("yes") : lang("no")));
            sendMessage(sender, lang("stats.enchanttable", "" + ChatColor.GREEN, hasEnchantTable ? lang("yes") : lang("no")) + (hasEnchantTable ? lang("stats.books", "" + bookshelves): ""));
            int i = getConfigInt("chest", "max", groups, true);
            sendMessage(sender, lang("stats.chest", "" + ChatColor.GREEN, "" + chests.size() + (i != -1 ? "/" + i : "")));
            i = getConfigInt("furnace", "max", groups, true);
            sendMessage(sender, lang("stats.furnace", "" + ChatColor.GREEN, "" + furnaces.size() + (i != -1 ? "/" + i : "")) + (lang("stats.link", "" + ChatColor.WHITE, "" + ChatColor.GREEN, "" + flinks)));
            i = getConfigInt("brewingstand", "max", groups, true);
            sendMessage(sender, lang("stats.brewingstand", "" + ChatColor.GREEN, "" + brewingstands.size() + (i != -1 ? "/" + i : "")) + (lang("stats.link", "" + ChatColor.WHITE, "" + ChatColor.GREEN, "" + blinks)));
        }
    }
    
    public void alphaChest(VInv inv)
    {
        chests.put((Integer)(chests.size() + 1), inv);
    }
    
    public int getChestSize()
    {
        int s = getConfigInt("chest", "size", groups, true);
        if(s < 1)
        {
            s = 6;
        }
        else if(s > 28)
        {
            s = 28;
        }
        return s;
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
        openWorkbench(sender, economyDisabled);
    }
    
    public void openWorkbench(CommandSender sender, boolean forfree)
    {
        if(!hasWorkbench)
        {
            sendMessage(sender, lang("workbench.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree && !moneyHasTake(player.name, getConfigDouble("workbench", "use", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        player.#FIELD_NETSERVERHANDLER_1#.sendPacket(new Packet100OpenWindow(1, 1, "", 9));
        VWorkbench container = new VWorkbench(player);
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyWorkbench(CommandSender sender)
    {
        if(economyDisabled)
        {
            sendMessage(sender, lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        if(hasWorkbench)
        {
            sendMessage(sender, lang("workbench.max"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!moneyHasTake(player.name, getConfigDouble("workbench", "buy", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        hasWorkbench = true;
        sendMessage(sender, lang("workbench.bought"), ChatColor.GREEN);
    }
    
    public void openUncrafter(CommandSender sender)
    {
        openUncrafter(sender, economyDisabled);
    }
    
    public void openUncrafter(CommandSender sender, boolean forfree)
    {
        if(!hasUncrafter)
        {
            sendMessage(sender, lang("uncrafter.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree && !moneyHasTake(player.name, getConfigDouble("uncrafter", "use", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        String guiname = lang("uncrafter.name");
        if(guiname.length() > 32)
        {
            guiname = guiname.substring(0, 32);
        }
        player.#FIELD_NETSERVERHANDLER_1#.sendPacket(new Packet100OpenWindow(1, 0, guiname, 2 * 9));
        VUncrafter container = new VUncrafter(player);
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyUncrafter(CommandSender sender)
    {
        if(economyDisabled)
        {
            sendMessage(sender, lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        if(hasUncrafter)
        {
            sendMessage(sender, lang("uncrafter.max"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!moneyHasTake(player.name, getConfigDouble("uncrafter", "buy", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        hasUncrafter = true;
        sendMessage(sender, lang("uncrafter.bought"), ChatColor.GREEN);
    }
    
    /*public void buyInvGuard(CommandSender sender)
    {
        if(economyDisabled)
        {
            sendMessage(sender, lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        if(hasInvGuard)
        {
            sendMessage(sender, lang("invguard.max"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!moneyHasTake(player.name, getConfigDouble("invguard", "buy", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        hasInvGuard = true;
        sendMessage(sender, lang("invguard.bought"), ChatColor.GREEN);
    }
    
    public boolean useInvGuard(CommandSender sender)
    {
        if(!hasPermission(sender, "vpack.use.invguard") || !hasInvGuard)
        {
            return false;
        }
        return moneyHasTake(sender.getName(), getConfigDouble("invguard", "use", groups, false, owner));
    }*/
    
    public void openEnchantTable(CommandSender sender)
    {
        openEnchantTable(sender, economyDisabled);
    }
    
    public void openEnchantTable(CommandSender sender, boolean forfree)
    {
        if(!hasEnchantTable)
        {
            sendMessage(sender, lang("enchanttable.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree && !moneyHasTake(player.name, getConfigDouble("enchanttable", "use", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        player.#FIELD_NETSERVERHANDLER_1#.sendPacket(new Packet100OpenWindow(1, 4, "", 9));
        VEnchantTable container = new VEnchantTable(player, bookshelves);
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyEnchantTable(CommandSender sender)
    {
        if(economyDisabled)
        {
            sendMessage(sender, lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        if(hasEnchantTable)
        {
            sendMessage(sender, lang("enchanttable.max"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!moneyHasTake(player.name, getConfigDouble("enchanttable", "buy", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        hasEnchantTable = true;
        sendMessage(sender, lang("enchanttable.bought"), ChatColor.GREEN);
    }
    
    public void buyBookshelf(CommandSender sender, int amount)
    {
        if(economyDisabled)
        {
            sendMessage(sender, lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        if(bookshelves >= 15)
        {
            sendMessage(sender, lang("enchanttable.book.max"), ChatColor.RED);
            return;
        }
        if(amount > 15 - bookshelves)
        {
            amount = 15 - bookshelves;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = 0.0D;
        for(int i = 0; i < amount; i++)
        {
            price += getConfigDouble("enchanttable", "book", groups, false, owner) * Math.pow(getConfigDouble("enchanttable", "multiply", groups, false, owner), bookshelves + i);
        }
        if(!moneyHasTake(player.name, price))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        bookshelves += amount;
        if(bookshelves == 1)
        {
            sendMessage(sender, lang("enchanttable.book.one"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, lang("enchanttable.book.many", "" + bookshelves), ChatColor.GREEN);
        }
    }
    
    public void openChest(CommandSender sender, int nr)
    {
        openChest(sender, nr, economyDisabled);
    }
    
    public void openChest(CommandSender sender, int nr, boolean forfree)
    {
        VInv inv = chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(sender, lang("chest.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree && !moneyHasTake(player.name, getConfigDouble("chest", "use", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        VChest container = new VChest(player, inv);
        String chestname = lang("chest.name", "" + nr);
        if(chestname.length() > 32)
        {
            chestname = chestname.substring(0, 32);
        }
        player.#FIELD_NETSERVERHANDLER_1#.sendPacket(new Packet100OpenWindow(1, 0, chestname, getChestSize() * 9));
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyChest(CommandSender sender, int amount)
    {
        if(economyDisabled)
        {
            sendMessage(sender, lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        int max = getConfigInt("chest", "max", groups, true);
        if((chests.size() + amount > max) && (max != -1))
        {
            sendMessage(sender, lang("chest.max", "" + max), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = 0.0D;
        for(int i = chests.size(); i < chests.size() + amount; i++)
        {
            price += getConfigDouble("chest", "buy", groups, false, owner) * Math.pow(getConfigDouble("chest", "multiply", groups, false, owner), i);
        }
        if(!moneyHasTake(player.name, price))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        int end = chests.size();
        for(int i = end; i < amount + end; i++)
        {
            chests.put((Integer)(i + 1), new VInv(getChestSize()));
        }
        if(chests.size() == 1)
        {
            sendMessage(sender, lang("chest.bought.one"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, lang("chest.bought.many", "" + chests.size()), ChatColor.GREEN);
        }
    }
    
    public void dropChest(CommandSender sender, int nr)
    {
        VInv inv = chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(sender, lang("chest.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        ItemStack item;
        for(int i = 0; i < inv.getSize(); i++)
        {
            item = inv.getItem(i);
            if(item != null)
            {
                item = copy(item);
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
            sendMessage(sender, lang("chest.none"), ChatColor.RED);
            return;
        }
        chests.put((Integer)nr, new VInv(getChestSize()));
        sendMessage(sender, lang("chest.trashed", "" + nr), ChatColor.GREEN);
    }
    
    public void openFurnace(CommandSender sender, int nr)
    {
        openFurnace(sender, nr, economyDisabled);
    }
    
    public void openFurnace(CommandSender sender, int nr, boolean forfree)
    {
        VTEFurnace fur = furnaces.get((Integer)nr);
        if(fur == null)
        {
            sendMessage(sender, lang("furnace.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree && !moneyHasTake(player.name, getConfigDouble("furnace", "use", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        VFurnace container = new VFurnace(player, fur);
        player.#FIELD_NETSERVERHANDLER_1#.sendPacket(new Packet100OpenWindow(1, 2, "", 3));
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyFurnace(CommandSender sender, int amount)
    {
        if(economyDisabled)
        {
            sendMessage(sender, lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        int max = getConfigInt("furnace", "max", groups, true);
        if((furnaces.size() + amount > max) && (max != -1))
        {
            sendMessage(sender, lang("furnace.max", "" + max), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = 0.0D;
        for(int i = furnaces.size(); i < furnaces.size() + amount; i++)
        {
            price += getConfigDouble("furnace", "buy", groups, false, owner) * Math.pow(getConfigDouble("furnace", "multiply", groups, false, owner), i);
        }
        if(!moneyHasTake(player.name, price))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        int end = furnaces.size();
        for(int i = end; i < amount + end; i++)
        {
            furnaces.put((Integer)(i + 1), new VTEFurnace(this));
        }
        if(furnaces.size() == 1)
        {
            sendMessage(sender, lang("furnace.bought.one"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, lang("furnace.bought.many", "" + furnaces.size()), ChatColor.GREEN);
        }
    }
    
    public void linkFurnace(CommandSender sender, int furnaceNR, int chestNR)
    {
        linkFurnace(sender, furnaceNR, chestNR, economyDisabled);
    }
    
    public void linkFurnace(CommandSender sender, int furnaceNR, int chestNR, boolean forfree)
    {
        VTEFurnace fur = furnaces.get((Integer)furnaceNR);
        if(fur == null)
        {
            sendMessage(sender, lang("furnace.none"), ChatColor.RED);
            return;
        }
        if(chests.get((Integer)chestNR) == null)
        {
            sendMessage(sender, lang("chest.none"), ChatColor.RED);
            return;
        }
        if((!forfree) && (fur.link <= 0))
        {
            if(flinks <= 0)
            {
                EntityPlayer player = ((CraftPlayer)sender).getHandle();
                if(!moneyHasTake(player.name, getConfigDouble("furnace", "link", groups, false, owner)))
                {
                    sendMessage(sender, lang("money.toofew"), ChatColor.RED);
                    return;
                }
            }
            else
            {
                flinks--;
            }
        }
        fur.link = chestNR;
        sendMessage(sender, lang("furnace.linked", "" + furnaceNR, "" + chestNR), ChatColor.GREEN);
    }
    
    public void unlinkFurnace(CommandSender sender, int furnaceNR)
    {
        unlinkFurnace(sender, furnaceNR, economyDisabled);
    }
    
    public void unlinkFurnace(CommandSender sender, int furnaceNR, boolean forfree)
    {
        VTEFurnace fur = furnaces.get((Integer)furnaceNR);
        if(fur == null)
        {
            sendMessage(sender, lang("furnace.none"), ChatColor.RED);
            return;
        }
        if(fur.link <= 0)
        {
            sendMessage(sender, lang("furnace.nolink"), ChatColor.RED);
            return;
        }
        if(!forfree)
        {
            flinks++;
        }
        fur.link = 0;
        sendMessage(sender, lang("furnace.unlinked", "" + furnaceNR), ChatColor.GREEN);
    }
    
    public void openBrewingstand(CommandSender sender, int nr)
    {
        openBrewingstand(sender, nr, economyDisabled);
    }
    
    public void openBrewingstand(CommandSender sender, int nr, boolean forfree)
    {
        VTEBrewingstand brew = brewingstands.get((Integer)nr);
        if(brew == null)
        {
            sendMessage(sender, lang("brewingstand.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!forfree && !moneyHasTake(player.name, getConfigDouble("brewingstand", "use", groups, false, owner)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        VBrewingstand container = new VBrewingstand(player, brew);
        player.#FIELD_NETSERVERHANDLER_1#.sendPacket(new Packet100OpenWindow(1, 5, "", 4));
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public void buyBrewingstand(CommandSender sender, int amount)
    {
        if(economyDisabled)
        {
            sendMessage(sender, lang("vpack.ecodisabled"), ChatColor.YELLOW);
            return;
        }
        int max = getConfigInt("brewingstand", "max", groups, true);
        if((brewingstands.size() + amount > max) && (max != -1))
        {
            sendMessage(sender, lang("brewingstand.max", "" + max), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        double price = 0.0D;
        for(int i = brewingstands.size(); i < brewingstands.size() + amount; i++)
        {
            price += getConfigDouble("brewingstand", "buy", groups, false, owner) * Math.pow(getConfigDouble("brewingstand", "multiply", groups, false, owner), i);
        }
        if(!moneyHasTake(player.name, price))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        int end = brewingstands.size();
        for(int i = end; i < amount + end; i++)
        {
            brewingstands.put((Integer)(i + 1), new VTEBrewingstand(this));
        }
        if(brewingstands.size() == 1)
        {
            sendMessage(sender, lang("brewingstand.bought.one"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, lang("brewingstand.bought.many", "" + brewingstands.size()), ChatColor.GREEN);
        }
    }
    
    public void linkBrewingstand(CommandSender sender, int brewNR, int chestNR)
    {
        linkBrewingstand(sender, brewNR, chestNR, economyDisabled);
    }
    
    public void linkBrewingstand(CommandSender sender, int brewNR, int chestNR, boolean forfree)
    {
        VTEBrewingstand brew = brewingstands.get((Integer)brewNR);
        if(brew == null)
        {
            sendMessage(sender, lang("brewingstand.none"), ChatColor.RED);
            return;
        }
        if(chests.get((Integer)chestNR) == null)
        {
            sendMessage(sender, lang("chest.none"), ChatColor.RED);
            return;
        }
        if((!forfree) && (brew.link <= 0))
        {
            if(blinks <= 0)
            {
                EntityPlayer player = ((CraftPlayer)sender).getHandle();
                if(!moneyHasTake(player.name, getConfigDouble("brewingstand", "link", groups, false, owner)))
                {
                    sendMessage(sender, lang("money.toofew"), ChatColor.RED);
                    return;
                }
            }
            else
            {
                blinks--;
            }
        }
        brew.link = chestNR;
        sendMessage(sender, lang("brewingstand.linked", "" + brewNR, "" + chestNR), ChatColor.GREEN);
    }
    
    public void unlinkBrewingstand(CommandSender sender, int brewNR)
    {
        unlinkBrewingstand(sender, brewNR, economyDisabled);
    }
    
    public void unlinkBrewingstand(CommandSender sender, int brewNR, boolean forfree)
    {
        VTEBrewingstand brew = brewingstands.get((Integer)brewNR);
        if(brew == null)
        {
            sendMessage(sender, lang("brewingstand.none"), ChatColor.RED);
            return;
        }
        if(brew.link <= 0)
        {
            sendMessage(sender, lang("brewingstand.nolink"), ChatColor.RED);
            return;
        }
        if(!forfree)
        {
            blinks++;
        }
        brew.link = 0;
        sendMessage(sender, lang("brewingstand.unlinked", "" + brewNR), ChatColor.GREEN);
    }
    
    public void openTrash(CommandSender sender)
    {
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        VTrash container = new VTrash(player);
        String name = lang("trash.name");
        if(name.length() > 32)
        {
            name = name.substring(0, 32);
        }
        player.#FIELD_NETSERVERHANDLER_1#.sendPacket(new Packet100OpenWindow(1, 0, name, 9));
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
}
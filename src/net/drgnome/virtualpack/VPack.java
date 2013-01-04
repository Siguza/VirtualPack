// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftPlayer;
import net.minecraft.server.v#MC_VERSION#.*;
import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.util.*;

import static net.drgnome.virtualpack.util.Global.*;

public class VPack
{
    public static final int _maxBookshelves = 15;
    private final String _world;
    private final String _player;
    public boolean _hasWorkbench;
    public boolean _hasUncrafter;
    public boolean _hasEnchantTable;
    public boolean _hasAnvil;
    public int _bookshelves;
    private int _fLinks = 0;
    private int _bLinks = 0;
    public HashMap<Integer, VInv> _chests = new HashMap<Integer, VInv>();
    public HashMap<Integer, VTEFurnace> _furnaces = new HashMap<Integer, VTEFurnace>();
    public HashMap<Integer, VTEBrewingstand> _brews = new HashMap<Integer, VTEBrewingstand>();
    public ArrayList<ItemStack> _left = new ArrayList<ItemStack>();
    public ArrayList<String> _messages = new ArrayList<String>();
    
    public VPack(String world, String player)
    {
        _world = world;
        _player = player;
        String[] groups = Perm.getGroups(_world, _player);
        if(Money.world(_world).enabled())
        {
            _hasWorkbench = Config.getDouble(_world, groups, "tools", "workbench", "buy", false) == 0D;
            _hasUncrafter = Config.getDouble(_world, groups, "tools","uncrafter", "buy", false) == 0D;
            _hasEnchantTable = Config.getDouble(_world, groups, "tools","enchanttable", "buy", false) == 0D;
            _bookshelves = Config.getDouble(_world, groups, "tools","enchanttable", "book", false) == 0D ? _maxBookshelves : 0;
            _hasAnvil = Config.getDouble(_world, groups, "tools","anvil", "buy", false) == 0D;
            for(int i = 1; i <= Config.getInt(_world, groups, "tools","chest", "start", true); i++)
            {
                _chests.put((Integer)i, new VInv(getChestSize()));
            }
            for(int i = 1; i <= Config.getInt(_world, groups, "tools", "furnace", "start", true); i++)
            {
                _furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = 1; i <= Config.getInt(_world, groups, "tools", "brewingstand", "start", true); i++)
            {
                _brews.put((Integer)i, new VTEBrewingstand(this));
            }
        }
        else
        {
            _hasWorkbench = true;
            _hasUncrafter = true;
            _hasEnchantTable = true;
            _bookshelves = _maxBookshelves;
            _hasAnvil = true;
            for(int i = 1; i <= Config.getInt(_world, groups, "tools", "chest", "max", true); i++)
            {
                _chests.put((Integer)i, new VInv(getChestSize()));
            }
            for(int i = 1; i <= Config.getInt(_world, groups, "tools", "furnace", "max", true); i++)
            {
                _furnaces.put((Integer)i, new VTEFurnace(this));
            }
            for(int i = 1; i <= Config.getInt(_world, groups, "tools", "brewingstand", "max", true); i++)
            {
                _brews.put((Integer)i, new VTEBrewingstand(this));
            }
        }
    }
    
    public VPack(String world, String player, String data[])
    {
        this(world, player);
        int[] count = {0, 0, 0};
        for(String line : data)
        {
            String[] a = line.split(_separator[1]);
            if(a.length <= 0)
            {
                continue;
            }
            if(a[0].equals("w"))
            {
                _hasWorkbench = a[1].equals("1") || _hasWorkbench;
            }
            else if(a[0].equals("u"))
            {
                _hasUncrafter = a[1].equals("1") || _hasUncrafter;
            }
            else if(a[0].equals("e"))
            {
                _hasEnchantTable = a[1].equals("1") || _hasEnchantTable;
                _bookshelves = Util.max(_bookshelves, Util.tryParse(a[2], _bookshelves));
            }
            else if(a[0].equals("av"))
            {
                _hasAnvil = a[1].equals("1") || _hasAnvil;
            }
            else if(a[0].equals("c"))
            {
                count[0]++;
                _chests.put((Integer)count[0], new VInv(getChestSize(), Util.copy(a, 1)));
            }
            else if(a[0].equals("f"))
            {
                count[1]++;
                _furnaces.put((Integer)count[1], new VTEFurnace(this, Util.copy(a, 1)));
            }
            else if(a[0].equals("b"))
            {
                count[2]++;
                _brews.put((Integer)count[2], new VTEBrewingstand(this, Util.copy(a, 1)));
            }
            else if(a[0].equals("fl"))
            {
                _fLinks = Util.tryParse(a[1], _fLinks);
            }
            else if(a[0].equals("bl"))
            {
                _bLinks = Util.tryParse(a[1], _bLinks);
            }
            else if(a[0].equals("lft"))
            {
                for(int i = 1; i < a.length; i++)
                {
                    _left.add(Util.stringToItemStack(a[i]));
                }
            }
            else if(a[0].equals("msg"))
            {
                for(int i = 1; i < a.length; i++)
                {
                    _messages.add(Util.base64de(a[i]));
                }
            }
        }
    }
    
    public String save()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add("w" + _separator[1] + (_hasWorkbench ? "1" : "0"));
        list.add("u" + _separator[1] + (_hasUncrafter ? "1" : "0"));
        list.add(Util.implode(_separator[1], "e", _hasEnchantTable ? "1" : "0", "" + _bookshelves));
        list.add("av" + _separator[1] + (_hasAnvil ? "1" : "0"));
        list.add("fl" + _separator[1] + _fLinks);
        list.add("bl" + _separator[1] + _bLinks);
        for(VInv inv : _chests.values().toArray(new VInv[0]))
        {
            list.add("c" + _separator[1] + Util.implode(_separator[1], inv.save()));
        }
        for(VTEFurnace fur : _furnaces.values().toArray(new VTEFurnace[0]))
        {
            list.add("f" + _separator[1] + Util.implode(_separator[1], fur.save()));
        }
        for(VTEBrewingstand brew : _brews.values().toArray(new VTEBrewingstand[0]))
        {
            list.add("b" + _separator[1] + Util.implode(_separator[1], brew.save()));
        }
        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add("lft");
        for(ItemStack item : _left)
        {
            tmp.add(Util.itemStackToString(item));
        }
        list.add(Util.implode(_separator[1], tmp.toArray(new String[0])));
        tmp = new ArrayList<String>();
        tmp.add("msg");
        for(String s : _messages)
        {
            tmp.add(Util.base64en(s));
        }
        list.add(Util.implode(_separator[1], tmp.toArray(new String[0])));
        return Util.implode(_separator[0], list.toArray(new String[0]));
    }
    
    public String getPlayer()
    {
        return _player;
    }
    
    public void tick()
    {
        for(VTEFurnace fur : _furnaces.values().toArray(new VTEFurnace[0]))
        {
            fur.tick();
        }
        for(VTEBrewingstand brew : _brews.values().toArray(new VTEBrewingstand[0]))
        {
            brew.tick();
        }
    }
    
    public void drop(Player player)
    {
        EntityPlayer p = ((CraftPlayer)player).getHandle();
        ArrayList<IInventory> list = new ArrayList<IInventory>();
        if(!Perm.has(_world, _player, "vpack.keep.chest"))
        {
            list.addAll(_chests.values());
        }
        if(!Perm.has(_world, _player, "vpack.keep.furnace"))
        {
            list.addAll(_furnaces.values());
        }
        if(!Perm.has(_world, _player, "vpack.keep.brewingstand"))
        {
            list.addAll(_brews.values());
        }
        for(IInventory inv : list.toArray(new IInventory[0]))
        {
            for(ItemStack i : inv.getContents())
            {
                p.drop(i);
            }
        }
    }
    
    public void wipe()
    {
        if(!Perm.has(_world, _player, "vpack.keep.enchanttable"))
        {
            _bookshelves = 0;
        }
        if(!Perm.has(_world, _player, "vpack.keep.chest"))
        {
            Integer keys[] = _chests.keySet().toArray(new Integer[0]);
            for(Integer i : keys)
            {
                _chests.put(i, new VInv(getChestSize()));
            }
        }
        int l;
        Integer keys[];
        if(!Perm.has(_world, _player, "vpack.keep.furnace"))
        {
            keys = _furnaces.keySet().toArray(new Integer[0]);
            VTEFurnace f;
            for(Integer i : keys)
            {
                l = _furnaces.get(i).link;
                f = new VTEFurnace(this);
                f.link = l;
                _furnaces.put(i, f);
            }
        }
        if(!Perm.has(_world, _player, "vpack.keep.brewingstand"))
        {
            keys = _brews.keySet().toArray(new Integer[0]);
            VTEBrewingstand b;
            for(Integer i : keys)
            {
                l = _brews.get(i).link;
                b = new VTEBrewingstand(this);
                b.link = l;
                _brews.put(i, b);
            }
        }
    }
    
    public void reset()
    {
        if(!Perm.has(_world, _player, "vpack.keep.workbench"))
        {
            _hasWorkbench = false;
        }
        if(!Perm.has(_world, _player, "vpack.keep.uncrafter"))
        {
            _hasUncrafter = false;
        }
        if(!Perm.has(_world, _player, "vpack.keep.enchanttable"))
        {
            _hasEnchantTable = false;
            _bookshelves = 0;
        }
        if(!Perm.has(_world, _player, "vpack.keep.anvil"))
        {
            _hasAnvil = false;
        }
        if(!Perm.has(_world, _player, "vpack.keep.chest"))
        {
            _chests = new HashMap<Integer, VInv>();
            if(Perm.has(_world, _player, "vpack.keep.furnace"))
            {
                VTEFurnace fs[] = _furnaces.values().toArray(new VTEFurnace[0]);
                for(VTEFurnace f : fs)
                {
                    f.link = 0;
                }
            }
            if(Perm.has(_world, _player, "vpack.keep.brewingstand"))
            {
                VTEBrewingstand bs[] = _brews.values().toArray(new VTEBrewingstand[0]);
                for(VTEBrewingstand b : bs)
                {
                    b.link = 0;
                }
            }
        }
        if(!Perm.has(_world, _player, "vpack.keep.furnace"))
        {
            _furnaces = new HashMap<Integer, VTEFurnace>();
            _fLinks = 0;
        }
        if(!Perm.has(_world, _player, "vpack.keep.brewingstand"))
        {
            _brews = new HashMap<Integer, VTEBrewingstand>();
            _bLinks = 0;
        }
    }
    
    public void printStats(Player player)
    {
        if(Money.world(_world).enabled())
        {
            sendMessage(player, Lang.get("stats.workbench", "" + ChatColor.GREEN, _hasWorkbench ? Lang.get("yes") : Lang.get("no")));
            sendMessage(player, Lang.get("stats.uncrafter", "" + ChatColor.GREEN, _hasUncrafter ? Lang.get("yes") : Lang.get("no")));
            sendMessage(player, Lang.get("stats.enchanttable", "" + ChatColor.GREEN, _hasEnchantTable ? Lang.get("yes") : Lang.get("no")) + (_hasEnchantTable ? Lang.get("stats.books", "" + _bookshelves): ""));
            sendMessage(player, Lang.get("stats.anvil", "" + ChatColor.GREEN, _hasAnvil ? Lang.get("yes") : Lang.get("no")));
            int i = Config.getInt(_world, _player, "tools", "chest", "max", true);
            sendMessage(player, Lang.get("stats.chest", "" + ChatColor.GREEN, "" + _chests.size() + (i != -1 ? "/" + i : "")));
            i = Config.getInt(_world, _player, "tools", "furnace", "max", true);
            sendMessage(player, Lang.get("stats.furnace", "" + ChatColor.GREEN, "" + _furnaces.size() + (i != -1 ? "/" + i : "")) + (Lang.get("stats.link", "" + ChatColor.WHITE, "" + ChatColor.GREEN, "" + _fLinks)));
            i = Config.getInt(_world, _player, "tools", "brewingstand", "max", true);
            sendMessage(player, Lang.get("stats.brewingstand", "" + ChatColor.GREEN, "" + _brews.size() + (i != -1 ? "/" + i : "")) + (Lang.get("stats.link", "" + ChatColor.WHITE, "" + ChatColor.GREEN, "" + _bLinks)));
        }
        else
        {
            sendMessage(player, Lang.get("stats.workbench", "" + ChatColor.GREEN, Lang.get("yes")));
            sendMessage(player, Lang.get("stats.uncrafter", "" + ChatColor.GREEN, Lang.get("yes")));
            sendMessage(player, Lang.get("stats.enchanttable", "" + ChatColor.GREEN, Lang.get("yes")) + Lang.get("stats.books", "15"));
            sendMessage(player, Lang.get("stats.anvil", "" + ChatColor.GREEN, Lang.get("yes")));
            sendMessage(player, Lang.get("stats.chest", "" + ChatColor.GREEN, "" + _chests.size()));
            sendMessage(player, Lang.get("stats.furnace", "" + ChatColor.GREEN, "" + _furnaces.size()));
            sendMessage(player, Lang.get("stats.brewingstand", "" + ChatColor.GREEN, "" + _brews.size()));
        }
    }
    
    public VInv[] getInvs()
    {
        return _chests.values().toArray(new VInv[0]);
    }
    
    public VInv getInv(int i)
    {
        return _chests.get((Integer)i);
    }
    
    public void addInv(VInv inv)
    {
        _chests.put((Integer)(_chests.size() + 1), inv);
    }
    
    public int numBookshelves()
    {
        return _bookshelves;
    }
    
    public int numChests()
    {
        return _chests.size();
    }
    
    public int numFurnaces()
    {
        return _furnaces.size();
    }
    
    public int numBrews()
    {
        return _brews.size();
    }
    
    public double priceWorkbenchBuy()
    {
        return Config.getDouble(_world, _player, "tools", "workbench", "buy", false, 2);
    }
    
    public double priceWorkbenchUse()
    {
        return Config.getDouble(_world, _player, "tools", "workbench", "use", false, 2);
    }
    
    public double priceUncrafterBuy()
    {
        return Config.getDouble(_world, _player, "tools", "uncrafter", "buy", false, 2);
    }
    
    public double priceUncrafterUse()
    {
        return Config.getDouble(_world, _player, "tools", "uncrafter", "use", false, 2);
    }
    
    public double priceEnchBuy()
    {
        return Config.getDouble(_world, _player, "tools", "enchanttable", "buy", false, 2);
    }
    
    public double priceEnchUse()
    {
        return Config.getDouble(_world, _player, "tools", "enchanttable", "use", false, 2);
    }
    
    public double priceEnchBook(int amount)
    {
        double base = Config.getDouble(_world, _player, "tools", "enchanttable", "book", false);
        double factor = Config.getDouble(_world, _player, "tools", "enchanttable", "multiply", false);
        double price = 0;
        for(int i = 0; i < amount; i++)
        {
            price += (base * Math.pow(factor, numBookshelves() + i));
        }
        return Util.smooth(price, 2);
    }
    
    public double priceAnvilBuy()
    {
        return Config.getDouble(_world, _player, "tools", "anvil", "buy", false, 2);
    }
    
    public double priceAnvilUse()
    {
        return Config.getDouble(_world, _player, "tools", "anvil", "use", false, 2);
    }
    
    public double priceChestBuy(int amount)
    {
        double base = Config.getDouble(_world, _player, "tools", "chest", "buy", false);
        double factor = Config.getDouble(_world, _player, "tools", "chest", "multiply", false);
        double price = 0;
        for(int i = 0; i < amount; i++)
        {
            price += (base * Math.pow(factor, numChests() + i));
        }
        return Util.smooth(price, 2);
    }
    
    public double priceChestUse()
    {
        return Config.getDouble(_world, _player, "tools", "chest", "use", false, 2);
    }
    
    public double priceFurnaceBuy(int amount)
    {
        double base = Config.getDouble(_world, _player, "tools", "furnace", "buy", false);
        double factor = Config.getDouble(_world, _player, "tools", "furnace", "multiply", false);
        double price = 0;
        for(int i = 0; i < amount; i++)
        {
            price += (base * Math.pow(factor, numFurnaces() + i));
        }
        return Util.smooth(price, 2);
    }
    
    public double priceFurnaceUse()
    {
        return Config.getDouble(_world, _player, "tools", "furnace", "use", false, 2);
    }
    
    public double priceFurnaceLink()
    {
        return Config.getDouble(_world, _player, "tools", "furnace", "link", false, 2);
    }
    
    public double priceBrewBuy(int amount)
    {
        double base = Config.getDouble(_world, _player, "tools", "brewingstand", "buy", false);
        double factor = Config.getDouble(_world, _player, "tools", "brewingstand", "multiply", false);
        double price = 0;
        for(int i = 0; i < amount; i++)
        {
            price += (base * Math.pow(factor, numBrews() + i));
        }
        return Util.smooth(price, 2);
    }
    
    public double priceBrewUse()
    {
        return Config.getDouble(_world, _player, "tools", "brewingstand", "use", false, 2);
    }
    
    public double priceBrewLink()
    {
        return Config.getDouble(_world, _player, "tools", "brewingstand", "link", false, 2);
    }
    
    public int getChestSize()
    {
        int s = Config.getInt(_world, _player, "tools", "chest", "size", true);
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
    
    /** Workbench **/
    
    public void buyWorkbench(Player bukkitPlayer)
    {
        if(_hasWorkbench)
        {
            sendMessage(bukkitPlayer, Lang.get("workbench.max"), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceWorkbenchBuy()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        _hasWorkbench = true;
        sendMessage(bukkitPlayer, Lang.get("workbench.bought"), ChatColor.GREEN);
    }
    
    public void openWorkbench(Player bukkitPlayer, boolean free)
    {
        if(!_hasWorkbench)
        {
            sendMessage(bukkitPlayer, Lang.get("workbench.none"), ChatColor.RED);
            return;
        }
        free = free || !Money.world(_world).enabled();
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!free && !Money.world(_world).hasTake(_player, priceWorkbenchUse()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VWorkbench(player), "", 1, 9);
    }
    
    /** Uncrafter **/
    
    public void buyUncrafter(Player bukkitPlayer)
    {
        if(_hasUncrafter)
        {
            sendMessage(bukkitPlayer, Lang.get("uncrafter.max"), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceUncrafterBuy()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        _hasUncrafter = true;
        sendMessage(bukkitPlayer, Lang.get("uncrafter.bought"), ChatColor.GREEN);
    }
    
    public void openUncrafter(Player bukkitPlayer, boolean free)
    {
        if(!_hasUncrafter)
        {
            sendMessage(bukkitPlayer, Lang.get("uncrafter.none"), ChatColor.RED);
            return;
        }
        free = free || !Money.world(_world).enabled();
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!free && !Money.world(_world).hasTake(_player, priceUncrafterUse()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        String guiname = Lang.get("uncrafter.name");
        if(guiname.length() > 32)
        {
            guiname = guiname.substring(0, 32);
        }
        Util.openWindow(player, new VUncrafter(player), guiname, 0, 18);
    }
    
    /** Enchanting table **/
    
    public void buyEnchantTable(Player bukkitPlayer)
    {
        if(_hasEnchantTable)
        {
            sendMessage(bukkitPlayer, Lang.get("enchanttable.max"), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceEnchBuy()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        _hasEnchantTable = true;
        sendMessage(bukkitPlayer, Lang.get("enchanttable.bought"), ChatColor.GREEN);
    }
    
    public void openEnchantTable(Player bukkitPlayer, boolean free)
    {
        if(!_hasEnchantTable)
        {
            sendMessage(bukkitPlayer, Lang.get("enchanttable.none"), ChatColor.RED);
            return;
        }
        free = free || !Money.world(_world).enabled();
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!free && !Money.world(_world).hasTake(_player, priceEnchUse()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VEnchantTable(player, _bookshelves), "", 4, 9);
    }
    
    public void buyBookshelf(Player bukkitPlayer, int amount)
    {
        if(_bookshelves >= _maxBookshelves)
        {
            sendMessage(bukkitPlayer, Lang.get("enchanttable.book.max"), ChatColor.RED);
            return;
        }
        if(amount > _maxBookshelves - _bookshelves)
        {
            amount = _maxBookshelves - _bookshelves;
        }
        if(!Money.world(_world).hasTake(_player, priceEnchBook(amount)))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        _bookshelves += amount;
        sendMessage(bukkitPlayer, (_bookshelves == 1) ? Lang.get("enchanttable.book.one") : Lang.get("enchanttable.book.many", "" + _bookshelves), ChatColor.GREEN);
    }
    
    /** Anvil **/
    
    public void buyAnvil(Player bukkitPlayer)
    {
        if(_hasAnvil)
        {
            sendMessage(bukkitPlayer, Lang.get("anvil.max"), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceAnvilBuy()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        _hasAnvil = true;
        sendMessage(bukkitPlayer, Lang.get("anvil.bought"), ChatColor.GREEN);
    }
    
    public void openAnvil(Player bukkitPlayer, boolean free)
    {
        if(!_hasAnvil)
        {
            sendMessage(bukkitPlayer, Lang.get("anvil.none"), ChatColor.RED);
            return;
        }
        free = free || !Money.world(_world).enabled();
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!free && !Money.world(_world).hasTake(_player, priceAnvilUse()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VAnvil(player), "", 8, 9);
    }
    
    /** Chest **/
    
    public void buyChest(Player bukkitPlayer, int amount)
    {
        int max = Config.getInt(_world, _player, "tools", "chest", "max", true);
        if((_chests.size() + amount > max) && (max != -1))
        {
            sendMessage(bukkitPlayer, Lang.get("chest.max", "" + max), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceChestBuy(amount)))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        for(int i = 0; i < amount; i++)
        {
            _chests.put((Integer)(_chests.size() + 1), new VInv(getChestSize()));
        }
        sendMessage(bukkitPlayer, (_chests.size() == 1) ? Lang.get("chest.bought.one") : Lang.get("chest.bought.many", "" + _chests.size()), ChatColor.GREEN);
    }
    
    public void openChest(Player bukkitPlayer, int nr, boolean free)
    {
        free = free || !Money.world(_world).enabled();
        VInv inv = _chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(bukkitPlayer, Lang.get("chest.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!free && !Money.world(_world).hasTake(_player, priceChestUse()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        VChest container = new VChest(player, inv);
        String chestname = Lang.get("chest.name", "" + nr);
        if(chestname.length() > 32)
        {
            chestname = chestname.substring(0, 32);
        }
        Util.openWindow(player, container, chestname, 0, getChestSize() * 9);
    }
    
    public void dropChest(Player bukkitPlayer, int nr)
    {
        VInv inv = _chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(bukkitPlayer, Lang.get("chest.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        for(int i = 0; i < inv.getSize(); i++)
        {
            ItemStack item = inv.getItem(i);
            if(item != null)
            {
                player.drop(Util.copy(item));
                inv.setItem(i, null);
            }
        }
    }
    
    public void trashChest(Player bukkitPlayer, int nr)
    {
        VInv inv = _chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(bukkitPlayer, Lang.get("chest.none"), ChatColor.RED);
            return;
        }
        _chests.put((Integer)nr, new VInv(getChestSize()));
        sendMessage(bukkitPlayer, Lang.get("chest.trashed", "" + nr), ChatColor.GREEN);
    }
    
    /** Furnace **/
    
    public void buyFurnace(Player bukkitPlayer, int amount)
    {
        int max = Config.getInt(_world, _player, "tools", "furnace", "max", true);
        if((_furnaces.size() + amount > max) && (max != -1))
        {
            sendMessage(bukkitPlayer, Lang.get("furnace.max", "" + max), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceFurnaceBuy(amount)))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        for(int i = 0; i < amount; i++)
        {
            _furnaces.put((Integer)(_furnaces.size() + 1), new VTEFurnace(this));
        }
        sendMessage(bukkitPlayer, (_furnaces.size() == 1) ? Lang.get("furnace.bought.one") : Lang.get("furnace.bought.many", "" + _furnaces.size()), ChatColor.GREEN);
    }
    
    public void openFurnace(Player bukkitPlayer, int nr, boolean free)
    {
        free = free || !Money.world(_world).enabled();
        VTEFurnace fur = _furnaces.get((Integer)nr);
        if(fur == null)
        {
            sendMessage(bukkitPlayer, Lang.get("furnace.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!free && !Money.world(_world).hasTake(_player, priceFurnaceUse()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VFurnace(player, fur), "", 2, 3);
    }
    
    public void linkFurnace(Player bukkitPlayer, int furnaceNR, int chestNR, boolean free)
    {
        free = free || !Money.world(_world).enabled();
        VTEFurnace fur = _furnaces.get((Integer)furnaceNR);
        if(fur == null)
        {
            sendMessage(bukkitPlayer, Lang.get("furnace.none"), ChatColor.RED);
            return;
        }
        if(_chests.get((Integer)chestNR) == null)
        {
            sendMessage(bukkitPlayer, Lang.get("chest.none"), ChatColor.RED);
            return;
        }
        if(!free && (fur.link <= 0))
        {
            if(_fLinks <= 0)
            {
                if(!Money.world(_world).hasTake(_player, priceFurnaceLink()))
                {
                    sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
                    return;
                }
            }
            else
            {
                _fLinks--;
            }
        }
        fur.link = chestNR;
        sendMessage(bukkitPlayer, Lang.get("furnace.linked", "" + furnaceNR, "" + chestNR), ChatColor.GREEN);
    }
    
    public void unlinkFurnace(Player bukkitPlayer, int furnaceNR, boolean free)
    {
        free = free || !Money.world(_world).enabled();
        VTEFurnace fur = _furnaces.get((Integer)furnaceNR);
        if(fur == null)
        {
            sendMessage(bukkitPlayer, Lang.get("furnace.none"), ChatColor.RED);
            return;
        }
        if(fur.link <= 0)
        {
            sendMessage(bukkitPlayer, Lang.get("furnace.nolink"), ChatColor.RED);
            return;
        }
        if(!free)
        {
            _fLinks++;
        }
        fur.link = 0;
        sendMessage(bukkitPlayer, Lang.get("furnace.unlinked", "" + furnaceNR), ChatColor.GREEN);
    }
    
    /** Brewing stand **/
    
    public void buyBrewingstand(Player bukkitPlayer, int amount)
    {
        int max = Config.getInt(_world, _player, "tools", "brewingstand", "max", true);
        if((_brews.size() + amount > max) && (max != -1))
        {
            sendMessage(bukkitPlayer, Lang.get("brewingstand.max", "" + max), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceBrewBuy(amount)))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        for(int i = 0; i < amount; i++)
        {
            _brews.put((Integer)(_brews.size() + 1), new VTEBrewingstand(this));
        }
        sendMessage(bukkitPlayer, (_brews.size() == 1) ? Lang.get("brewingstand.bought.one") : Lang.get("brewingstand.bought.many", "" + _brews.size()), ChatColor.GREEN);
    }
    
    public void openBrewingstand(Player bukkitPlayer, int nr, boolean free)
    {
        free = free || !Money.world(_world).enabled();
        VTEBrewingstand brew = _brews.get((Integer)nr);
        if(brew == null)
        {
            sendMessage(bukkitPlayer, Lang.get("brewingstand.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!free && !Money.world(_world).hasTake(_player, priceBrewUse()))
        {
            sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VBrewingstand(player, brew), "", 5, 4);
    }
    
    public void linkBrewingstand(Player bukkitPlayer, int brewNR, int chestNR, boolean free)
    {
        free = free || !Money.world(_world).enabled();
        VTEBrewingstand brew = _brews.get((Integer)brewNR);
        if(brew == null)
        {
            sendMessage(bukkitPlayer, Lang.get("brewingstand.none"), ChatColor.RED);
            return;
        }
        if(_chests.get((Integer)chestNR) == null)
        {
            sendMessage(bukkitPlayer, Lang.get("chest.none"), ChatColor.RED);
            return;
        }
        if(!free && (brew.link <= 0))
        {
            if(_bLinks <= 0)
            {
                if(!Money.world(_world).hasTake(_player, priceBrewLink()))
                {
                    sendMessage(bukkitPlayer, Lang.get("money.toofew"), ChatColor.RED);
                    return;
                }
            }
            else
            {
                _bLinks--;
            }
        }
        brew.link = chestNR;
        sendMessage(bukkitPlayer, Lang.get("brewingstand.linked", "" + brewNR, "" + chestNR), ChatColor.GREEN);
    }
    
    public void unlinkBrewingstand(Player bukkitPlayer, int brewNR, boolean free)
    {
        free = free || !Money.world(_world).enabled();
        VTEBrewingstand brew = _brews.get((Integer)brewNR);
        if(brew == null)
        {
            sendMessage(bukkitPlayer, Lang.get("brewingstand.none"), ChatColor.RED);
            return;
        }
        if(brew.link <= 0)
        {
            sendMessage(bukkitPlayer, Lang.get("brewingstand.nolink"), ChatColor.RED);
            return;
        }
        if(!free)
        {
            _bLinks++;
        }
        brew.link = 0;
        sendMessage(bukkitPlayer, Lang.get("brewingstand.unlinked", "" + brewNR), ChatColor.GREEN);
    }
    
    /** Trash **/
    
    public void openTrash(Player bukkitPlayer)
    {
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        VTrash container = new VTrash(player);
        String name = Lang.get("trash.name");
        if(name.length() > 32)
        {
            name = name.substring(0, 32);
        }
        Util.openWindow(player, container, name, 0, 9);
    }
    
    /** Sending **/
    
    public void sendItem(Player bukkitPlayer, String reciever, int chestNR)
    {
        ItemStack[] items;
        if(chestNR == 0)
        {
            EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
            ItemStack hand = player.inventory.getItemInHand();
            if(hand == null)
            {
                sendMessage(bukkitPlayer, Lang.get("send.empty"), ChatColor.RED);
                return;
            }
            items = new ItemStack[]{Util.copy(hand)};
            player.inventory.setItem(player.inventory.itemInHandIndex, null);
        }
        else
        {
            VInv inv = _chests.get((Integer)chestNR);
            if(inv == null)
            {
                sendMessage(bukkitPlayer, Lang.get("chest.none"), ChatColor.RED);
                return;
            }
            items = Util.copy(inv.getContents());
            boolean found = false;
            for(ItemStack item : items)
            {
                if(item != null)
                {
                    found = true;
                    break;
                }
            }
            if(!found)
            {
                sendMessage(bukkitPlayer, Lang.get("send.empty"), ChatColor.RED);
                return;
            }
            inv.clear();
        }
        VPack pack = _plugin.getPack(_world, reciever);
        ItemStack[] left = Util.stack(pack.getInvs(), items);
        String message = Lang.get("send.get1", bukkitPlayer.getName());
        String[] touched = Util.getLastStackingIds();
        if(touched.length > 0)
        {
            message += " " + Lang.get("send.get2", Util.implode(", ", touched));
            if(left.length > 0)
            {
                message += " " + Lang.get("send.get3");
            }
        }
        else
        {
            message += " " + Lang.get("send.get4");
        }
        if(left.length > 0)
        {
            for(ItemStack stack : left)
            {
                pack._left.add(stack);
            }
        }
        pack._messages.add(message);
        pack.processSent();
        sendMessage(bukkitPlayer, Lang.get((chestNR == 0) ? "send.done1" : "send.done2", reciever), ChatColor.GREEN);
    }
    
    public void processSent()
    {
        Player bukkitPlayer = Bukkit.getPlayer(_player);
        if(bukkitPlayer == null)
        {
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        for(ItemStack item : _left.toArray(new ItemStack[0]))
        {
            player.drop(item);
        }
        for(String s : _messages.toArray(new String[0]))
        {
            sendMessage(bukkitPlayer, s, ChatColor.GREEN);
        }
        _left = new ArrayList<ItemStack>();
        _messages = new ArrayList<String>();
    }
}
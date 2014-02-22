// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftPlayer;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.minecraft.server.v#MC_VERSION#.*;
import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.util.*;
import net.drgnome.virtualpack.tmp.*; /** FUUU **/

import static net.drgnome.virtualpack.util.Global.*;

public class VPack
{
    public static final int _maxBookshelves = 15;
    private final String _world;
    private final String _player;
    public boolean _hasWorkbench = false;
    public boolean _hasUncrafter = false;
    public boolean _hasEnchantTable = false;
    public boolean _hasAnvil = false;
    /** FUUU **/
    // public MatterInv _matter = null;
    public TmpMatterInv _matter = null;
    public int _bookshelves = 0;
    private int _fLinks = 0;
    private int _bLinks = 0;
    // 0 = workbench
    // 1 = uncrafter
    // 2 = enchanttable
    // 3 = anvil
    // 4 = materializer
    // 5 = chest
    // 6 = furnace
    // 7 = brewingstand
    private long[] _cooldown = new long[8];
    public HashMap<Integer, VInv> _chests = new HashMap<Integer, VInv>();
    public HashMap<Integer, VTEFurnace> _furnaces = new HashMap<Integer, VTEFurnace>();
    public HashMap<Integer, VTEBrewingstand> _brews = new HashMap<Integer, VTEBrewingstand>();
    public ArrayList<ItemStack> _left = new ArrayList<ItemStack>();
    public ArrayList<String> _messages = new ArrayList<String>();
    
    public VPack(String world, String player)
    {
        _world = world;
        _player = player;
        recalculate();
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
            else if(a[0].equals("m"))
            {
                if(a[1].equals("1"))
                {
                    /** FUUU **/
                    // _matter = new MatterInv(_world, _player, Util.cut(a, 2));
                    _matter = new TmpMatterInv(_world, _player, Util.cut(a, 2));
                }
            }
            else if(a[0].equals("c"))
            {
                count[0]++;
                _chests.put(count[0], new VInv(getChestSize(), Util.cut(a, 1)));
            }
            else if(a[0].equals("f"))
            {
                count[1]++;
                _furnaces.put(count[1], new VTEFurnace(this, Util.cut(a, 1)));
            }
            else if(a[0].equals("b"))
            {
                count[2]++;
                _brews.put(count[2], new VTEBrewingstand(this, Util.cut(a, 1)));
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
        list.add("m" + _separator[1] + (_matter == null ? "0" : "1") + (_matter == null ? "" : (_separator[1] + Util.implode(_separator[1], _matter.serialize()))));
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
    
    public String getWorld()
    {
        return _world;
    }
    
    public String getPlayer()
    {
        return _player;
    }
    
    public void tick(int ticks)
    {
        for(VTEFurnace fur : _furnaces.values().toArray(new VTEFurnace[0]))
        {
            fur.tick(ticks);
        }
        for(VTEBrewingstand brew : _brews.values().toArray(new VTEBrewingstand[0]))
        {
            brew.tick(ticks);
        }
    }
    
    public void recalculate()
    {
        String[] groups = Perm.getGroups(_world, _player);
        if(Money.world(_world).enabled())
        {
            _hasWorkbench = _hasWorkbench || (Perm.has(_world, _player, "vpack.use.workbench") && (Config.getDouble(_world, groups, "tools", "workbench", "buy", Config.MODE_MIN) == 0D));
            _hasUncrafter = _hasUncrafter || (Perm.has(_world, _player, "vpack.use.uncrafter") && (Config.getDouble(_world, groups, "tools", "uncrafter", "buy", Config.MODE_MIN) == 0D));
            _hasEnchantTable = _hasEnchantTable || (Perm.has(_world, _player, "vpack.use.enchanttable") && (Config.getDouble(_world, groups, "tools", "enchanttable", "buy", Config.MODE_MIN) == 0D));
            _bookshelves = Util.max(_bookshelves, (_hasEnchantTable && (Config.getDouble(_world, groups, "tools", "enchanttable", "book", Config.MODE_MIN) == 0D)) ? _maxBookshelves : 0);
            _hasAnvil = _hasAnvil || (Perm.has(_world, _player, "vpack.use.anvil") && (Config.getDouble(_world, groups, "tools", "anvil", "buy", Config.MODE_MIN) == 0D));
            if(Perm.has(_world, _player, "vpack.use.materializer") && (Config.getDouble(_world, groups, "tools", "anvil", "buy", Config.MODE_MIN) == 0D))
            {
                /** FUUU **/
                // _matter = new MatterInv(_world, _player);
                _matter = new TmpMatterInv(_world, _player);
            }
            if(Perm.has(_world, _player, "vpack.use.chest"))
            {
                int max = Config.getInt(_world, groups, "tools", "chest", "start", Config.MODE_MAX);
                while(_chests.size() < max)
                {
                    _chests.put(_chests.size() + 1, new VInv(getChestSize()));
                }
            }
            if(Perm.has(_world, _player, "vpack.use.furnace"))
            {
                int max = Config.getInt(_world, groups, "tools", "furnace", "start", Config.MODE_MAX);
                while(_furnaces.size() < max)
                {
                    _furnaces.put(_furnaces.size() + 1, new VTEFurnace(this));
                }
            }
            if(Perm.has(_world, _player, "vpack.use.brewingstand"))
            {
                int max = Config.getInt(_world, groups, "tools", "brewingstand", "start", Config.MODE_MAX);
                while(_brews.size() < max)
                {
                    _brews.put(_brews.size() + 1, new VTEBrewingstand(this));
                }
            }
        }
        else
        {
            _hasWorkbench = _hasWorkbench || Perm.has(_world, _player, "vpack.use.workbench");
            _hasUncrafter = _hasUncrafter || Perm.has(_world, _player, "vpack.use.uncrafter");
            _hasEnchantTable = _hasEnchantTable || Perm.has(_world, _player, "vpack.use.enchanttable");
            _bookshelves = Util.max(_bookshelves, _hasEnchantTable ? _maxBookshelves : 0);
            _hasAnvil = _hasAnvil || Perm.has(_world, _player, "vpack.use.anvil");
            if(Perm.has(_world, _player, "vpack.use.materializer"))
            {
                /** FUUU **/
                // _matter = new MatterInv(_world, _player);
                _matter = new TmpMatterInv(_world, _player);
            }
            if(Perm.has(_world, _player, "vpack.use.chest"))
            {
                int max = Config.getInt(_world, groups, "tools", "chest", "max", Config.MODE_MAX);
                while(_chests.size() < max)
                {
                    _chests.put(_chests.size() + 1, new VInv(getChestSize()));
                }
            }
            if(Perm.has(_world, _player, "vpack.use.furnace"))
            {
                int max = Config.getInt(_world, groups, "tools", "furnace", "max", Config.MODE_MAX);
                while(_furnaces.size() < max)
                {
                    _furnaces.put(_furnaces.size() + 1, new VTEFurnace(this));
                }
            }
            if(Perm.has(_world, _player, "vpack.use.brewingstand"))
            {
                int max = Config.getInt(_world, groups, "tools", "brewingstand", "max", Config.MODE_MAX);
                while(_brews.size() < max)
                {
                    _brews.put(_brews.size() + 1, new VTEBrewingstand(this));
                }
            }
        }
    }
    
    public void cut()
    {
        String[] groups = Perm.getGroups(_world, _player);
        _hasWorkbench = _hasWorkbench && Perm.has(_world, _player, "vpack.use.workbench");
        _hasUncrafter = _hasUncrafter && Perm.has(_world, _player, "vpack.use.uncrafter");
        _hasEnchantTable = _hasEnchantTable && Perm.has(_world, _player, "vpack.use.enchanttable");
        _bookshelves = _hasEnchantTable ? _bookshelves : 0;
        _hasAnvil = _hasAnvil && Perm.has(_world, _player, "vpack.use.anvil");
        if(!Perm.has(_world, _player, "vpack.use.materializer"))
        {
            _matter = null;
        }
        int max = Perm.has(_world, _player, "vpack.use.chest") ? Config.getInt(_world, groups, "tools", "chest", "max", Config.MODE_INFINITE) : 0;
        while((max != - 1) && (_chests.size() > max))
        {
            for(ItemStack item : _chests.get(_chests.size()).getContents())
            {
                _left.add(item);
            }
            _chests.remove(_chests.size());
        }
        max = Perm.has(_world, _player, "vpack.use.furnace") ? Config.getInt(_world, groups, "tools", "furnace", "max", Config.MODE_INFINITE) : 0;
        while((max != - 1) && (_furnaces.size() > max))
        {
            for(ItemStack item : _furnaces.get(_furnaces.size()).getContents())
            {
                _left.add(item);
            }
            _furnaces.remove(_furnaces.size());
        }
        max = Perm.has(_world, _player, "vpack.use.brewingstand") ? Config.getInt(_world, groups, "tools", "brewingstand", "max", Config.MODE_INFINITE) : 0;
        while((max != - 1) && (_brews.size() > max))
        {
            for(ItemStack item : _brews.get(_brews.size()).getContents())
            {
                _left.add(item);
            }
            _brews.remove(_brews.size());
        }
        processDrops();
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
                p.drop(i, false); // I have no idea what the "false" does here
            }
        }
    }
    
    public void wipe()
    {
        if(!Perm.has(_world, _player, "vpack.keep.enchanttable"))
        {
            _bookshelves = 0;
        }
        if((_matter != null) && !Perm.has(_world, _player, "vpack.keep.materializer"))
        {
            _matter.reset();
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
        if(!Perm.has(_world, _player, "vpack.keep.materializer"))
        {
            _matter = null;
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
            if(Perm.has(_world, _player, "vpack.use.workbench"))
            {
                sendMessage(player, Lang.get(player, "stats.workbench", "" + ChatColor.GREEN, _hasWorkbench ? Lang.get(player, "yes") : Lang.get(player, "no")));
            }
            if(Perm.has(_world, _player, "vpack.use.uncrafter"))
            {
                sendMessage(player, Lang.get(player, "stats.uncrafter", "" + ChatColor.GREEN, _hasUncrafter ? Lang.get(player, "yes") : Lang.get(player, "no")));
            }
            if(Perm.has(_world, _player, "vpack.use.enchanttable"))
            {
                sendMessage(player, Lang.get(player, "stats.enchanttable", "" + ChatColor.GREEN, _hasEnchantTable ? Lang.get(player, "yes") : Lang.get(player, "no")) + (_hasEnchantTable ? Lang.get(player, "stats.books", "" + _bookshelves): ""));
            }
            if(Perm.has(_world, _player, "vpack.use.anvil"))
            {
                sendMessage(player, Lang.get(player, "stats.anvil", "" + ChatColor.GREEN, _hasAnvil ? Lang.get(player, "yes") : Lang.get(player, "no")));
            }
            if(Perm.has(_world, _player, "vpack.use.materializer"))
            {
                sendMessage(player, Lang.get(player, "stats.matter", "" + ChatColor.GREEN, _matter == null ? Lang.get(player, "no") : Lang.get(player, "yes")));
            }
            if(Perm.has(_world, _player, "vpack.use.chest"))
            {
                int i = Config.getInt(_world, _player, "tools", "chest", "max", Config.MODE_INFINITE);
                sendMessage(player, Lang.get(player, "stats.chest", "" + ChatColor.GREEN, "" + _chests.size() + (i != -1 ? "/" + i : "")));
            }
            if(Perm.has(_world, _player, "vpack.use.furnace"))
            {
                int i = Config.getInt(_world, _player, "tools", "furnace", "max", Config.MODE_INFINITE);
                sendMessage(player, Lang.get(player, "stats.furnace", "" + ChatColor.GREEN, "" + _furnaces.size() + (i != -1 ? "/" + i : "")) + (Lang.get(player, "stats.link", "" + ChatColor.WHITE, "" + ChatColor.GREEN, "" + _fLinks)));
            }
            if(Perm.has(_world, _player, "vpack.use.brewingstand"))
            {
                int i = Config.getInt(_world, _player, "tools", "brewingstand", "max", Config.MODE_INFINITE);
                sendMessage(player, Lang.get(player, "stats.brewingstand", "" + ChatColor.GREEN, "" + _brews.size() + (i != -1 ? "/" + i : "")) + (Lang.get(player, "stats.link", "" + ChatColor.WHITE, "" + ChatColor.GREEN, "" + _bLinks)));
            }
        }
        else
        {
            if(Perm.has(_world, _player, "vpack.use.workbench"))
            {
                sendMessage(player, Lang.get(player, "stats.workbench", "" + ChatColor.GREEN, Lang.get(player, "yes")));
            }
            if(Perm.has(_world, _player, "vpack.use.uncrafter"))
            {
                sendMessage(player, Lang.get(player, "stats.uncrafter", "" + ChatColor.GREEN, Lang.get(player, "yes")));
            }
            if(Perm.has(_world, _player, "vpack.use.enchanttable"))
            {
                sendMessage(player, Lang.get(player, "stats.enchanttable", "" + ChatColor.GREEN, Lang.get(player, "yes")) + Lang.get(player, "stats.books", "" + _maxBookshelves));
            }
            if(Perm.has(_world, _player, "vpack.use.anvil"))
            {
                sendMessage(player, Lang.get(player, "stats.anvil", "" + ChatColor.GREEN, Lang.get(player, "yes")));
            }
            if(Perm.has(_world, _player, "vpack.use.materializer"))
            {
                sendMessage(player, Lang.get(player, "stats.matter", "" + ChatColor.GREEN, Lang.get(player, "yes")));
            }
            if(Perm.has(_world, _player, "vpack.use.chest"))
            {
                sendMessage(player, Lang.get(player, "stats.chest", "" + ChatColor.GREEN, "" + _chests.size()));
            }
            if(Perm.has(_world, _player, "vpack.use.furnace"))
            {
                sendMessage(player, Lang.get(player, "stats.furnace", "" + ChatColor.GREEN, "" + _furnaces.size()));
            }
            if(Perm.has(_world, _player, "vpack.use.brewingstand"))
            {
                sendMessage(player, Lang.get(player, "stats.brewingstand", "" + ChatColor.GREEN, "" + _brews.size()));
            }
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
    
    public void addInv(Inventory bukkitInv)
    {
        if(bukkitInv == null)
        {
            addInv(new VInv(getChestSize()));
            return;
        }
        org.bukkit.inventory.ItemStack[] stack = bukkitInv.getContents();
        ItemStack[] items = new ItemStack[stack.length];
        for(int i = 0; i < items.length; i++)
        {
            items[i] = CraftItemStack.asNMSCopy(stack[i]);
        }
        addInv(new VInv(getChestSize(), items));
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
        return Config.getDouble(_world, _player, "tools", "workbench", "buy", Config.MODE_MIN, 2);
    }
    
    public double priceWorkbenchUse()
    {
        return Config.getDouble(_world, _player, "tools", "workbench", "use", Config.MODE_MIN, 2);
    }
    
    public double priceUncrafterBuy()
    {
        return Config.getDouble(_world, _player, "tools", "uncrafter", "buy", Config.MODE_MIN, 2);
    }
    
    public double priceUncrafterUse()
    {
        return Config.getDouble(_world, _player, "tools", "uncrafter", "use", Config.MODE_MIN, 2);
    }
    
    public double priceEnchBuy()
    {
        return Config.getDouble(_world, _player, "tools", "enchanttable", "buy", Config.MODE_MIN, 2);
    }
    
    public double priceEnchUse()
    {
        return Config.getDouble(_world, _player, "tools", "enchanttable", "use", Config.MODE_MIN, 2);
    }
    
    public double priceEnchBook(int amount)
    {
        double base = Config.getDouble(_world, _player, "tools", "enchanttable", "book", Config.MODE_MIN);
        double factor = Config.getDouble(_world, _player, "tools", "enchanttable", "multiply", Config.MODE_MIN);
        double price = 0;
        for(int i = 0; i < amount; i++)
        {
            price += (base * Math.pow(factor, numBookshelves() + i));
        }
        return Util.smooth(price, 2);
    }
    
    public double priceAnvilBuy()
    {
        return Config.getDouble(_world, _player, "tools", "anvil", "buy", Config.MODE_MIN, 2);
    }
    
    public double priceAnvilUse()
    {
        return Config.getDouble(_world, _player, "tools", "anvil", "use", Config.MODE_MIN, 2);
    }
    
    public double priceMatterBuy()
    {
        return Config.getDouble(_world, _player, "tools", "materializer", "buy", Config.MODE_MIN, 2);
    }
    
    public double priceMatterUse()
    {
        return Config.getDouble(_world, _player, "tools", "materializer", "use", Config.MODE_MIN, 2);
    }
    
    public double priceChestBuy(int amount)
    {
        double base = Config.getDouble(_world, _player, "tools", "chest", "buy", Config.MODE_MIN);
        double factor = Config.getDouble(_world, _player, "tools", "chest", "multiply", Config.MODE_MIN);
        double price = 0;
        for(int i = 0; i < amount; i++)
        {
            price += (base * Math.pow(factor, numChests() + i));
        }
        return Util.smooth(price, 2);
    }
    
    public double priceChestUse()
    {
        return Config.getDouble(_world, _player, "tools", "chest", "use", Config.MODE_MIN, 2);
    }
    
    public double priceFurnaceBuy(int amount)
    {
        double base = Config.getDouble(_world, _player, "tools", "furnace", "buy", Config.MODE_MIN);
        double factor = Config.getDouble(_world, _player, "tools", "furnace", "multiply", Config.MODE_MIN);
        double price = 0;
        for(int i = 0; i < amount; i++)
        {
            price += (base * Math.pow(factor, numFurnaces() + i));
        }
        return Util.smooth(price, 2);
    }
    
    public double priceFurnaceUse()
    {
        return Config.getDouble(_world, _player, "tools", "furnace", "use", Config.MODE_MIN, 2);
    }
    
    public double priceFurnaceLink()
    {
        return Config.getDouble(_world, _player, "tools", "furnace", "link", Config.MODE_MIN, 2);
    }
    
    public double priceBrewBuy(int amount)
    {
        double base = Config.getDouble(_world, _player, "tools", "brewingstand", "buy", Config.MODE_MIN);
        double factor = Config.getDouble(_world, _player, "tools", "brewingstand", "multiply", Config.MODE_MIN);
        double price = 0;
        for(int i = 0; i < amount; i++)
        {
            price += (base * Math.pow(factor, numBrews() + i));
        }
        return Util.smooth(price, 2);
    }
    
    public double priceBrewUse()
    {
        return Config.getDouble(_world, _player, "tools", "brewingstand", "use", Config.MODE_MIN, 2);
    }
    
    public double priceBrewLink()
    {
        return Config.getDouble(_world, _player, "tools", "brewingstand", "link", Config.MODE_MIN, 2);
    }
    
    public int workbenchCooldown()
    {
        return Config.getInt(_world, _player, "tools", "workbench", "cooldown", Config.MODE_MIN);
    }
    
    public int uncrafterCooldown()
    {
        return Config.getInt(_world, _player, "tools", "uncrafter", "cooldown", Config.MODE_MIN);
    }
    
    public int enchanttableCooldown()
    {
        return Config.getInt(_world, _player, "tools", "enchanttable", "cooldown", Config.MODE_MIN);
    }
    
    public int anvilCooldown()
    {
        return Config.getInt(_world, _player, "tools", "anvil", "cooldown", Config.MODE_MIN);
    }
    
    public int materializerCooldown()
    {
        return Config.getInt(_world, _player, "tools", "materializer", "cooldown", Config.MODE_MIN);
    }
    
    public int chestCooldown()
    {
        return Config.getInt(_world, _player, "tools", "chest", "cooldown", Config.MODE_MIN);
    }
    
    public int furnaceCooldown()
    {
        return Config.getInt(_world, _player, "tools", "furnace", "cooldown", Config.MODE_MIN);
    }
    
    public int brewingstandCooldown()
    {
        return Config.getInt(_world, _player, "tools", "brewingstand", "cooldown", Config.MODE_MIN);
    }
    
    public int getChestSize()
    {
        int s = Config.getInt(_world, _player, "tools", "chest", "size", Config.MODE_MAX);
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
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "workbench.max"), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceWorkbenchBuy()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        _hasWorkbench = true;
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "workbench.bought"), ChatColor.GREEN);
    }
    
    public void openWorkbench(Player bukkitPlayer, boolean admin)
    {
        if(!_hasWorkbench)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "workbench.none"), ChatColor.RED);
            return;
        }
        int wait = workbenchCooldown() - (int)Util.round((double)(System.currentTimeMillis() - _cooldown[0]) / 1000D);
        if(!admin && (wait >= 0))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "cooldown.wait", "" + wait), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!admin && Money.world(_world).enabled() && !Money.world(_world).hasTake(_player, priceWorkbenchUse()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VWorkbench(player), Lang.get(bukkitPlayer, "workbench.name"), 1, 9);
        _cooldown[0] = System.currentTimeMillis();
    }
    
    /** Uncrafter **/
    
    public void buyUncrafter(Player bukkitPlayer)
    {
        if(_hasUncrafter)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "uncrafter.max"), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceUncrafterBuy()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        _hasUncrafter = true;
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "uncrafter.bought"), ChatColor.GREEN);
    }
    
    public void openUncrafter(Player bukkitPlayer, boolean admin)
    {
        if(!_hasUncrafter)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "uncrafter.none"), ChatColor.RED);
            return;
        }
        int wait = uncrafterCooldown() - (int)Util.round((double)(System.currentTimeMillis() - _cooldown[1]) / 1000D);
        if(!admin && (wait >= 0))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "cooldown.wait", "" + wait), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!admin && Money.world(_world).enabled() && !Money.world(_world).hasTake(_player, priceUncrafterUse()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VUncrafter(player), Lang.get(bukkitPlayer, "uncrafter.name"), 0, 18);
        _cooldown[1] = System.currentTimeMillis();
    }
    
    /** Enchanting table **/
    
    public void buyEnchantTable(Player bukkitPlayer)
    {
        if(_hasEnchantTable)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "enchanttable.max"), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceEnchBuy()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        _hasEnchantTable = true;
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "enchanttable.bought"), ChatColor.GREEN);
    }
    
    public void openEnchantTable(Player bukkitPlayer, boolean admin)
    {
        if(!_hasEnchantTable)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "enchanttable.none"), ChatColor.RED);
            return;
        }
        int wait = enchanttableCooldown() - (int)Util.round((double)(System.currentTimeMillis() - _cooldown[2]) / 1000D);
        if(!admin && (wait >= 0))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "cooldown.wait", "" + wait), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!admin && Money.world(_world).enabled() && !Money.world(_world).hasTake(_player, priceEnchUse()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VEnchantTable(player, _bookshelves), Lang.get(bukkitPlayer, "enchanttable.name"), 4, 9);
        _cooldown[2] = System.currentTimeMillis();
    }
    
    public void buyBookshelf(Player bukkitPlayer, int amount)
    {
        if(_bookshelves >= _maxBookshelves)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "enchanttable.book.max", "" + _maxBookshelves), ChatColor.RED);
            return;
        }
        if(amount > _maxBookshelves - _bookshelves)
        {
            amount = _maxBookshelves - _bookshelves;
        }
        if(!Money.world(_world).hasTake(_player, priceEnchBook(amount)))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        _bookshelves += amount;
        sendMessage(bukkitPlayer, (_bookshelves == 1) ? Lang.get(bukkitPlayer, "enchanttable.book.one") : Lang.get(bukkitPlayer, "enchanttable.book.many", "" + _bookshelves), ChatColor.GREEN);
    }
    
    /** Anvil **/
    
    public void buyAnvil(Player bukkitPlayer)
    {
        if(_hasAnvil)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "anvil.max"), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceAnvilBuy()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        _hasAnvil = true;
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "anvil.bought"), ChatColor.GREEN);
    }
    
    public void openAnvil(Player bukkitPlayer, boolean admin)
    {
        if(!_hasAnvil)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "anvil.none"), ChatColor.RED);
            return;
        }
        int wait = anvilCooldown() - (int)Util.round((double)(System.currentTimeMillis() - _cooldown[3]) / 1000D);
        if(!admin && (wait >= 0))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "cooldown.wait", "" + wait), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!admin && Money.world(_world).enabled() && !Money.world(_world).hasTake(_player, priceAnvilUse()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VAnvil(player), Lang.get(bukkitPlayer, "anvil.name"), 8, 9);
        _cooldown[3] = System.currentTimeMillis();
    }
    
    /** Materializer **/
    
    public void buyMaterializer(Player bukkitPlayer)
    {
        if(_matter != null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "matter.max"), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceMatterBuy()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        /** FUUU **/
        // _matter = new MatterInv(_world, _player);
        _matter = new TmpMatterInv(_world, _player);
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "matter.bought"), ChatColor.GREEN);
    }
    
    public void openMaterializer(Player player, boolean admin)
    {
        if(_matter == null)
        {
            sendMessage(player, Lang.get(player, "matter.none"), ChatColor.RED);
            return;
        }
        int wait = materializerCooldown() - (int)Util.round((double)(System.currentTimeMillis() - _cooldown[4]) / 1000D);
        if(!admin && (wait >= 0))
        {
            sendMessage(player, Lang.get(player, "cooldown.wait", "" + wait), ChatColor.RED);
            return;
        }
        EntityPlayer mcPlayer = ((CraftPlayer)player).getHandle(); /** FUUU **/
        if(!admin && Money.world(_world).enabled() && !Money.world(_world).hasTake(_player, priceAnvilUse()))
        {
            sendMessage(player, Lang.get(player, "money.toofew"), ChatColor.RED);
            return;
        }
        _matter.updateInv();
        /** FUUU ALL THE WAY **/
        //Util.openInv(player, _matter);
        Util.openWindow(mcPlayer, new TmpMatter(mcPlayer, _matter), Lang.get(player, "matter.name"), 0, 54);
        _cooldown[4] = System.currentTimeMillis();
    }
    
    /** Chest **/
    
    public void buyChest(Player bukkitPlayer, int amount)
    {
        int max = Config.getInt(_world, _player, "tools", "chest", "max", Config.MODE_INFINITE);
        if((_chests.size() + amount > max) && (max != -1))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "chest.max", "" + max), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceChestBuy(amount)))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        for(int i = 0; i < amount; i++)
        {
            _chests.put((Integer)(_chests.size() + 1), new VInv(getChestSize()));
        }
        sendMessage(bukkitPlayer, (_chests.size() == 1) ? Lang.get(bukkitPlayer, "chest.bought.one") : Lang.get(bukkitPlayer, "chest.bought.many", "" + _chests.size()), ChatColor.GREEN);
    }
    
    public void openChest(Player bukkitPlayer, int nr, boolean admin)
    {
        VInv inv = _chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "chest.none"), ChatColor.RED);
            return;
        }
        int wait = chestCooldown() - (int)Util.round((double)(System.currentTimeMillis() - _cooldown[5]) / 1000D);
        if(!admin && (wait >= 0))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "cooldown.wait", "" + wait), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!admin && Money.world(_world).enabled() && !Money.world(_world).hasTake(_player, priceChestUse()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        int size = getChestSize() * 9;
        inv.resize(size);
        VChest container = new VChest(player, inv);
        Util.openWindow(player, container, Lang.get(bukkitPlayer, "chest.name", "" + nr), 0, size);
        _cooldown[5] = System.currentTimeMillis();
    }
    
    public void dropChest(Player bukkitPlayer, int nr)
    {
        VInv inv = _chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "chest.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        for(int i = 0; i < inv.getSize(); i++)
        {
            ItemStack item = inv.getItem(i);
            if(item != null)
            {
                player.drop(Util.copy_old(item), false); // I have no idea what the "false" does here
                inv.setItem(i, null);
            }
        }
    }
    
    public void trashChest(Player bukkitPlayer, int nr)
    {
        VInv inv = _chests.get((Integer)nr);
        if(inv == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "chest.none"), ChatColor.RED);
            return;
        }
        _chests.put((Integer)nr, new VInv(getChestSize()));
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "chest.trashed", "" + nr), ChatColor.GREEN);
    }
    
    /** Furnace **/
    
    public void buyFurnace(Player bukkitPlayer, int amount)
    {
        int max = Config.getInt(_world, _player, "tools", "furnace", "max", Config.MODE_INFINITE);
        if((_furnaces.size() + amount > max) && (max != -1))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "furnace.max", "" + max), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceFurnaceBuy(amount)))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        for(int i = 0; i < amount; i++)
        {
            _furnaces.put((Integer)(_furnaces.size() + 1), new VTEFurnace(this));
        }
        sendMessage(bukkitPlayer, (_furnaces.size() == 1) ? Lang.get(bukkitPlayer, "furnace.bought.one") : Lang.get(bukkitPlayer, "furnace.bought.many", "" + _furnaces.size()), ChatColor.GREEN);
    }
    
    public void openFurnace(Player bukkitPlayer, int nr, boolean admin)
    {
        VTEFurnace fur = _furnaces.get((Integer)nr);
        if(fur == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "furnace.none"), ChatColor.RED);
            return;
        }
        int wait = furnaceCooldown() - (int)Util.round((double)(System.currentTimeMillis() - _cooldown[6]) / 1000D);
        if(!admin && (wait >= 0))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "cooldown.wait", "" + wait), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!admin && Money.world(_world).enabled() && !Money.world(_world).hasTake(_player, priceFurnaceUse()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VFurnace(player, fur), Lang.get(bukkitPlayer, "furnace.name", "" + nr), 2, 3);
        _cooldown[6] = System.currentTimeMillis();
    }
    
    public void linkFurnace(Player bukkitPlayer, int furnaceNR, int chestNR, boolean admin)
    {
        VTEFurnace fur = _furnaces.get((Integer)furnaceNR);
        if(fur == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "furnace.none"), ChatColor.RED);
            return;
        }
        if(_chests.get((Integer)chestNR) == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "chest.none"), ChatColor.RED);
            return;
        }
        if(!admin && Money.world(_world).enabled() && (fur.link <= 0))
        {
            if(_fLinks <= 0)
            {
                if(!Money.world(_world).hasTake(_player, priceFurnaceLink()))
                {
                    sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
                    return;
                }
            }
            else
            {
                _fLinks--;
            }
        }
        fur.link = chestNR;
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "furnace.linked", "" + furnaceNR, "" + chestNR), ChatColor.GREEN);
    }
    
    public void unlinkFurnace(Player bukkitPlayer, int furnaceNR, boolean admin)
    {
        VTEFurnace fur = _furnaces.get((Integer)furnaceNR);
        if(fur == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "furnace.none"), ChatColor.RED);
            return;
        }
        if(fur.link <= 0)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "furnace.nolink"), ChatColor.RED);
            return;
        }
        if(!admin && Money.world(_world).enabled())
        {
            _fLinks++;
        }
        fur.link = 0;
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "furnace.unlinked", "" + furnaceNR), ChatColor.GREEN);
    }
    
    /** Brewing stand **/
    
    public void buyBrewingstand(Player bukkitPlayer, int amount)
    {
        int max = Config.getInt(_world, _player, "tools", "brewingstand", "max", Config.MODE_INFINITE);
        if((_brews.size() + amount > max) && (max != -1))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "brewingstand.max", "" + max), ChatColor.RED);
            return;
        }
        if(!Money.world(_world).hasTake(_player, priceBrewBuy(amount)))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        for(int i = 0; i < amount; i++)
        {
            _brews.put((Integer)(_brews.size() + 1), new VTEBrewingstand(this));
        }
        sendMessage(bukkitPlayer, (_brews.size() == 1) ? Lang.get(bukkitPlayer, "brewingstand.bought.one") : Lang.get(bukkitPlayer, "brewingstand.bought.many", "" + _brews.size()), ChatColor.GREEN);
    }
    
    public void openBrewingstand(Player bukkitPlayer, int nr, boolean admin)
    {
        VTEBrewingstand brew = _brews.get((Integer)nr);
        if(brew == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "brewingstand.none"), ChatColor.RED);
            return;
        }
        int wait = brewingstandCooldown() - (int)Util.round((double)(System.currentTimeMillis() - _cooldown[7]) / 1000D);
        if(!admin && (wait >= 0))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "cooldown.wait", "" + wait), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        if(!admin && Money.world(_world).enabled() && !Money.world(_world).hasTake(_player, priceBrewUse()))
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
            return;
        }
        Util.openWindow(player, new VBrewingstand(player, brew), Lang.get(bukkitPlayer, "brewingstand.name", "" + nr), 5, 4);
        _cooldown[7] = System.currentTimeMillis();
    }
    
    public void linkBrewingstand(Player bukkitPlayer, int brewNR, int chestNR, boolean admin)
    {
        VTEBrewingstand brew = _brews.get((Integer)brewNR);
        if(brew == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "brewingstand.none"), ChatColor.RED);
            return;
        }
        if(_chests.get((Integer)chestNR) == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "chest.none"), ChatColor.RED);
            return;
        }
        if(!admin && Money.world(_world).enabled() && (brew.link <= 0))
        {
            if(_bLinks <= 0)
            {
                if(!Money.world(_world).hasTake(_player, priceBrewLink()))
                {
                    sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "money.toofew"), ChatColor.RED);
                    return;
                }
            }
            else
            {
                _bLinks--;
            }
        }
        brew.link = chestNR;
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "brewingstand.linked", "" + brewNR, "" + chestNR), ChatColor.GREEN);
    }
    
    public void unlinkBrewingstand(Player bukkitPlayer, int brewNR, boolean admin)
    {
        VTEBrewingstand brew = _brews.get((Integer)brewNR);
        if(brew == null)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "brewingstand.none"), ChatColor.RED);
            return;
        }
        if(brew.link <= 0)
        {
            sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "brewingstand.nolink"), ChatColor.RED);
            return;
        }
        if(!admin && Money.world(_world).enabled())
        {
            _bLinks++;
        }
        brew.link = 0;
        sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "brewingstand.unlinked", "" + brewNR), ChatColor.GREEN);
    }
    
    /** Trash **/
    
    public void openTrash(Player bukkitPlayer)
    {
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        VTrash container = new VTrash(player);
        Util.openWindow(player, container, Lang.get(bukkitPlayer, "trash.name"), 0, 9);
    }
    
    /** Sending **/
    
    public void sendItem(Player bukkitPlayer, String reciever, int chestNR, boolean copy)
    {
        ItemStack[] items;
        if(chestNR == 0)
        {
            EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
            ItemStack hand = player.inventory.getItemInHand();
            if(hand == null)
            {
                sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "send.empty"), ChatColor.RED);
                return;
            }
            items = new ItemStack[]{Util.copy_old(hand)};
            if(!copy)
            {
                player.inventory.setItem(player.inventory.itemInHandIndex, null);
            }
        }
        else
        {
            VInv inv = _chests.get((Integer)chestNR);
            if(inv == null)
            {
                sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "chest.none"), ChatColor.RED);
                return;
            }
            items = Util.copy_old(inv.getContents());
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
                sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, "send.empty"), ChatColor.RED);
                return;
            }
            if(!copy)
            {
                inv.clear();
            }
        }
        if(reciever.equalsIgnoreCase("-all"))
        {
            for(VPack pack : _plugin.getPacks(_world))
            {
                if(_player.equalsIgnoreCase(pack.getPlayer()))
                {
                    continue;
                }
                sendItem(bukkitPlayer, pack, Util.copy_old(items));
                sendMessage(bukkitPlayer, Lang.get(bukkitPlayer, (chestNR == 0) ? "send.done1" : "send.done2", pack.getPlayer()), ChatColor.GREEN);
            }
        }
        else
        {
            sendItem(bukkitPlayer, _plugin.getPack(_world, reciever), items);
        }
    }
    
    private void sendItem(Player bukkitPlayer, VPack pack, ItemStack... items)
    {
        String message = Lang.get(bukkitPlayer, "send.get1", bukkitPlayer.getName());
        ItemStack[] left;
        if(Config.bool("send.drop"))
        {
            left = items;
            message += " " + Lang.get(bukkitPlayer, "send.get4");
        }
        else
        {
            left = Util.stack(pack.getInvs(), items);
            String[] touched = Util.getLastStackingIds();
            if(touched.length > 0)
            {
                message += " " + Lang.get(bukkitPlayer, "send.get2", Util.implode(", ", touched));
                if(left.length > 0)
                {
                    message += " " + Lang.get(bukkitPlayer, "send.get3");
                }
            }
            else
            {
                message += " " + Lang.get(bukkitPlayer, "send.get4");
            }
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
    }
    
    public void processSent()
    {
        Player bukkitPlayer = Bukkit.getPlayer(_player);
        if(bukkitPlayer == null)
        {
            return;
        }
        processDrops(bukkitPlayer);
        _left = new ArrayList<ItemStack>();
        if(_messages.size() <= 0)
        {
            return;
        }
        String[] array = _messages.toArray(new String[0]);
        for(String s : array)
        {
            sendMessage(bukkitPlayer, s, ChatColor.GREEN);
        }
        if(Config.getInt("send.notify-interval") > 0)
        {
            _plugin.annoyPlayer(bukkitPlayer, array);
        }
        _messages = new ArrayList<String>();
    }
    
    private void processDrops()
    {
        Player bukkitPlayer = Bukkit.getPlayer(_player);
        if(bukkitPlayer != null)
        {
            processDrops(bukkitPlayer);
        }
    }
    
    private void processDrops(Player bukkitPlayer)
    {
        EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        for(ItemStack item : _left.toArray(new ItemStack[0]))
        {
            player.drop(item, false); // I have no idea what the "false" does here
        }
    }
}
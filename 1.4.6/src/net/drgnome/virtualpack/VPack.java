// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.util.*;

public class VPack
{
    public static final int _maxBookshelves = 15;
    private final String _world;
    private final String _player;
    public boolean _hasWorkbench;
    public boolean _hasUncrafter;
    public boolean _hasEnchantTable;
    public int _bookshelves;
    private int _fLinks;
    private int _bLinks;
    public HashMap<Integer, VInv> _chests;
    public HashMap<Integer, VTEFurnace> _furnaces;
    public HashMap<Integer, VTEBrewingstand> _brewingstands;
    
    public VPack(String world, String player)
    {
        _world = world;
        _player = player;
        _fLinks = 0;
        _bLinks = 0;
        _chests = new HashMap<Integer, VInv>();
        _furnaces = new HashMap<Integer, VTEFurnace>();
        _brewingstands = new HashMap<Integer, VTEBrewingstand>();
        String[] groups = Perm.getGroups(_world, _player);
        if(Money.world(_world).enabled())
        {
            _hasWorkbench = Config.getDouble(_world, groups, "tools", "workbench", "buy", false) == 0D;
            _hasUncrafter = Config.getDouble(_world, groups, "tools","uncrafter", "buy", false) == 0D;
            _hasEnchantTable = Config.getDouble(_world, groups, "tools","enchanttable", "buy", false) == 0D;
            _bookshelves = Config.getDouble(_world, groups, "tools","enchanttable", "book", false) == 0D ? _maxBookshelves : 0;
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
                _brewingstands.put((Integer)i, new VTEBrewingstand(this));
            }
        }
        else
        {
            _hasWorkbench = true;
            _hasUncrafter = true;
            _hasEnchantTable = true;
            _bookshelves = _maxBookshelves;
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
                _brewingstands.put((Integer)i, new VTEBrewingstand(this));
            }
        }
    }
    
    public void tick()
    {
        
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
    
    private int getChestSize()
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
}
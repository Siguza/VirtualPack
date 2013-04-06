// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.InventoryType;
import net.drgnome.virtualpack.data.TransmutationHelper;
import net.drgnome.virtualpack.item.ComparativeItemStack;
import net.drgnome.virtualpack.item.ValuedItemStack;
import net.drgnome.virtualpack.util.Config;
import net.drgnome.virtualpack.util.Lang;
import net.drgnome.virtualpack.util.Util;

public class MatterInv extends BaseInv
{
    public static final String _prefix = Util.parseColors("afar");
    protected ArrayList<ComparativeItemStack> _unlocked = new ArrayList<ComparativeItemStack>();
    protected boolean _allUnlocked = false;
    protected double _value = 0D;
    protected String _worldname;
    protected String _playername;
    
    public MatterInv(String worldname, String playername)
    {
        super(Lang.get("matter.name"), 54);
        _worldname = worldname;
        _playername = playername;
        if(Config.bool("transmutation.enabled"))
        {
            updateInfo();
            updateInv();
        }
    }
    
    public MatterInv(String worldname, String playername, String[] data)
    {
        super(Lang.get("matter.name"), 54);
        _worldname = worldname;
        _playername = playername;
        if(data.length > 0)
        {
            _value = Util.parseBigDouble(data[0]);
        }
        for(int i = 1; i < data.length; i++)
        {
            if(data[i].equals("[all]"))
            {
                _allUnlocked = true;
                break;
            }
            _unlocked.add(new ComparativeItemStack(Util.base64de(data[i])));
        }
        updateInfo();
        updateInv();
    }
    
    public String[] serialize()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add(Util.printDoublePlain(_value));
        if(_allUnlocked)
        {
            list.add("[all]");
        }
        else
        {
            for(ComparativeItemStack stack : _unlocked)
            {
                list.add(Util.base64en(stack.serialize()));
            }
        }
        return list.toArray(new String[0]);
    }
    
    public InventoryType getType()
    {
        return InventoryType.CHEST;
    }
    
    public void setItem(int index, ItemStack item)
    {
        if((index > 0) && (index < 9))
        {
            if(index == 8)
            {
                super.setItem(index, item);
                updateInv();
                return;
            }
            double value = TransmutationHelper.getValue(item);
            if((value > 0) && ((item.getType().getMaxDurability() <= 0) || (item.getDurability() == 0)))
            {
                if(unlock(item))
                {
                    _value += value * (double)item.getAmount();
                }
                updateInfo();
            }
            else
            {
                super.setItem(index, item);
            }
        }
    }
    
    public void onClose(HumanEntity player)
    {
        for(int i = 1; i < 9; i++)
        {
            if(_contents[i] != null)
            {
                player.getWorld().dropItemNaturally(player.getLocation(), _contents[i]);
                _contents[i] = null;
            }
        }
    }
    
    public void updateInfo()
    {
        _contents[0] = new ItemStack(399, 1);
        ItemMeta meta = _contents[0].getItemMeta();
        meta.setDisplayName(_prefix + Util.parseColors(Config.string("transmutation.color.stored-name")) + Lang.get("matter.stored"));
        meta.setLore(Util.createList(Util.parseColors(Config.string("transmutation.color.stored-value")) + Util.formatDouble(_value)));
        _contents[0].setItemMeta(meta);
    }
    
    public void updateInv()
    {
        ValuedItemStack[] items = _allUnlocked ? TransmutationHelper.getAllFiltered(_worldname, _playername) : TransmutationHelper.getAllFiltered(_worldname, _playername, _unlocked.toArray(new ComparativeItemStack[0]));
        int off = 0;
        if(_contents[8] != null)
        {
            ComparativeItemStack lock = new ComparativeItemStack(_contents[8]);
            for(; off < items.length; off++)
            {
                if(lock.matches(items[off]))
                {
                    break;
                }
            }
        }
        for(int i = 9; i < _contents.length; i++)
        {
            if((i + off - 9) >= items.length)
            {
                _contents[i] = null;
                continue;
            }
            double value = TransmutationHelper.getValue(items[i + off - 9]);
            if(value <= 0)
            {
                _contents[i] = null;
                continue;
            }
            _contents[i] = items[i + off - 9].createStack(1);
        }
    }
    
    public boolean allowClick(Player player, int slot, boolean right, boolean shift)
    {
        if(slot == 0)
        {
            return false;
        }
        else if((slot >= 9) && (slot < 54))
        {
            if((_contents[slot] == null) || ((player.getItemOnCursor() != null) && (shift || !player.getItemOnCursor().isSimilar(_contents[slot]))))
            {
                return false;
            }
            double value = TransmutationHelper.getValue(_contents[slot]);
            if(_value < value)
            {
                return false;
            }
            if(shift)
            {
                int max = Util.min(Util.floor(_value / value), _contents[slot].getMaxStackSize());
                ItemStack stack = _contents[slot].clone();
                stack.setAmount(max);
                HashMap<Integer, ItemStack> map = player.getInventory().addItem(stack);
                if(map.size() > 0)
                {
                    max -= map.values().iterator().next().getAmount();
                }
                _value -= (value * (double)max);
            }
            else
            {
                int max = right ? Util.min(Util.floor(_value / value), player.getItemOnCursor() == null ? _contents[slot].getMaxStackSize() : (player.getItemOnCursor().getMaxStackSize() - player.getItemOnCursor().getAmount())) : 1;
                ItemStack stack;
                if(player.getItemOnCursor() == null)
                {
                    stack = _contents[slot].clone();
                    stack.setAmount(max);
                }
                else
                {
                    stack = player.getItemOnCursor();
                    stack.setAmount(player.getItemOnCursor().getAmount() + max);
                }
                player.setItemOnCursor(stack);
                _value -= (value * (double)max);
            }
            updateInfo();
            updateInv();
            return false;
        }
        else if(shift && (slot >= 54))
        {
            for(int i = 1; i < 8; i++)
            {
                if(_contents[i] == null)
                {
                    int s = slot - 54;
                    setItem(i, player.getInventory().getItem((s >= 27) ? (s - 27) : (s + 9)));
                }
            }
            return false;
        }
        return true;
    }
    
    private boolean unlock(ItemStack item)
    {
        if(_allUnlocked)
        {
            return true;
        }
        if(Config.isGodItem(item))
        {
            _allUnlocked = true;
            updateInv();
            return false;
        }
        for(ComparativeItemStack stack : _unlocked)
        {
            if(stack.matches(item))
            {
                return true;
            }
        }
        _unlocked.add(new ComparativeItemStack(item));
        updateInv();
        return true;
    }
    
    public void reset()
    {
        _allUnlocked = false;
        _unlocked = new ArrayList<ComparativeItemStack>();
        _value = 0D;
    }
}
// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import net.drgnome.virtualpack.util.*;
import net.drgnome.virtualpack.item.ComparativeItemStack;

public abstract class BaseInv implements Inventory
{
    protected String _name;
    protected ItemStack[] _contents;
    
    public BaseInv(String name, int size)
    {
        _name = name;
        _contents = new ItemStack[size];
    }
    
    public String getName()
    {
        return _name;
    }
    
    public String getTitle()
    {
        return getName();
    }
    
    public int getSize()
    {
        return _contents.length;
    }
    
    public int getMaxStackSize()
    {
        return 64;
    }
    
    public void setMaxStackSize(int size)
    {
    }
    
    public ItemStack getItem(int index)
    {
        if((index < 0) || (index >= _contents.length))
        {
            return null;
        }
        return _contents[index];
    }
    
    public void setItem(int index, ItemStack item)
    {
        if((index >= 0) && (index < _contents.length))
        {
            _contents[index] = item;
        }
    }
    
    public ItemStack getItemCopy(int index)
    {
        return Util.copy(getItem(index));
    }
    
    public void setItemCopy(int index, ItemStack item)
    {
        setItem(index, Util.copy(item));
    }
    
    public ItemStack[] getContents()
    {
        return _contents;
    }
    
    public void setContents(ItemStack... items)
    {
        _contents = items;
    }
    
    public ItemStack[] getContentsCopy()
    {
        return Util.copy(_contents);
    }
    
    public void setContentsCopy(ItemStack... items)
    {
        _contents = Util.copy(items);
    }
    
    public int first(int id)
    {
        return first(id, (short)-1);
    }
    
    public int first(int id, short meta)
    {
        return first(new ComparativeItemStack(id, meta));
    }
    
    public int first(ComparativeItemStack stack)
    {
        for(int i = 0; i < _contents.length; i++)
        {
            if(stack.matches(_contents[i]))
            {
                return i;
            }
        }
        return -1;
    }
    
    public int first(Material material)
    {
        return first(material == null ? 0 : material.getId());
    }
    
    public int first(ItemStack item)
    {
        return first(new ComparativeItemStack(item));
    }
    
    public int firstEmpty()
    {
        return first(0);
    }
    
    public void clear()
    {
        _contents = new ItemStack[_contents.length];
    }
    
    public void clear(int index)
    {
        setItem(index, null);
    }
    
    public List<HumanEntity> getViewers()
    {
        return new ArrayList<HumanEntity>();
    }
    
    public InventoryHolder getHolder()
    {
        return null;
    }
    
    public ListIterator<ItemStack> iterator()
    {
        return getContentsList().listIterator();
    }
    
    public ListIterator<ItemStack> iterator(int index)
    {
        return getContentsList().listIterator(index);
    }
    
    private List<ItemStack> getContentsList()
    {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        for(ItemStack item : _contents)
        {
            if(item != null)
            {
                list.add(item);
            }
        }
        return list;
    }
    
    public HashMap<Integer, ItemStack> all(int id)
    {
        return all(id, (short)-1);
    }
    
    public HashMap<Integer, ItemStack> all(int id, short meta)
    {
        return all(new ComparativeItemStack(id, meta));
    }
    
    public HashMap<Integer, ItemStack> all(Material material)
    {
        return all(material == null ? 0 : material.getId());
    }
    
    public HashMap<Integer, ItemStack> all(ItemStack item)
    {
        return all(new ComparativeItemStack(item));
    }
    
    public HashMap<Integer, ItemStack> all(ComparativeItemStack stack)
    {
        HashMap<Integer, ItemStack> map = new HashMap<Integer, ItemStack>();
        for(int i = 0; i < _contents.length; i++)
        {
            if(stack.matches(_contents[i]))
            {
                map.put(i, _contents[i]);
            }
        }
        return map;
    }
    
    public boolean contains(int id)
    {
        return contains(id, 1);
    }
    
    public boolean contains(int id, int amount)
    {
        return contains(id, amount, (short)-1);
    }
    
    public boolean contains(int id, int amount, short meta)
    {
        return contains(id, amount, (short)-1);
    }
    
    public boolean contains(Material material)
    {
        return contains(material, 1);
    }
    
    public boolean contains(Material material, int amount)
    {
        return contains(material == null ? 0 : material.getId(), amount);
    }
    
    public boolean contains(ItemStack item)
    {
        return contains(item, 1);
    }
    
    public boolean contains(ItemStack item, int amount)
    {
        return contains(new ComparativeItemStack(item), amount);
    }
    
    public boolean contains(ComparativeItemStack stack, int amount)
    {
        for(ItemStack item : _contents)
        {
            if((item != null) && stack.matches(item) && ((amount -= item.getAmount()) <= 0))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean containsAtLeast(ItemStack item, int amount)
    {
        return contains(item, amount);
    }
    
    public void remove(int id)
    {
        remove(id, (short)-1);
    }
    
    public void remove(int id, short meta)
    {
        remove(new ComparativeItemStack(id, meta));
    }
    
    public void remove(Material material)
    {
        remove(material == null ? 0 : material.getId());
    }
    
    public void remove(ItemStack item)
    {
        remove(new ComparativeItemStack(item));
    }
    
    public void remove(ComparativeItemStack stack)
    {
        for(int i = 0; i < _contents.length; i++)
        {
            if(stack.matches(_contents[i]))
            {
                _contents[i] = null;
            }
        }
    }
    
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items)
    {
        HashMap<Integer, ItemStack> map = new HashMap<Integer, ItemStack>();
        for(int j = 0; j < items.length; j++)
        {
            ItemStack item = items[j];
            ComparativeItemStack mirror = new ComparativeItemStack(item);
            int amount = item.getAmount();
            for(int i = 0; i < _contents.length; i++)
            {
                if(mirror.matches(_contents[i]))
                {
                    int max = (amount > _contents[i].getAmount()) ? _contents[i].getAmount() : amount;
                    int now = _contents[i].getAmount() - max;
                    if(now <= 0)
                    {
                        _contents[i] = null;
                    }
                    else
                    {
                        _contents[i].setAmount(now);
                    }
                    if((amount -= max) <= 0)
                    {
                        break;
                    }
                }
            }
            if(amount > 0)
            {
                item.setAmount(amount);
                map.put(j, item);
            }
        }
        return map;
    }
    
    public HashMap<Integer, ItemStack> addItem(ItemStack... items)
    {
        HashMap<Integer, ItemStack> map = new HashMap<Integer, ItemStack>();
        for(int j = 0; j < items.length; j++)
        {
            ItemStack item = items[j];
            ComparativeItemStack mirror = new ComparativeItemStack(item);
            int amount = item.getAmount();
            for(int i = 0; i < _contents.length; i++)
            {
                if(_contents[i] == null)
                {
                    amount = 0;
                    _contents[i] = item;
                    break;
                }
                if(mirror.matches(_contents[i]))
                {
                    int max = (_contents[i].getAmount() + amount > _contents[i].getMaxStackSize()) ? _contents[i].getMaxStackSize() : (_contents[i].getAmount() + amount);
                    int add = max - _contents[i].getAmount();
                    _contents[i].setAmount(max);
                    if((amount -= add) <= 0)
                    {
                        break;
                    }
                }
            }
            if(amount > 0)
            {
                item.setAmount(amount);
                map.put(j, item);
            }
        }
        return map;
    }
    
    public boolean allowClick(Player player, int slot, boolean right, boolean shift)
    {
        return true;
    }
    
    public void onClose(HumanEntity player)
    {
    }
}
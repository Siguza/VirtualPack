// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.entity.HumanEntity;
import net.drgnome.virtualpack.util.*;

public abstract class BaseInventory implements Inventory
{
    protected String _name;
    protected int _stackSize = 64;
    protected ItemStack[] _contents;
    
    public BaseInventory(String name, int size)
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
        return _stackSize;
    }
    
    public void setMaxStackSize(int size)
    {
        _stackSize = size;
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
    
    public int first(int id)
    {
        return first(id, -1);
    }
    
    public int first(int id, int meta)
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
        return first(material, -1);
    }
    
    public int first(Material material, int meta)
    {
        return first(material == null ? 0 : material.getId(), meta);
    }
    
    public int first(ItemStack item)
    {
        return first(new ComparativeItemStack(item));
    }
    
    public int firstEmpty()
    {
        return first(0);
    }
    
    public void remove(int id)
    {
        remove(id, -1);
    }
    
    public void remove(int id, int meta)
    {
        remove(new ComparativeItemStack(id, meta));
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
    
    public void remove(Material material)
    {
        remove(material, -1);
    }
    
    public void remove(Material material, int meta)
    {
        remove(material == null ? 0 : material.getId(), meta);
    }
    
    public void remove(ItemStack item)
    {
        remove(new ComparativeItemStack(item));
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
    
    public void onClose(HumanEntity player)
    {
    }
}
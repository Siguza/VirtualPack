// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import net.drgnome.virtualpack.util.Util;

public class ComparativeItemStack
{
    protected static short _defaultMeta = 0;
    protected int _id;
    protected short _meta;

    public static boolean hasSubtypes(int id)
    {
        return hasSubtypes(Material.getMaterial(id));
    }

    public static boolean hasSubtypes(Material m)
    {
        return (m == null) || m.isBlock() || (m.getMaxDurability() == 0);
    }

    public ComparativeItemStack(String data)
    {
        _meta = _defaultMeta;
        if(data.contains(":"))
        {
            String[] split = data.split(":");
            if(split[1].equals("*"))
            {
                _meta = -1;
            }
            else
            {
                _meta = (short)Util.tryParse(split[1], _meta);
            }
            data = split[0];
        }
        try
        {
            _id = Integer.parseInt(data);
        }
        catch(Exception e)
        {
            Material m = Material.getMaterial(data.toUpperCase());
            _id = (m == null) ? 0 : m.getId();
        }
    }

    public ComparativeItemStack(ItemStack item)
    {
        this(item == null ? 0 : item.getTypeId(), item == null ? _defaultMeta : item.getDurability());
    }

    public ComparativeItemStack(int id, short meta)
    {
        _id = id;
        _meta = meta;
    }

    public boolean matches(ItemStack item)
    {
        if(item == null)
        {
            return _id == 0;
        }
        return (item.getTypeId() == _id) && ((_meta == -1) || (item.getDurability() == -1) || (!hasSubtypes(_id)) || (_meta == item.getDurability()));
    }

    public boolean matches(ComparativeItemStack stack)
    {
        if(stack == null)
        {
            return _id == 0;
        }
        return (stack._id == _id) && ((_meta == -1) || (stack._meta == -1) || (!hasSubtypes(_id)) || (_meta == stack._meta));
    }

    public int getId()
    {
        return _id;
    }

    public ItemStack createStack(int stackSize)
    {
        if(stackSize <= 0)
        {
            return null;
        }
        int maxStack = Material.getMaterial(_id).getMaxStackSize();
        return new ItemStack(_id, stackSize > maxStack ? maxStack : stackSize, _meta < 0 ? 0 : _meta);
    }

    public String serialize()
    {
        return (_id + ":" + (_meta == -1 ? "*" : _meta));
    }
}

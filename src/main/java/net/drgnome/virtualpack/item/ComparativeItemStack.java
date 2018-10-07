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
    protected Material _type;
    protected short _meta;

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
        _type = Material.getMaterial(data.toUpperCase());
    }

    public ComparativeItemStack(ItemStack item)
    {
        this(item == null ? Material.AIR : item.getType(), item == null ? _defaultMeta : item.getDurability());
    }

    public ComparativeItemStack(Material type, short meta)
    {
        _type = type;
        _meta = meta;
    }

    public boolean matches(ItemStack item)
    {
        if(item == null)
        {
            return _type == Material.AIR;
        }
        return (item.getType() == _type) && ((_meta == -1) || (item.getDurability() == -1) || (!hasSubtypes(_type)) || (_meta == item.getDurability()));
    }

    public boolean matches(ComparativeItemStack stack)
    {
        if(stack == null)
        {
            return _type == Material.AIR;
        }
        return (stack._type == _type) && ((_meta == -1) || (stack._meta == -1) || (!hasSubtypes(_type)) || (_meta == stack._meta));
    }

    public Material getType()
    {
        return _type;
    }

    public ItemStack createStack(int stackSize)
    {
        if(stackSize <= 0)
        {
            return null;
        }
        int maxStack = _type.getMaxStackSize();
        return new ItemStack(_type, stackSize > maxStack ? maxStack : stackSize, _meta < 0 ? 0 : _meta);
    }

    public String serialize()
    {
        return _type.name() + ":" + (_meta == -1 ? "*" : _meta);
    }
}

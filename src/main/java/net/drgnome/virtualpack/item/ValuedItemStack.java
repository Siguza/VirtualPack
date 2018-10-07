// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import net.drgnome.virtualpack.util.Util;

public class ValuedItemStack extends ComparativeItemStack
{
    private double _value;

    public ValuedItemStack(String data, double value)
    {
        super(data);
        _value = value;
    }

    public ValuedItemStack(ItemStack item, double value)
    {
        super(item);
        _value = value;
    }

    public ValuedItemStack(Material type, short meta, double value)
    {
        super(type, meta);
        _value = value;
    }

    public double getValue()
    {
        return _value;
    }

    public String toString()
    {
        return _type.name() + (_meta > 0 ? ":" + _meta : "");
    }
}

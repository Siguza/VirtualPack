// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
    
    public ValuedItemStack(int id, short meta, double value)
    {
        super(id, meta);
        _value = value;
    }
    
    public double getValue()
    {
        return _value;
    }
    
    public ItemStack createStack()
    {
        return new ItemStack(_id, Material.getMaterial(_id).getMaxStackSize(), _meta < 0 ? 0 : _meta);
    }
}
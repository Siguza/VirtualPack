// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ComparativeItemStack
{
    private int _id;
    private int _meta;
    
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
        _meta = -1;
        if(data.contains(":"))
        {
            String[] split = data.split(":");
            _meta = Util.tryParse(split[1], _meta);
            data = split[0];
        }
        try
        {
            _id = Integer.parseInt(data);
        }
        catch(Throwable t)
        {
            Material m = Material.getMaterial(data.toUpperCase());
            _id = (m == null) ? 0 : m.getId();
        }
    }
    
    public ComparativeItemStack(ItemStack item)
    {
        this(item == null ? 0 : item.getTypeId(), item == null ? -1 : (int)item.getDurability());
    }
    
    public ComparativeItemStack(int id, int meta)
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
        return (item.getTypeId() == _id) && ((_meta == -1) || (!hasSubtypes(_id)) || (_meta == (int)item.getDurability()));
    }
}
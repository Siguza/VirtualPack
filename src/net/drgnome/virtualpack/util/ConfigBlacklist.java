// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.util.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import net.drgnome.virtualpack.item.ComparativeItemStack;

import static net.drgnome.virtualpack.util.Global.*;

public class ConfigBlacklist
{
    private final boolean _whitelist;
    private final ComparativeItemStack[] _list;
    
    public ConfigBlacklist(ConfigurationSection section)
    {
        _whitelist = section.isSet("whitelist") && section.getString("whitelist").equalsIgnoreCase("true");
        ArrayList<ComparativeItemStack> list = new ArrayList<ComparativeItemStack>();
        if(section.isSet("list") && section.isList("list"))
        {
            for(String s : section.getStringList("list").toArray(new String[0]))
            {
                list.add(new ComparativeItemStack(s));
            }
        }
        _list = list.toArray(new ComparativeItemStack[0]);
    }
    
    public boolean isBlacklisted(ItemStack item)
    {
        for(ComparativeItemStack stack : _list)
        {
            if(stack.matches(item))
            {
                return !_whitelist;
            }
        }
        return _whitelist;
    }
}
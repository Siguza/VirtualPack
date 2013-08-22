// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.drgnome.virtualpack.item.ComparativeItemStack;

import static net.drgnome.virtualpack.util.Global.*;

public class Config
{
    public static final int MODE_MIN = 0;
    public static final int MODE_MAX = 1;
    public static final int MODE_INFINITE = 2;
    private static ConfigProxy _proxy;
    private static ArrayList<String> _worlds;
    
    public static synchronized void reload()
    {
        _proxy = new ConfigProxy(_plugin.getConfig(), _plugin.getDataFolder());
        List<World> list = Bukkit.getWorlds();
        _worlds = new ArrayList<String>();
        _worlds.add("*");
        for(World world : list.toArray(new World[0]))
        {
            String name = world.getName();
            if(bool(name, "enabled"))
            {
                _worlds.add(name);
            }
        }
    }
    
    public static String world(String world)
    {
        synchronized(_proxy)
        {
            return _proxy.world(world);
        }
    }
    
    public static synchronized boolean worldEnabled(String world)
    {
        synchronized(_worlds)
        {
            return _worlds.contains(world);
        }
    }
    
    public static String[] worlds()
    {
        synchronized(_worlds)
        {
            return _worlds.toArray(new String[0]);
        }
    }
    
    public static String string(String string)
    {
        return string("*", string);
    }
    
    public static String string(String world, String string)
    {
        synchronized(_proxy)
        {
            return _proxy.get(world, string);
        }
    }
    
    public static boolean bool(String string)
    {
        return bool("*", string);
    }
    
    public static boolean bool(String world, String string)
    {
        return string(world, string).equalsIgnoreCase("true");
    }
    
    public static List<String> list(String string)
    {
        return list("*", string);
    }
    
    public static List<String> list(String world, String string)
    {
        synchronized(_proxy)
        {
            return _proxy.list(world, string);
        }
    }
    
    public static int getInt(String string)
    {
        return getInt("*", string);
    }
    
    public static int getInt(String world, String string)
    {
        String value = string(world, string);
        try
        {
            return Integer.parseInt(value);
        }
        catch(Throwable t)
        {
            try
            {
                return Util.round(Double.parseDouble(value));
            }
            catch(Throwable t2)
            {
                return 0;
            }
        }
    }
    
    public static int getInt(Player player, String prefix, String string, String suffix, int mode)
    {
        return getInt(player.getWorld().getName(), player.getName(), prefix, string, suffix, mode);
    }
    
    public static int getInt(String world, String player, String prefix, String string, String suffix, int mode)
    {
        return getInt(world, Perm.getGroups(world, player), prefix, string, suffix, mode);
    }
    
    public static int getInt(String world, String[] groups, String prefix, String string, String suffix, int mode)
    {
        boolean max, infinite;
        if(mode >= 1)
        {
            max = true;
            infinite = mode >= 2;
        }
        else
        {
            max = infinite = false;
        }
        int value = getInt(world, Util.implode(".", prefix, string, suffix));
        int tmp;
        for(int i = 0; i < groups.length; i++)
        {
            String path1 = Util.implode(".", prefix, groups[i], string, suffix);
            String path2 = Util.implode(".", prefix, string, groups[i], suffix);
            if(isSet(world, path1))
            {
                tmp = getInt(world, path1);
                if(infinite && (tmp == -1))
                {
                    return -1;
                }
                if((tmp > value) == max)
                {
                    value = tmp;
                }
            }
            if(isSet(world, path2))
            {
                tmp = getInt(world, path2);
                if(infinite && (tmp == -1))
                {
                    return -1;
                }
                if((tmp > value) == max)
                {
                    value = tmp;
                }
            }
        }
        return value;
    }
    
    public static double getDouble(String string)
    {
        return getDouble("*", string);
    }
    
    public static double getDouble(String world, String string)
    {
        String value = string(world, string);
        try
        {
            return Double.parseDouble(value);
        }
        catch(Throwable t)
        {
            return 0D;
        }
    }
    
    public static double getDouble(Player player, String prefix, String string, String suffix, int mode, int digits)
    {
        return Util.smooth(getDouble(player, prefix, string, suffix, mode), digits);
    }
    
    public static double getDouble(String world, String player, String prefix, String string, String suffix, int mode, int digits)
    {
        return Util.smooth(getDouble(world, player, prefix, string, suffix, mode), digits);
    }
    
    public static double getDouble(String world, String[] groups, String prefix, String string, String suffix, int mode, int digits)
    {
        return Util.smooth(getDouble(world, groups, prefix, string, suffix, mode), digits);
    }
    
    public static double getDouble(Player player, String prefix, String string, String suffix, int mode)
    {
        return getDouble(player.getWorld().getName(), player.getName(), prefix, string, suffix, mode);
    }
    
    public static double getDouble(String world, String player, String prefix, String string, String suffix, int mode)
    {
        return getDouble(world, Perm.getGroups(world, player), prefix, string, suffix, mode);
    }
    
    public static double getDouble(String world, String[] groups, String prefix, String string, String suffix, int mode)
    {
        boolean max, infinite;
        if(mode >= 1)
        {
            max = true;
            infinite = mode >= 2;
        }
        else
        {
            max = infinite = false;
        }
        double value = getDouble(world, Util.implode(".", prefix, string, suffix));
        if(groups != null)
        {
            double tmp;
            for(int i = 0; i < groups.length; i++)
            {
                String path1 = Util.implode(".", prefix, groups[i], string, suffix);
                String path2 = Util.implode(".", prefix, string, groups[i], suffix);
                if(isSet(world, path1))
                {
                    tmp = getDouble(world, path1);
                    if(infinite && (tmp == -1D))
                    {
                        return -1D;
                    }
                    if((tmp > value) == max)
                    {
                        value = tmp;
                    }
                }
                if(isSet(world, path2))
                {
                    tmp = getDouble(world, path2);
                    if(infinite && (tmp == -1D))
                    {
                        return -1D;
                    }
                    if((tmp > value) == max)
                    {
                        value = tmp;
                    }
                }
            }
        }
        return value;
    }
    
    private static boolean isSet(String world, String string)
    {
        synchronized(_proxy)
        {
            return _proxy.isSet(world, string);
        }
    }
    
    public static boolean isBlacklisted(Player player, String section, ItemStack item)
    {
        return isBlacklisted(player.getWorld().getName(), player.getName(), section, item);
    }
    
    public static boolean isBlacklisted(String world, String player, String section, ItemStack item)
    {
        if(Perm.has(world, player, "vpack.bypass.blacklist." + section))
        {
            return false;
        }
        synchronized(_proxy)
        {
            return _proxy.isBlacklisted(section, item);
        }
    }
    
    public static boolean isBlacklisted(String world, String player, String section, ComparativeItemStack item)
    {
        if(Perm.has(world, player, "vpack.bypass.blacklist." + section))
        {
            return false;
        }
        synchronized(_proxy)
        {
            return _proxy.isBlacklisted(section, item);
        }
    }
    
    public static boolean isGodItem(ItemStack item)
    {
        synchronized(_proxy)
        {
            return _proxy.isGodItem(item);
        }
    }
}
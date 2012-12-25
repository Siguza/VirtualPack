// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static net.drgnome.virtualpack.util.Global.*;

public class Config
{
    private static ConfigProxy _proxy;
    private static ArrayList<String> _worlds;
    
    public static void reload()
    {
        _proxy = new ConfigProxy(_plugin.getConfig(), _plugin.getDataFolder());
        List<World> list = Bukkit.getWorlds();
        _worlds = new ArrayList<String>();
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
        return _proxy.world(world);
    }
    
    public static boolean worldEnabled(String world)
    {
        return _worlds.contains(world);
    }
    
    public static String[] worlds()
    {
        return _worlds.toArray(new String[0]);
    }
    
    public static String string(String string)
    {
        return string("*", string);
    }
    
    public static String string(String world, String string)
    {
        return _proxy.get(world, string);
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
        return _proxy.list(world, string);
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
    
    public static int getInt(Player player, String prefix, String suffix, String string, boolean max)
    {
        return getInt(player.getWorld().getName(), player.getName(), prefix, suffix, string, max);
    }
    
    public static int getInt(String world, String player, String prefix, String suffix, String string, boolean max)
    {
        return getInt(world, Perm.getGroups(world, player), prefix, suffix, string, max);
    }
    
    public static int getInt(String world, String[] groups, String prefix, String suffix, String string, boolean max)
    {
        int value = getInt(world, Util.implode(".", prefix, string, suffix));
        if(groups != null)
        {
            int tmp;
            for(int i = 0; i < groups.length; i++)
            {
                String path1 = Util.implode(".", prefix, groups[i], string, suffix);
                String path2 = Util.implode(".", prefix, string, groups[i], suffix);
                if(isSet(world, path1))
                {
                    tmp = getInt(world, path1);
                    if((tmp > value) == max)
                    {
                        value = tmp;
                    }
                }
                if(isSet(world, path2))
                {
                    tmp = getInt(world, path2);
                    if((tmp > value) == max)
                    {
                        value = tmp;
                    }
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
    
    public static double getDouble(Player player, String prefix, String suffix, String string, boolean max, int digits)
    {
        return Util.smooth(getDouble(player, prefix, suffix, string, max), digits);
    }
    
    public static double getDouble(String world, String player, String prefix, String suffix, String string, boolean max, int digits)
    {
        return Util.smooth(getDouble(world, player, prefix, suffix, string, max), digits);
    }
    
    public static double getDouble(String world, String[] groups, String prefix, String suffix, String string, boolean max, int digits)
    {
        return Util.smooth(getDouble(world, groups, prefix, suffix, string, max), digits);
    }
    
    public static double getDouble(Player player, String prefix, String suffix, String string, boolean max)
    {
        return getDouble(player.getWorld().getName(), player.getName(), prefix, suffix, string, max);
    }
    
    public static double getDouble(String world, String player, String prefix, String suffix, String string, boolean max)
    {
        return getDouble(world, Perm.getGroups(world, player), prefix, suffix, string, max);
    }
    
    public static double getDouble(String world, String[] groups, String prefix, String suffix, String string, boolean max)
    {
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
                    if((tmp > value) == max)
                    {
                        value = tmp;
                    }
                }
                if(isSet(world, path2))
                {
                    tmp = getDouble(world, path2);
                    if((tmp > value) == max)
                    {
                        value = tmp;
                    }
                }
            }
        }
        return value;
    }
    
    private boolean isSet(String world, String string)
    {
        return _proxy.isSet(string);
    }
    
    /*public static String getConfigString(String string)
    {
        return _global.getString(string);
    }
    
    public static int getConfigInt(String prefix, String suffix, CommandSender sender, boolean max)
    {
        String groups[] = getPlayerGroups(sender);
        return getConfigInt(prefix, suffix, groups, max);
    }
    
    public static int getConfigInt(String prefix, String suffix, String groups[], boolean max)
    {
        int value = getConfigInt(prefix + "." + suffix);
        if(groups != null)
        {
            int tmp;
            for(int i = 0; i < groups.length; i++)
            {
                if(_global.isSet(groups[i] + "." + prefix + "." + suffix))
                {
                    tmp = getConfigInt(groups[i] + "." + prefix + "." + suffix);
                    if(max == (tmp > value))
                    {
                        value = tmp;
                    }
                }
                if(_global.isSet(prefix + "." + groups[i] + "." + suffix))
                {
                    tmp = getConfigInt(prefix + "." + groups[i] + "." + suffix);
                    if(max == (tmp > value))
                    {
                        value = tmp;
                    }
                }
            }
        }
        return value;
    }
    
    public static int getConfigInt(String string)
    {
        try
        {
            return Integer.parseInt(_global.getString(string));
        }
        catch(Throwable t)
        {
            try
            {
                return (int)Math.round(Double.parseDouble(_global.getString(string)));
            }
            catch(Throwable t2)
            {
                return 0;
            }
        }
    }
    
    public static double getConfigDouble(String prefix, String suffix, CommandSender sender, boolean max)
    {
        return getConfigDouble(prefix, suffix, sender, max, 0);
    }
    
    public static double getConfigDouble(String prefix, String suffix, CommandSender sender, boolean max, int digits)
    {
        String groups[] = getPlayerGroups(sender);
        return getConfigDouble(prefix, suffix, groups, max, digits, sender.getName());
    }
    
    public static double getConfigDouble(String prefix, String suffix, String groups[], boolean max, String user)
    {
        return getConfigDouble(prefix, suffix, groups, max, 0, user);
    }
    
    public static double getConfigDouble(String prefix, String suffix, String groups[], boolean max, int digits, String user)
    {
        double value = getConfigDouble(prefix + "." + suffix, digits, user);
        if(groups != null)
        {
            double tmp;
            for(int i = 0; i < groups.length; i++)
            {
                if(_global.isSet(groups[i] + "." + prefix + "." + suffix))
                {
                    tmp = getConfigDouble(groups[i] + "." + prefix + "." + suffix, digits, user);
                    if(max == (tmp > value))
                    {
                        value = tmp;
                    }
                }
                if(_global.isSet(prefix + "." + groups[i] + "." + suffix))
                {
                    tmp = getConfigDouble(prefix + "." + groups[i] + "." + suffix, digits, user);
                    if(max == (tmp > value))
                    {
                        value = tmp;
                    }
                }
            }
        }
        return value;
    }
    
    public static double getConfigDouble(String string, int digits, String user)
    {
        try
        {
            String value = _global.getString(string);
            boolean percent = false;
            if(value.substring(value.length() - 1).equals("%"))
            {
                percent = true;
                value = value.substring(0, value.length() - 1);
            }
            double d = Double.parseDouble(smoothDouble(Double.parseDouble(value), digits));
            if(percent)
            {
                if(d < 0.0D)
                {
                    d = 0.0D;
                }
                d *= moneyGet(user) / 100.0D;
            }
            return d;
        }
        catch(Throwable t)
        {
            return 0D;
        }
    }
    
    public static String getConfigItemValue(String string, int id, int damage)
    {
        if(_global.isSet(string + "." + id + "-" + damage))
        {
            return _global.getString(string + "." + id + "-" + damage);
        }
        return _global.getString(string + "." + id);
    }
    
    public static ItemStack getConfigItemStack(String string)
    {
        int id = getConfigInt(string + ".id");
        int amount = getConfigInt(string + ".amount");
        int meta = getConfigInt(string + ".meta");
        if((id < 0) || (id > 32000) || (Item.byId[id] == null))
        {
            return null;
        }
        if(amount <= 0)
        {
            amount = 1;
        }
        else if(amount > 64)
        {
            amount = 64;
        }
        if(meta < 0)
        {
            meta = 0;
        }
        return new ItemStack(id, amount, meta);
    }
    
    public static boolean getConfigIsInList(String key, String search)
    {
        try
        {
            for(Object o : _global.getList(key).toArray())
            {
                if((o instanceof String) && ((String)o).equals(search))
                {
                    return true;
                }
            }
            return false;
        }
        catch(Throwable t)
        {
            return false;
        }
    }*/
}
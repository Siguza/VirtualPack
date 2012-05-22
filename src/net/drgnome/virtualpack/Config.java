// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;
import java.util.*;

import net.minecraft.server.*;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.*;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import static net.drgnome.virtualpack.Lang.*;
import static net.drgnome.virtualpack.Util.*;

// Thought for static import
public class Config
{    
    private static FileConfiguration config;
    
    // Because reloadConfig is already used
    public static void reloadConf(FileConfiguration file)
    {
        config = file;
        setDefs();
    }
    
    // Set all default values
    private static void setDefs()
    {
        setDef("save-interval", "300");
        setDef("economy-disabled", "false");
        setDef("workbench.buy", "20000");
        setDef("workbench.use", "0");
        setDef("uncrafter.buy", "30000");
        setDef("uncrafter.use", "0");
        setDef("chest.max", "10");
        setDef("chest.start", "0");
        setDef("chest.multiply", "1");
        setDef("chest.buy", "40000");
        setDef("chest.use", "0");
        setDef("furnace.max", "10");
        setDef("furnace.start", "0");
        setDef("furnace.multiply", "1");
        setDef("furnace.buy", "50000");
        setDef("furnace.use", "0");
        setDef("furnace.link", "100000");
        setDef("brewingstand.max", "10");
        setDef("brewingstand.start", "0");
        setDef("brewingstand.multiply", "1");
        setDef("brewingstand.buy", "75000");
        setDef("brewingstand.use", "0");
        setDef("brewingstand.link", "100000");
        setDef("enchanttable.multiply", "1");
        setDef("enchanttable.buy", "30000");
        setDef("enchanttable.use", "0");
        setDef("enchanttable.book", "5000");
    }
    
    // Set a default value
    private static void setDef(String path, String value)
    {
        if(!config.isSet(path))
        {
            config.set(path, value);
        }
    }
    
    public static String getConfigString(String string)
    {
        return config.getString(string);
    }
    
    public static int getConfigInt(String prefix, String suffix, CommandSender sender, boolean max)
    {
        String groups[] = getPlayerGroups(sender);
        return getConfigInt(prefix, suffix, groups, max);
    }
    
    public static int getConfigInt(String prefix, String suffix, String groups[], boolean max)
    {
        if(groups == null)
        {
            System.out.println("[VirtualPack] Config : getInt : groups[] == null");
        }
        int value = getConfigInt(prefix + "." + suffix);
        int tmp;
        for(int i = 0; i < groups.length; i++)
        {
            if(!config.isSet(prefix + "." + groups[i] + "." + suffix))
            {
                continue;
            }
            tmp = getConfigInt(prefix + "." + groups[i] + "." + suffix);
            if(((max) && (tmp > value)) || ((!max) && (tmp < value)))
            {
                value = tmp;
            }
        }
        return value;
    }
    
    public static int getConfigInt(String string)
    {
        try
        {
            return Integer.parseInt(config.getString(string));
        }
        catch(Exception e)
        {
            try
            {
                return (int)Math.round(Double.parseDouble(config.getString(string)));
            }
            catch(Exception e2)
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
        return getConfigDouble(prefix, suffix, groups, max, digits);
    }
    
    public static double getConfigDouble(String prefix, String suffix, String groups[], boolean max)
    {
        return getConfigDouble(prefix, suffix, groups, max, 0);
    }
    
    public static double getConfigDouble(String prefix, String suffix, String groups[], boolean max, int digits)
    {
        if(groups == null)
        {
            System.out.println("[VirtualPack] Config : getInt : groups[] == null");
        }
        double value = getConfigDouble(prefix + "." + suffix, digits);
        double tmp;
        for(int i = 0; i < groups.length; i++)
        {
            if(!config.isSet(prefix + "." + groups[i] + "." + suffix))
            {
                continue;
            }
            tmp = getConfigDouble(prefix + "." + groups[i] + "." + suffix, digits);
            if(((max) && (tmp > value)) || ((!max) && (tmp < value)))
            {
                value = tmp;
            }
        }
        return value;
    }
    
    public static double getConfigDouble(String string, int digits)
    {
        try
        {
            return Double.parseDouble(smoothDouble(Double.parseDouble(config.getString(string)), digits));
        }
        catch(Exception e)
        {
            return 0;
        }
    }
    
    public static String getConfigItemValue(String string, int id, int damage)
    {
        if(config.isSet(string + "." + id + "-" + damage))
        {
            return config.getString(string + "." + id + "-" + damage);
        }
        return config.getString(string + "." + id);
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
}
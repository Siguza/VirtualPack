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
        setDef("db.use", "false");
        setDef("db.url", "jdbc:mysql://localhost:3306/minecraft");
        setDef("db.user", "herp");
        setDef("db.pw", "derp");
        setDef("save-interval", "0");
        setDef("on-death", "keep");
        setDef("economy-disabled", "false");
        setDef("workbench.buy", "20000");
        setDef("workbench.use", "0");
        setDef("uncrafter.buy", "30000");
        setDef("uncrafter.use", "0");
        setDef("invguard.buy", "5000");
        setDef("invguard.use", "5%");
        setDef("chest.max", "10");
        setDef("chest.start", "0");
        setDef("chest.multiply", "1");
        setDef("chest.buy", "40000");
        setDef("chest.use", "0");
        setDef("chest.size", "6");
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
        int value = getConfigInt(prefix + "." + suffix);
        if(groups != null)
        {
            int tmp;
            for(int i = 0; i < groups.length; i++)
            {
                if(config.isSet(groups[i] + "." + prefix + "." + suffix))
                {
                    tmp = getConfigInt(groups[i] + "." + prefix + "." + suffix);
                    if(max == (tmp > value))
                    {
                        value = tmp;
                    }
                }
                if(config.isSet(prefix + "." + groups[i] + "." + suffix))
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
            return Integer.parseInt(config.getString(string));
        }
        catch(Throwable t)
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
                if(config.isSet(groups[i] + "." + prefix + "." + suffix))
                {
                    tmp = getConfigDouble(groups[i] + "." + prefix + "." + suffix, digits, user);
                    if(max == (tmp > value))
                    {
                        value = tmp;
                    }
                }
                if(config.isSet(prefix + "." + groups[i] + "." + suffix))
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
            String value = config.getString(string);
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
// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.command.CommandSender;
import net.milkbowl.vault.permission.Permission;

import static net.drgnome.virtualpack.util.Global.*;

public class Perm
{
    private static Permission _perm;
    
    public static boolean init()
    {
        RegisteredServiceProvider perm = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if(perm == null)
        {
            _log.warning(Lang.get("vpack.missperm"));
            return false;
        }
        _perm = (Permission)perm.getProvider();
        return true;
    }
    
    public static boolean has(CommandSender sender, String permission)
    {
        return (sender instanceof Player) ? has(((Player)sender).getWorld().getName(), ((Player)sender).getName(), permission) : true;
    }
    
    public static boolean has(String world, String username, String permission)
    {
        return _perm == null ? false : _perm.has(world, username, permission);
    }
    
    public static String[] getGroups(String world, String username)
    {
        try
        {
            return  _perm.getPlayerGroups(world, username);
        }
        catch(NullPointerException n)
        {
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
        return new String[0];
    }
}
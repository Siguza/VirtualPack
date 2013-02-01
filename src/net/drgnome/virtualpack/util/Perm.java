// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
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
        if(sender instanceof Player)
        {
            Player player = (Player)sender;
            return has(player.getWorld().getName(), player.getName(), permission);
        }
        return true;
    }
    
    public static boolean has(String world, String username, String permission)
    {
        if(_perm == null)
        {
            _log.warning("[VirtualPack] Permission instance is null!");
            return false;
        }
        return (_perm.has(world, username, permission) || _perm.has((String)null, username, permission));
    }
    
    public static String[] getGroups(String world, String username)
    {
        if(_perm == null)
        {
            _log.warning("[VirtualPack] Permission instance is null!");
            return new String[0];
        }
        return Util.<String> merge(_perm.getPlayerGroups(world, username), _perm.getPlayerGroups((String)null, username));
    }
}
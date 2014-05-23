// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.util.UUID;
import java.util.ConcurrentModificationException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            _log.warning(Lang.get(null, "vpack.missperm"));
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
            return has(player.getWorld().getName(), player, permission);
        }
        return true;
    }

    public static boolean has(String world, UUID uuid, String permission)
    {
        return has(world, Bukkit.getOfflinePlayer(uuid), permission);
    }

    public static boolean has(String world, OfflinePlayer op, String permission)
    {
        while(true)
        {
            try
            {
                boolean has = false;
                if(Config.bool("superperms"))
                {
                    Player player;
                    synchronized(Bukkit.getServer())
                    {
                        player = op.getPlayer();
                    }
                    if(player != null)
                    {
                        synchronized(player)
                        {
                            has = player.hasPermission(permission);
                        }
                    }
                }
                if(!has)
                {
                    if(_perm == null)
                    {
                        _log.warning("[VirtualPack] Permission instance is null!");
                    }
                    else
                    {
                        String name = op.getName();
                        if(name == null)
                        {
                            return false;
                        }
                        synchronized(_perm)
                        {
                            has = _perm.has(world, name, permission);
                        }
                        if(!has && Config.bool("global-perms"))
                        {
                            synchronized(_perm)
                            {
                                has = _perm.has((String)null, name, permission);
                            }
                        }
                    }
                }
                return has;
            }
            catch(ConcurrentModificationException e)
            {
                continue;
            }
            catch(UnsupportedOperationException e)
            {
                break;
            }
            catch(NullPointerException e)
            {
                break;
            }
            catch(Exception e)
            {
                warn();
                e.printStackTrace();
                break;
            }
        }
        return false;
    }

    public static String[] getGroups(String world, UUID uuid)
    {
        return getGroups(world, Bukkit.getOfflinePlayer(uuid));
    }

    public static String[] getGroups(String world, OfflinePlayer op)
    {
        while(true)
        {
            try
            {
                if(_perm == null)
                {
                    _log.warning("[VirtualPack] Permission instance is null!");
                    return new String[0];
                }
                String name = op.getName();
                if(name == null)
                {
                    return new String[0];
                }
                String[] groups;
                synchronized(_perm)
                {
                    groups = _perm.getPlayerGroups(world, name);
                }
                if(Config.bool("global-perms"))
                {
                    String[] grp2;
                    synchronized(_perm)
                    {
                        grp2 = _perm.getPlayerGroups((String)null, name);
                    }
                    groups = Util.merge(groups, grp2);
                }
                return groups;
            }
            catch(ConcurrentModificationException e)
            {
                continue;
            }
            catch(UnsupportedOperationException e)
            {
                break;
            }
            catch(NullPointerException e)
            {
                break;
            }
            catch(Exception e)
            {
                warn();
                e.printStackTrace();
                break;
            }
        }
        return new String[0];
    }

    public static boolean inGroup(String world, UUID uuid, String group)
    {
        return inGroup(world, Bukkit.getOfflinePlayer(uuid), group);
    }

    public static boolean inGroup(String world, OfflinePlayer op, String group)
    {
        while(true)
        {
            try
            {
                if(_perm == null)
                {
                    _log.warning("[VirtualPack] Permission instance is null!");
                    return false;
                }
                String name = op.getName();
                if(name == null)
                {
                    return false;
                }
                boolean in;
                synchronized(_perm)
                {
                    in = _perm.playerInGroup(world, name, group);
                }
                if(Config.bool("global-perms") && !in)
                {
                    synchronized(_perm)
                    {
                        in = _perm.playerInGroup((String)null, name, group);
                    }
                }
                return in;
            }
            catch(ConcurrentModificationException e)
            {
                continue;
            }
            catch(UnsupportedOperationException e)
            {
                break;
            }
            catch(NullPointerException e)
            {
                break;
            }
            catch(Exception e)
            {
                warn();
                e.printStackTrace();
                break;
            }
        }
        return false;
    }
}

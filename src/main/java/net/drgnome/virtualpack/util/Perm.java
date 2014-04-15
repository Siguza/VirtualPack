// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.command.CommandSender;
import net.milkbowl.vault.permission.Permission;

import static net.drgnome.virtualpack.util.Global.*;

public class Perm
{
    private static Thread _mainThread;
    private static final Object _internalLock = new Object();
    private static final ArrayList<Object> _waits = new ArrayList<Object>();
    private static int _tCount;
    private static Permission _perm;
    
    public static void tick()
    {
        while(_waits.size() > 0)
        {
            Object[] list;
            synchronized(_waits)
            {
                list = _waits.toArray();
                _waits.clear();
                _tCount = list.length;
            }
            for(Object o : list)
            {
                synchronized(o)
                {
                    o.notifyAll();
                }
            }
            synchronized(_internalLock)
            {
                try
                {
                    _internalLock.wait();
                }
                catch(InterruptedException e)
                {
                }
            }
        }
    }
    
    public static boolean init()
    {
        _mainThread = Thread.currentThread();
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
            return has(player.getWorld().getName(), player.getName(), permission);
        }
        return true;
    }
    
    // Code executed between "lock();" and "unlock();" is guaranteed to be executed in sync with the main thread
    
    private static void lock()
    {
        if(Thread.currentThread().equals(_mainThread))
        {
            return;
        }
        Object o = new Object();
        synchronized(_waits)
        {
            _waits.add(o);
        }
        synchronized(o)
        {
            try
            {
                o.wait();
            }
            catch(InterruptedException e)
            {
            }
        }
    }
    
    private static void unlock()
    {
        if(Thread.currentThread().equals(_mainThread))
        {
            return;
        }
        _tCount--;
        if(_tCount == 0)
        {
            synchronized(_internalLock)
            {
                _internalLock.notifyAll();
            }
        }
    }
    
    public static boolean has(String world, String username, String permission)
    {
        try
        {
            boolean has = false;
            boolean locked = false;
            if(Config.bool("superperms"))
            {
                lock();
                locked = true;
                Player player;
                synchronized(Bukkit.class)
                {
                    player = Bukkit.getPlayerExact(username);
                }
                synchronized(player)
                {
                    if(player != null)
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
                    if(!locked)
                    {
                        lock();
                        locked = true;
                    }
                    synchronized(_perm)
                    {
                        has = _perm.has(world, username, permission);
                    }
                    if(!has && Config.bool("global-perms"))
                    {
                        synchronized(_perm)
                        {
                            has = _perm.has((String)null, username, permission);
                        }
                    }
                }
            }
            if(locked)
            {
                unlock();
            }
            return has;
        }
        catch(UnsupportedOperationException e) {}
        catch(NullPointerException e) {}
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
        return false;
    }
    
    public static String[] getGroups(String world, String username)
    {
        try
        {
            if(_perm == null)
            {
                _log.warning("[VirtualPack] Permission instance is null!");
                return new String[0];
            }
            String[] groups;
            lock();
            synchronized(_perm)
            {
                groups = _perm.getPlayerGroups(world, username);
            }
            if(Config.bool("global-perms"))
            {
                String[] grp2;
                synchronized(_perm)
                {
                    grp2 = _perm.getPlayerGroups((String)null, username);
                }
                groups = Util.merge(groups, grp2);
            }
            unlock();
            return groups;
        }
        catch(UnsupportedOperationException e) {}
        catch(NullPointerException e) {}
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
        return new String[0];
    }
    
    public static boolean inGroup(String world, String username, String group)
    {
        try
        {
            if(_perm == null)
            {
                _log.warning("[VirtualPack] Permission instance is null!");
                return false;
            }
            boolean in;
            lock();
            synchronized(_perm)
            {
                in = _perm.playerInGroup(world, username, group);
            }
            if(Config.bool("global-perms") && !in)
            {
                synchronized(_perm)
                {
                    in = _perm.playerInGroup((String)null, username, group);
                }
            }
            unlock();
            return in;
        }
        catch(UnsupportedOperationException e) {}
        catch(NullPointerException e) {}
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
        return false;
    }
}
// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;

import static net.drgnome.virtualpack.util.Global.*;

public class Money
{
    private static HashMap<String, Money> map;
    private static Economy _eco;
    private final boolean _enabled;
    
    public static boolean init()
    {
        map = new HashMap<String, Money>();
        boolean once = false;
        for(String world : Config.worlds())
        {
            Money money = new Money(world);
            map.put(world, money);
            once = once || money.enabled();
        }
        if(!once)
        {
            return true;
        }
        RegisteredServiceProvider eco = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(eco == null)
        {
            _log.warning(Lang.get("vpack.misseco"));
            return false;
        }
        _eco = (Economy)eco.getProvider();
        return true;
    }
    
    public static Money world(String world)
    {
        Money money = map.get(world);
        return money == null ? new NullMoney() : money;
    }
    
    private Money()
    {
        _enabled = false;
    }
    
    private Money(String world)
    {
        _enabled = Config.bool(world, "economy");
    }
    
    public boolean enabled()
    {
        return _enabled;
    }
    
    public boolean has(String username, double amount)
    {
        if(!_enabled || (amount == 0.0D))
        {
            return true;
        }
        if(_eco == null)
        {
            return false;
        }
        return _eco.has(username, amount);
    }
    
    public void take(String username, double amount)
    {
        if(!_enabled || (amount == 0.0D) || (_eco == null))
        {
            return;
        }
        _eco.withdrawPlayer(username, amount);
    }
    
    public boolean hasTake(String username, double amount)
    {
        if(has(username, amount))
        {
            take(username, amount);
            return true;
        }
        return false;
    }
    
    // Rather link to false than crash with a NullPointerException
    private static class NullMoney extends Money
    {
        private NullMoney()
        {
            super();
        }
        
        public boolean enabled()
        {
            return false;
        }
        
        public boolean has(String username, double amount)
        {
            return false;
        }
        
        public void take(String username, double amount)
        {
        }
        
        public boolean hasTake(String username, double amount)
        {
            return false;
        }
    }
}
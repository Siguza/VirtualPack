// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            _log.warning(Lang.get(null, "vpack.misseco"));
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

    public boolean has(OfflinePlayer op, double amount)
    {
        if(!_enabled || (amount == 0.0D))
        {
            return true;
        }
        if(_eco == null)
        {
            return false;
        }
        return _eco.has(op.getName(), amount);
    }

    public void take(OfflinePlayer op, double amount)
    {
        if(!_enabled || (amount == 0.0D) || (_eco == null))
        {
            return;
        }
        _eco.withdrawPlayer(op.getName(), amount);
    }

    public boolean hasTake(OfflinePlayer op, double amount)
    {
        if(has(op, amount))
        {
            take(op, amount);
            return true;
        }
        return false;
    }

    // Rather link to false than crash with a NullPointerException
    public static class NullMoney extends Money
    {
        private NullMoney()
        {
            super();
        }

        public boolean enabled()
        {
            return false;
        }

        public boolean has(OfflinePlayer op, double amount)
        {
            return false;
        }

        public void take(OfflinePlayer op, double amount)
        {
        }

        public boolean hasTake(OfflinePlayer op, double amount)
        {
            return false;
        }
    }
}

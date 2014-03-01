// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.io.*;
import java.util.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.*;
import net.drgnome.virtualpack.VPlugin;
import net.drgnome.virtualpack.item.ComparativeItemStack;
import static net.drgnome.virtualpack.util.Global.*;

public class ConfigProxy
{
    public static final String _configversion = "1";
    private FileConfiguration _global;
    private HashMap<String, YamlConfiguration> _worlds = new HashMap<String, YamlConfiguration>();
    private HashMap<String, ConfigBlacklist> _blacklists = new HashMap<String, ConfigBlacklist>();
    private ArrayList<ComparativeItemStack> _godItems = new ArrayList<ComparativeItemStack>();
    
    public ConfigProxy(FileConfiguration global, File dir)
    {
        _global = global;
        if(!_global.isSet("version") || !_global.getString("version").equalsIgnoreCase(_configversion))
        {
            try
            {
                _global.save(new File(dir, "config_" + _configversion + ".yml"));
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }
            for(String s : _global.getKeys(false).toArray(new String[0]))
            {
                _global.set(s, null);
            }
        }
        for(File file : dir.listFiles())
        {
            if(!file.isFile())
            {
                continue;
            }
            String string = file.getName();
            if(string.matches("world_(.*)\\.yml"))
            {
                String world = string.substring(6, string.length() - 4);
                YamlConfiguration yaml = new YamlConfiguration();
                try
                {
                    yaml.load(file);
                }
                catch(Throwable t)
                {
                    t.printStackTrace();
                }
                yaml.set("world", world);
                _worlds.put(world, yaml);
            }
        }
        for(String world : _worlds.keySet().toArray(new String[0]))
        {
            YamlConfiguration yaml = _worlds.get(world);
            if(yaml.isSet("copy"))
            {
                if(_worlds.containsKey(yaml.getString("copy")))
                {
                    if(_worlds.get(yaml.getString("copy")) == yaml)
                    {
                        _log.severe("[VirtualPack] World \"" + world + "\" copies configuration of world \"" + yaml.getString("copy") + "\" which results in a loop!");
                        _log.severe("[VirtualPack] Disabling VirtualPack on world \"" + world + "\"!");
                        yaml.set("enabled", "false");
                    }
                    else
                    {
                        _worlds.put(world, _worlds.get(yaml.getString("copy")));
                    }
                }
                else
                {
                    _log.severe("[VirtualPack] World \"" + world + "\" copies configuration of world \"" + yaml.getString("copy") + "\", but there is no config for this world!");
                    _log.severe("[VirtualPack] Disabling VirtualPack on world \"" + world + "\"!");
                    yaml.set("enabled", "false");
                }
            }
        }
        setDefs();
        for(Map.Entry<String, Object> entry : _global.getConfigurationSection("blacklist").getValues(false).entrySet())
        {
            if(entry.getValue() instanceof ConfigurationSection)
            {
                _blacklists.put(entry.getKey().toLowerCase(), new ConfigBlacklist((ConfigurationSection)entry.getValue()));
            }
        }
        for(String s : _global.getStringList("transmutation.god-items"))
        {
            _godItems.add(new ComparativeItemStack(s));
        }
    }
    
    private void setDefs()
    {
        setDef("version", _configversion);
        setDef("enabled", "true");
        setDef("language", "en");
        setDef("load-multithreaded", "false");
        setDef("reload-on-failure", "0");
        setDef("import-world", "");
        setDef("check-update", "true");
        setDef("global-perms", "true");
        setDef("superperms", "false");
        setDef("save-interval", "300");
        setDef("tick.interval", "10");
        setDef("on-death", "keep");
        setDef("economy", "true");
        setDef("allow-creative", "false");
        setDef("uncraft-enchanted", "true");
        setDef("events.use", "true");
        setDef("events.ignorecancelled", "false");
        setDef("inject.anvil", "false");
        setDef("transmutation.enabled", "false");
        setDef("transmutation.show-value", "true");
        setDef("transmutation.god-items", Util.createList("DRAGON_EGG"));
        setDef("transmutation.notify-mismatch", "true");
        setDef("transmutation.color.name", "1n");
        setDef("transmutation.color.value", "4");
        setDef("transmutation.color.stored-name", "2l");
        setDef("transmutation.color.stored-value", "e");
        setDef("db.use", "false");
        setDef("db.url", "jdbc:mysql://localhost:3306/minecraft");
        setDef("db.user", "herp");
        setDef("db.pw", "derp");
        setDef("db.table", "vpack");
        setDef("commands." + VPlugin._components[0], Util.createList("v", "virtual", "virtualpack"));
        setDef("commands." + VPlugin._components[1], Util.createList("wb", "workbench"));
        setDef("commands." + VPlugin._components[2], Util.createList("uc", "uncrafter"));
        setDef("commands." + VPlugin._components[3], Util.createList("chest"));
        setDef("commands." + VPlugin._components[4], Util.createList("furnace"));
        setDef("commands." + VPlugin._components[5], Util.createList("brew", "brewingstand"));
        setDef("commands." + VPlugin._components[6], Util.createList("ench", "enchtable", "enchanttable", "enchantingtable"));
        setDef("commands." + VPlugin._components[7], Util.createList("trash"));
        setDef("commands." + VPlugin._components[8], Util.createList("send"));
        setDef("commands." + VPlugin._components[9], Util.createList("an", "anvil"));
        setDef("commands." + VPlugin._components[10], Util.createList("mat", "matter"));
        setDef("commands." + VPlugin._components[11], Util.createList("ec", "enderchest"));
        setDef("tools.workbench.buy", "20000");
        setDef("tools.workbench.use", "0");
        setDef("tools.workbench.cooldown", "0");
        setDef("tools.uncrafter.buy", "30000");
        setDef("tools.uncrafter.use", "0");
        setDef("tools.uncrafter.cooldown", "0");
        setDef("tools.enderchest.buy", "30000");
        setDef("tools.enderchest.use", "0");
        setDef("tools.enderchest.cooldown", "0");
        setDef("tools.enchanttable.multiply", "1");
        setDef("tools.enchanttable.buy", "30000");
        setDef("tools.enchanttable.use", "0");
        setDef("tools.enchanttable.book", "5000");
        setDef("tools.enchanttable.cooldown", "0");
        setDef("tools.anvil.buy", "25000");
        setDef("tools.anvil.use", "0");
        setDef("tools.anvil.maxlevel", "40");
        setDef("tools.anvil.cooldown", "0");
        setDef("tools.materializer.buy", "50000");
        setDef("tools.materializer.use", "0");
        setDef("tools.materializer.cooldown", "0");
        setDef("tools.chest.max", "10");
        setDef("tools.chest.start", "0");
        setDef("tools.chest.multiply", "1");
        setDef("tools.chest.buy", "40000");
        setDef("tools.chest.use", "0");
        setDef("tools.chest.size", "6");
        setDef("tools.chest.cooldown", "0");
        setDef("tools.furnace.max", "10");
        setDef("tools.furnace.start", "0");
        setDef("tools.furnace.multiply", "1");
        setDef("tools.furnace.buy", "50000");
        setDef("tools.furnace.use", "0");
        setDef("tools.furnace.link", "100000");
        setDef("tools.furnace.cooldown", "0");
        setDef("tools.brewingstand.max", "10");
        setDef("tools.brewingstand.start", "0");
        setDef("tools.brewingstand.multiply", "1");
        setDef("tools.brewingstand.buy", "75000");
        setDef("tools.brewingstand.use", "0");
        setDef("tools.brewingstand.link", "100000");
        setDef("tools.brewingstand.cooldown", "0");
        setDef("send.notify-interval", "0");
        setDef("send.drop", "false");
        setDef("blacklist.uncrafter.whitelist", "false");
        setDef("blacklist.uncrafter.list", new ArrayList<String>());
        setDef("blacklist.store.whitelist", "false");
        setDef("blacklist.store.list", new ArrayList<String>());
        setDef("blacklist.materializer.whitelist", "false");
        setDef("blacklist.materializer.list", new ArrayList<String>());
    }
    
    private void setDef(String path, Object value)
    {
        if(!_global.isSet(path))
        {
            _global.set(path, value);
        }
    }
    
    public String get(String world, String string)
    {
        if(!world.equals("*") && _worlds.containsKey(world))
        {
            YamlConfiguration yaml = _worlds.get(world);
            if(yaml.isSet(string))
            {
                return yaml.getString(string);
            }
        }
        if(_global.isSet(string))
        {
            return _global.getString(string);
        }
        return "";
    }
    
    public List<String> list(String world, String string)
    {
        if(!world.equals("*") && _worlds.containsKey(world))
        {
            YamlConfiguration yaml = _worlds.get(world);
            if(yaml.isSet(string))
            {
                return yaml.getStringList(string);
            }
        }
        if(_global.isSet(string))
        {
            return _global.getStringList(string);
        }
        return null;
    }
    
    public String world(String world)
    {
        return _worlds.containsKey(world) ? _worlds.get(world).getString("world") : "*";
    }
    
    public boolean isSet(String world, String string)
    {
        return (!world.equals("*") && _worlds.containsKey(world)) ? _worlds.get(world).isSet(string) : _global.isSet(string);
    }
    
    public boolean isBlacklisted(String section, ItemStack item)
    {
        ConfigBlacklist blacklist = _blacklists.get(section.toLowerCase());
        if(blacklist == null)
        {
            return false;
        }
        return blacklist.isBlacklisted(item);
    }
    
    public boolean isBlacklisted(String section, ComparativeItemStack item)
    {
        ConfigBlacklist blacklist = _blacklists.get(section.toLowerCase());
        if(blacklist == null)
        {
            return false;
        }
        return blacklist.isBlacklisted(item);
    }
    
    public boolean isGodItem(ItemStack item)
    {
        if(item == null)
        {
            return false;
        }
        for(ComparativeItemStack stack : _godItems)
        {
            if(stack.matches(item))
            {
                return true;
            }
        }
        return false;
    }
}
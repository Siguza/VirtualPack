// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.io.*;
import java.util.*;
import org.bukkit.configuration.file.*;
import net.drgnome.virtualpack.VPlugin;

import static net.drgnome.virtualpack.util.Global.*;

public class ConfigProxy
{
    public static final String _configversion = "1";
    private FileConfiguration _global;
    private HashMap<String, YamlConfiguration> _worlds;
    
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
        _worlds = new HashMap<String, YamlConfiguration>();
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
                        _log.severe(Lang.get("config.fail2", world, yaml.getString("copy")));
                        _log.severe(Lang.get("config.fail3", world));
                        yaml.set("enabled", "false");
                    }
                    else
                    {
                        _worlds.put(world, _worlds.get(yaml.getString("copy")));
                    }
                }
                else
                {
                    _log.severe(Lang.get("config.fail1", world, yaml.getString("copy")));
                    _log.severe(Lang.get("config.fail3", world));
                    yaml.set("enabled", "false");
                }
            }
        }
        setDefs();
    }
    
    private void setDefs()
    {
        setDef("version", _configversion);
        setDef("enabled", "true");
        setDef("import-world", "");
        setDef("check-update", "true");
        setDef("save-interval", "0");
        setDef("on-death", "keep");
        setDef("economy", "true");
        setDef("allow-creative", "false");
        setDef("events.use", "true");
        setDef("events.ignorecancelled", "false");
        setDef("db.use", "false");
        setDef("db.url", "jdbc:mysql://localhost:3306/minecraft");
        setDef("db.user", "herp");
        setDef("db.pw", "derp");
        setDef("db.table", "vpack");
        ArrayList<String> list0 = new ArrayList<String>();
        list0.add("v");
        list0.add("virtual");
        list0.add("virtualpack");
        setDef("commands." + VPlugin._components[0], list0);
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("wb");
        list1.add("workbench");
        setDef("commands." + VPlugin._components[1], list1);
        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("uc");
        list2.add("uncrafter");
        setDef("commands." + VPlugin._components[2], list2);
        ArrayList<String> list3 = new ArrayList<String>();
        list3.add("chest");
        setDef("commands." + VPlugin._components[3], list3);
        ArrayList<String> list4 = new ArrayList<String>();
        list4.add("furnace");
        setDef("commands." + VPlugin._components[4], list4);
        ArrayList<String> list5 = new ArrayList<String>();
        list5.add("brew");
        list5.add("brewingstand");
        setDef("commands." + VPlugin._components[5], list5);
        ArrayList<String> list6 = new ArrayList<String>();
        list6.add("ench");
        list6.add("enchtable");
        list6.add("enchanttable");
        list6.add("enchantingtable");
        setDef("commands." + VPlugin._components[6], list6);
        ArrayList<String> list7 = new ArrayList<String>();
        list7.add("trash");
        setDef("commands." + VPlugin._components[7], list7);
        ArrayList<String> list8 = new ArrayList<String>();
        list8.add("send");
        setDef("commands." + VPlugin._components[8], list8);
        ArrayList<String> list9 = new ArrayList<String>();
        list9.add("an");
        list9.add("anvil");
        setDef("commands." + VPlugin._components[9], list9);
        setDef("tools.workbench.buy", "20000");
        setDef("tools.workbench.use", "0");
        setDef("tools.uncrafter.buy", "30000");
        setDef("tools.uncrafter.use", "0");
        setDef("tools.enchanttable.multiply", "1");
        setDef("tools.enchanttable.buy", "30000");
        setDef("tools.enchanttable.use", "0");
        setDef("tools.enchanttable.book", "5000");
        setDef("tools.anvil.buy", "25000");
        setDef("tools.anvil.use", "0");
        setDef("tools.chest.max", "10");
        setDef("tools.chest.start", "0");
        setDef("tools.chest.multiply", "1");
        setDef("tools.chest.buy", "40000");
        setDef("tools.chest.use", "0");
        setDef("tools.chest.size", "6");
        setDef("tools.furnace.max", "10");
        setDef("tools.furnace.start", "0");
        setDef("tools.furnace.multiply", "1");
        setDef("tools.furnace.buy", "50000");
        setDef("tools.furnace.use", "0");
        setDef("tools.furnace.link", "100000");
        setDef("tools.brewingstand.max", "10");
        setDef("tools.brewingstand.start", "0");
        setDef("tools.brewingstand.multiply", "1");
        setDef("tools.brewingstand.buy", "75000");
        setDef("tools.brewingstand.use", "0");
        setDef("tools.brewingstand.link", "100000");
        setDef("send.notify-interval", "0");
        /*setDef("blacklist.use-as-whitelist", "false");
        setDef("blacklist.uncrafter", new ArrayList<String>());
        setDef("blacklist.store", new ArrayList<String>());*/
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
}
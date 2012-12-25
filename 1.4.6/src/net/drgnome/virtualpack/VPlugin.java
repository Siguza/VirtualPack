// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.sql.*;
import java.sql.Connection; // Java compiler needs it
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.bukkit.util.*;
import net.drgnome.virtualpack.util.*;

import static net.drgnome.virtualpack.util.Global.*;

public class VPlugin extends JavaPlugin
{
    public static final String _version = "1.2";
    public static final String[] _components = {"main", "workbench", "uncrafter", "chest", "furnace", "brewingstand", "enchanttable", "trash"};
    
    private HashMap<String, HashMap<String, VPack>> _packs;
    private VThreadSave _saveThread;
    private int _saveTick;
    private int _upTick;
    private boolean _update;
    private boolean _saveRequested;
    private boolean _loadRequested;
    private boolean _waitForGroupManager;
    private boolean _portMysql;
    private boolean _loadSuccess;
    private CommandRegistration _reg;
    
    public VPlugin()
    {
        super();
        _plugin = this;
    }
    
    public Connection mysql()
    {
        try
        {
            return DriverManager.getConnection(Config.string("db.url"), Config.string("db.user"), Config.string("db.pw"));
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            return null;
        }
    }
    
    private void init()
    {
        _saveTick = 0;
        _upTick = 60 * 60 * 20;
        _update = false;
        _saveRequested = false;
        _loadRequested = false;
        _waitForGroupManager = false;
        _portMysql = false;
        _loadSuccess = false;
        _packs = new HashMap<String, HashMap<String, VPack>>();
        checkFiles();
        Lang.init();
        Config.reload();
        saveConfig();
        if(Config.bool("db.use"))
        {
            try
            {
                String table = Config.string("db.table");
                Connection db = mysql();
                db.prepareStatement("CREATE TABLE IF NOT EXISTS `" + table + "` (`world` varchar(255) NOT NULL, `user` varchar(255) NOT NULL, `data` longtext NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;").execute();
                ResultSet row = db.prepareStatement("SELECT * FROM `vpack`").executeQuery();
                _portMysql = !row.next();
                db.close();
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }
        }
        if(!Perm.init() || !Money.init())
        {
            getPluginLoader().disablePlugin(this);
            return;
        }
        loadUserData();
        getServer().getPluginManager().registerEvents(new VEvents(), this);
        if(!registerCommands())
        {
            getPluginLoader().disablePlugin(this);
        }
        _log.info(Lang.get("vpack.enable", _version));
    }
    
    private boolean registerCommands()
    {
        try
        {
            Class clazz = Class.forName("com.sk89q.bukkit.util.CommandRegistration");
        }
        catch(Throwable t)
        {
            boolean found = false;
            for(File file : getDataFolder().listFiles())
            {
                if(!file.isFile())
                {
                    continue;
                }
                String name = file.getName().toLowerCase();
                if(name.contains("worldedit") && name.substring(name.length() - 4).equals(".jar"))
                {
                    found = true;
                    ClassLoader loader = this.getClass().getClassLoader();
                    if(loader instanceof URLClassLoader)
                    {
                        try
                        {
                            URLClassLoader cl = (URLClassLoader)loader;
                            Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                            m.setAccessible(true);
                            m.invoke(cl, file.toURI().toURL());
                        }
                        catch(Throwable t1)
                        {
                            warn();
                            t1.printStackTrace();
                            return false;
                        }
                    }
                    else
                    {
                        warn();
                        _log.severe("[VirtualPack] PluginClassLoader not URLClassLoader!");
                        return false;
                    }
                }
            }
            if(!found)
            {
                _log.severe(Lang.get("worldedit"));
                return false;
            }
        }
        ArrayList<CommandInfo> list = new ArrayList<CommandInfo>();
        for(String component : _components)
        {
            String[] commands = Config.list("commands." + component).toArray(new String[0]);
            if(commands.length <= 0)
            {
                continue;
            }
            list.add(new CommandInfo("/" + commands[0], "VirtualPack", commands, this, new String[]{"vpack.use"}));
        }
        try
        {
            _reg = new CommandRegistration(this, new VCommands());
            return _reg.register(list);
        }
        catch(Throwable t)
        {
            _log.severe(Lang.get("worldedit"));
            t.printStackTrace();
        }
        return false;
    }
    
    public void onEnable()
    {
        super.onEnable();
        _waitForGroupManager = false;
        try
        {
            if(((Boolean)(Class.forName("org.anjocaido.groupmanager.GroupManager").getMethod("isLoaded").invoke(null))).booleanValue())
            {
                init();
            }
            else
            {
                _waitForGroupManager = true;
            }
        }
        catch(ClassNotFoundException e)
        {
            init();
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThread(this), 0L, 1L);
    }
    
    public void onDisable()
    {
        super.onDisable();
        getServer().getScheduler().cancelTasks(this);
        if(!_waitForGroupManager)
        {
            saveUserData();
            try 
            {
                _saveThread.join();
            } 
            catch(InterruptedException e) 
            {
                _log.log(Level.WARNING, "[VirtualPack] Save interrupted: {0}", e.getMessage());
            }
            catch(Throwable t)
            {
            }
            try
            {
                _reg.unregisterCommands();
            }
            catch(Throwable t)
            {
            }
            _log.info(Lang.get("vpack.disable", _version));
        }
    }
    
    public VPack getPack(Player player)
    {
        return getPack(player.getWorld().getName(), player.getName());
    }
    
    public VPack getPack(String world, String player)
    {
        if(!Config.bool(world, "enabled"))
        {
            return null;
        }
        world = Config.world(world);
        HashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            map = new HashMap<String, VPack>();
            _packs.put(world, map);
        }
        player = player.toLowerCase();
        VPack pack = map.get(player);
        if(pack == null)
        {
            pack = new VPack(world, player);
            map.put(player, pack);
        }
        return pack;
    }
    
    public void setPack(Player player, VPack pack)
    {
        setPack(player.getWorld().getName(), player.getName(), pack);
    }
    
    public void setPack(String world, String player, VPack pack)
    {
        if(!Config.bool(world, "enabled"))
        {
            return;
        }
        world = Config.world(world);
        HashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            map = new HashMap<String, VPack>();
            _packs.put(world, map);
        }
        map.put(player.toLowerCase(), pack);
    }
    
    protected synchronized void loadUserData()
    {
        // TODO
    }
    
    protected synchronized void saveUserData()
    {
        // TODO
    }
    
    public void handleDeath(String player)
    {
        
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        Config.reload();
        saveConfig();
        Lang.reload();
    }
    
    private void checkFiles()
    {
        try
        {
            File file = getDataFolder();
            if(!file.exists())
            {
                file.mkdirs();
            }
            String files[] = {"config.yml", "data.db"};
            for(int i = 0; i < files.length; i++)
            {
                File data = new File(file, files[i]);
                if(!data.exists())
                {
                    PrintStream writer = new PrintStream(new FileOutputStream(data));
                    writer.close();
                }
            }
        }
        catch(Throwable t)
        {
        }
    }
    
    public synchronized void tick()
    {
        if(_waitForGroupManager)
        {
            try
            {
                if(((Boolean)(Class.forName("org.anjocaido.groupmanager.GroupManager").getMethod("isLoaded").invoke(null))).booleanValue())
                {
                    init();
                }
                else
                {
                    return;
                }
            }
            catch(ClassNotFoundException e)
            {
                init();
            }
            catch(Throwable t)
            {
                warn();
                t.printStackTrace();
            }
        }
        Iterator<HashMap<String, VPack>> iterator = _packs.values().iterator();
        while(iterator.hasNext())
        {
            HashMap<String, VPack> map = iterator.next();
            if(map == null)
            {
                continue;
            }
            for(VPack pack : map.values().toArray(new VPack[0]))
            {
                if(pack == null)
                {
                    continue;
                }
                pack.tick();
            }
        }
        if(!_update && Config.bool("check-update"))
        {
            _upTick++;
            if(_upTick >= 60 * 60 * 20)
            {
                checkUpdate();
            }
        }
        if(_saveRequested)
        {
            saveUserData();
        }
        if(_loadRequested)
        {
            loadUserData();
        }
        if(Config.getInt("save-interval") > 0)
        {
            _saveTick++;
            if(_saveTick >= Config.getInt("save-interval") * 20)
            {
                _log.info("[VirtualPack] Saving user data...");
                saveUserData();
                _saveTick = 0;
            }
        }
    }
    
    public boolean checkUpdate()
    {
        _update = Util.hasUpdate("vpack", _version);
        _upTick = 0;
        return _update;
    }
    
    public boolean hasUpdate()
    {
        return _update;
    }
}
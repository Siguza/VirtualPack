// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.server.v1_5_R2.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;

import com.sk89q.bukkit.util.CommandInfo;
import com.sk89q.bukkit.util.CommandRegistration;

import net.drgnome.virtualpack.components.VGUI;
import net.drgnome.virtualpack.data.AlphaChestHelper;
import net.drgnome.virtualpack.data.BackpackHelper;
import net.drgnome.virtualpack.data.TransmutationHelper;
import net.drgnome.virtualpack.data.TransmutationListener;
import net.drgnome.virtualpack.data.VirtualChestHelper;
import net.drgnome.virtualpack.thread.VThreadInit;
import net.drgnome.virtualpack.thread.VThreadLoad;
import net.drgnome.virtualpack.thread.VThreadSave;
import net.drgnome.virtualpack.util.Config;
import net.drgnome.virtualpack.util.Lang;
import net.drgnome.virtualpack.util.Money;
import net.drgnome.virtualpack.util.ObfuscationHelper;
import net.drgnome.virtualpack.util.Perm;
import net.drgnome.virtualpack.util.Util;
import static net.drgnome.virtualpack.util.Global._separator;
import static net.drgnome.virtualpack.util.Global._plugin;
import static net.drgnome.virtualpack.util.Global._log;
import static net.drgnome.virtualpack.util.Global.warn;
import static net.drgnome.virtualpack.util.Global.sendMessage;

public class VPlugin extends JavaPlugin implements Runnable
{
    public static String _version;
    public static final String[] _components = {"main", "workbench", "uncrafter", "chest", "furnace", "brewingstand", "enchanttable", "trash", "send", "anvil", "materializer"};
    
    private HashMap<String, HashMap<String, VPack>> _packs = new HashMap<String, HashMap<String, VPack>>();
    private HashMap<Player, ArrayList<String>> _annoyPlayers = new HashMap<Player, ArrayList<String>>();
    public ArrayList<VThreadLoad> _loadThreads = new ArrayList<VThreadLoad>();
    private int _numLoadThreads = 0;
    private int _saveTick = 0;
    private int _annoyTick = 0;
    private int _upTick = 72000;
    private int _loadTick = 0;
    private boolean _update = false;
    private boolean _saveRequested = false;
    private boolean _loadRequested = false;
    private boolean _waitForGroupManager = false;
    private boolean _portMysql = false;
    private boolean _loadSuccess = false;
    private VThreadInit _initThread;
    private VThreadSave _saveThread;
    private CommandRegistration _reg;
    private boolean _starting = true;
    
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
        _waitForGroupManager = false;
        checkFiles();
        Config.reload();
        Lang.init();
        saveConfig();
        if(Config.bool("db.use"))
        {
            try
            {
                String table = Config.string("db.table");
                Connection db = mysql();
                db.prepareStatement("CREATE TABLE IF NOT EXISTS `" + table + "` (`world` varchar(255) NOT NULL, `user` varchar(255) NOT NULL, `data` longtext NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;").execute();
                ResultSet row = db.prepareStatement("SELECT * FROM `" + table + "`").executeQuery();
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
        getServer().getPluginManager().registerEvents(new VEvents(), this);
        if(!registerCommands())
        {
            getPluginLoader().disablePlugin(this);
        }
        if(Config.bool("transmutation.enabled"))
        {
            checkMatterFile();
            TransmutationHelper.init();
            if(Config.bool("transmutation.show-value"))
            {
                try
                {
                    Class.forName("com.comphenix.protocol.ProtocolLibrary"); // Need to check
                    TransmutationListener.register();
                }
                catch(ClassNotFoundException e)
                {
                    _log.severe(Lang.get("protocollib"));
                }
            }
        }
        loadUserData();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, (long)Util.getTick());
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
                    if(!Util.loadJar(file))
                    {
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
        String main = "/" + Config.list("commands.main").get(0);
        ArrayList<CommandInfo> list = new ArrayList<CommandInfo>();
        for(String component : _components)
        {
            try
            {
                String[] commands = Config.list("commands." + component).toArray(new String[0]);
                if(commands.length <= 0)
                {
                    continue;
                }
                for(String c : commands)
                {
                    list.add(new CommandInfo(main, Lang.get("cmd." + component), new String[]{c}, this, new String[]{"vpack.use"}));
                }
            }
            catch(NullPointerException e)
            {
                _log.info("[VirtualPack] Could not add commands for " + component);
            }
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
    	VPlugin._version = this.getDescription().getVersion();
    	/* Needed for reflection */
    	InputStream mappingFile = getResource("mappings.srg");
    	if(mappingFile == null)
    	{
    		getLogger().log(Level.WARNING, "No mapping file was found! Disabling!");
    		Bukkit.getPluginManager().disablePlugin(this);
    		return;
    	}
    	ObfuscationHelper.registerMappings(Util.convertStreamToString(mappingFile));
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
                getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, 1L);
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
    
    public void onDisable()
    {
        super.onDisable();
        getServer().getScheduler().cancelTasks(this);
        if(Config.bool("transmutation.enabled") && Config.bool("transmutation.show-value"))
        {
            try
            {
                Class.forName("com.comphenix.protocol.ProtocolLibrary");
                TransmutationListener.unregister();
            }
            catch(ClassNotFoundException e)
            {
            }
        }
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
    
    public boolean hasPack(Player player)
    {
        return hasPack(player.getWorld().getName(), player.getName());
    }
    
    public boolean hasPack(String world, String player)
    {
        world = Config.world(world);
        HashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            return false;
        }
        return map.containsKey(player.toLowerCase());
    }
    
    public VPack[] getAllPacks()
    {
        ArrayList<VPack> list = new ArrayList<VPack>();
        Iterator<HashMap<String, VPack>> iterator = _packs.values().iterator();
        while(iterator.hasNext())
        {
            HashMap<String, VPack> map = iterator.next();
            if(map == null)
            {
                continue;
            }
            list.addAll(map.values());
        }
        return list.toArray(new VPack[0]);
    }
    
    public VPack[] getPacks(String world)
    {
        if(!Config.bool(world, "enabled"))
        {
            return null;
        }
        world = Config.world(world);
        return _packs.get(world).values().toArray(new VPack[0]);
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
    
    public void saveUserData()
    {
        saveUserData(false);
    }
    
    public void saveUserData(boolean forcefile)
    {
        saveUserData(forcefile, "data.db");
    }
    
    public void saveUserData(boolean forcefile, String filename)
    {
        if(!_loadSuccess)
        {
            _log.warning("[VirtualPack] CANNOT SAVE USER DATA, LOADING ALREADY FAILED!");
            return;
        }
        if(!canReload())
        {
            _saveRequested = true;
            return;
        }
        if(Config.bool("db.use") && !forcefile)
        {
            try
            {
                _saveThread = new VThreadSave(_packs);
            }
            catch(Throwable t)
            {
                warn();
                t.printStackTrace();
                return;
            }
        }
        else
        {
            _saveThread = new VThreadSave(new File(getDataFolder(), filename), _packs);
        }
        _saveThread.start();
        _saveRequested = false;
    }
    
    public void loadUserData()
    {
        if(Config.bool("load-multithreaded") || Config.string("load-multithreaded").equalsIgnoreCase("semi"))
        {
            if(isActuallyReloading())
            {
                return;
            }
            _numLoadThreads = 1;
            _initThread = new VThreadInit();
            _initThread.start();
        }
        else
        {
            loadUserData0();
        }
    }
    
    public synchronized void loadUserData0()
    {
        _loadRequested = true;
        if((_saveThread != null) && !_saveThread.done())
        {
            return;
        }
        for(Player bukkitPlayer : Bukkit.getOnlinePlayers())
        {
            if(bukkitPlayer instanceof CraftPlayer)
            {
                EntityPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
                if((player.activeContainer != null) && (player.activeContainer instanceof VGUI))
                {
                    player.closeInventory();
                }
            }
        }
        try
        {
            if(_portMysql)
            {
                loadFlatfile();
                saveUserData();
                _portMysql = false;
            }
            else if(Config.bool("db.use"))
            {
                loadMysql();
            }
            else
            {
                loadFlatfile();
            }
            AlphaChestHelper.check();
            BackpackHelper.check();
            VirtualChestHelper.check();
        }
        catch(Throwable t)
        {
            _log.severe("[VirtualPack] COULD NOT LOAD USER DATA!");
            t.printStackTrace();
            _loadSuccess = false;
        }
        _loadRequested = false;
    }
    
    private void loadMysql() throws Throwable
    {
        Connection db = DriverManager.getConnection(Config.string("db.url"), Config.string("db.user"), Config.string("db.pw"));
        int version = 0;
        String world = Config.string("import-world").length() > 0 ? Config.string("import-world") : "*";
        ArrayList<String[]> list = new ArrayList<String[]>();
        ResultSet row = db.prepareStatement("SELECT * FROM `" + Config.string("db.table") + "`").executeQuery();
        while(row.next())
        {
            if(version == 0)
            {
                try
                {
                    row.getString("world");
                    version = 2;
                }
                catch(SQLException e)
                {
                    version = 1;
                }
            }
            switch(version)
            {
                case 1:
                    String data = row.getString("data");
                    if(!data.contains(_separator[0]))
                    {
                        continue;
                    }
                    list.add(new String[]{world, data.substring(0, data.indexOf(_separator[0])), data.substring(data.indexOf(_separator[0]) + 1)});
                    break;
                case 2:
                    list.add(new String[]{row.getString("world"), row.getString("user"), row.getString("data")});
                    break;
            }
        }
        db.close();
        load(list);
    }
    
    private void loadFlatfile() throws Throwable
    {
        BufferedReader file = new BufferedReader(new FileReader(new File(getDataFolder(), "data.db")));
        int version = 0;
        String world = Config.string("import-world").length() > 0 ? Config.string("import-world") : "*";
        String line;
        ArrayList<String[]> list = new ArrayList<String[]>();
        while((line = file.readLine()) != null)
        {
            if(version == 0)
            {
                version = line.contains(_separator[4]) ? 2 : 1;
            }
            switch(version)
            {
                case 1:
                    if(!line.contains(_separator[0]))
                    {
                        continue;
                    }
                    list.add(new String[]{world, line.substring(0, line.indexOf(_separator[0])), line.substring(line.indexOf(_separator[0]) + 1)});
                    break;
                case 2:
                    list.add(line.split(_separator[4]));
                    break;
            }
        }
        file.close();
        load(list);
    }
    
    private void load(List<String[]> list)
    {
        if(Config.bool("load-multithreaded"))
        {
            _numLoadThreads = 0;
            for(String[] data : list)
            {
                _numLoadThreads++;
                VThreadLoad loadThread = new VThreadLoad(data);
                _loadThreads.add(loadThread);
                loadThread.start();
            }
        }
        else
        {
            for(String[] data : list)
            {
                setPack(data[0], data[1], new VPack(data[0], data[1], data[2].split(_separator[0])));
            }
        }
        _loadSuccess = true;
    }
    
    public void forceMysqlPort()
    {
        _portMysql = true;
    }
    
    public void handleDeath(Player player)
    {
        String death = Config.string("on-death").toLowerCase();
        if(death.equals("harddrop") || death.equals("drop"))
        {
            getPack(player).drop(player);
        }
        if(death.equals("wipe") || death.equals("drop"))
        {
            getPack(player).wipe();
        }
        if(death.equals("hardwipe") || death.equals("harddrop"))
        {
            getPack(player).reset();
        }
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        Config.reload();
        saveConfig();
        if(_starting)
        {
            _starting = false;
        }
        else
        {
            Lang.reload();
            if(Config.bool("transmutation.enabled"))
            {
                TransmutationHelper.init();
            }
        }
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
                    data.createNewFile();
                }
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    private void checkMatterFile()
    {
        try
        {
            String tName = "transmutation.ini";
            File transmutation = new File(getDataFolder(), tName);
            if(!transmutation.exists())
            {
                transmutation.createNewFile();
                DataInputStream in = new DataInputStream(getResource(tName));
                FileOutputStream out = new FileOutputStream(transmutation);
                int buf;
                while((buf = in.available()) > 0)
                {
                    byte[] array = new byte[buf];
                    in.readFully(array);
                    out.write(array);
                }
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    public void run()
    {
        if(_waitForGroupManager)
        {
            try
            {
                if(((Boolean)(Class.forName("org.anjocaido.groupmanager.GroupManager").getMethod("isLoaded").invoke(null))).booleanValue())
                {
                    getServer().getScheduler().cancelTasks(this);
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
        if(_loadRequested)
        {
            loadUserData();
        }
        if(isReloading())
        {
            return;
        }
        int ticks = Util.getTick();
        if(!_loadSuccess)
        {
            int reloadInterval = Config.getInt("reload-on-failure");
            if(reloadInterval > 0)
            {
                _loadTick += ticks;
                if(_loadTick > reloadInterval * 20)
                {
                    _loadTick = 0;
                    loadUserData();
                }
            }
        }
        if(_saveRequested)
        {
            saveUserData();
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
                pack.tick(ticks);
            }
        }
        if(Config.getInt("send.notify-interval") > 0)
        {
            _annoyTick -= ticks;
            if(_annoyTick <= 0)
            {
                _annoyTick = Config.getInt("send.notify-interval") * 20;
                for(Map.Entry<Player, ArrayList<String>> entry : _annoyPlayers.entrySet())
                {
                    for(String msg : entry.getValue())
                    {
                        sendMessage(entry.getKey(), msg, ChatColor.GREEN);
                    }
                    List<String> cmds = Config.list("commands." + VPlugin._components[0]);
                    if(cmds.size() > 0)
                    {
                        sendMessage(entry.getKey(), Lang.get("send.relieve", cmds.get(0)), ChatColor.RED);
                    }
                }
            }
        }
        if(!_update && Config.bool("check-update"))
        {
            _upTick += ticks;
            if(_upTick >= 72000)
            {
                checkUpdate();
            }
        }
        if(Config.getInt("save-interval") > 0)
        {
            _saveTick += ticks;
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
    
    public void annoyPlayer(Player player, String[] messages)
    {
        ArrayList<String> list = _annoyPlayers.get(player);
        if(list == null)
        {
            list = new ArrayList<String>();
            _annoyPlayers.put(player, list);
        }
        for(String msg : messages)
        {
            list.add(msg);
        }
    }
    
    public void stopAnnoyingPlayer(Player player)
    {
        _annoyPlayers.remove(player);
    }
    
    public String getLoadingProgress()
    {
        return "" + Util.smooth((double)(_numLoadThreads - _loadThreads.size()) * 100D / (double)_plugin._numLoadThreads, 2);
    }
    
    public boolean isReloading()
    {
        return _loadRequested || isActuallyReloading();
    }
    
    private boolean isActuallyReloading()
    {
        return !_loadThreads.isEmpty() || ((_initThread != null) && !_initThread.done());
    }
    
    private boolean canReload()
    {
        return ((_saveThread == null) || _saveThread.done()) && !isActuallyReloading();
    }
}
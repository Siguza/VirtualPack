// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.*;
import net.minecraft.server.v#MC_VERSION#.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftPlayer;
import com.sk89q.bukkit.util.*;
import net.drgnome.virtualpack.components.VGUI;
import net.drgnome.virtualpack.data.*;
import net.drgnome.virtualpack.util.*;
import net.drgnome.virtualpack.thread.*;
import net.drgnome.virtualpack.inject.BlockVAnvil;
import static net.drgnome.virtualpack.util.Global.*;

public class VPlugin extends JavaPlugin implements Runnable
{
    public static final String _version = "#VERSION#";
    public static final int _projectID = 37545; // Bukkit
    public static final String[] _components = {"main", "workbench", "uncrafter", "chest", "furnace", "brewingstand", "enchanttable", "trash", "send", "anvil", "materializer", "enderchest"};
    
    static final VCommands _commandHandler = new VCommands();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, VPack>> _packs = new ConcurrentHashMap<String, ConcurrentHashMap<String, VPack>>();
    private HashMap<Player, ArrayList<String>> _annoyPlayers = new HashMap<Player, ArrayList<String>>();
    public ArrayList<VThreadLoad> _loadThreads = new ArrayList<VThreadLoad>();
    private int _numLoadThreads = 0;
    private int _loadTick = 0;
    private boolean _update = false;
    private boolean _saveRequested = false;
    private boolean _loadRequested = false;
    private boolean _portMysql = false;
    private boolean _loadSuccess = false;
    private VThreadInit _initThread;
    private VThreadSave _saveThread;
    private CommandRegistration _reg;
    private boolean _starting = true;
    int[] _threadId = new int[6];
    
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
    
    public synchronized void init()
    {
        if(_threadId[5] != 0)
        {
            getServer().getScheduler().cancelTask(_threadId[5]);
            _threadId[5] = 0;
        }
        checkFiles();
        Config.reload();
        Debug.init();
        if(Config.bool("inject.anvil"))
        {
            BlockVAnvil.inject();
        }
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
                    _log.severe(Lang.get(null, "protocollib"));
                }
            }
        }
        loadUserData();
        registerThreads();
    }
    
    public void registerThreads()
    {
        _threadId[0] = getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThreadLoadManager(), 0L, 1L);
        long tmp = ((long)Config.getInt("save-interval")) * 20L;
        if(tmp > 0)
        {
            _threadId[1] = getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThreadSaveManager(), tmp, tmp);
        }
        tmp = (long)Config.getInt("tick.interval");
        if(tmp > 0)
        {
            _threadId[2] = getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, tmp);
        }
        tmp = (long)Config.getInt("send.notify-interval");
        if(tmp > 0)
        {
            _threadId[3] = getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThreadAnnoy(), 0L, tmp);
        }
        if(Config.bool("check-update"))
        {
            _threadId[4] = getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThreadUpdate(), 0L, 72000L);
        }
        _log.info(Lang.get(null, "vpack.enable", _version));
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
                _log.severe(Lang.get(null, "worldedit"));
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
                    list.add(new CommandInfo(main, Lang.get(null, "cmd." + component), new String[]{c}, this, new String[]{"vpack.use"}));
                }
            }
            catch(NullPointerException e)
            {
                _log.info("[VirtualPack] Could not add commands for " + component);
            }
        }
        try
        {
            _reg = new CommandRegistration(this, _commandHandler);
            return _reg.register(list);
        }
        catch(Throwable t)
        {
            _log.severe(Lang.get(null, "worldedit"));
            t.printStackTrace();
        }
        return false;
    }
    
    public void onEnable()
    {
        super.onEnable();
        try
        {
            if(((Boolean)(Class.forName("org.anjocaido.groupmanager.GroupManager").getMethod("isLoaded").invoke(null))).booleanValue())
            {
                init();
            }
            else
            {
                _threadId[5] = getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThreadWait(), 0L, 1L);
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
        if(!isReloading())
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
            _log.info(Lang.get(null, "vpack.disable", _version));
        }
    }
    
    public boolean hasPack(Player player)
    {
        return hasPack(player.getWorld().getName(), player.getName());
    }
    
    public boolean hasPack(String world, String player)
    {
        world = Config.world(world);
        ConcurrentHashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            return false;
        }
        return map.containsKey(player.toLowerCase());
    }
    
    public VPack[] getAllPacks()
    {
        ArrayList<VPack> list = new ArrayList<VPack>();
        Iterator<ConcurrentHashMap<String, VPack>> iterator = _packs.values().iterator();
        while(iterator.hasNext())
        {
            ConcurrentHashMap<String, VPack> map = iterator.next();
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
        ConcurrentHashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            map = new ConcurrentHashMap<String, VPack>();
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
        ConcurrentHashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            if(pack == null)
            {
                return;
            }
            map = new ConcurrentHashMap<String, VPack>();
            _packs.put(world, map);
        }
        if(pack == null)
        {
            map.remove(player.toLowerCase());
        }
        else
        {
            map.put(player.toLowerCase(), pack);
        }
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
            ChestKeeperHelper.check();
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
                synchronized(_loadThreads)
                {
                    _loadThreads.add(loadThread);
                }
                loadThread.start();
            }
        }
        else
        {
            if(Config.bool("debug-load"))
            {
                int i = 0;
                for(String[] data : list)
                {
                    setPack(data[0], data[1], new VPack(data[0], data[1], data[2].split(_separator[0])));
                    System.out.println("[VPack/Debug] Loaded pack " + (++i) + "/" + list.size());
                }
            }
            else
            {
                for(String[] data : list)
                {
                    setPack(data[0], data[1], new VPack(data[0], data[1], data[2].split(_separator[0])));
                }
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
        if(!hasPack(player))
        {
            return;
        }
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
    
    public void runLoad()
    {
        if(_loadRequested)
        {
            loadUserData();
        }
        if(isReloading())
        {
            return;
        }
        if(!_loadSuccess)
        {
            int reloadInterval = Config.getInt("reload-on-failure");
            if(reloadInterval > 0)
            {
                _loadTick++;
                if(_loadTick > reloadInterval * 20)
                {
                    _loadTick = 0;
                    loadUserData();
                }
            }
            else
            {
                return;
            }
        }
        if(_saveRequested)
        {
            saveUserData();
        }
    }
    
    public void runSave()
    {
        _log.info("[VirtualPack] Saving user data...");
        saveUserData();
    }
    
    public void runAnnoy()
    {
        for(Map.Entry<Player, ArrayList<String>> entry : _annoyPlayers.entrySet())
        {
            for(String msg : entry.getValue())
            {
                sendMessage(entry.getKey(), msg, ChatColor.GREEN);
            }
            List<String> cmds = Config.list("commands." + VPlugin._components[0]);
            if(cmds.size() > 0)
            {
                sendMessage(entry.getKey(), Lang.get(entry.getKey(), "send.relieve", cmds.get(0)), ChatColor.RED);
            }
        }
    }
    
    public void runUpdate()
    {
        if(checkUpdate())
        {
            getServer().getScheduler().cancelTask(_threadId[4]);
            _threadId[4] = 0;
        }
    }
    
    public void run()
    {
        int ticks = Config.getInt("tick.interval");
        Iterator<ConcurrentHashMap<String, VPack>> iterator = _packs.values().iterator();
        while(iterator.hasNext())
        {
            ConcurrentHashMap<String, VPack> map = iterator.next();
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
    }
    
    public boolean checkUpdate()
    {
        _update = Util.hasUpdate(_projectID, _version);
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
        int size;
        synchronized(_loadThreads)
        {
            size = _loadThreads.size();
        }
        return "" + Util.smooth((double)(_numLoadThreads - size) * 100D / (double)_numLoadThreads, 2);
    }
    
    public boolean isReloading()
    {
        return _loadRequested || isActuallyReloading();
    }
    
    private boolean isActuallyReloading()
    {
        boolean isDone;
        synchronized(_loadThreads)
        {
            isDone = _loadThreads.isEmpty();
        }
        return !isDone || ((_initThread != null) && !_initThread.done());
    }
    
    private boolean canReload()
    {
        return ((_saveThread == null) || _saveThread.done()) && !isActuallyReloading();
    }
    
    void deleteEverything()
    {
        _loadSuccess = true;
        synchronized(_loadThreads)
        {
            _loadThreads = new ArrayList<VThreadLoad>();
        }
        _numLoadThreads = 0;
        _saveRequested = false;
        _loadRequested = false;
        _packs = new ConcurrentHashMap<String, ConcurrentHashMap<String, VPack>>();
        _annoyPlayers = new HashMap<Player, ArrayList<String>>();
    }
}
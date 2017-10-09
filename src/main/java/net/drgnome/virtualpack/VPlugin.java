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
import com.mojang.authlib.*;
import net.minecraft.server.v#MC_VERSION#.EntityPlayer;
import net.minecraft.server.v#MC_VERSION#.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftPlayer;
import com.sk89q.bukkit.util.*;
import net.drgnome.virtualpack.components.VGUI;
import net.drgnome.virtualpack.components.VUncrafterInv;
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
    public int _numActiveLoadThreads = 0;
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
    int[] _threadId = new int[7];
    private boolean _uuids = true;
    private boolean _uuidsConvert = false;

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
        catch(Exception e)
        {
            e.printStackTrace();
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
        _uuids = Config.bool("uuids.use");
        _uuidsConvert = Config.bool("uuids.force-convert");
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
            catch(Exception e)
            {
                e.printStackTrace();
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
        //_threadId[0] = getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThreadLoadManager(), 0L, 1L);
        _threadId[0] = new VThreadLoadManager().runTaskTimer(this, 0L, 1L).getTaskId();
        long tmp = ((long)Config.getInt("save-interval")) * 20L;
        if(tmp > 0)
        {
            //_threadId[1] = getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThreadSaveManager(), tmp, tmp);
            _threadId[1] = new VThreadSaveManager().runTaskTimer(this, tmp, tmp).getTaskId();
        }
        tmp = (long)Config.getInt("tick.interval");
        if(tmp > 0)
        {
            // No need for thread pooling here
            _threadId[2] = getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, tmp);
        }
        tmp = (long)Config.getInt("send.notify-interval");
        if(tmp > 0)
        {
            //_threadId[3] = getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThreadAnnoy(), 0L, tmp);
            _threadId[3] = new VThreadAnnoy().runTaskTimer(this, 0L, tmp).getTaskId();
        }
        if(Config.bool("check-update"))
        {
            //_threadId[4] = getServer().getScheduler().runTaskTimerAsynchronously(this, new VThreadUpdate(), 0L, 72000L).getTaskId();
            _threadId[4] = new VThreadUpdate().runTaskTimerAsynchronously(this, 0L, 72000L).getTaskId();
        }
        _threadId[6] = new BukkitRunnable()
        {
            public void run()
            {
                VUncrafterInv.init();
            }
        }.runTask(this).getTaskId();
        _log.info(Lang.get(null, "vpack.enable", _version));
    }

    private boolean registerCommands()
    {
        try
        {
            Class clazz = Class.forName("com.sk89q.bukkit.util.CommandRegistration");
        }
        catch(Exception e)
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
        catch(Exception e)
        {
            _log.severe(Lang.get(null, "worldedit"));
            e.printStackTrace();
        }
        return false;
    }

    public void onEnable()
    {
        super.onEnable();
        try
        {
            Method m = Class.forName("org.anjocaido.groupmanager.GroupManager").getMethod("isLoaded");
            m.setAccessible(true);
            if(((Boolean)(m.invoke(null))).booleanValue())
            {
                init();
            }
            else
            {
                //_threadId[5] = getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThreadWait(m), 0L, 20L);
                _threadId[5] = new VThreadWait(m).runTaskTimer(this, 0L, 20L).getTaskId();
            }
        }
        catch(ClassNotFoundException e)
        {
            init();
        }
        catch(Exception e)
        {
            warn();
            e.printStackTrace();
            init();
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
        try
        {
            _reg.unregisterCommands();
        }
        catch(Exception e)
        {
        }
        // If the plugin is still loading, there's nothing to save
        if((_initThread != null) && !_initThread.done() || _loadThreads.size() > 0)
        {
            return;
        }
        if((_saveThread == null) || _saveThread.done())
        {
            saveUserData();
        }
        if(_saveThread != null)
        {
            try
            {
                _saveThread.join();
            }
            catch(InterruptedException e)
            {
                _log.log(Level.WARNING, "[VirtualPack] Waiting for ThreadSave interrupted: " + e.getMessage());
            }
        }
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

    private String trueUUID(String name)
    {
        CallbackCache cache = new CallbackCache();
        MinecraftServer.getServer().getGameProfileRepository().findProfilesByNames(new String[]{name}, Agent.MINECRAFT, cache);
        return cache.result == null ? null : "*" + cache.result.toString();
    }

    private String idPlayer(Player player)
    {
        return _uuids ? "*" + player.getUniqueId().toString() : player.getName().toLowerCase();
    }

    private String idOffline(String name)
    {
        return _uuids ? "*" + Bukkit.getOfflinePlayer(name).getUniqueId().toString() : name.toLowerCase();
    }

    private String[] possibleIds(String name)
    {
        String[] s = new String[4];
        s[0] = name.toLowerCase();
        s[1] = trueUUID(name);
        s[2] = "*" + UUID.nameUUIDFromBytes(("OfflinePlayer:" + s[0]).getBytes(java.nio.charset.Charset.forName("UTF-8"))).toString();
        s[3] = "*" + UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.Charset.forName("UTF-8"))).toString();
        return s;
    }

    public boolean tryRestore(String world, Player player)
    {
        ConcurrentHashMap<String, VPack> map = _packs.get(Config.world(world));
        if(map == null)
        {
            return false;
        }
        String id = idPlayer(player);
        String[] u = possibleIds(player.getName());
        if(u[1] != null && !u[1].equals(id) && map.containsKey(u[1]))
        {
            map.put(id, map.get(u[1]));
            map.remove(u[1]);
            return true;
        }
        if(!u[0].equals(id) && map.containsKey(u[0]))
        {
            map.put(id, map.get(u[0]));
            map.remove(u[0]);
            return true;
        }
        if(!u[2].equals(id) && map.containsKey(u[2]))
        {
            map.put(id, map.get(u[2]));
            map.remove(u[2]);
            return true;
        }
        if(!u[3].equals(u[2]) && !u[3].equals(id) && map.containsKey(u[3]))
        {
            map.put(id, map.get(u[3]));
            map.remove(u[3]);
            return true;
        }
        return false;
    }

    public boolean hasPack(Player player)
    {
        return hasPack(player.getWorld().getName(), player);
    }

    public boolean hasPack(String world, Player player)
    {
        if(!Config.bool(world, "enabled"))
        {
            return false;
        }
        world = Config.world(world);
        ConcurrentHashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            return false;
        }
        boolean has = map.containsKey(idPlayer(player));
        if(!has)
        {
            has = tryRestore(world, player);
        }
        return has;
    }

    public boolean hasPack(String world, String player)
    {
        Player p = Bukkit.getPlayer(player);
        if(p != null)
        {
            return hasPack(world, p);
        }
        if(!Config.bool(world, "enabled"))
        {
            return false;
        }
        ConcurrentHashMap<String, VPack> map = _packs.get(Config.world(world));
        if(map == null)
        {
            return false;
        }
        if(map.containsKey(idOffline(player)))
        {
            return true;
        }
        for(String u : possibleIds(player))
        {
            if(u == null)
            {
                continue;
            }
            if(map.containsKey(u))
            {
                return true;
            }
        }
        return false;
    }

    public VPack getPack(Player player)
    {
        return getPack(player.getWorld().getName(), player);
    }

    public VPack getPack(String world, Player player)
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
        String id = idPlayer(player);
        VPack pack = map.get(id);
        if(pack == null)
        {
            if(tryRestore(world, player))
            {
                pack = map.get(id);
            }
            else
            {
                pack = new VPack(world, player);
                map.put(id, pack);
            }
        }
        return pack;
    }

    public VPack getPack(String world, String player)
    {
        Player p = Bukkit.getPlayer(player);
        if(p != null)
        {
            return getPack(world, p);
        }
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
        String id = idOffline(player);
        VPack pack = map.get(id);
        if(pack == null)
        {
            for(String u : possibleIds(player))
            {
                if(u == null)
                {
                    continue;
                }
                pack = map.get(u);
                if(pack != null)
                {
                    break;
                }
            }
            if(pack == null)
            {
                pack = new VPack(world, player);
                map.put(id, pack);
            }
        }
        return pack;
    }

    public void setPack(Player player, VPack pack)
    {
        setPack(player.getWorld().getName(), player, pack);
    }

    public void setPack(String world, Player player, VPack pack)
    {
        if(!Config.bool(world, "enabled"))
        {
            return;
        }
        world = Config.world(world);
        ConcurrentHashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            map = new ConcurrentHashMap<String, VPack>();
            _packs.put(world, map);
        }
        String id = idPlayer(player);
        if(pack == null)
        {
            map.remove(id);
        }
        else
        {
            map.put(id, pack);
        }
    }

    public void setPack(String world, String player, VPack pack)
    {
        Player p = Bukkit.getPlayer(player);
        if(p != null)
        {
            setPack(world, p, pack);
            return;
        }
        if(!Config.bool(world, "enabled"))
        {
            return;
        }
        world = Config.world(world);
        ConcurrentHashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            map = new ConcurrentHashMap<String, VPack>();
            _packs.put(world, map);
        }
        String id = idOffline(player);
        if(pack == null)
        {
            map.remove(id);
        }
        else
        {
            map.put(id, pack);
        }
    }

    public void setPackRaw(String world, String player, VPack pack)
    {
        if(!Config.bool(world, "enabled"))
        {
            return;
        }
        if(_uuids && _uuidsConvert && !player.startsWith("*"))
        {
            String uuid = trueUUID(player);
            if(uuid != null)
            {
                player = uuid;
            }
        }
        world = Config.world(world);
        ConcurrentHashMap<String, VPack> map = _packs.get(world);
        if(map == null)
        {
            map = new ConcurrentHashMap<String, VPack>();
            _packs.put(world, map);
        }
        map.put(player, pack);
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
            catch(Exception e)
            {
                warn();
                e.printStackTrace();
                return;
            }
        }
        else
        {
            _saveThread = new VThreadSave(new File(getDataFolder(), filename), _packs);
        }
        _saveThread.start();
        //_saveThread.runTaskAsynchronously(this);
        _saveRequested = false;
    }

    public void loadUserData()
    {
        if(!canReload())
        {
            _loadRequested = true;
            return;
        }
        if(Config.bool("load-multithreaded") || Config.string("load-multithreaded").equalsIgnoreCase("semi"))
        {
            if(isActuallyReloading())
            {
                return;
            }
            _numLoadThreads = 1;
            _initThread = new VThreadInit();
            _initThread.start();
            //_initThread.runTaskAsynchronously(this);
        }
        else
        {
            loadUserData0();
        }
    }

    public synchronized void loadUserData0()
    {
        //_loadRequested = true;
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
            PlayerVaultsHelper.check();
            VirtualChestHelper.check();
        }
        catch(Exception e)
        {
            _log.severe("[VirtualPack] COULD NOT LOAD USER DATA!");
            e.printStackTrace();
            _loadSuccess = false;
        }
        _loadRequested = false;
    }

    private void loadMysql() throws Exception
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

    private void loadFlatfile() throws Exception
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
        boolean lazy = Config.bool("lazy-loading-hack");
        if(Config.bool("load-multithreaded"))
        {
            boolean bypass = Config.bool("bypass-thread-pool-on-load");
            int max = Config.getInt("max-threads");
            _numLoadThreads = 0;
            _numActiveLoadThreads = 0;
            VThreadLoad._debug = Config.bool("debug-load");
            VThreadLoad._num = 0;
            VThreadLoad._total = list.size();
            for(String[] data : list)
            {
                while(_numActiveLoadThreads >= max)
                {
                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch(Exception e)
                    {
                    }
                }
                _numLoadThreads++;
                _numActiveLoadThreads++;
                VThreadLoad loadThread = new VThreadLoad(data, lazy);
                synchronized(_loadThreads)
                {
                    _loadThreads.add(loadThread);
                }
                if(bypass)
                {
                    new Thread(loadThread).start();
                }
                else
                {
                    loadThread.runTaskAsynchronously(this);
                }
            }
        }
        else
        {
            if(Config.bool("debug-load"))
            {
                int i = 0;
                int total = list.size();
                for(String[] data : list)
                {
                    setPackRaw(data[0], data[1], new VPack(data[0], data[1], data[2], lazy));
                    System.out.println("[VPack/Debug] Loaded pack " + (++i) + "/" + total);
                }
            }
            else
            {
                for(String[] data : list)
                {
                    setPackRaw(data[0], data[1], new VPack(data[0], data[1], data[2], lazy));
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
        _uuids = Config.bool("uuids.use");
        _uuidsConvert = Config.bool("uuids.force-convert");
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
        catch(Exception e)
        {
            e.printStackTrace();
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
        catch(Exception e)
        {
            e.printStackTrace();
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
        if(Config.bool("save-message"))
        {
            _log.info("[VirtualPack] Saving user data...");
        }
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

    private static class CallbackCache implements ProfileLookupCallback
    {
        public UUID result = null;

        public void onProfileLookupSucceeded(GameProfile profile)
        {
            result = profile.getId();
        }

        public void onProfileLookupFailed(GameProfile profile, Exception e)
        {
        }
    }
}

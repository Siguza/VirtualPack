// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

// Thanks to theodik for a bugfix :)

package net.drgnome.virtualpack;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.sql.*;
import java.net.*;

import net.minecraft.server.*;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.configuration.file.*;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import static net.drgnome.virtualpack.Config.*;
import static net.drgnome.virtualpack.Lang.*;
import static net.drgnome.virtualpack.Util.*;

public abstract class VPluginBase extends JavaPlugin implements Listener
{
    public static final String version = "1.1.5";
    public static final String dbVer = "1";
    public static int dbVersion;
    protected HashMap<String, VPack> packs;
    private int saveTick;
    private int upTick;
    private boolean update;
    private VThreadSave saveThread;
    private boolean saveRequested;
    private boolean loadRequested;
    private boolean waitForPlugin;
    private Connection db;
    private boolean portMysql;
    private boolean loadSuccess;

    public void onEnable()
    {
        waitForPlugin = false;
        try
        {
            if(org.anjocaido.groupmanager.GroupManager.isLoaded())
            {
                init();
            }
            else
            {
                waitForPlugin = true;
            }
        }
        catch(NoClassDefFoundError e)
        {
            init();
        }
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThread(this), 0L, 1L);
    }
    
    private void init()
    {
        log.info("Enabling VirtualPack " + version);
        saveTick = 0;
        update = false;
        upTick = 60 * 60 * 20;
        saveRequested = false;
        loadRequested = false;
        waitForPlugin = false;
        portMysql = false;
        loadSuccess = false;
        packs = new HashMap<String, VPack>();
        checkFiles();
        initLang(getDataFolder());
        reloadConf(getConfig());
        saveConfig();
        if(getConfigString("debug").equalsIgnoreCase("true"))
        {
            Debug.init(new File(getDataFolder(), "debug.log"));
        }
        if(getConfigString("db.use").equalsIgnoreCase("true"))
        {
            try
            {
                db = DriverManager.getConnection(getConfigString("db.url"), getConfigString("db.user"), getConfigString("db.pw"));
                db.prepareStatement("CREATE TABLE IF NOT EXISTS `vpack` (`data` longtext NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;").execute();
                ResultSet row = db.prepareStatement("SELECT * FROM `vpack`").executeQuery();
                portMysql = !row.next();
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }
        }
        reloadLang();
        if(!initPerms())
        {
            getPluginLoader().disablePlugin(this);
            return;
        }
        economyDisabled = getConfigString("economy-disabled").equalsIgnoreCase("true") ? true : false;
        if(!initEconomy())
        {
            getPluginLoader().disablePlugin(this);
            return;
        }
        loadUserData();
        getServer().getPluginManager().registerEvents(this, this);
        log.info(lang("vpack.enable", new String[]{version}));
    }

    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
        if(!waitForPlugin)
        {
            log.info(lang("vpack.startdisable", new String[]{version}));
            saveUserData();
            try 
            {
                saveThread.join();
            } 
            catch (InterruptedException ex) 
            {
                log.log(Level.WARNING, "[VirtualPack] Save interrupted: {0}", ex.getMessage());
            }
            log.info(lang("vpack.disable", new String[]{version}));
        }
        if(getConfigString("forceload").equalsIgnoreCase("true"))
        {
            getConfig().set("forceload", "false");
            saveConfig();
        }
    }
    
    public synchronized void tick()
    {
        if(waitForPlugin)
        {
            try
            {
                if(org.anjocaido.groupmanager.GroupManager.isLoaded())
                {
                    init();
                }
                else
                {
                    return;
                }
            }
            catch(NoClassDefFoundError e)
            {
                init();
            }
        }
        Object values[] = packs.values().toArray();
        for(int i = 0; i < values.length; i++)
        {
            if(values[i] == null)
            {
                continue;
            }
            ((VPack)values[i]).tick();
        }
        if(!update && getConfigString("check-update").equalsIgnoreCase("true"))
        {
            upTick++;
            if(upTick >= 60 * 60 * 20)
            {
                checkForUpdate();
                upTick = 0;
            }
        }
        if(saveRequested)
        {
            saveUserData();
        }
        if(loadRequested)
        {
            loadUserData();
        }
        if(getConfigInt("save-interval") > 0)
        {
            saveTick++;
            if(saveTick >= getConfigInt("save-interval") * 20)
            {
                log.info("[VirtualPack] Saving user data...");
                saveUserData();
                saveTick = 0;
            }
        }
    }
    
    private void checkForUpdate()
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection)(new URL("http://dev.drgnome.net/version.php?t=vpack")).openConnection();            
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JVM)");                        
            con.setRequestProperty("Pragma", "no-cache");
            con.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            StringBuilder stringb = new StringBuilder();
            if((line = reader.readLine()) != null)
            {
                stringb.append(line);
            }
            String vdigits[] = this.version.toLowerCase().split("\\.");
            String cdigits[] = stringb.toString().toLowerCase().split("\\.");
            int max = vdigits.length > cdigits.length ? cdigits.length : vdigits.length;
            int a = 0;
            int b = 0;
            for(int i = 0; i < max; i++)
            {
                try
                {
                    a = Integer.parseInt(cdigits[i]);
                }
                catch(Throwable t1)
                {
                    char c[] = cdigits[i].toCharArray();
                    for(int j = 0; j < c.length; j++)
                    {
                        a += (c[j] << ((c.length - (j + 1)) * 8));
                    }
                }
                try
                {
                    b = Integer.parseInt(vdigits[i]);
                }
                catch(Throwable t1)
                {
                    char c[] = vdigits[i].toCharArray();
                    for(int j = 0; j < c.length; j++)
                    {
                        b += (c[j] << ((c.length - (j + 1)) * 8));
                    }
                }
                if(a > b)
                {
                    update = true;
                    break;
                }
                else if(a < b)
                {
                    update = false;
                    break;
                }
                else if((i == max - 1) && (cdigits.length > vdigits.length))
                {
                    update = true;
                    break;
                }
            }
        }
        catch(UnknownHostException e)
        {
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        reloadConf(getConfig());
        saveConfig();
        reloadLang();
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
    
    protected synchronized void loadUserData()
    {
        if((saveThread != null) && !(saveThread.done()))
        {
            loadRequested = true;
            return;
        }
        try
		{
            File alphaChest = new File(getDataFolder(), "chests");
            if(alphaChest.exists() && alphaChest.isDirectory())
            {
                AlphaChestHelper.load(this, alphaChest);
            }
            if(portMysql)
            {
                loadFlatfile();
                saveUserData();
                portMysql = false;
            }
            else if(getConfigString("db.use").equalsIgnoreCase("true"))
            {
                loadMysql();
            }
            else
            {
                loadFlatfile();
            }
		}
		catch(Throwable t)
		{
            warn();
            t.printStackTrace();
		}
        loadRequested = false;
    }
    
    private void loadMysql() throws Throwable
    {
        try
        {
            if(db == null)
            {
                db = DriverManager.getConnection(getConfigString("db.url"), getConfigString("db.user"), getConfigString("db.pw"));
            }
            ArrayList<String[]> list = new ArrayList<String[]>();
            ResultSet row = db.prepareStatement("SELECT * FROM `vpack`").executeQuery();
            while(row.next())
            {
                Debug.log("load: " + row.getString("data"));
                list.add(row.getString("data").split(separator[0]));
            }
            load(list);
        }
        catch(Throwable t)
        {
            log.warning("[VirtualPack] COULD NOT LOAD USER DATA!");
            t.printStackTrace();
            for(StackTraceElement st : t.getStackTrace())
            {
                Debug.log(st.toString());
            }
            loadSuccess = false;
        }
    }
    
    private void loadFlatfile() throws Throwable
    {
        try
        {
            BufferedReader file = new BufferedReader(new FileReader(new File(getDataFolder(), "data.db")));
            String line;
            ArrayList<String[]> list = new ArrayList<String[]>();
            while((line = file.readLine()) != null)
            {
                Debug.log("load: " + line);
                list.add(line.split(separator[0]));
            }
            file.close();
            load(list);
        }
        catch(Throwable t)
        {
            log.warning("[VirtualPack] COULD NOT LOAD USER DATA!");
            t.printStackTrace();
            for(StackTraceElement st : t.getStackTrace())
            {
                Debug.log(st.toString());
            }
            loadSuccess = false;
        }
    }
    
    private void load(ArrayList<String[]> list) throws Throwable
    {
        boolean first = true;
        for(String[] data : list.toArray(new String[0][]))
        {
            if(first)
            {
                first = false;
                if(getConfigString("debug").equalsIgnoreCase("true"))
                {
                    for(int i = 0; i < data.length; i++)
                    {
                        Debug.log("first " + i + ": " + data[i]);
                    }
                }
                if(data.length == 1)
                {
                    this.dbVersion = tryParse(data[0], 0);
                }
                else
                {
                    this.dbVersion = 0;
                }
                Debug.log("decision: " + this.dbVersion);
            }
            else if(data.length >= 2)
            {
                putPack(data[0], new VPack(data[0].toLowerCase(), data, 1));
            }
        }
        loadSuccess = true;
    }
    
    protected synchronized void saveUserData()
    {
        saveUserData(false);
    }
    
    protected synchronized void saveUserData(boolean forcefile)
    {
        if(!loadSuccess)
        {
            log.warning("[VirtualPack] CANNOT SAVE USER DATA, LOADING ALREADY FAILED!");
            return;
        }
        if((saveThread != null) && !(saveThread.done()))
        {
            saveRequested = true;
            return;
        }
        if(getConfigString("db.use").equalsIgnoreCase("true") && !forcefile)
        {
            saveThread = new VThreadSave(db, packs);
        }
        else
        {
            saveThread = new VThreadSave(new File(getDataFolder(), "data.db"), packs);
        }
        saveThread.start();
        saveRequested = false;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(waitForPlugin)
        {
            sender.sendMessage("VirtualPack is waiting for GroupManager.");
            return true;
        }
        if(!loadSuccess && (((sender instanceof Player) && !(sender.hasPermission("vpack.admin"))) || (args.length < 2) || !(longname(args[0]).equals("admin") && args[1].equalsIgnoreCase("reload"))))
        {
            sender.sendMessage("Data loading failed, tell an admin to do a reload.");
            return true;
        }
        if((sender instanceof CraftPlayer) && hasPack(sender.getName()) && (getPack(sender.getName()).inv != null))
        {
            restoreInv(((CraftPlayer)sender).getHandle());
        }
        if(update && (!(sender instanceof Player) || (sender.hasPermission("vpack.update"))))
        {
            sendMessage(sender, lang("update.msg"), ChatColor.GREEN);
            sendMessage(sender, lang("update.link"), ChatColor.RED);
        }
        if((args.length <= 0) || ((args.length >= 1) && (args[0].equals("help"))))
        {
            cmdHelp(sender, args);
            return true;
        }
        args[0] = longname(args[0]);
        if(args[0].equals("version"))
        {
            sendMessage(sender, lang("version", new String[]{version}), ChatColor.BLUE);
            return true;
        }
        else if(args[0].equals("update"))
        {
            if(!sender.hasPermission("vpack.update"))
            {
                sendMessage(sender, lang("update.perm"), ChatColor.RED);
                return true;
            }
            checkForUpdate();
            if(update)
            {
                sendMessage(sender, lang("update.msg"), ChatColor.GREEN);
                sendMessage(sender, lang("update.link"), ChatColor.RED);
            }
            else
            {
                sendMessage(sender, lang("update.no"), ChatColor.GREEN);
            }
            return true;
        }
        else if((args[0].equals("stats")) && !(sender instanceof Player))
        {
            cmdConsoleStats(sender, args);
            return true;
        }
        else if(args[0].equals("admin"))
        {
            if(!sender.hasPermission("vpack.admin"))
            {
                sendMessage(sender, lang("admin.perm"), ChatColor.RED);
                return true;
            }
            cmdAdmin(sender, args);
            if((args.length < 2) || (!args[1].equals("use")))
            {
                return true;
            }
        }
        else if(!(sender instanceof Player))
        {
            sendMessage(sender, lang("use.player"), ChatColor.RED);
            return true;
        }
        else if(!sender.hasPermission("vpack.use"))
        {
            sendMessage(sender, lang("use.perm"), ChatColor.RED);
            return true;
        }
        try
        {
            if(args[0].equals("admin"))
            {
                cmdAdminUse(sender, args);
            }
            else if(args[0].equals("stats"))
            {
                cmdStats(sender, args);
            }
            else if(args[0].equals("price"))
            {
                cmdPrices(sender, args);
            }
            else if(args[0].equals("workbench"))
            {
                cmdWorkbench(sender, args);
            }
            else if(args[0].equals("uncrafter"))
            {
                cmdUncrafter(sender, args);
            }
            else if(args[0].equals("invguard"))
            {
                cmdInvGuard(sender, args);
            }
            else if(args[0].equals("enchanttable"))
            {
                cmdEnchanttable(sender, args);
            }
            else if(args[0].equals("chest"))
            {
                cmdChest(sender, args);
            }
            else if(args[0].equals("furnace"))
            {
                cmdFurnace(sender, args);
            }
            else if(args[0].equals("brewingstand"))
            {
                cmdBrewingstand(sender, args);
            }
            else if(args[0].equals("trash"))
            {
                cmdTrash(sender, args);
            }
            else if(args[0].equals("debug"))
            {
                cmdDebug(sender, args);
            }
            else
            {
                sendMessage(sender, lang("argument.unknown"), ChatColor.RED);
            }
        }
        catch(Throwable t)
        {
            sendMessage(sender, lang("argument.error"), ChatColor.RED);
            warn();
            t.printStackTrace();
        }
        return true;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public abstract void handleEntityDamage(EntityDamageEvent event);
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public abstract void handleRespawn(PlayerRespawnEvent event);
    
    protected abstract void cmdHelp(CommandSender sender, String[] args);
    protected abstract void cmdConsoleStats(CommandSender sender, String[] args);
    protected abstract void cmdAdmin(CommandSender sender, String[] args);
    protected abstract void cmdAdminUse(CommandSender sender, String[] args);
    protected abstract void cmdStats(CommandSender sender, String[] args);
    protected abstract void cmdPrices(CommandSender sender, String[] args);
    protected abstract void cmdWorkbench(CommandSender sender, String[] args);
    protected abstract void cmdUncrafter(CommandSender sender, String[] args);
    protected abstract void cmdInvGuard(CommandSender sender, String[] args);
    protected abstract void cmdEnchanttable(CommandSender sender, String[] args);
    protected abstract void cmdChest(CommandSender sender, String[] args);
    protected abstract void cmdFurnace(CommandSender sender, String[] args);
    protected abstract void cmdBrewingstand(CommandSender sender, String[] args);
    protected abstract void cmdTrash(CommandSender sender, String[] args);
    protected abstract void cmdDebug(CommandSender sender, String[] args);
    
    public String longname(String s)
    {
        s = s.toLowerCase().trim();
        if(s.length() > 2)
        {
            return s;
        }
        if(s.equals("a"))
        {
            return "admin";
        }
        if(s.equals("w"))
        {
            return "workbench";
        }
        if(s.equals("uc"))
        {
            return "uncrafter";
        }
        if(s.equals(""))
        {
            return "invguard";
        }
        if(s.equals("e"))
        {
            return "enchanttable";
        }
        if(s.equals("c"))
        {
            return "chest";
        }
        if(s.equals("f"))
        {
            return "furnace";
        }
        if(s.equals("b"))
        {
            return "brewingstand";
        }
        if(s.equals("l"))
        {
            return "link";
        }
        if(s.equals("u"))
        {
            return "unlink";
        }
        if(s.equals("t"))
        {
            return "trash";
        }
        if(s.equals("v"))
        {
            return "version";
        }
        if(s.equals("s"))
        {
            return "stats";
        }
        if(s.equals("p"))
        {
            return "price";
        }
        if(s.equals("d"))
        {
            return "debug";
        }
        if(s.equals("up"))
        {
            return "update";
        }
        return s;
    }
    
    protected void handleDeath(CraftPlayer cp)
    {
        String death = getConfigString("on-death").toLowerCase();
        if(death.equals("harddrop") || death.equals("drop"))
        {
            getPack(cp.getName()).drop(cp);
        }
        if(death.equals("wipe") || death.equals("drop"))
        {
            getPack(cp.getName()).wipe(cp);
        }
        if(death.equals("hardwipe") || death.equals("harddrop"))
        {
            getPack(cp.getName()).reset(cp);
        }
        if((getPack(cp.getName()).inv == null) && (death.equals("hardkeep") || getPack(cp.getName()).useInvGuard(cp)))
        {
            EntityPlayer player = cp.getHandle();
            getPack(cp.getName()).inv = new PlayerInventory((EntityPlayer)null);
            getPack(cp.getName()).inv.items = copy(player.inventory.items);
            getPack(cp.getName()).inv.armor = copy(player.inventory.armor);
            player.inventory.items = new ItemStack[player.inventory.items.length];
            player.inventory.armor = new ItemStack[player.inventory.armor.length];
            player.inventory.update();
        }
    }
    
    protected void restoreInv(EntityPlayer player)
    {
        player.inventory.m(); // Derpnote
        player.inventory.items = copy(getPack(player.name).inv.items);
        player.inventory.armor = copy(getPack(player.name).inv.armor);
        getPack(player.name).inv = null;
        player.inventory.update();
    }
    
    public boolean hasPack(String name)
    {
        name = name.toLowerCase();
        return !(packs.get(name) == null);
    }
    
    public VPack getPack(String name)
    {
        name = name.toLowerCase();
        VPack pack = packs.get(name);
        if(pack == null)
        {
            pack = new VPack(name);
            putPack(name, pack);
        }
        return pack;
    }
    
    public void putPack(String name, VPack pack)
    {
        name = name.toLowerCase();
        packs.put(name, pack);
    }
}
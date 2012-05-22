// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

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

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import static net.drgnome.virtualpack.Config.*;
import static net.drgnome.virtualpack.Lang.*;
import static net.drgnome.virtualpack.Util.*;

public abstract class VPluginBase extends JavaPlugin
{
    public static final String version = "1.0.4";
    protected HashMap<String, VPack> packs;
    private int saveTick;

    public void onEnable()
    {
        log.info("Enabling VirtualPack " + version);
        saveTick = 0;
        packs = new HashMap<String, VPack>();
        checkFiles();
        initLang(getDataFolder());
        reloadConf(getConfig());
        saveConfig();
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
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThread(this), 0L, 1L);
        log.info(lang("vpack.enable", new String[]{version}));
    }

    public void onDisable()
    {
        log.info(lang("vpack.startdisable", new String[]{version}));
        getServer().getScheduler().cancelTasks(this);
        saveUserData();
        log.info(lang("vpack.disable", new String[]{version}));
    }
    
    public void tick()
    {
        Object values[] = packs.values().toArray();
        for(int i = 0; i < values.length; i++)
        {
            if(values[i] == null)
            {
                continue;
            }
            ((VPack)values[i]).tick();
        }
        if(getConfigInt("save-interval") > 0)
        {
            saveTick++;
            if(saveTick >= getConfigInt("save-interval") * 20)
            {
                saveUserData();
                loadUserData();
                log.info("[VirtualPack] Saving user data...");
                saveTick = 0;
            }
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
            PrintStream writer;
            File data;
            String files[] = new String[]{"config.yml", "data.db"};
            for(int i = 0; i < files.length; i++)
            {
                data = new File(file, files[i]);
                if(!data.exists())
                {
                    writer = new PrintStream(new FileOutputStream(data));
                    writer.close();
                }
            }
        }
        catch(Exception e)
        {
        }
    }
    
    protected void loadUserData()
    {
        try
		{
			BufferedReader file = new BufferedReader(new FileReader(new File(getDataFolder(), "data.db")));
			String line;
            String data[];
			while((line = file.readLine()) != null)
			{
                data = line.split(separator[0]);
                if(data.length >= 2)
                {
                    putPack(data[0], new VPack(data[0].toLowerCase(), data, 1));
                }
			}
			file.close();
		}
		catch(Exception e)
		{
            warn();
            e.printStackTrace();
		}
    }
    
    protected void saveUserData()
    {
        try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(getDataFolder(), "data.db")));
			Object key[] = packs.keySet().toArray();
            String name;
            VPack vpack;
            String contents;
            String data[];
            for(int i = 0; i < key.length; i++)
            {
                name = (String)key[i];
                vpack = getPack(name);
                if(vpack != null)
                {
                    contents = name;
                    data = vpack.save();
                    for(int j = 0; j < data.length; j++)
                    {
                        contents += separator[0] + data[j];
                    }
                    writer.write(contents);
                    writer.newLine();
                }
            }
			writer.close();
		}
		catch(Exception e)
		{
            warn();
            e.printStackTrace();
        }
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
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
        else if((args[0].equals("stats")) && !(sender instanceof Player))
        {
            cmdConsoleStats(sender, args);
            return true;
        }
        else if(args[0].equals("admin"))
        {
            cmdAdmin(sender, args);
            if((args.length > 1) && (!args[1].equals("use")))
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
            else if(args[0].equals("debug"))
            {
                cmdDebug(sender, args);
            }
            else
            {
                sendMessage(sender, lang("argument.unknown"), ChatColor.RED);
            }
        }
        catch(Exception e)
        {
            sendMessage(sender, lang("argument.error"), ChatColor.RED);
            warn();
            e.printStackTrace();
        }
        return true;
    }
    
    protected abstract void cmdHelp(CommandSender sender, String[] args);
    protected abstract void cmdConsoleStats(CommandSender sender, String[] args);
    protected abstract void cmdAdmin(CommandSender sender, String[] args);
    protected abstract void cmdAdminUse(CommandSender sender, String[] args);
    protected abstract void cmdStats(CommandSender sender, String[] args);
    protected abstract void cmdPrices(CommandSender sender, String[] args);
    protected abstract void cmdWorkbench(CommandSender sender, String[] args);
    protected abstract void cmdEnchanttable(CommandSender sender, String[] args);
    protected abstract void cmdUncrafter(CommandSender sender, String[] args);
    protected abstract void cmdChest(CommandSender sender, String[] args);
    protected abstract void cmdFurnace(CommandSender sender, String[] args);
    protected abstract void cmdBrewingstand(CommandSender sender, String[] args);
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
        return s;
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
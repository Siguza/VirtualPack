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

import static net.drgnome.virtualpack.Util.*;

public abstract class VPluginBase extends JavaPlugin
{
    public static final String version = "1.0.3";
    public static final String langv = "2";
    protected static boolean economyDisabled;
    public static Economy economy;
    public static Permission perms;
    public static FileConfiguration config;
    public static YamlConfiguration lang;
    protected Logger log = Logger.getLogger("Minecraft");
    protected HashMap<String, VPack> packs;

    public void onEnable()
    {
        log.info("Enabling VirtualPack " + version);
        packs = new HashMap<String, VPack>();
        checkFiles();
        config = getConfig();
        reloadLang();
        String lv = lang.getString("langv");
        if((lv == null) || (!lv.equalsIgnoreCase(langv)))
        {
            try
            {
                lang.save(new File(getDataFolder(), "lang" + langv + ".yml"));
            }
            catch(Exception e)
            {
            }
            lang = new YamlConfiguration();
        }
        setDefaults();
        economyDisabled = config.getString("economy-disabled").equalsIgnoreCase("true") ? true : false;
        if(!economyDisabled)
        {
            RegisteredServiceProvider eco = getServer().getServicesManager().getRegistration(Economy.class);
            if(eco != null)
            {
                economy = (Economy)eco.getProvider();
            }
            else
            {
                log.warning(lang("vpack.misseco"));
                getPluginLoader().disablePlugin(this);
                return;
            }
        }
        RegisteredServiceProvider perm = getServer().getServicesManager().getRegistration(Permission.class);
        if(perm != null)
        {
            perms = (Permission)perm.getProvider();
        }
        else
        {
            log.warning(lang("vpack.missperm"));
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
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        config = getConfig();
        reloadLang();
    }
    
    public void reloadLang()
    {
        try
        {
            if(lang == null)
            {
                lang = new YamlConfiguration();
            }
            lang.load(new File(getDataFolder(), "lang.yml"));
        }
        catch(Exception e)
        {
            checkFiles();
        }
    }
    
    private void setDefaults()
    {
        setDef(config, "economy-disabled", "false");
        setDef(config, "workbench.buy", "20000");
        setDef(config, "workbench.use", "0");
        setDef(config, "uncrafter.buy", "30000");
        setDef(config, "uncrafter.use", "0");
        setDef(config, "chest.max", "10");
        setDef(config, "chest.start", "0");
        setDef(config, "chest.multiply", "1");
        setDef(config, "chest.buy", "40000");
        setDef(config, "chest.use", "0");
        setDef(config, "furnace.max", "10");
        setDef(config, "furnace.start", "0");
        setDef(config, "furnace.multiply", "1");
        setDef(config, "furnace.buy", "50000");
        setDef(config, "furnace.use", "0");
        setDef(config, "furnace.link", "100000");
        setDef(config, "brewingstand.max", "10");
        setDef(config, "brewingstand.start", "0");
        setDef(config, "brewingstand.multiply", "1");
        setDef(config, "brewingstand.buy", "75000");
        setDef(config, "brewingstand.use", "0");
        setDef(config, "brewingstand.link", "100000");
        setDef(config, "enchanttable.multiply", "1");
        setDef(config, "enchanttable.buy", "30000");
        setDef(config, "enchanttable.use", "0");
        setDef(config, "enchanttable.book", "5000");
        saveConfig();
        config = getConfig();
        setDef(lang, "langv", langv);
        setDef(lang, "vpack.ecodisabled", "Economy is disabled, you don't need to buy anything.");
        setDef(lang, "vpack.misseco", "VirtualPack: Cannot find any vault-hooked economy plugin, disabling.");
        setDef(lang, "vpack.missperm", "VirtualPack: Cannot find any vault-hooked permissions plugin, disabling.");
        setDef(lang, "vpack.enable", "VirtualPack %1 enabled");
        setDef(lang, "vpack.startdisable", "Disabling VirtualPack %1...");
        setDef(lang, "vpack.disable", "VirtualPack %1 disabled");
        setDef(lang, "vpack.none", "This user doesn't have a VirtualPack.");
        setDef(lang, "help.uncrafter.title", "----- ----- ----- Uncrafter Guide ----- ----- -----");
        setDef(lang, "help.uncrafter.description", "The Uncrafter is the exact opposite of the workbench, it splits crafted items back into their crafting ingredients. To uncraft an item, open the Uncrafter and drag the item or stack you want to uncraft into the upper row.");
        setDef(lang, "help.link.title", "----- ----- ----- Linking Guide ----- ----- -----");
        setDef(lang, "help.link.description", "You can \"link\" a furnace or brewing stand with a chest. This means that whenever the furnace runs out of fuel or anything to smelt or the brewing stand runs out of ingredients, they will look into the specified chest and try to get things out of there that they can use. They will also put their smelting and brewing results in that chest if their \"output stack\" has reached the maximum stack size. Once you linked a furnace or brewing stand with a chest, you will always be able to link that furnace or brewing stand with an other chest for free. Also, for every unlink you do you'll be able to link a furnace or brewing stand for free. The number of \"free links\" is shown on the stats page.");
        setDef(lang, "help.link.note", "Note: You can NOT convert a furnace link into a brewing stand link or the other way round!");
        setDef(lang, "help.title", "----- ----- ----- Help Page %1/%2 ----- ----- -----");
        setDef(lang, "help.admin", "/v a - Admin Help");
        setDef(lang, "help.args", "Arguments in () must be given, those in [] are optional and will usually be 1 if not given");
        setDef(lang, "help.help", "/v help [x] - Show help (page x)");
        setDef(lang, "help.version", "/v v - Version");
        setDef(lang, "help.stats", "/v s - Show stats");
        setDef(lang, "help.price", "/v p - Show prices");
        setDef(lang, "help.workbench.buy", "/v w buy - Buy a workbench for yourself");
        setDef(lang, "help.workbench.use", "/v w - Open your workbench");
        setDef(lang, "help.uncrafter.buy", "/v uc buy - Buy the Uncrafter");
        setDef(lang, "help.uncrafter.use", "/v uc - Open the Uncrafter (see %1/v help uc%2)");
        setDef(lang, "help.chest.buy", "/v c buy [x] - Buy [x] chest(s)");
        setDef(lang, "help.chest.use", "/v c [x] - Open your Chest (nr. x)");
        setDef(lang, "help.chest.drop", "/v c drop [x] - Drop the contents of chest (nr. x)");
        setDef(lang, "help.chest.trash", "/v c trash [x] - Delete all the contents of chest (nr. x)");
        setDef(lang, "help.furnace.buy", "/v f buy [x] - Buy [x] furnaces");
        setDef(lang, "help.furnace.use", "/v f [x] - Use your furnace (nr. x)");
        setDef(lang, "help.furnace.link", "/v f l (x) (y) - Link your furnace (x) with a chest (y)");
        setDef(lang, "help.furnace.unlink", "/v f u (x) - Remove the link from your furnace (x)");
        setDef(lang, "help.link.info", "See %1/v help link%2 for more information");
        setDef(lang, "help.brewingstand", "The brewing stand has exactly the same commands asthe furnace, just replace the \"f\" by a \"b\".");
        setDef(lang, "help.enchanttable.buy", "/v e buy - Buy an enchantmant table");
        setDef(lang, "help.enchanttable.use", "/v e - Open your enchantment table");
        setDef(lang, "help.enchanttable.book", "/v e buy b [x] - Buy [x] bookshelves");
        setDef(lang, "help.more", "More: %1");
        setDef(lang, "version", "VirtualPack %1");
        setDef(lang, "stats.usernotfound", "Can't find that user.");
        setDef(lang, "stats.wrong", "You're doing it wrong.");
        setDef(lang, "stats.allow", "You're no allowed to do that.");
        setDef(lang, "stats.workbench", "Workbench: %1%2");
        setDef(lang, "stats.uncrafter", "Uncrafter: %1%2");
        setDef(lang, "stats.enchanttable", "Enchantment Table: %1%2");
        setDef(lang, "stats.books", ", with %1 bookshelves");
        setDef(lang, "stats.chest", "Chests: %1%2");
        setDef(lang, "stats.furnace", "Furnaces: %1%2");
        setDef(lang, "stats.brewingstand", "Brewing Stands: %1%2");
        setDef(lang, "stats.link", "%1, free links: %2%3");
        setDef(lang, "use.player", "This command can only be used by a player.");
        setDef(lang, "use.perm", "You're not allowed to use VPack.");
        setDef(lang, "price.title", "----- ----- ----- Prices ----- ----- -----");
        setDef(lang, "price.workbench", "Workbench: %1Buy: %2%3 %1Use: %2%4");
        setDef(lang, "price.uncrafter", "Uncrafter: %1Buy: %2%3 %1Use: %2%4");
        setDef(lang, "price.chest", "Chest: %1Buy: %2%3 %1Use: %2%4");
        setDef(lang, "price.furnace", "Furnace: %1Buy: %2%3 %1Use: %2%4 %1Link: %2%5");
        setDef(lang, "price.brewingstand", "Brewing Stand: %1Buy: %2%3 %1Use: %2%4 %1Link: %2%5");
        setDef(lang, "price.enchanttable", "Enchantment Table: %1Buy: %2%3 %1Use: %2%4 %1Bookshelf: %2%5");
        setDef(lang, "price.free", "Economy is disabled, everything is for free, have fun! :)");
        setDef(lang, "yes", "yes");
        setDef(lang, "no", "no");
        setDef(lang, "money.toofew", "You don't have enough money.");
        setDef(lang, "argument.invalid", "Invalid argument.");
        setDef(lang, "argument.unknown", "Unknown argument.");
        setDef(lang, "argument.few", "Too few arguments.");
        setDef(lang, "argument.error", "Invalid command.");
        setDef(lang, "workbench.perm", "You're not allowed to use the workbench.");
        setDef(lang, "workbench.none", "You don't have a workbench.");
        setDef(lang, "workbench.max", "You already have a workbench.");
        setDef(lang, "workbench.bought", "You bought a workbench.");
        setDef(lang, "uncrafter.name", "Uncrafter");
        setDef(lang, "uncrafter.perm", "You're not allowed to use the uncrafter.");
        setDef(lang, "uncrafter.none", "You don't have an uncrafter.");
        setDef(lang, "uncrafter.max", "You already have an uncrafter.");
        setDef(lang, "uncrafter.bought", "You bought an uncrafter.");
        setDef(lang, "chest.name", "V Chest %1");
        setDef(lang, "chest.perm", "You're not allowed to use chests.");
        setDef(lang, "chest.none", "This chest doesn't exist.");
        setDef(lang, "chest.max", "You can't have more than %1 chests.");
        setDef(lang, "chest.bought.one", "You now have one chest.");
        setDef(lang, "chest.bought.many", "You now have %1 chests.");
        setDef(lang, "chest.trashed", "Trashed chest nr. %1");
        setDef(lang, "furnace.perm", "You're not allowed to use the furnace.");
        setDef(lang, "furnace.none", "This furnace doesn't exist.");
        setDef(lang, "furnace.max", "You can't have more than %1 furnaces.");
        setDef(lang, "furnace.bought.one", "You now have one furnace.");
        setDef(lang, "furnace.bought.many", "You now have %1 furnaces.");
        setDef(lang, "furnace.linked", "Linked furnace nr. %1 with chest nr. %2");
        setDef(lang, "furnace.nolink", "This furnace isn't linked.");
        setDef(lang, "furnace.unlinked", "Unlinked furnace nr. %1");
        setDef(lang, "brewingstand.perm", "You're not allowed to use the brewing stand.");
        setDef(lang, "brewingstand.none", "This brewing stand doesn't exist.");
        setDef(lang, "brewingstand.max", "You can't have more than %1 brewing stands.");
        setDef(lang, "brewingstand.bought.one", "You now have one brewing stand.");
        setDef(lang, "brewingstand.bought.many", "You now have %1 brewing stands.");
        setDef(lang, "brewingstand.linked", "Linked brewing stand nr. %1 with chest nr. %2");
        setDef(lang, "brewingstand.nolink", "This brewing stand isn't linked.");
        setDef(lang, "brewingstand.unlinked", "Unlinked brewing stand nr. %1");
        setDef(lang, "enchanttable.perm", "You're not allowed to use the enchantment table.");
        setDef(lang, "enchanttable.none", "You don't have an enchantment table.");
        setDef(lang, "enchanttable.max", "You already have an enchantment table.");
        setDef(lang, "enchanttable.bought", "You bought an enchantment table.");
        setDef(lang, "enchanttable.book.max", "You already have 30 bookshelves.");
        setDef(lang, "enchanttable.book.one", "You now have 1 bookshelf.");
        setDef(lang, "enchanttable.book.many", "You now have %1 bookshelves");
        setDef(lang, "admin.perm", "You're not allowed to do that!");
        setDef(lang, "admin.help.title", "----- ----- ----- Admin Help ----- ----- -----");
        setDef(lang, "admin.help.reload", "/v a reload - Reload the configs");
        setDef(lang, "admin.help.use", "/v a use (name) (action) - Access the VirtualPack of player 'name'. The 'action' parameter is what you normally type after '/v', e.g. 'c drop 5'");
        setDef(lang, "admin.help.give", "/v a give (name) (tool) [x] - Give [x] of a 'tool' to the player 'name'");
        setDef(lang, "admin.help.take", "/v a take (name) (tool) [x] - Take [x] of a 'tool' away from the player 'name'");
        setDef(lang, "admin.help.delete", "/v a delete (name) - Delete the VirtualPack of the player 'name'");
        setDef(lang, "admin.reloaded", "Reloaded the configs");
        setDef(lang, "admin.give.workbench.have", "This user already has a workbench.");
        setDef(lang, "admin.give.workbench.done", "Gave %1 a workbench.");
        setDef(lang, "admin.give.uncrafter.have", "This user already has an uncrafter.");
        setDef(lang, "admin.give.uncrafter.done", "Gave %1 an uncrafter.");
        setDef(lang, "admin.give.enchanttable.have", "This user already has an enchantment table.");
        setDef(lang, "admin.give.enchanttable.done", "Gave %1 an enchantment table.");
        setDef(lang, "admin.give.chest.one", "Gave %1 a chest.");
        setDef(lang, "admin.give.chest.many", "Gave %1 %2 chests.");
        setDef(lang, "admin.give.furnace.one", "Gave %1 a furnaces.");
        setDef(lang, "admin.give.furnace.many", "Gave %1 %2 furnaces.");
        setDef(lang, "admin.give.brewingstand.one", "Gave %1 a brewing stands.");
        setDef(lang, "admin.give.brewingstand.many", "Gave %1 %2 brewing stands.");
        setDef(lang, "admin.give.book.one", "Gave %1 a bookshelf.");
        setDef(lang, "admin.give.book.many", "Gave %1 %1 bookshelves.");
        setDef(lang, "admin.take.workbench.none", "This user doesn't have a workbench.");
        setDef(lang, "admin.take.workbench.done", "Took the workbench away from %1.");
        setDef(lang, "admin.take.uncrafter.none", "This user doesn't have an uncrafter.");
        setDef(lang, "admin.take.uncrafter.done", "Took the uncrafter away from %1.");
        setDef(lang, "admin.take.enchanttable.none", "This user doesn't have an enchantment table.");
        setDef(lang, "admin.take.enchanttable.done", "Tool the enchantment table away from %1.");
        setDef(lang, "admin.take.chest.one", "Took a chest away from %1.");
        setDef(lang, "admin.take.chest.many", "Took %2 chests away from %1.");
        setDef(lang, "admin.take.furnace.one", "Took a furnace away from %1.");
        setDef(lang, "admin.take.furnace.many", "Took %2 furnaces away from %1.");
        setDef(lang, "admin.take.brewingstand.one", "Took a brewing stand away from %1.");
        setDef(lang, "admin.take.brewingstand.many", "Took %2 brewing stands away from %1.");
        setDef(lang, "admin.take.book.one", "Took a bookshelf away from %1.");
        setDef(lang, "admin.take.book.many", "Took %2 bookshelves away from %1.");
        setDef(lang, "admin.delete", "Deleted %1's VirtualPack.");
        // setDef(lang, "", );
        try
        {
            lang.save(new File(getDataFolder(), "lang.yml"));
        }
        catch(Exception e)
        {
        }
    }
    
    private void setDef(FileConfiguration file, String path, String value)
    {
        if(!file.isSet(path))
        {
            file.set(path, value);
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
            PrintStream writer;
            File data;
            String files[] = new String[]{"config.yml", "lang.yml", "data.db"};
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
            log.warning("[VirtualPack] AN ERROR OCCURED! PLEASE SEND THE MESSAGE BELOW TO THE DEVELOPER!");
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
            log.warning("[VirtualPack] AN ERROR OCCURED! PLEASE SEND THE MESSAGE BELOW TO THE DEVELOPER!");
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
            if(!args[1].equals("use"))
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
            else
            {
                sendMessage(sender, lang("argument.unknown"), ChatColor.RED);
            }
        }
        catch(Exception e)
        {
            sendMessage(sender, lang("argument.error"), ChatColor.RED);
            log.warning("[VirtualPack] AN ERROR OCCURED! PLEASE SEND THE MESSAGE BELOW TO THE DEVELOPER!");
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
    
    public static String lang(String string, String replacements[])
    {
        string = lang(string);
        for(int i = 1; i <= replacements.length; i++)
        {
            string = string.replaceAll("%" + i, replacements[i - 1]);
        }
        return string;
    }
    
    public static String lang(String string)
    {
        if((lang != null) && (lang.isSet(string)))
        {
            return lang.getString(string);
        }
        return "STRING NOT FOUND";
    }
    
    public static int getConfigInt(String prefix, String suffix, CommandSender sender, boolean max)
    {
        String groups[] = perms.getPlayerGroups((CraftPlayer)sender);
        return getConfigInt(prefix, suffix, groups, max);
    }
    
    public static int getConfigInt(String prefix, String suffix, String groups[], boolean max)
    {
        int value = getConfigInt(prefix + "." + suffix);
        int tmp;
        for(int i = 0; i < groups.length; i++)
        {
            if(!config.isSet(prefix + "." + groups[i] + "." + suffix))
            {
                continue;
            }
            tmp = getConfigInt(prefix + "." + groups[i] + "." + suffix);
            if(((max) && (tmp > value)) || ((!max) && (tmp < value)))
            {
                value = tmp;
            }
        }
        return value;
    }
    
    public static int getConfigInt(String string)
    {
        try
        {
            return Integer.parseInt(config.getString(string));
        }
        catch(Exception e)
        {
            try
            {
                return (int)Math.round(Double.parseDouble(config.getString(string)));
            }
            catch(Exception e2)
            {
                return 0;
            }
        }
    }
    
    public static double getConfigDouble(String prefix, String suffix, CommandSender sender, boolean max)
    {
        return getConfigDouble(prefix, suffix, sender, max, 0);
    }
    
    public static double getConfigDouble(String prefix, String suffix, CommandSender sender, boolean max, int digits)
    {
        String groups[] = perms.getPlayerGroups((CraftPlayer)sender);
        return getConfigDouble(prefix, suffix, groups, max, digits);
    }
    
    public static double getConfigDouble(String prefix, String suffix, String groups[], boolean max)
    {
        return getConfigDouble(prefix, suffix, groups, max, 0);
    }
    
    public static double getConfigDouble(String prefix, String suffix, String groups[], boolean max, int digits)
    {
        double value = getConfigDouble(prefix + "." + suffix, digits);
        double tmp;
        for(int i = 0; i < groups.length; i++)
        {
            if(!config.isSet(prefix + "." + groups[i] + "." + suffix))
            {
                continue;
            }
            tmp = getConfigDouble(prefix + "." + groups[i] + "." + suffix, digits);
            if(((max) && (tmp > value)) || ((!max) && (tmp < value)))
            {
                value = tmp;
            }
        }
        return value;
    }
    
    public static double getConfigDouble(String string, int digits)
    {
        try
        {
            return Double.parseDouble(smoothDouble(Double.parseDouble(config.getString(string)), digits));
        }
        catch(Exception e)
        {
            return 0;
        }
    }
    
    public static String getConfigItemValue(String string, int id, int damage)
    {
        if(config.isSet(string + "." + id + "-" + damage))
        {
            return config.getString(string + "." + id + "-" + damage);
        }
        return config.getString(string + "." + id);
    }
    
    public static ItemStack getConfigItemStack(String string)
    {
        int id = getConfigInt(string + ".id");
        int amount = getConfigInt(string + ".amount");
        int meta = getConfigInt(string + ".meta");
        if((id < 0) || (id > 32000) || (Item.byId[id] == null))
        {
            return null;
        }
        if(amount <= 0)
        {
            amount = 1;
        }
        else if(amount > 64)
        {
            amount = 64;
        }
        if(meta < 0)
        {
            meta = 0;
        }
        return new ItemStack(id, amount, meta);
    }
}
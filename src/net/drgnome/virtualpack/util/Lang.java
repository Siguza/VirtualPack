// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.io.*;
import org.bukkit.configuration.file.*;

import static net.drgnome.virtualpack.util.Global.*;

public class Lang
{
    public static final String _langVersion = "5";
    private static YamlConfiguration _file = new YamlConfiguration();
    private static File _dir;
    
    public static void init()
    {
        _dir = _plugin.getDataFolder();
        reload();
    }
    
    public static void reload()
    {
        try
        {
            File file = new File(_dir, "lang.yml");
            boolean create = !file.exists();
            if(create)
            {
                file.getParentFile().mkdirs();
                PrintStream writer = new PrintStream(new FileOutputStream(file));
                writer.close();
            }
            _file.load(file);
            if(!create)
            {
                String lv = _file.getString("langv");
                if((lv == null) || (!lv.equalsIgnoreCase(_langVersion)))
                {
                    _file.save(new File(_dir, "lang" + _langVersion + ".yml"));
                    _file = new YamlConfiguration();
                }
            }
            setDefs();
            _file.save(file);
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
    }
    
    // Set all default values
    private static void setDefs()
    {
        setDef("langv", _langVersion);
        setDef("update.no", "VirtualPack is up to date! :)");
        setDef("update.perm", "You're not allowed to use this feature.");
        setDef("update.msg", "There is an update for VirtualPack available! Click here:");
        setDef("update.link", "http://dev.bukkit.org/server-mods/virtualpack/files/");
        setDef("vpack.ecodisabled", "Economy is disabled, you don't need to buy anything.");
        setDef("vpack.nocreative", "You can't access VirtualPack in creative mode.");
        setDef("vpack.misseco", "VirtualPack: Cannot find any vault-hooked economy plugin, disabling.");
        setDef("vpack.missperm", "VirtualPack: Cannot find any vault-hooked permissions plugin, disabling.");
        setDef("vpack.enable", "VirtualPack %1 enabled");
        setDef("vpack.disable", "VirtualPack %1 disabled");
        setDef("vpack.none", "This user doesn't have a VirtualPack.");
        setDef("help.uncrafter.title", "----- ----- ----- Uncrafter Guide ----- ----- -----");
        setDef("help.uncrafter.description", "The Uncrafter is the exact opposite of the workbench, it splits crafted items back into their crafting ingredients. To uncraft an item, open the Uncrafter and drag the item or stack you want to uncraft into the upper row.");
        setDef("help.link.title", "----- ----- ----- Linking Guide ----- ----- -----");
        setDef("help.link.description", "You can \"link\" a furnace or brewing stand with a chest. This means that whenever the furnace runs out of fuel or anything to smelt or the brewing stand runs out of ingredients, they will look into the specified chest and try to get things out of there that they can use. They will also put their smelting and brewing results in that chest if their \"output stack\" has reached the maximum stack size. Once you linked a furnace or brewing stand with a chest, you will always be able to link that furnace or brewing stand with an other chest for free. Also, for every unlink you do you'll be able to link a furnace or brewing stand for free. The number of \"free links\" is shown on the stats page.");
        setDef("help.link.note", "Note: You can NOT convert a furnace link into a brewing stand link or the other way round!");
        setDef("help.title", "----- ----- ----- VirtualPack Help ----- ----- -----");
        setDef("help.admin", "/%1 ad - Admin Help");
        setDef("help.args", "Arguments in () must be given, those in [] are optional and will usually be 1 if not given");
        setDef("help.commands", "/%1 help commands - Show the available commands");
        setDef("help.version", "/%1 v - Version");
        setDef("help.stats", "/%1 s - Show stats");
        setDef("help.price", "/%1 p - Show prices");
        setDef("help.send1", "/%1 send (player) - Send the item in your hand to a player");
        setDef("help.send2", "/%1 send (player) (x) - Send all the contents of your chest nr. x to a player");
        setDef("help.send3", "/%1 send -copy (player) [x] - Copy the item(s) and send it to a player");
        setDef("help.send4", "/%1 send -all [x] - Copy the item(s) and send it to ALL players");
        setDef("help.workbench.buy", "/%1 w buy - Buy a workbench for yourself");
        setDef("help.workbench.use", "/%1 w - Open your workbench");
        setDef("help.uncrafter.buy", "/%1 uc buy - Buy the Uncrafter");
        setDef("help.uncrafter.use", "/%1 uc - Open the Uncrafter (see %2/%1 help uc%3)");
        setDef("help.enchanttable.buy", "/%1 e buy - Buy an enchantmant table");
        setDef("help.enchanttable.use", "/%1 e - Open your enchantment table");
        setDef("help.enchanttable.book", "/%1 e buy b [x] - Buy [x] bookshelves");
        setDef("help.anvil.buy", "/%1 a buy - Buy the Anvil");
        setDef("help.anvil.use", "/%1 a - Open the Anvil");
        setDef("help.chest.buy", "/%1 c buy [x] - Buy [x] chest(s)");
        setDef("help.chest.use", "/%1 c [x] - Open your Chest (nr. x)");
        setDef("help.chest.drop", "/%1 c drop [x] - Drop the contents of chest (nr. x)");
        setDef("help.chest.trash", "/%1 c trash [x] - Delete all the contents of chest (nr. x)");
        setDef("help.furnace.buy", "/%1 f buy [x] - Buy [x] furnaces");
        setDef("help.furnace.use", "/%1 f [x] - Use your furnace (nr. x)");
        setDef("help.furnace.link", "/%1 f l (x) (y) - Link your furnace (x) with a chest (y)");
        setDef("help.furnace.unlink", "/%1 f u (x) - Remove the link from your furnace (x)");
        setDef("help.brewingstand.buy", "/%1 b buy [x] - Buy [x] brewing stands");
        setDef("help.brewingstand.use", "/%1 b [x] - Use your brewing stand (nr. x)");
        setDef("help.brewingstand.link", "/%1 b l (x) (y) - Link your brewing stand (x) with a chest (y)");
        setDef("help.brewingstand.unlink", "/%1 b u (x) - Remove the link from your brewing stand (x)");
        setDef("help.link.info", "See %2/%1 help link%3 for more information about links");
        setDef("help.trash", "/%1 t - Open the trash");
        setDef("help.more", "More: %1");
        setDef("version", "VirtualPack %1");
        setDef("stats.usernotfound", "Can't find that user.");
        setDef("stats.wrong", "You're doing it wrong.");
        setDef("stats.allow", "You're no allowed to do that.");
        setDef("stats.workbench", "Workbench: %1%2");
        setDef("stats.uncrafter", "Uncrafter: %1%2");
        setDef("stats.enchanttable", "Enchantment Table: %1%2");
        setDef("stats.anvil", "Anvil: %1%2");
        setDef("stats.books", ", with %1 bookshelves");
        setDef("stats.chest", "Chests: %1%2");
        setDef("stats.furnace", "Furnaces: %1%2");
        setDef("stats.brewingstand", "Brewing Stands: %1%2");
        setDef("stats.link", "%1, free links: %2%3");
        setDef("use.player", "This command can only be used by a player.");
        setDef("use.world", "You have to specify a world when using this command in the console.");
        setDef("use.perm", "You're not allowed to use VPack.");
        setDef("price.title", "----- ----- ----- Prices ----- ----- -----");
        setDef("price.workbench", "Workbench: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.uncrafter", "Uncrafter: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.enchanttable", "Enchantment Table: %1Buy: %2%3 %1Use: %2%4 %1Bookshelf: %2%5");
        setDef("price.anvil", "Anvil: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.chest", "Chest: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.furnace", "Furnace: %1Buy: %2%3 %1Use: %2%4 %1Link: %2%5");
        setDef("price.brewingstand", "Brewing Stand: %1Buy: %2%3 %1Use: %2%4 %1Link: %2%5");
        setDef("price.free", "Economy is disabled, everything is for free, have fun! :)");
        setDef("yes", "yes");
        setDef("no", "no");
        setDef("money.toofew", "You don't have enough money.");
        setDef("argument.invalid", "Invalid argument.");
        setDef("argument.unknown", "Unknown argument.");
        setDef("argument.few", "Too few arguments.");
        setDef("argument.error", "Invalid command.");
        setDef("workbench.perm", "You're not allowed to use the workbench.");
        setDef("workbench.none", "You don't have a workbench.");
        setDef("workbench.max", "You already have a workbench.");
        setDef("workbench.bought", "You bought a workbench.");
        setDef("uncrafter.name", "Uncrafter");
        setDef("uncrafter.perm", "You're not allowed to use the uncrafter.");
        setDef("uncrafter.none", "You don't have an uncrafter.");
        setDef("uncrafter.max", "You already have an uncrafter.");
        setDef("uncrafter.bought", "You bought an uncrafter.");
        setDef("anvil.perm", "You're not allowed to use the anvil.");
        setDef("anvil.none", "You don't have an anvil.");
        setDef("anvil.max", "You already have an anvil.");
        setDef("anvil.bought", "You bought an anvil.");
        setDef("chest.name", "V Chest %1");
        setDef("chest.perm", "You're not allowed to use chests.");
        setDef("chest.none", "This chest doesn't exist.");
        setDef("chest.max", "You can't have more than %1 chests.");
        setDef("chest.bought.one", "You now have one chest.");
        setDef("chest.bought.many", "You now have %1 chests.");
        setDef("chest.trashed", "Trashed chest nr. %1");
        setDef("furnace.perm", "You're not allowed to use the furnace.");
        setDef("furnace.none", "This furnace doesn't exist.");
        setDef("furnace.max", "You can't have more than %1 furnaces.");
        setDef("furnace.bought.one", "You now have one furnace.");
        setDef("furnace.bought.many", "You now have %1 furnaces.");
        setDef("furnace.linked", "Linked furnace nr. %1 with chest nr. %2");
        setDef("furnace.nolink", "This furnace isn't linked.");
        setDef("furnace.unlinked", "Unlinked furnace nr. %1");
        setDef("brewingstand.perm", "You're not allowed to use the brewing stand.");
        setDef("brewingstand.none", "This brewing stand doesn't exist.");
        setDef("brewingstand.max", "You can't have more than %1 brewing stands.");
        setDef("brewingstand.bought.one", "You now have one brewing stand.");
        setDef("brewingstand.bought.many", "You now have %1 brewing stands.");
        setDef("brewingstand.linked", "Linked brewing stand nr. %1 with chest nr. %2");
        setDef("brewingstand.nolink", "This brewing stand isn't linked.");
        setDef("brewingstand.unlinked", "Unlinked brewing stand nr. %1");
        setDef("enchanttable.perm", "You're not allowed to use the enchantment table.");
        setDef("enchanttable.none", "You don't have an enchantment table.");
        setDef("enchanttable.max", "You already have an enchantment table.");
        setDef("enchanttable.bought", "You bought an enchantment table.");
        setDef("enchanttable.book.max", "You already have 30 bookshelves.");
        setDef("enchanttable.book.one", "You now have 1 bookshelf.");
        setDef("enchanttable.book.many", "You now have %1 bookshelves");
        setDef("send.perm", "You're not allowed send items.");
        setDef("send.empty", "You can't send nothing.");
        setDef("send.done1", "The item has been sent to %1.");
        setDef("send.done2", "The items have been sent to %1.");
        setDef("send.get1", "%1 has sent you some items.");
        setDef("send.get2", "They have been put into the following chests: %1.");
        setDef("send.get3", "Some have also been dropped to you.");
        setDef("send.get4", "They have been dropped to you.");
        setDef("send.relieve", "Type '/%1' to stop this notifications.");
        setDef("admin.perm", "You're not allowed to do that!");
        setDef("admin.help.title", "----- ----- ----- Admin Help ----- ----- -----");
        setDef("admin.help.reload", "/%1 ad reload - Reload the configs");
        setDef("admin.help.save", "/%1 ad save - Manually save user data");
        setDef("admin.help.savefile", "/%1 ad savefile - Manually save user data to the data.db file");
        setDef("admin.help.loadfile", "/%1 ad loadfile - Force loading data form data.db file");
        setDef("admin.help.world", "/%1 ad w:(world) (use/give/take/delete) - Execute one of the commands below in a different world than the one you're in");
        setDef("admin.help.use", "/%1 ad use (name) (action) - Access the VirtualPack of player 'name'. The 'action' parameter is what you normally type after '/%1', e.g. 'c drop 5'");
        setDef("admin.help.give", "/%1 ad give (name) (tool) [x] - Give [x] of a 'tool' to the player 'name'");
        setDef("admin.help.take", "/%1 ad take (name) (tool) [x] - Take [x] of a 'tool' away from the player 'name'");
        setDef("admin.help.delete", "/%1 ad delete (name) - Delete the VirtualPack of the player 'name'");
        setDef("admin.reloaded", "Reloaded the configs.");
        setDef("admin.saved", "User data saved.");
        setDef("admin.loaded", "User data loaded, MySQL data saved to backup.db.");
        setDef("admin.mysql", "This command can only be used when using MySQL.");
        setDef("admin.give.workbench.have", "This user already has a workbench.");
        setDef("admin.give.workbench.done", "Gave %1 a workbench.");
        setDef("admin.give.uncrafter.have", "This user already has an uncrafter.");
        setDef("admin.give.uncrafter.done", "Gave %1 an uncrafter.");
        setDef("admin.give.enchanttable.have", "This user already has an enchantment table.");
        setDef("admin.give.enchanttable.done", "Gave %1 an enchantment table.");
        setDef("admin.give.anvil.have", "This user already has an anvil.");
        setDef("admin.give.anvil.done", "Gave %1 an anvil.");
        setDef("admin.give.chest.one", "Gave %1 a chest.");
        setDef("admin.give.chest.many", "Gave %1 %2 chests.");
        setDef("admin.give.furnace.one", "Gave %1 a furnace.");
        setDef("admin.give.furnace.many", "Gave %1 %2 furnaces.");
        setDef("admin.give.brewingstand.one", "Gave %1 a brewing stand.");
        setDef("admin.give.brewingstand.many", "Gave %1 %2 brewing stands.");
        setDef("admin.give.book.one", "Gave %1 a bookshelf.");
        setDef("admin.give.book.many", "Gave %1 %1 bookshelves.");
        setDef("admin.take.workbench.none", "This user doesn't have a workbench.");
        setDef("admin.take.workbench.done", "Took the workbench away from %1.");
        setDef("admin.take.uncrafter.none", "This user doesn't have an uncrafter.");
        setDef("admin.take.uncrafter.done", "Took the uncrafter away from %1.");
        setDef("admin.take.enchanttable.none", "This user doesn't have an enchantment table.");
        setDef("admin.take.enchanttable.done", "Tool the enchantment table away from %1.");
        setDef("admin.take.anvil.none", "This user doesn't have an anvil.");
        setDef("admin.take.anvil.done", "Took the anvil away from %1.");
        setDef("admin.take.chest.one", "Took a chest away from %1.");
        setDef("admin.take.chest.many", "Took %2 chests away from %1.");
        setDef("admin.take.furnace.one", "Took a furnace away from %1.");
        setDef("admin.take.furnace.many", "Took %2 furnaces away from %1.");
        setDef("admin.take.brewingstand.one", "Took a brewing stand away from %1.");
        setDef("admin.take.brewingstand.many", "Took %2 brewing stands away from %1.");
        setDef("admin.take.book.one", "Took a bookshelf away from %1.");
        setDef("admin.take.book.many", "Took %2 bookshelves away from %1.");
        setDef("admin.delete", "Deleted %1's VirtualPack.");
        setDef("trash.name", "V Trash");
        setDef("config.fail1", "[VirtualPack] World \"%1\" copies configuration of world \"%2\", but there is no config for this world!");
        setDef("config.fail2", "[VirtualPack] World \"%1\" copies configuration of world \"%2\" which results in a loop!");
        setDef("config.fail3", "[VirtualPack] Disabling VirtualPack on world \"%1\"!");
        setDef("worldedit", "[VirtualPack] Could not find WorldEdit!");
        setDef("virtualchest", "[VirtualPack] Could not find VirtualChest!");
        setDef("world.disabled", "VirtualPack is not enabled on this world.");
        setDef("command.error", "Internal error while executing command. See the console.");
        setDef("loading.single", "VirtualPack is currently reloading, please be patient.%1");
        setDef("loading.multi", " %1% done.");
        setDef("nocraftplayer", "Can't get the CraftBukkit player instance, sorry.");
    }
    
    // Set a default value
    private static void setDef(String path, String value)
    {
        if(!_file.isSet(path))
        {
            _file.set(path, value);
        }
    }
    
    public static String get(String string, String... replacements)
    {
        string = get(string);
        if(replacements != null)
        {
            for(int i = 1; i <= replacements.length; i++)
            {
                string = string.replaceAll("%" + i, replacements[i - 1]);
            }
        }
        return string;
    }
    
    public static String get(String string)
    {
        if((_file != null) && (_file.isSet(string)))
        {
            return _file.getString(string);
        }
        return "[VirtualPack] STRING NOT FOUND";
    }
}
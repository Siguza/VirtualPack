// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;

import org.bukkit.configuration.file.*;

import static net.drgnome.virtualpack.Util.*;

// Thought for static import
public class Lang
{
    public static final String langv = "2";
    private static YamlConfiguration config = new YamlConfiguration();
    private static File dir;
    
    public static void initLang(File folder)
    {
        dir = folder;
        reloadLang();
    }
    
    public static void reloadLang()
    {
        try
        {
            File file = new File(dir, "lang.yml");
            if(!file.exists())
            {
                file.mkdirs();
                PrintStream writer = new PrintStream(new FileOutputStream(file));
                writer.close();
            }
            config.load(file);
            String lv = config.getString("langv");
            if((lv == null) || (!lv.equalsIgnoreCase(langv)))
            {
                config.save(new File(dir, "lang" + langv + ".yml"));
                config = new YamlConfiguration();
            }
            setDefs();
            config.save(file);
        }
        catch(Exception e)
        {
            warn();
            e.printStackTrace();
        }
    }
    
    // Set all default values
    private static void setDefs()
    {
        setDef("langv", langv);
        setDef("vpack.ecodisabled", "Economy is disabled, you don't need to buy anything.");
        setDef("vpack.misseco", "VirtualPack: Cannot find any vault-hooked economy plugin, disabling.");
        setDef("vpack.missperm", "VirtualPack: Cannot find any vault-hooked permissions plugin, disabling.");
        setDef("vpack.enable", "VirtualPack %1 enabled");
        setDef("vpack.startdisable", "Disabling VirtualPack %1...");
        setDef("vpack.disable", "VirtualPack %1 disabled");
        setDef("vpack.none", "This user doesn't have a VirtualPack.");
        setDef("help.uncrafter.title", "----- ----- ----- Uncrafter Guide ----- ----- -----");
        setDef("help.uncrafter.description", "The Uncrafter is the exact opposite of the workbench, it splits crafted items back into their crafting ingredients. To uncraft an item, open the Uncrafter and drag the item or stack you want to uncraft into the upper row.");
        setDef("help.link.title", "----- ----- ----- Linking Guide ----- ----- -----");
        setDef("help.link.description", "You can \"link\" a furnace or brewing stand with a chest. This means that whenever the furnace runs out of fuel or anything to smelt or the brewing stand runs out of ingredients, they will look into the specified chest and try to get things out of there that they can use. They will also put their smelting and brewing results in that chest if their \"output stack\" has reached the maximum stack size. Once you linked a furnace or brewing stand with a chest, you will always be able to link that furnace or brewing stand with an other chest for free. Also, for every unlink you do you'll be able to link a furnace or brewing stand for free. The number of \"free links\" is shown on the stats page.");
        setDef("help.link.note", "Note: You can NOT convert a furnace link into a brewing stand link or the other way round!");
        setDef("help.title", "----- ----- ----- Help Page %1/%2 ----- ----- -----");
        setDef("help.admin", "/v a - Admin Help");
        setDef("help.args", "Arguments in () must be given, those in [] are optional and will usually be 1 if not given");
        setDef("help.help", "/v help [x] - Show help (page x)");
        setDef("help.version", "/v v - Version");
        setDef("help.stats", "/v s - Show stats");
        setDef("help.price", "/v p - Show prices");
        setDef("help.workbench.buy", "/v w buy - Buy a workbench for yourself");
        setDef("help.workbench.use", "/v w - Open your workbench");
        setDef("help.uncrafter.buy", "/v uc buy - Buy the Uncrafter");
        setDef("help.uncrafter.use", "/v uc - Open the Uncrafter (see %1/v help uc%2)");
        setDef("help.chest.buy", "/v c buy [x] - Buy [x] chest(s)");
        setDef("help.chest.use", "/v c [x] - Open your Chest (nr. x)");
        setDef("help.chest.drop", "/v c drop [x] - Drop the contents of chest (nr. x)");
        setDef("help.chest.trash", "/v c trash [x] - Delete all the contents of chest (nr. x)");
        setDef("help.furnace.buy", "/v f buy [x] - Buy [x] furnaces");
        setDef("help.furnace.use", "/v f [x] - Use your furnace (nr. x)");
        setDef("help.furnace.link", "/v f l (x) (y) - Link your furnace (x) with a chest (y)");
        setDef("help.furnace.unlink", "/v f u (x) - Remove the link from your furnace (x)");
        setDef("help.link.info", "See %1/v help link%2 for more information");
        setDef("help.brewingstand", "The brewing stand has exactly the same commands asthe furnace, just replace the \"f\" by a \"b\".");
        setDef("help.enchanttable.buy", "/v e buy - Buy an enchantmant table");
        setDef("help.enchanttable.use", "/v e - Open your enchantment table");
        setDef("help.enchanttable.book", "/v e buy b [x] - Buy [x] bookshelves");
        setDef("help.more", "More: %1");
        setDef("help.debug", "/v d - Debug Help");
        setDef("version", "VirtualPack %1");
        setDef("stats.usernotfound", "Can't find that user.");
        setDef("stats.wrong", "You're doing it wrong.");
        setDef("stats.allow", "You're no allowed to do that.");
        setDef("stats.workbench", "Workbench: %1%2");
        setDef("stats.uncrafter", "Uncrafter: %1%2");
        setDef("stats.enchanttable", "Enchantment Table: %1%2");
        setDef("stats.books", ", with %1 bookshelves");
        setDef("stats.chest", "Chests: %1%2");
        setDef("stats.furnace", "Furnaces: %1%2");
        setDef("stats.brewingstand", "Brewing Stands: %1%2");
        setDef("stats.link", "%1, free links: %2%3");
        setDef("use.player", "This command can only be used by a player.");
        setDef("use.perm", "You're not allowed to use VPack.");
        setDef("price.title", "----- ----- ----- Prices ----- ----- -----");
        setDef("price.workbench", "Workbench: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.uncrafter", "Uncrafter: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.chest", "Chest: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.furnace", "Furnace: %1Buy: %2%3 %1Use: %2%4 %1Link: %2%5");
        setDef("price.brewingstand", "Brewing Stand: %1Buy: %2%3 %1Use: %2%4 %1Link: %2%5");
        setDef("price.enchanttable", "Enchantment Table: %1Buy: %2%3 %1Use: %2%4 %1Bookshelf: %2%5");
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
        setDef("admin.perm", "You're not allowed to do that!");
        setDef("admin.help.title", "----- ----- ----- Admin Help ----- ----- -----");
        setDef("admin.help.reload", "/v a reload - Reload the configs");
        setDef("admin.help.use", "/v a use (name) (action) - Access the VirtualPack of player 'name'. The 'action' parameter is what you normally type after '/v', e.g. 'c drop 5'");
        setDef("admin.help.give", "/v a give (name) (tool) [x] - Give [x] of a 'tool' to the player 'name'");
        setDef("admin.help.take", "/v a take (name) (tool) [x] - Take [x] of a 'tool' away from the player 'name'");
        setDef("admin.help.delete", "/v a delete (name) - Delete the VirtualPack of the player 'name'");
        setDef("admin.reloaded", "Reloaded the configs.");
        setDef("admin.saved", "User data saved.");
        setDef("admin.give.workbench.have", "This user already has a workbench.");
        setDef("admin.give.workbench.done", "Gave %1 a workbench.");
        setDef("admin.give.uncrafter.have", "This user already has an uncrafter.");
        setDef("admin.give.uncrafter.done", "Gave %1 an uncrafter.");
        setDef("admin.give.enchanttable.have", "This user already has an enchantment table.");
        setDef("admin.give.enchanttable.done", "Gave %1 an enchantment table.");
        setDef("admin.give.chest.one", "Gave %1 a chest.");
        setDef("admin.give.chest.many", "Gave %1 %2 chests.");
        setDef("admin.give.furnace.one", "Gave %1 a furnaces.");
        setDef("admin.give.furnace.many", "Gave %1 %2 furnaces.");
        setDef("admin.give.brewingstand.one", "Gave %1 a brewing stands.");
        setDef("admin.give.brewingstand.many", "Gave %1 %2 brewing stands.");
        setDef("admin.give.book.one", "Gave %1 a bookshelf.");
        setDef("admin.give.book.many", "Gave %1 %1 bookshelves.");
        setDef("admin.take.workbench.none", "This user doesn't have a workbench.");
        setDef("admin.take.workbench.done", "Took the workbench away from %1.");
        setDef("admin.take.uncrafter.none", "This user doesn't have an uncrafter.");
        setDef("admin.take.uncrafter.done", "Took the uncrafter away from %1.");
        setDef("admin.take.enchanttable.none", "This user doesn't have an enchantment table.");
        setDef("admin.take.enchanttable.done", "Tool the enchantment table away from %1.");
        setDef("admin.take.chest.one", "Took a chest away from %1.");
        setDef("admin.take.chest.many", "Took %2 chests away from %1.");
        setDef("admin.take.furnace.one", "Took a furnace away from %1.");
        setDef("admin.take.furnace.many", "Took %2 furnaces away from %1.");
        setDef("admin.take.brewingstand.one", "Took a brewing stand away from %1.");
        setDef("admin.take.brewingstand.many", "Took %2 brewing stands away from %1.");
        setDef("admin.take.book.one", "Took a bookshelf away from %1.");
        setDef("admin.take.book.many", "Took %2 bookshelves away from %1.");
        setDef("admin.delete", "Deleted %1's VirtualPack.");
        setDef("debug.help.groups1", "/v d groups - List the groups you're in.");
        setDef("debug.help.groups2", "/v d groups [name] - List the groups the player [name] is in.");
        setDef("debug.perm", "You're not allowed to use debug commands.");
        setDef("debug.groups", "Groups (%1):");
    }
    
    // Set a default value
    private static void setDef(String path, String value)
    {
        if(!config.isSet(path))
        {
            config.set(path, value);
        }
    }
    
    public static String lang(String string, String... replacements)
    {
        string = lang(string);
        if(replacements != null)
        {
            for(int i = 1; i <= replacements.length; i++)
            {
                string = string.replaceAll("%" + i, replacements[i - 1]);
            }
        }
        return string;
    }
    
    public static String lang(String string)
    {
        if((config != null) && (config.isSet(string)))
        {
            return config.getString(string);
        }
        return "[VirtualPack] STRING NOT FOUND";
    }
}
// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.data;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class ChestKeeperHelper
{
    private static Field[] _fields = null;

    public static void check()
    {
        File chests = new File(_plugin.getDataFolder(), "chestkeeper");
        if(chests.exists() && chests.isDirectory())
        {
            boolean found = false;
            for(File file : _plugin.getDataFolder().listFiles())
            {
                if(!file.isFile())
                {
                    continue;
                }
                String name = file.getName().toLowerCase();
                if(name.contains("chestkeeper") && name.substring(name.length() - 4).equals(".jar"))
                {
                    found = true;
                    if(!Util.loadJar(file))
                    {
                        return;
                    }
                }
            }
            if(!found)
            {
                _log.severe(Lang.get(null, "chestkeeper"));
                return;
            }
            load(chests);
        }
    }

    private static void load(File dataFolder)
    {
        _log.info("[VirtualPack] Converting ChestKeeper database...");
        if(_fields == null)
        {
            try
            {
                _fields = new Field[2];
                _fields[0] = Class.forName("com.koletar.jj.chestkeeper.CKUser").getDeclaredField("chests");
                _fields[1] = Class.forName("com.koletar.jj.chestkeeper.CKChest").getDeclaredField("contents");
                for(Field f : _fields)
                {
                    f.setAccessible(true);
                }
            }
            catch(Exception e)
            {
                _log.severe("[VirtualPack] Failed to load ChestKeeper database!");
                e.printStackTrace();
                return;
            }
        }
        String world = Config.string("import-world").length() > 0 ? Config.string("import-world") : "*";
        for(File chestFile : dataFolder.listFiles())
        {
			String chestFileName = chestFile.getName();
			if(!chestFileName.endsWith(".yml"))
            {
                continue;
            }
            String playerName = chestFileName.substring(0, chestFile.getName().length() - 4);
            VInv[] invs = loadFile(chestFile, _plugin.getPack(world, playerName).getChestSize());
            if(invs == null)
            {
                _log.warning("[VirtualPack] Couldn't load ChestKeeper chest file: " + chestFileName);
                continue;
            }
            for(VInv inv : invs)
            {
                _plugin.getPack(world, playerName).addInv(inv);
            }
            _log.info("[VirtualPack] (ChestKeeper) Loaded " + playerName + "'s chests");
		}
        try
        {
            dataFolder.renameTo(new File(dataFolder.getParentFile(), "chestkeeper_old"));
        }
        catch(Exception e)
        {
            _log.warning("[VirtualPack] Couldn't rename ChestKeeper data folder!");
        }
        _log.info("[VirtualPack] ChestKeeper data loaded.");
    }

    private static VInv[] loadFile(File file, int chestSize)
    {
        try
        {
            ArrayList<VInv> invlist = new ArrayList<VInv>();
            for(Object chest : ((Map)_fields[0].get(YamlConfiguration.loadConfiguration(file).get("user"))).values())
            {
                ArrayList<ItemStack> itemlist = new ArrayList<ItemStack>();
                for(org.bukkit.inventory.ItemStack stack : (org.bukkit.inventory.ItemStack[])_fields[1].get(chest))
                {
                    itemlist.add(CraftItemStack.asNMSCopy(stack));
                }
                invlist.add(new VInv(chestSize, itemlist.toArray(new ItemStack[0])));
            }
            return invlist.toArray(new VInv[0]);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}

// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.data;

import java.io.*;
import java.util.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class BackpackHelper
{
    public static void check()
    {
        File packs = new File(_plugin.getDataFolder(), "backpacks");
        if(packs.exists() && packs.isDirectory())
        {
            for(File world : packs.listFiles())
            {
                if(world.isDirectory())
                {
                    load(world);
                }
            }
            try
            {
                packs.renameTo(new File(packs.getParentFile(), "backpacks_old"));
            }
            catch(Exception e)
            {
                _log.warning("[VirtualPack] Couldn't rename Backpack data folder!");
            }
            _log.info("[VirtualPack] Backpack data loaded.");
        }
    }

    private static void load(File dataFolder)
    {
        String world = dataFolder.getName();
        _log.info("[VirtualPack] Converting Backpack database \"" + world + "\"...");
        for(File chestFile : dataFolder.listFiles())
        {
			String chestFileName = chestFile.getName();
			if(!chestFileName.endsWith(".yml"))
            {
                continue;
            }
            String playerName = chestFileName.substring(0, chestFile.getName().length() - 4);
            VInv inv = loadFile(chestFile, _plugin.getPack(world, playerName).getChestSize());
            if(inv == null)
            {
                _log.warning("[VirtualPack] Couldn't load Backpack file: " + world + "/" + chestFileName);
                continue;
            }
            _plugin.getPack(world, playerName).addInv(inv);
            _log.info("[VirtualPack] (Backpack) Loaded " + playerName + "'s backpack in world " + world);
		}
    }

    private static VInv loadFile(File file, int chestSize)
    {
        try
        {
            ItemStack[] items = new ItemStack[chestSize * 9];
            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
            ConfigurationSection map = (ConfigurationSection)config.get("backpack");
            for(String key : map.getKeys(false).toArray(new String[0]))
            {
                try
                {
                    items[Integer.parseInt(key.substring(5))] = CraftItemStack.asNMSCopy(map.getItemStack(key + ".ItemStack"));
                }
                catch(Exception e1)
                {
                    continue;
                }
            }
            return new VInv(chestSize, items);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}

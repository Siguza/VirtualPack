// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.data;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import net.minecraft.server.v1_5_R2.*;
import org.bukkit.inventory.Inventory;
import net.drgnome.virtualpack.*;
import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class AlphaChestHelper
{
    public static void check()
    {
        File chests = new File(_plugin.getDataFolder(), "chests");
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
                if(name.contains("alphachest") && name.substring(name.length() - 4).equals(".jar"))
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
                _log.severe(Lang.get("alphachest"));
                return;
            }
            load(chests);
        }
    }
    
    private static void load(File dataFolder)
    {
        _log.info("[VirtualPack] Converting AlphaChest database...");
        String world = Config.string("import-world").length() > 0 ? Config.string("import-world") : "*";
        try
        {
            Class clazz = Class.forName("net.sradonia.bukkit.alphachest.VirtualChestManager");
            Constructor c = clazz.getConstructor(File.class, Logger.class);
            c.setAccessible(true);
            Field f = clazz.getDeclaredField("chests");
            f.setAccessible(true);
            HashMap<String, Inventory> map = (HashMap<String, Inventory>)f.get(c.newInstance(dataFolder, _log));
            for(Map.Entry<String, Inventory> entry : map.entrySet())
            {
                _plugin.getPack(world, entry.getKey()).addInv(entry.getValue());
                _log.info("[VirtualPack] (AlphaChest) Loaded " + entry.getKey() + "'s chest");
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        try
        {
            dataFolder.renameTo(new File(dataFolder.getParentFile(), "chests_old"));
        }
        catch(Throwable t)
        {
            _log.warning("[VirtualPack] Couldn't rename AlphaChest data folder!");
        }
        _log.info("[VirtualPack] AlphaChest data loaded.");
	}
}
// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.data;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.drgnome.virtualpack.components.VInv;
import net.drgnome.virtualpack.util.Config;
import net.drgnome.virtualpack.util.Lang;
import net.drgnome.virtualpack.util.Util;
import net.minecraft.server.v1_5_R2.ItemStack;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
import static net.drgnome.virtualpack.util.Global._plugin;
import static net.drgnome.virtualpack.util.Global._log;

public class VirtualChestHelper
{
    private static Method _method1;
    private static Method _method2;
    
    public static void check()
    {
        File chests = new File(_plugin.getDataFolder(), "vchests");
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
                if(name.contains("virtualchest") && name.substring(name.length() - 4).equals(".jar"))
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
                _log.severe(Lang.get("virtualchest"));
                return;
            }
            load(chests);
        }
    }
    
    private static void load(File dataFolder)
    {
        _log.info("[VirtualPack] Converting VirtualChest database...");
        try
        {
            Method m = Class.forName("com.zone.vchest.tools.config.file.ExtendedConfiguration").getDeclaredMethod("setClassLoader", ClassLoader.class);
            m.setAccessible(true);
            m.invoke(null, _plugin.getClass().getClassLoader());
            _method1 = Class.forName("com.zone.vchest.tools.config.file.ExtendedConfiguration").getDeclaredMethod("loadConfiguration", File.class);
            _method1.setAccessible(true);
            _method2 = Class.forName("com.aranai.virtualchest.ItemStackSave").getDeclaredMethod("getItemStack");
            _method2.setAccessible(true);
        }
        catch(Throwable t)
        {
            _log.severe("[VirtualPack] Failed to load VirtualChest database!");
            t.printStackTrace();
            return;
        }
        String world = Config.string("import-world").length() > 0 ? Config.string("import-world") : "*";
        for(File chestFile : dataFolder.listFiles())
        {
			String chestFileName = chestFile.getName();
			if(!chestFileName.endsWith(".chestYml"))
            {
                continue;
            }
            String playerName = chestFileName.substring(0, chestFile.getName().length() - 9);
            VInv[] invs = loadFile(chestFile, _plugin.getPack(world, playerName).getChestSize());
            if(invs == null)
            {
                _log.warning("[VirtualPack] Couldn't load VirtualChest chest file: " + chestFileName);
                continue;
            }
            for(VInv inv : invs)
            {
                _plugin.getPack(world, playerName).addInv(inv);
            }
            _log.info("[VirtualPack] (VirtualChest) Loaded " + playerName + "'s chests");
		}
        try
        {
            dataFolder.renameTo(new File(dataFolder.getParentFile(), "vchests_old"));
        }
        catch(Throwable t)
        {
            _log.warning("[VirtualPack] Couldn't rename VirtualChest data folder!");
        }
        _log.info("[VirtualPack] VirtualChest data loaded.");
    }
    
    private static VInv[] loadFile(File file, int chestSize)
    {
        try
        {
            ArrayList<VInv> invlist = new ArrayList<VInv>();
            ConfigurationSection yaml = (ConfigurationSection)_method1.invoke(null, file);
            for(ConfigurationSection section : yaml.getValues(false).values().toArray(new ConfigurationSection[0]))
            {
                ArrayList<ItemStack> itemlist = new ArrayList<ItemStack>();
                ArrayList list = (ArrayList)section.get("eitems");
                for(Object obj : list.toArray())
                {
                    itemlist.add(CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack)_method2.invoke(obj)));
                }
                invlist.add(new VInv(chestSize, itemlist.toArray(new ItemStack[0])));
            }
            return invlist.toArray(new VInv[0]);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        return null;
    }
}
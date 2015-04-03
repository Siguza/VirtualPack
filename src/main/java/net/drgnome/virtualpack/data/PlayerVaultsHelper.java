package net.drgnome.virtualpack.data;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.inventory.Inventory;
import net.drgnome.virtualpack.*;
import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class PlayerVaultsHelper
{
    private static final String _className = "com.drtshock.playervaults.vaultmanagement.Serialization";
    private static Method _method = null;

    public static void check()
    {
        File vaults = new File(_plugin.getDataFolder(), "uuidvaults");
        if(vaults.exists() && vaults.isDirectory())
        {
            if(_method == null)
            {
                boolean found = false;
                Class clazz = null;
                try
                {
                    clazz = Class.forName(_className);
                    found = true;
                }
                catch(ClassNotFoundException e)
                {
                    for(File file : _plugin.getDataFolder().listFiles())
                    {
                        if(!file.isFile())
                        {
                            continue;
                        }
                        String name = file.getName().toLowerCase();
                        if(name.contains("playervaults") && name.substring(name.length() - 4).equals(".jar"))
                        {
                            if(!Util.loadJar(file))
                            {
                                return;
                            }
                            try
                            {
                                clazz = Class.forName(_className);
                                found = true;
                            }
                            catch(ClassNotFoundException e1)
                            {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
                if(!found)
                {
                    _log.severe(Lang.get(null, "playervaults"));
                    return;
                }
                try
                {
                    Method m = clazz.getDeclaredMethod("toInventory", List.class, int.class, int.class);
                    m.setAccessible(true);
                    _method = m;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    return;
                }
            }
            _log.info("[VirtualPack] Converting PlayerVaults database...");
            String world = Config.string("import-world");
            world = world.length() > 0 ? world : "*";
            for(File file : vaults.listFiles(new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.isFile() && pathname.getName().endsWith(".yml");
                }
            }))
            {
                try
                {
                    loadVault(world, file);
                }
                catch(Exception e)
                {
                    _log.warning("[VirtualPack] Failed to convert file: " + file.getName());
                    e.printStackTrace();
                }
            }
            vaults.renameTo(new File(vaults.getParentFile(), "uuidvaults_old"));
            _log.info("[VirtualPack] PlayerVaults data loaded.");
        }
    }

    private static void loadVault(String world, File file) throws FileNotFoundException, IOException, InvalidConfigurationException
    {
        String fileName = file.getName();
        String name = Bukkit.getOfflinePlayer(UUID.fromString(fileName.substring(0, fileName.length() - 4))).getName();
        _log.info("[VirtualPack] (PlayerVaults) Converting " + name + "'s vault...");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(file);
        VPack pack = _plugin.getPack(world, name);
        Object[] vaults = yaml.getValues(false).values().toArray(new Object[0]);
        for(int i = 0; i < vaults.length; i++)
        {
            try
            {
                Map<String, Object> map = ((MemorySection)vaults[i]).getValues(false);
                String[] keys = map.keySet().toArray(new String[0]);
                //Set<Map.Entry<String, Object>> entries = map.entrySet();
                ArrayList<String> list = new ArrayList<String>();
                int size = Integer.parseInt(keys[keys.length - 1]) + 1;
                for(int j = 0; j < size; j++)
                {
                    Object o = map.get(((Integer)j).toString());
                    list.add(o == null ? "null" : (String)o);
                }
                pack.addInv((Inventory)_method.invoke(null, list, i, size));
                //ItemStack[] items = new ItemStack[Integer.parseInt(keys[keys.length - 1]) + 1];
                /*for(Map.Entry<String, Object> entry : entries)
                {
                    try
                    {
                        items[Integer.parseInt(entry.getKey())] = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack)entry.getValue());
                    }
                    catch(ClassCastException e)
                    {
                        e.printStackTrace();
                    }
                    catch(NumberFormatException e)
                    {
                        e.printStackTrace();
                    }
                }*/
                //pack.addInv(new VInv((int)Math.ceil(((float)items.length) / 9F), items));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

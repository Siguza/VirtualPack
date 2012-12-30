// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;
import javax.xml.bind.DatatypeConverter;
import net.minecraft.server.v#MC_VERSION#.*;
import static net.drgnome.virtualpack.util.Global.*;

public class Util
{
    private static boolean _lastStack;
    private static String[] _lastStackIds;
    
    // Math.round? Too damn slow!
    public static int round(double d)
    {
        int i = (int)d;
        d -= i;
        return i + (d >= 0.5 ? 1 : (d <= -0.5 ? -1 : 0));
    }
    
    // Same for Math.floor
    public static int floor(double d)
    {
        return d < 0 ? (int)d - 1 : (int)d;
    }
    
    public static boolean hasUpdate(String name, String version)
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection)(new URL("http://dev.drgnome.net/version.php?t=" + name)).openConnection();            
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
            String vdigits[] = version.toLowerCase().split("\\.");
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
                    return true;
                }
                else if(a < b)
                {
                    return false;
                }
                else if((i == max - 1) && (cdigits.length > vdigits.length))
                {
                    return true;
                }
            }
        }
        catch(Throwable t)
        {
        }
        return false;
    }
    
    public static String[] copy(String[] string, int start)
    {
        String[] array = new String[string.length - start];
        for(int i = start; i < string.length; i++)
        {
            array[i - start] = string[i];
        }
        return array;
    }
    
    public static boolean areEqual(ItemStack item1, ItemStack item2)
    {
        return (item1.id == item2.id) && (item1.count == item2.count) && (item1.getData() == item2.getData());
    }
    
    public static int tryParse(String s, int i)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch(Throwable t)
        {
            return i;
        }
    }
    
    public static double tryParse(String s, double d)
    {
        try
        {
            return Double.parseDouble(s);
        }
        catch(Throwable t)
        {
            return d;
        }
    }
    
    public static String implode(String glue, String... parts)
    {
        if((glue == null) || (parts.length <= 0))
        {
            return "";
        }
        String string = parts[0];
        for(int i = 1; i < parts.length; i++)
        {
            string += glue + parts[i];
        }
        return string;
    }
    
    public static double smooth(double d, int digits)
    {
        double factor = Math.pow(10, digits);
        return round(d * factor) / factor;
    }
    
    public static int max(int... values)
    {
        int tmp = values[0];
        for(int i = 1; i < values.length; i++)
        {
            if(values[i] > tmp)
            {
                tmp = values[i];
            }
        }
        return tmp;
    }
    
    public static int min(int... values)
    {
        int tmp = values[0];
        for(int i = 1; i < values.length; i++)
        {
            if(values[i] < tmp)
            {
                tmp = values[i];
            }
        }
        return tmp;
    }
    
    // null.cloneItemStack throws a NullPointerException, therefore:
    public static ItemStack copy(ItemStack item)
    {
        return item == null ? null : item.cloneItemStack();
    }
    
    public static ItemStack[] copy(ItemStack item[])
    {
        ItemStack it[] = new ItemStack[item.length];
        for(int i = 0; i < it.length; i++)
        {
            it[i] = copy(item[i]);
        }
        return it;
    }
    
    public static ItemStack stringToItemStack(String string)
    {
        if((string == null) || (string.length() == 0))
        {
            return null;
        }
        return ItemStack.#FIELD_ITEMSTACK_1#(NBTCompressedStreamTools.#FIELD_NBTCOMPRESSEDSTREAMTOOLS_1#(DatatypeConverter.parseBase64Binary(string)));
    }
    
    public static String itemStackToString(ItemStack item)
    {
        if(item == null)
        {
            return "";
        }
        return DatatypeConverter.printBase64Binary(NBTCompressedStreamTools.#FIELD_NBTCOMPRESSEDSTREAMTOOLS_2#(item.save(new NBTTagCompound())));
    }
    
    public static ItemStack[] stack(ItemStack item1, ItemStack item2)
    {
        _lastStack = false;
        if(!areEqual(item1, item2))
        {
            return new ItemStack[]{item1, item2};
        }
        if(item1 == null)
        {
            _lastStack = true;
            return new ItemStack[]{item2, null};
        }
        int max = (item2.count > (item1.getMaxStackSize() - item1.count)) ? (item1.getMaxStackSize() - item1.count) : item2.count;
        if(max <= 0)
        {
            return new ItemStack[]{item1, item2};
        }
        _lastStack = true;
        item1.count += max;
        item2.count -= max;
        return new ItemStack[]{item1, (item2.count <= 0) ? null : item2};
    }
    
    public static ItemStack[] stack(IInventory[] invs, ItemStack... items)
    {
        boolean[] stacked = new boolean[invs.length];
        ArrayList<ItemStack> left = new ArrayList<ItemStack>();
        for(ItemStack item : items)
        {
            for(int j = 0; j < invs.length; j++)
            {
                IInventory inv = invs[j];
                ItemStack[] contents = inv.getContents();
                stacked[j] = false;
                for(int i = 0; i < contents.length; i++)
                {
                    ItemStack[] tmp = stack(contents[i], item);
                    inv.setItem(i, tmp[0]);
                    item = tmp[1];
                    stacked[j] = stacked[j] || _lastStack;
                    if(item == null)
                    {
                        break;
                    }
                }
                if(item == null)
                {
                    break;
                }
            }
            if(item != null)
            {
                left.add(item);
            }
        }
        ArrayList<String> touched = new ArrayList<String>();
        for(int i = 0; i < stacked.length; i++)
        {
            if(stacked[i])
            {
                touched.add("" + i);
            }
        }
        _lastStackIds = touched.toArray(new String[0]);
        return left.toArray(new ItemStack[0]);
    }
    
    public static String[] getLastStackingIds()
    {
        return _lastStackIds;
    }
    
    public static void openWindow(EntityPlayer player, Container container, String name, int id, int size)
    {
        player.playerConnection.sendPacket(new Packet100OpenWindow(1, id, name, size));
        player.activeContainer = container;
        container.windowId = 1;
        container.addSlotListener((ICrafting)player);
    }
    
    public static boolean loadJar(File file)
    {
        ClassLoader loader = _plugin.getClass().getClassLoader();
        if(loader instanceof URLClassLoader)
        {
            try
            {
                URLClassLoader cl = (URLClassLoader)loader;
                Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                m.setAccessible(true);
                m.invoke(cl, file.toURI().toURL());
            }
            catch(Throwable t1)
            {
                warn();
                t1.printStackTrace();
                return false;
            }
        }
        else
        {
            warn();
            _log.severe("[VirtualPack] PluginClassLoader not URLClassLoader!");
            return false;
        }
        return true;
    }
    
    public static String base64en(String string)
    {
        return DatatypeConverter.printBase64Binary(string.getBytes());
    }
    
    public static String base64de(String string)
    {
        return new String(DatatypeConverter.parseBase64Binary(string));
    }
}
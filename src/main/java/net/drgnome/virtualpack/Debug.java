// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;
import java.util.*;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class Debug
{
    private static boolean _initialized = false;
    private static File _logFile = null;
    private static File _dataFile = null;
    private static HashMap<String, String> _chestMap = new HashMap<String, String>();
    
    public static void init()
    {
        if(!Config.bool("debug-log") || _initialized)
        {
            return;
        }
        _initialized = true;
        Runtime.getRuntime().addShutdownHook(new FinishHook());
        _logFile = new File(_plugin.getDataFolder(), "debug.log");
        _dataFile = new File(_plugin.getDataFolder(), "debug.sjo");
        try
        {
            _chestMap = (HashMap<String, String>)(new ObjectInputStream(new FileInputStream(_dataFile)).readObject());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static void save()
    {
        if(!_initialized)
        {
            return;
        }
        try
        {
            new ObjectOutputStream(new FileOutputStream(_dataFile)).writeObject(_chestMap);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void loadChest(String world, String user, int k, String data)
    {
        if(!_initialized)
        {
            return;
        }
        String id = world + ":" + user + ":" + k;
        String old = _chestMap.get(id);
        if((old != null) && !old.equals(data))
        {
            try
            {
                Calendar c = new GregorianCalendar();
                BufferedWriter w = new BufferedWriter(new FileWriter(_logFile, true));
                w.write("[" + c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR) + " " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND) + "] Chest data mismatch on " + id + "! Data is printed below.");
                w.newLine();
                w.write(Util.base64en(old));
                w.newLine();
                w.write(Util.base64en(data));
                w.newLine();
                w.write("========== ========== ========== END OF REPORT ========== ========== ==========");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        _chestMap.put(id, data);
    }
    
    public static void saveChest(String world, String user, int k, String data)
    {
        if(!_initialized)
        {
            return;
        }
        _chestMap.put(world + ":" + user + ":" + k, data);
    }
    
    private static class FinishHook extends Thread
    {
        public void run()
        {
            save();
        }
    }
}
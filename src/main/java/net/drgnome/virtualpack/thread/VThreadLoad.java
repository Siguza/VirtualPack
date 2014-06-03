// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.thread;

import java.util.*;
import net.drgnome.virtualpack.*;
import static net.drgnome.virtualpack.util.Global.*;

public class VThreadLoad extends Thread
{
    public static boolean _debug;
    public static int _num;
    public static int _total;
    private String[] _data;
    private boolean _lazy;

    public VThreadLoad(String[] data, boolean lazy)
    {
        _data = data;
        _lazy = lazy;
    }

    public void run()
    {
        try
        {
            _plugin.setPack(_data[0], _data[1], new VPack(_data[0], _data[1], _data[2], _lazy));
            if(_debug)
            {
                System.out.println("[VPack/Debug] Loaded pack " + (_num++) + "/" + _total);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        synchronized(_plugin._loadThreads)
        {
            _plugin._loadThreads.remove(this);
            _plugin._numActiveLoadThreads--;
        }
    }
}

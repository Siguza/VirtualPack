// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.thread;

import java.util.*;
import net.drgnome.virtualpack.*;
import static net.drgnome.virtualpack.util.Global.*;

public class VThreadLoad extends Thread
{
    String[] _data;
    
    public VThreadLoad(String[] data)
    {
        _data = data;
    }
    
    public void run()
    {
        try
        {
            _plugin.setPack(_data[0], _data[1], new VPack(_data[0], _data[1], _data[2].split(_separator[0])));
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        _plugin._loadThreads.remove(this);
    }
}
// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.thread;

import static net.drgnome.virtualpack.util.Global.*;

public class VThreadWait extends Thread
{
    public void run()
    {
        try
        {
            if(((Boolean)(Class.forName("org.anjocaido.groupmanager.GroupManager").getMethod("isLoaded").invoke(null))).booleanValue())
            {
                _plugin.init();
            }
        }
        catch(ClassNotFoundException e)
        {
            _plugin.init();
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
    }
}
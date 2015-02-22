// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.thread;

import java.lang.reflect.Method;
import org.bukkit.scheduler.BukkitRunnable;
import static net.drgnome.virtualpack.util.Global.*;

public class VThreadWait extends BukkitRunnable
{
    private final Method _m;

    public VThreadWait(Method m)
    {
        _m = m;
    }

    public void run()
    {
        try
        {
            if(((Boolean)(_m.invoke(null))).booleanValue())
            {
                _plugin.init();
            }
        }
        catch(Exception e)
        {
            warn();
            e.printStackTrace();
        }
    }
}

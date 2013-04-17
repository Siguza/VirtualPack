// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.thread;

import static net.drgnome.virtualpack.util.Global.*;

public class VThreadAnnoy extends Thread
{
    public void run()
    {
        _plugin.runAnnoy();
    }
}
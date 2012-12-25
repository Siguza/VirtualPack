// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

public class VThread extends Thread
{
    private VPlugin plugin;
    
    public VThread(VPlugin plugin)
    {
        super();
        this.plugin = plugin;
    }
    
    public void run()
    {
        if(plugin != null)
        {
            plugin.tick();
        }
    }
}
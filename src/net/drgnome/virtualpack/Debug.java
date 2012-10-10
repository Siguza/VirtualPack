// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;

import static net.drgnome.virtualpack.Config.*;
import static net.drgnome.virtualpack.Util.*;

public class Debug
{
    private static BufferedWriter writer;
    
    public static void init(File file)
    {
        try
        {
            writer = new BufferedWriter(new FileWriter(file));
        }
        catch(Throwable t)
        {
            log.severe("[VirtualPack] Debugging failed:");
            t.printStackTrace();
        }
    }
    
    public static void log(String string)
    {
        if(writer == null)
        {
            return;
        }
        try
        {
            writer.write(string);
            writer.newLine();
        }
        catch(Throwable t)
        {
            log.severe("[VirtualPack] Debugging failed:");
            t.printStackTrace();
        }
    }
    
    public static void end()
    {
        if(writer == null)
        {
            return;
        }
        try
        {
            writer.close();
        }
        catch(Throwable t)
        {
            log.severe("[VirtualPack] Debugging failed:");
            t.printStackTrace();
        }
    }
}
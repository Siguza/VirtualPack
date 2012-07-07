// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;
import java.util.*;

import static net.drgnome.virtualpack.Util.*;

public class VThreadSave extends Thread
{
    private File file;
    private HashMap<String, VPack> packs;
    private boolean done;
    
    public VThreadSave(File file, HashMap<String, VPack> packs)
    {
        super();
        this.file = file;
        this.packs = packs;
        this.done = false;
    }
    
    public void run()
    {
        try
        {
            Object key[] = this.packs.keySet().toArray();
            String name;
            VPack vpack;
            String contents;
            String data[];
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for(int i = 0; i < key.length; i++)
            {
                name = (String)key[i];
                vpack = packs.get(name);
                if(vpack != null)
                {
                    contents = name;
                    data = vpack.save();
                    for(int j = 0; j < data.length; j++)
                    {
                        contents += separator[0] + data[j];
                    }
                    writer.write(contents);
                    writer.newLine();
                }
            }
            writer.close();
        }
        catch(Exception e)
        {
            warn();
            e.printStackTrace();
        }
        this.done = true;
        return;
    }
    
    public boolean done()
    {
        return this.done;
    }
}
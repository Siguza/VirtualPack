// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;
import java.sql.*;
import java.util.*;

import static net.drgnome.virtualpack.Util.*;

public class VThreadSave extends Thread
{
    private boolean mysql;
    private File file;
    private Connection db;
    private HashMap<String, VPack> packs;
    private boolean done;
    
    public VThreadSave(File file, HashMap<String, VPack> packs)
    {
        super();
        this.mysql = false;
        this.file = file;
        this.packs = packs;
        this.done = false;
    }
    
    public VThreadSave(Connection db, HashMap<String, VPack> packs)
    {
        super();
        this.mysql = true;
        this.db = db;
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
            ArrayList<String> list = new ArrayList<String>();
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
                    list.add(contents);
                }
            }
            if(mysql)
            {
                db.prepareStatement("DELETE FROM `vpack`").execute();
                for(String con : list.toArray(new String[0]))
                {
                    PreparedStatement query = db.prepareStatement("INSERT INTO `vpack` (`data`) VALUES(?)");
                    query.setString(1, con);
                    query.execute();
                }
            }
            else
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for(String con : list.toArray(new String[0]))
                {
                    writer.write(con);
                    writer.newLine();
                }
                writer.close();
            }
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
        this.done = true;
        return;
    }
    
    public boolean done()
    {
        return this.done;
    }
}
// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.thread;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.drgnome.virtualpack.*;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class VThreadSave extends Thread
{
    private boolean _mysql;
    private File _file;
    private ConcurrentHashMap<String, ConcurrentHashMap<UUID, VPack>> _packs;
    
    public VThreadSave(File file, ConcurrentHashMap<String, ConcurrentHashMap<UUID, VPack>> packs)
    {
        super();
        _mysql = false;
        _file = file;
        _packs = packs;
    }
    
    public VThreadSave(ConcurrentHashMap<String, ConcurrentHashMap<UUID, VPack>> packs)
    {
        super();
        _mysql = true;
        _packs = packs;
    }
    
    public void run()
    {
        try
        {
            ArrayList<String[]> list = new ArrayList<String[]>();
            for(Map.Entry<String, ConcurrentHashMap<UUID, VPack>> entry1 : _packs.entrySet())
            {
                String world = entry1.getKey();
                for(Map.Entry<UUID, VPack> entry2 : entry1.getValue().entrySet())
                {
                    list.add(new String[]{world, "*" + entry2.getKey().toString(), entry2.getValue().save()});
                }
            }
            String[][] data = list.toArray(new String[0][]);
            if(_mysql)
            {
                Connection db = DriverManager.getConnection(Config.string("db.url"), Config.string("db.user"), Config.string("db.pw"));
                String table = Config.string("db.table");
                try
                {
                    ResultSet row = db.prepareStatement("SELECT * FROM `" + table + "`").executeQuery();
                    row.getString("world");
                    db.prepareStatement("DELETE FROM `" + table + "`").execute();
                }
                catch(SQLException e)
                {
                    db.prepareStatement("DROP TABLE `" + table + "`").execute();
                    db.prepareStatement("CREATE TABLE IF NOT EXISTS `" + table + "` (`world` varchar(255) NOT NULL, `user` varchar(255) NOT NULL, `data` longtext NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;").execute();
                }
                for(String[] line : data)
                {
                    PreparedStatement query = db.prepareStatement("INSERT INTO `" + table + "` (`world`, `user`, `data`) VALUES(?, ?, ?)");
                    query.setString(1, line[0]);
                    query.setString(2, line[1]);
                    query.setString(3, line[2]);
                    query.execute();
                }
                db.close();
            }
            else
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(_file));
                for(String[] line : data)
                {
                    writer.write(Util.implode(_separator[4], line));
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
    }
    
    public boolean done()
    {
        return this.getState() == State.TERMINATED;
    }
}
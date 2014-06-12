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
                // The "commented out version" does not work because it does not remove packs that do no longer exist.
                // If this can be solved other than by the statement in line 59, implement it below and remove line 59. 
                Connection db = DriverManager.getConnection(Config.string("db.url"), Config.string("db.user"), Config.string("db.pw"));
                String table = Config.string("db.table");
                try
                {
                    db.prepareStatement("SELECT `world` FROM `" + table + "` LIMIT 1").execute();
                    db.prepareStatement("DELETE FROM `" + table + "`").execute();
                }
                catch(SQLException e)
                {
                    db.prepareStatement("DROP TABLE `" + table + "`").execute();
                    db.prepareStatement("CREATE TABLE `" + table + "` (`world` varchar(255) NOT NULL, `user` varchar(255) NOT NULL, `data` longtext NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;").execute();
                }
                /*PreparedStatement check = db.prepareStatement("SELECT COUNT(`user`) as `num` FROM `" + table + "` WHERE `world` = ? AND `user` = ?");
                PreparedStatement update = db.prepareStatement("UPDATE `" + table + "` SET `data` = ? WHERE `world` = ? AND `user` = ?");*/
                PreparedStatement insert = db.prepareStatement("INSERT INTO `" + table + "` (`world`, `user`, `data`) VALUES(?, ?, ?)");
                for(String[] line : data)
                {
                    /*check.setString(1, line[0]);
                    check.setString(2, line[1]);
                    if(check.executeQuery().getInt("num") > 0)
                    {
                        update.setString(1, line[2]);
                        update.setString(2, line[0]);
                        update.setString(3, line[1]);
                        update.execute();
                    }
                    else
                    {*/
                        insert.setString(1, line[0]);
                        insert.setString(2, line[1]);
                        insert.setString(3, line[2]);
                        insert.execute();
                    //}
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
        catch(Exception e)
        {
            warn();
            e.printStackTrace();
        }
    }

    public boolean done()
    {
        return this.getState() == State.TERMINATED;
    }
}

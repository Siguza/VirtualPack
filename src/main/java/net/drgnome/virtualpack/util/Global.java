// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import net.drgnome.virtualpack.VPlugin;

public class Global
{
    public static final String _separator[] = {":", new String(new char[]{(char)17}), new String(new char[]{(char)18}), new String(new char[]{(char)19}), new String(new char[]{(char)20})};
    public static VPlugin _plugin;
    public static Logger _log = Logger.getLogger("Minecraft");
    
    // These 3 methods split up strings into multiple lines so that the message doesn't get messed up by the minecraft chat.
    // You can also give a prefix that is set before every line.
    public static void sendMessage(CommandSender sender, String message)
    {
        sendMessage(sender, message, ChatColor.WHITE);
    }
    
    public static void sendMessage(CommandSender sender, String message, ChatColor prefix)
    {
        sendMessage(sender, message, "" + prefix);
    }
    
    public static void sendMessage(CommandSender sender, String message, String prefix)
    {
        if((sender == null) || (message == null))
        {
            return;
        }
        if(prefix == null)
        {
            prefix = "";
        }
        if(sender instanceof ConsoleCommandSender)
        {
            sender.sendMessage(prefix + message);
            return;
        }
        int offset = 0;
        int xpos = 0;
        int pos = 0;
        String part;
        while(true)
        {
            if(offset + 60 >= message.length())
            {
                sender.sendMessage(prefix + message.substring(offset).trim());
                break;
            }
            part = message.substring(offset, offset + 60);
            xpos = part.lastIndexOf(" ");
            pos = xpos < 0 ? 60 : xpos;
            part = message.substring(offset, offset + pos).trim();
            sender.sendMessage(prefix + part);
            offset += pos + (xpos < 0 ? 0 : 1);
        }
    }
    
    public static void warn()
    {
        _log.warning("[VirtualPack] AN ERROR OCCURED! PLEASE SEND THE MESSAGE BELOW TO THE DEVELOPER!");
    }
}
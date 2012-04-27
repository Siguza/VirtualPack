// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import net.minecraft.server.*;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Util
{
    public static final String LS = System.getProperty("line.separator");
    public static final String separator[] = {":", new String(new char[]{(char)17}), new String(new char[]{(char)18}), new String(new char[]{(char)19}), new String(new char[]{(char)20})};
    
    // Get the smallest value out of a bunch of integers
    public static int min(int... params)
    {
        // If there are no params, what are we doing here?
        if(params.length <= 0)
        {
            return 0;
        }
        int min = params[0];
        // Start by 1 because 0 is already picked
        for(int i = 1; i < params.length; i++)
        {
            if(params[i] < min)
            {
                min = params[i];
            }
        }
        return min;
    }
    
    public static ItemStack stringToItemStack(String string)
    {
        String parts[] = string.split(separator[2]);
        if(parts.length < 3)
        {
            return null;
        }
        try
        {
            // I'm not using "tryParse" here because I don't want to get 0 as ID or amount
            int id = Integer.parseInt(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            int damage = Integer.parseInt(parts[2]);
            if(parts.length < 4)
            {
                return new ItemStack(id, amount, damage);
            }
            NBTTagList list = new NBTTagList();
            NBTTagCompound com;
            String ench[] = parts[3].split(separator[3]);
            for(int i = 0; i < ench.length; i++)
            {
                String e[] = ench[i].split(separator[4]);
                if(e.length < 1)
                {
                    continue;
                }
                com = new NBTTagCompound();
                com.setShort("id", Short.parseShort(e[0]));
                com.setShort("lvl", Short.parseShort(e[1]));
                list.add(com);
            }
            return new ItemStack(id, amount, damage, list);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public static String itemStackToString(ItemStack item)
    {
        if(item == null)
        {
            return "";
        }
        String string = Integer.toString(item.id) + separator[2] + Integer.toString(item.count) + separator[2] + Integer.toString(item.getData()) + separator[2];
        NBTTagList list = item.getEnchantments();
        if(list != null)
        {
            NBTTagCompound com;
            for(int i = 0; i < list.size(); i++)
            {
                com = (NBTTagCompound)list.get(i);
                if(com != null)
                {
                    string += Short.toString(com.getShort("id")) + separator[4] + Short.toString(com.getShort("lvl"));
                    if(i < list.size() - 1)
                    {
                        string += separator[3];
                    }
                }
            }
        }
        return string;
    }
    
    public static String smoothDouble(double d, int digits)
    {
        if(digits > 0)
        {
            String temp = "" + (int)Math.round(d * Math.pow(10, digits));
            if(digits > temp.length())
            {
                digits = temp.length();
            }
            return (digits == temp.length() ? "0" : "") + temp.substring(0, temp.length() - digits) + "." + temp.substring(temp.length() - digits, temp.length());
        }
        return "" + d;
    }
    
    // null.cloneItemStack doesn't throws a NullPointerException, therefore:
    public static ItemStack copy(ItemStack item)
    {
        return item == null ? null : item.cloneItemStack();
    }
    
    public static ItemStack[] copy(ItemStack item[])
    {
        ItemStack it[] = new ItemStack[item.length];
        for(int i = 0; i < it.length; i++)
        {
            it[i] = copy(item[i]);
        }
        return it;
    }
    
    public static void sendMessage(CommandSender sender, String message)
    {
        sendMessage(sender, message, "");
    }
    
    public static void sendMessage(CommandSender sender, String message, ChatColor prefix)
    {
        sendMessage(sender, message, "" + prefix);
    }
    
    public static void sendMessage(CommandSender sender, String message, String prefix)
    {
        int offset = 0;
        int xpos = 0;
        int pos = 0;
        String part;
        while(true)
        {
            if(offset + 60 >= message.length())
            {
                sender.sendMessage(prefix + message.substring(offset, message.length()));
                break;
            }
            part = message.substring(offset, offset + 60);
            xpos = part.lastIndexOf(" ");
            pos = xpos < 0 ? 60 : xpos;
            part = message.substring(offset, offset + pos);
            sender.sendMessage(prefix + part);
            offset += pos + (xpos < 0 ? 0 : 1);
        }
    }
    
    // Those two functions save a lot of code
    public static int tryParse(String s, int i)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch(Exception e)
        {
            return i;
        }
    }
    
    public static double tryParse(String s, double d)
    {
        try
        {
            return Double.parseDouble(s);
        }
        catch(Exception e)
        {
            return d;
        }
    }
}
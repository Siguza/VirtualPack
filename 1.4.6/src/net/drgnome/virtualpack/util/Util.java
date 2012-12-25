// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.util;

import java.io.*;
import java.net.*;

public class Util
{
    // Math.round? Too damn slow!
    public static int round(double d)
    {
        int i = (int)d;
        d -= i;
        return i + (d >= 0.5 ? 1 : (d <= -0.5 ? -1 : 0));
    }
    
    public static boolean hasUpdate(String name, String version)
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection)(new URL("http://dev.drgnome.net/version.php?t=" + name)).openConnection();            
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JVM)");                        
            con.setRequestProperty("Pragma", "no-cache");
            con.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            StringBuilder stringb = new StringBuilder();
            if((line = reader.readLine()) != null)
            {
                stringb.append(line);
            }
            String vdigits[] = version.toLowerCase().split("\\.");
            String cdigits[] = stringb.toString().toLowerCase().split("\\.");
            int max = vdigits.length > cdigits.length ? cdigits.length : vdigits.length;
            int a = 0;
            int b = 0;
            for(int i = 0; i < max; i++)
            {
                try
                {
                    a = Integer.parseInt(cdigits[i]);
                }
                catch(Throwable t1)
                {
                    char c[] = cdigits[i].toCharArray();
                    for(int j = 0; j < c.length; j++)
                    {
                        a += (c[j] << ((c.length - (j + 1)) * 8));
                    }
                }
                try
                {
                    b = Integer.parseInt(vdigits[i]);
                }
                catch(Throwable t1)
                {
                    char c[] = vdigits[i].toCharArray();
                    for(int j = 0; j < c.length; j++)
                    {
                        b += (c[j] << ((c.length - (j + 1)) * 8));
                    }
                }
                if(a > b)
                {
                    return true;
                }
                else if(a < b)
                {
                    return false;
                }
                else if((i == max - 1) && (cdigits.length > vdigits.length))
                {
                    return true;
                }
            }
        }
        catch(Throwable t)
        {
        }
        return false;
    }
    
    public static String[] copy(String[] string, int start)
    {
        String[] array = new String[string.length - start];
        for(int i = start; i < string.length; i++)
        {
            array[i - start] = string[i];
        }
        return array;
    }
    
    public static int tryParse(String s, int i)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch(Throwable t)
        {
            return i;
        }
    }
    
    public static String implode(String glue, String... parts)
    {
        if((glue == null) || (parts.length <= 0))
        {
            return "";
        }
        String string = parts[0];
        for(int i = 1; i < parts.length; i++)
        {
            string += glue + parts[i];
        }
        return string;
    }
    
    public static double smooth(double d, int digits)
    {
        double factor = Math.pow(10, digits);
        return Math.round(d * factor) / factor;
    }
}
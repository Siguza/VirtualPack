package net.drgnome.virtualpack.util;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class ObfuscationHelper 
{
	private static HashMap<String, String> mappings = new HashMap<String, String>();
	
	public static void registerMappings(String content)
	{
		String[] lines;
		int c = 0;
		if(!content.contains("\n"))
			lines = new String[] { content };
		else
			lines = content.split("\n");
		for(String line : lines)
		{
			if(!line.contains(" "))
				continue;
			String[] parts = line.split(" ");
			mappings.put(parts[0], parts[1]);
			++c;
		}
		Bukkit.getLogger().log(Level.INFO, "[VirtualPack] " + c + " mappings registered!");
	}
	
	public static String getMapping(String string)
	{
		if(!mappings.containsKey(string))
		{
			Bukkit.getLogger().log(Level.WARNING, "[VIRTUALPACK] Mapping " + string + " not found!");
			return string;
		}
		return mappings.get(string);
	}
}

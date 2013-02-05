// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.data;

import java.util.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import static net.drgnome.virtualpack.util.Global.*;

public class TransmutationListener extends PacketAdapter
{
    public static void register()
    {
        ProtocolLibrary.getProtocolManager().addPacketListener(new TransmutationListener());
    }
    
    public TransmutationListener()
    {
        super(_plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST, Packets.getServerRegistry().values());
    }
    
    public void onPacketSending(PacketEvent event)
    {
        if((event == null) || event.isCancelled() || (event.getPacket() == null))
        {
            return;
        }
        StructureModifier<ItemStack> mod1 = event.getPacket().getItemModifier();
        if(mod1 != null)
        {
            try
            {
                for(int i = 0; i < mod1.size(); i++)
                {
                    applyChange(mod1.readSafely(i));
                    //mod1.writeSafely(i, applyChange(mod1.readSafely(i)));
                }
            }
            catch(Throwable t)
            {
                _log.info("[VirtualPack] Nothing severe, but can't modify outgoing item stacks. (1)");
                t.printStackTrace();
            }
        }
        StructureModifier<ItemStack[]> mod2 = event.getPacket().getItemArrayModifier();
        if(mod2 != null)
        {
            try
            {
                for(int i = 0; i < mod2.size(); i++)
                {
                    applyChange(mod2.readSafely(i));
                    //mod2.writeSafely(i, applyChange(mod2.readSafely(i)));
                }
            }
            catch(Throwable t)
            {
                _log.info("[VirtualPack] Nothing severe, but can't modify outgoing item stacks. (2)");
                t.printStackTrace();
            }
        }
    }
    
    private ItemStack[] applyChange(ItemStack[] origItems)
    {
        if(origItems == null)
        {
            return null;
        }
        ItemStack[] items = new ItemStack[origItems.length];
        for(int i = 0; i < items.length; i++)
        {
            items[i] = applyChange(origItems[i]);
        }
        return items;
    }
    
    private ItemStack applyChange(ItemStack origItem)
    {
        if(origItem == null)
        {
            return null;
        }
        //ItemStack item = ItemStack.deserialize(origItem.serialize());
        ItemStack item = origItem.clone();
        System.out.println(item == origItem ? "yes" : "no");
        //ItemMeta meta = item.getItemMeta();
        ItemMeta meta = item.getItemMeta().clone();
        System.out.println(meta == origItem.getItemMeta() ? "ja" : "nein");
        List<String> lore = meta.hasLore() ? meta.getLore() : (new ArrayList<String>());
        lore.add("Le Test");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
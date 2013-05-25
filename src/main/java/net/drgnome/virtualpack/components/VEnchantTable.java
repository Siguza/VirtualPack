// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import java.lang.reflect.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.CraftServer;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.*;

public class VEnchantTable extends ContainerEnchantTable
{
    private Random rand = new Random();
    private int bookshelves;
    
    public VEnchantTable(EntityPlayer player, int bookshelves)
    {
        super(player.inventory, player.world, 0, 0, 0);
        this.checkReachable = false;
        this.bookshelves = bookshelves;
    }
    
    public void #FIELD_CONTAINER_6#(IInventory iinventory)
    {
        if(iinventory == this.enchantSlots)
        {
            ItemStack itemstack = iinventory.getItem(0);
            if(itemstack != null && itemstack.#FIELD_ITEMSTACK_2#())
            {
                this.#FIELD_CONTAINERENCHANTTABLE_3# = this.rand.nextLong();
                for(int i = 0; i < 3; ++i)
                {
                    this.costs[i] = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_1#(this.rand, i, this.bookshelves, itemstack);
                }
                if(Config.bool("events.use"))
                {
                    // CraftBukkit start
                    CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);
                    Player player = getPlayer();
                    PrepareItemEnchantEvent event = new PrepareItemEnchantEvent(player, this.getBukkitView(), player.getWorld().getBlockAt(0, 0, 0), item, this.costs, this.bookshelves);
                    ((CraftServer)Bukkit.getServer()).getPluginManager().callEvent(event);
                    if(event.isCancelled() && !Config.bool("events.ignorecancelled"))
                    {
                        for(int i = 0; i < 3; ++i)
                        {
                            this.costs[i] = 0;
                        }
                        return;
                    }
                    // CraftBukkit end
                }
                this.#FIELD_CONTAINERENCHANTTABLE_4#();
            }
            else
            {
                for(int i = 0; i < 3; ++i)
                {
                    this.costs[i] = 0;
                }
            }
        }
    }
    
    public boolean #FIELD_CONTAINERENCHANTTABLE_2#(EntityHuman entityhuman, int i)
    {
        ItemStack itemstack = this.enchantSlots.getItem(0);
        if(this.costs[i] > 0 && itemstack != null && (entityhuman.expLevel >= this.costs[i] || entityhuman.abilities.canInstantlyBuild || Perm.has(entityhuman.world.getWorld().getName(), entityhuman.name, "vpack.use.enchanttable.free")))
        {
            List list = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_2#(this.rand, itemstack, this.costs[i]);
            boolean flag = itemstack.id == Item.BOOK.id;
            if(list != null)
            {
                // CraftBukkit start
                Map<org.bukkit.enchantments.Enchantment, Integer> enchants = new HashMap<org.bukkit.enchantments.Enchantment, Integer>();
                for(Object obj : list)
                {
                    EnchantmentInstance instance = (EnchantmentInstance)obj;
                    enchants.put(org.bukkit.enchantments.Enchantment.getById(instance.enchantment.id), instance.level);
                }
                int level;
                Map<org.bukkit.enchantments.Enchantment, Integer> map;
                CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);
                if(Config.bool("events.use"))
                {
                    EnchantItemEvent event = new EnchantItemEvent(getPlayer(), this.getBukkitView(), getPlayer().getWorld().getBlockAt(0, 0, 0), item, this.costs[i], enchants, i);
                    ((CraftServer)Bukkit.getServer()).getPluginManager().callEvent(event);
                    level = event.getExpLevelCost();
                    if(event.isCancelled() && !Config.bool("events.ignorecancelled"))
                    {
                        return false;
                    }
                    map = event.getEnchantsToAdd();
                }
                else
                {
                    level = costs[i];
                    map = enchants;
                }
                if((level > entityhuman.expLevel && !entityhuman.abilities.canInstantlyBuild && !Perm.has(entityhuman.world.getWorld().getName(), entityhuman.name, "vpack.use.enchanttable.free")) || enchants.isEmpty())
                {
                    return false;
                }
                boolean applied = !flag;
                for(Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : map.entrySet())
                {
                    try
                    {
                        if(flag)
                        {
                            int enchantId = entry.getKey().getId();
                            if(Enchantment.byId[enchantId] == null)
                            {
                                continue;
                            }
                            EnchantmentInstance enchantment = new EnchantmentInstance(enchantId, entry.getValue());
                            Item.ENCHANTED_BOOK.#FIELD_ITEMENCHANTEDBOOK_1#(itemstack, enchantment);
                            applied = true;
                            itemstack.id = Item.ENCHANTED_BOOK.id;
                            break;
                        }
                        else
                        {
                            item.addEnchantment(entry.getKey(), entry.getValue());
                        }
                    }
                    catch(IllegalArgumentException e)
                    {
                        /* Just swallow invalid enchantments */
                    }
                }
                // Only down level if we've applied the enchantments
                if(applied && !entityhuman.abilities.canInstantlyBuild && !Perm.has(entityhuman.world.getWorld().getName(), entityhuman.name, "vpack.use.enchanttable.free"))
                {
                    entityhuman.levelDown(-level);
                }
                // CraftBukkit end
                this.#FIELD_CONTAINER_6#(this.enchantSlots);
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private Player getPlayer()
    {
        try
        {
            Field f = ContainerEnchantTable.class.getDeclaredField("player");
            f.setAccessible(true);
            return (Player)f.get(this);
        }
        catch(Throwable t)
        {
            return null;
        }
    }
}
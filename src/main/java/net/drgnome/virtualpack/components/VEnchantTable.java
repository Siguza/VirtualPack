// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import java.lang.reflect.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
//import org.bukkit.event.enchantment.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.CraftServer;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.*;

public class VEnchantTable extends ContainerEnchantTable
{
    private final EntityPlayer mcPlayer;
    private final Player player;
    private final World world;
    private final Random rand = new Random();
    private final int bookshelves;

    ---------- SINCE 1.9 START ----------
    ---------- PRE 1.13 START ----------
    private static int getEnchId(Enchantment ench)
    {
        return Enchantment.getId(ench);
    }
    ---------- PRE 1.13 END ----------
    ---------- SINCE 1.9 END ----------

    ---------- SINCE 1.13 START ----------
    ---------- PRE 1.13.1 START ----------
    private static int getEnchId(Enchantment ench)
    {
        return Enchantment.enchantments.#F_ENCH_GETID#(ench);
    }
    ---------- PRE 1.13.1 END ----------
    ---------- SINCE 1.13 END ----------

    ---------- SINCE 1.13.1 START ----------
    private static int getEnchId(Enchantment ench)
    {
        return IRegistry.ENCHANTMENT.#F_ENCH_GETID#(ench);
    }
    ---------- SINCE 1.13.1 END ----------

    private static org.bukkit.enchantments.Enchantment getBukkitEnch(int id)
    {
        ---------- PRE 1.13 START ----------
        return org.bukkit.enchantments.Enchantment.getById(id);
        ---------- PRE 1.13 END ----------

        ---------- SINCE 1.13 START ----------
        ---------- PRE 1.13.1 START ----------
        return org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.craftbukkit.v#MC_VERSION#.util.CraftNamespacedKey.fromMinecraft(Enchantment.getId(Enchantment.enchantments.getId(id))));
        ---------- PRE 1.13.1 END ----------
        ---------- SINCE 1.13 END ----------
        
        ---------- SINCE 1.13.1 START ----------
        return org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.craftbukkit.v#MC_VERSION#.util.CraftNamespacedKey.fromMinecraft(IRegistry.ENCHANTMENT.getKey(IRegistry.ENCHANTMENT.fromId(id))));
        ---------- SINCE 1.13.1 END ----------
    }

    public VEnchantTable(EntityPlayer player, int bookshelves)
    {
        super(player.inventory, player.world, new BlockPosition(0, 0, 0));
        this.checkReachable = false;
        this.bookshelves = bookshelves;
        this.mcPlayer = player;
        this.player = (Player)player.getBukkitEntity();
        this.world = player.world;
    }

    public final ItemStack #FIELD_CONTAINER_11#(int slot, int mouse, #F_INVCLICK_META# meta, EntityHuman human)
    {
        ItemStack stack = super.#FIELD_CONTAINER_11#(slot, mouse, meta, human);
        mcPlayer.updateInventory(mcPlayer.activeContainer);
        return stack;
    }

    public void #FIELD_CONTAINER_6#(IInventory iinventory)
    {
        if(iinventory == this.enchantSlots)
        {
            ItemStack itemstack = iinventory.getItem(0);
            int i;
            if(itemstack != null && itemstack != #F_ITEMSTACK_NULL#)
            {
                if(!this.world.#FIELD_WORLD_1#)
                {
                    int j;
                    rand.setSeed((long)this.#FIELD_CONTAINERENCHANTTABLE_3#);
                    for(j = 0; j < 3; ++j)
                    {
                        this.costs[j] = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_1#(rand, j, bookshelves, itemstack);
                        this.#FIELD_CONTAINERENCHANTTABLE_IDS#[j] = -1;
                        ---------- SINCE 1.9 START ----------
                        this.#FIELD_CONTAINERENCHANTTABLE_LEVELS#[j] = -1;
                        ---------- SINCE 1.9 END ----------
                        if(this.costs[j] < j + 1)
                        {
                            this.costs[j] = 0;
                        }
                    }
                    /*CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);
                    PrepareItemEnchantEvent event = new PrepareItemEnchantEvent(player, this.getBukkitView(), this.world.getWorld().getBlockAt(0, 0, 0), item, this.costs, bookshelves);
                    event.setCancelled(!itemstack.#FIELD_ITEMSTACK_12#());
                    this.world.getServer().getPluginManager().callEvent(event);
                    if(event.isCancelled() && !Config.bool("events.ignorecancelled"))
                    {
                        for(i = 0; i < 3; ++i)
                        {
                            this.costs[i] = 0;
                        }
                        return;
                    }*/
                    if(!itemstack.#FIELD_ITEMSTACK_12#())
                    {
                        for(i = 0; i < 3; ++i)
                        {
                            this.costs[i] = 0;
                            this.#FIELD_CONTAINERENCHANTTABLE_IDS#[i] = -1;
                            ---------- SINCE 1.9 START ----------
                            this.#FIELD_CONTAINERENCHANTTABLE_LEVELS#[i] = -1;
                            ---------- SINCE 1.9 END ----------
                        }
                        return;
                    }
                    for(j = 0; j < 3; ++j)
                    {
                        if(this.costs[j] > 0)
                        {
                            List list = getWeightedRandomEnchantList(itemstack, j, this.costs[j]);
                            if(list != null && !list.isEmpty())
                            {
                                WeightedRandomEnchant weightedrandomenchant = (WeightedRandomEnchant) list.get(rand.nextInt(list.size()));
                                ---------- PRE 1.9 START ----------
                                this.#FIELD_CONTAINERENCHANTTABLE_IDS#[j] = weightedrandomenchant.enchantment.id | weightedrandomenchant.level << 8;
                                ---------- PRE 1.9 END ----------
                                ---------- SINCE 1.9 START ----------
                                this.#FIELD_CONTAINERENCHANTTABLE_IDS#[j] = getEnchId(weightedrandomenchant.enchantment);
                                this.#FIELD_CONTAINERENCHANTTABLE_LEVELS#[j] = weightedrandomenchant.level;
                                ---------- SINCE 1.9 END ----------
                            }
                        }
                    }
                    this.#FIELD_CONTAINERENCHANTTABLE_4#();
                }
            }
            else
            {
                for(i = 0; i < 3; ++i)
                {
                    this.costs[i] = 0;
                    this.#FIELD_CONTAINERENCHANTTABLE_IDS#[i] = -1;
                    ---------- SINCE 1.9 START ----------
                    this.#FIELD_CONTAINERENCHANTTABLE_LEVELS#[i] = -1;
                    ---------- SINCE 1.9 END ----------
                }
            }
        }
    }

    public boolean #FIELD_CONTAINERENCHANTTABLE_2#(EntityHuman entityhuman, int i)
    {
        ItemStack itemstack = this.enchantSlots.getItem(0);
        ItemStack itemstack1 = this.enchantSlots.getItem(1);
        int j = i + 1;
        ---------- PRE 1.11 START ----------
        if((itemstack1 == null || itemstack1.count < j) && !playerFree(entityhuman))
        ---------- PRE 1.11 END ----------
        ---------- SINCE 1.11 START ----------
        if((itemstack1 == null || itemstack1.getCount() < j) && !playerFree(entityhuman))
        ---------- SINCE 1.11 END ----------
        {
            return false;
        }
        else if(this.costs[i] > 0 && itemstack != null && (entityhuman.expLevel >= j && entityhuman.expLevel >= this.costs[i] || playerFree(entityhuman)))
        {
            if(!this.world.#FIELD_WORLD_1#)
            {
                List list = getWeightedRandomEnchantList(itemstack, i, this.costs[i]);
                if(list == null)
                {
                    list = new java.util.ArrayList<WeightedRandomEnchant>();
                }
                boolean flag = itemstack.getItem() == Items.BOOK;
                if(list != null)
                {
                    Map<org.bukkit.enchantments.Enchantment, Integer> enchants = new java.util.HashMap<org.bukkit.enchantments.Enchantment, Integer>();
                    for(Object obj : list)
                    {
                        WeightedRandomEnchant instance = (WeightedRandomEnchant)obj;

                        enchants.put(getBukkitEnch(
                            ---------- PRE 1.9 START ----------
                            instance.enchantment.id
                            ---------- PRE 1.9 END ----------
                            ---------- SINCE 1.9 START ----------
                            getEnchId(instance.enchantment)
                            ---------- SINCE 1.9 END ----------
                        ), instance.level);
                    }
                    CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);
                    /*EnchantItemEvent event = new EnchantItemEvent((Player)entityhuman.getBukkitEntity(), this.getBukkitView(), this.world.getWorld().getBlockAt(0, 0, 0), item, this.costs[i], enchants, i);
                    this.world.getServer().getPluginManager().callEvent(event);
                    int level = event.getExpLevelCost();
                    if((event.isCancelled() && !Config.bool("events.ignorecancelled")) || (level > entityhuman.expLevel && !playerFree(entityhuman)) || event.getEnchantsToAdd().isEmpty())
                    {
                        return false;
                    }*/
                    if(flag)
                    {
                        itemstack.setItem(Items.ENCHANTED_BOOK);
                    }
                    for(Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : /*event.getEnchantsToAdd().entrySet()*/ enchants.entrySet())
                    {
                        try
                        {
                            if(flag)
                            {
                                ---------- PRE 1.13 START ----------
                                int enchantId = entry.getKey().getId();
                                ---------- PRE 1.13 END ----------
                                ---------- SINCE 1.13 START ----------
                                MinecraftKey enchantId = org.bukkit.craftbukkit.v#MC_VERSION#.util.CraftNamespacedKey.toMinecraft(entry.getKey().getKey());
                                ---------- SINCE 1.13 END ----------
                                ---------- PRE 1.13.1 START ----------
                                Enchantment ench = Enchantment.#FIELD_ENCHANTMENT_GETBYID#(enchantId);
                                ---------- PRE 1.13.1 END ----------
                                ---------- SINCE 1.13.1 START ----------
                                Enchantment ench = IRegistry.ENCHANTMENT.get(enchantId);
                                ---------- SINCE 1.13.1 END ----------
                                if(ench == null)
                                {
                                    continue;
                                }
                                #F_ENCHANTEDBOOK_OBJ#.#FIELD_ITEMENCHANTEDBOOK_3#(itemstack, new WeightedRandomEnchant(ench, entry.getValue()));
                            }
                            else
                            {
                                item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
                            }
                        }
                        catch(IllegalArgumentException e)
                        {
                            // Just swallow invalid enchantments
                        }
                    }
                    ---------- PRE 1.12 START ----------
                    entityhuman.#FIELD_ENTITYHUMAN_1#(j);
                    ---------- PRE 1.12 END ----------
                    ---------- SINCE 1.12 START ----------
                    entityhuman.#FIELD_ENTITYHUMAN_1#(itemstack, j);
                    ---------- SINCE 1.12 END ----------
                    if(!playerFree(entityhuman))
                    {
                        ---------- PRE 1.11 START ----------
                        itemstack1.count -= j;
                        if(itemstack1.count <= 0)
                        ---------- PRE 1.11 END ----------
                        ---------- SINCE 1.11 START ----------
                        int count = itemstack1.getCount() - j;
                        itemstack1.setCount(count);
                        if(count <= 0)
                        ---------- SINCE 1.11 END ----------
                        {
                            this.enchantSlots.setItem(1, (ItemStack)#F_ITEMSTACK_NULL#);
                        }
                    }
                    ---------- SINCE 1.8.3 START ----------
                    entityhuman.#FIELD_ENTITYHUMAN_2#(StatisticList.#FIELD_STATISTICSLIST_1#);
                    ---------- SINCE 1.8.3 END ----------
                    this.enchantSlots.update();
                    this.#FIELD_CONTAINERENCHANTTABLE_3# = entityhuman.#FIELD_ENTITYHUMAN_3#();
                    this.#FIELD_CONTAINER_6#(this.enchantSlots);
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    private List<WeightedRandomEnchant> getWeightedRandomEnchantList(ItemStack itemstack, int i, int j)
    {
        rand.setSeed((long)(this.#FIELD_CONTAINERENCHANTTABLE_3# + i));
        List list = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_ENCHANT#(rand, itemstack, j
            ---------- SINCE 1.9 START ----------
            , false
            ---------- SINCE 1.9 END ----------
        );
        if(itemstack.getItem() == Items.BOOK && list != null && list.size() > 1)
        {
            list.remove(rand.nextInt(list.size()));
        }
        return list;
    }

    public static boolean playerFree(EntityHuman entityhuman)
    {
        return entityhuman.abilities.canInstantlyBuild || Perm.has(entityhuman.world.getWorld().getName(), (Player)entityhuman.getBukkitEntity(), "vpack.use.enchanttable.free");
    }
}

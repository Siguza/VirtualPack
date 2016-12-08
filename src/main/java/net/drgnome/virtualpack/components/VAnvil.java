// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import java.lang.reflect.*;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.*;
import net.drgnome.virtualpack.util.*;

public class VAnvil extends ContainerAnvil
{
    private int _whatever = 0;
    private String _itemName;
    private final EntityPlayer _player;

    public VAnvil(EntityPlayer entityplayer)
    {
        super(entityplayer.inventory, null, new BlockPosition(0, 0, 0), entityplayer);
        checkReachable = false;
        _player = entityplayer;
        #FIELD_CONTAINER_3#.set(2, new VAnvilSlot(this, getInv("#FIELD_CONTAINERANVIL_2#"), 2, 134, 47));
    }

    public VAnvil(EntityPlayer entityplayer, int x, int y, int z)
    {
        super(entityplayer.inventory, entityplayer.world, new BlockPosition(x, y, z), entityplayer);
        _player = entityplayer;
    }

    public void #FIELD_CONTAINER_6#(IInventory iinventory)
    {
        super.#FIELD_CONTAINER_6#(iinventory);
        if(iinventory == VAnvil.#FIELD_CONTAINERANVIL_1#(this))
        {
            #FIELD_CONTAINERANVIL_4#();
        }
    }

    public final ItemStack #FIELD_CONTAINER_11#(int slot, int mouse, #F_INVCLICK_META# meta, EntityHuman human)
    {
        ItemStack stack = super.#FIELD_CONTAINER_11#(slot, mouse, meta, human);
        updatePlayerInventory();
        return stack;
    }

    public void #FIELD_CONTAINERANVIL_4#()
    {
        final int maxLevel = Config.getInt(_player.getBukkitEntity(), "tools", "anvil", "maxlevel", Config.MODE_MAX);
        IInventory invG = getInv("#FIELD_CONTAINERANVIL_2#");
        IInventory invH = VAnvil.#FIELD_CONTAINERANVIL_1#(this);
        boolean flag = false;
        boolean flag1 = true;
        boolean flag2 = true;
        boolean flag3 = true;
        boolean flag4 = true;
        boolean flag5 = true;
        boolean flag6 = true;
        ItemStack itemstack = invH.getItem(0);
        #FIELD_CONTAINERANVIL_5# = 1;
        int i = 0;
        byte b0 = 0;
        byte b1 = 0;
        if(itemstack == null || itemstack == #F_ITEMSTACK_NULL#)
        {
            invG.setItem(0, (ItemStack)#F_ITEMSTACK_NULL#);
            #FIELD_CONTAINERANVIL_5# = 0;
        }
        else
        {
            ItemStack itemstack1 = itemstack.cloneItemStack();
            ItemStack itemstack2 = invH.getItem(1);
            Map map = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_3#(itemstack1);
            boolean flag7 = false;
            int j = b0 + itemstack.getRepairCost() + ((itemstack2 == null || itemstack2 == #F_ITEMSTACK_NULL#) ? 0 : itemstack2.getRepairCost());
            _whatever = 0;
            int k;
            if(itemstack2 != null && itemstack2 != #F_ITEMSTACK_NULL#)
            {
                flag7 = itemstack2.getItem() == Items.ENCHANTED_BOOK && Items.ENCHANTED_BOOK.#FIELD_ITEMENCHANTEDBOOK_2#(itemstack2).size() > 0;
                int l;
                int i1;
                if(itemstack1.#FIELD_ITEMSTACK_10#() && itemstack1.getItem().#FIELD_ITEM_6#(itemstack, itemstack2))
                {
                    k = Math.min(itemstack1.#FIELD_ITEMSTACK_5#(), itemstack1.#FIELD_ITEMSTACK_6#() / 4);
                    if(k <= 0)
                    {
                        invG.setItem(0, (ItemStack)#F_ITEMSTACK_NULL#);
                        #FIELD_CONTAINERANVIL_5# = 0;
                        return;
                    }
                    ---------- PRE 1.11 START ----------
                    for(l = 0; k > 0 && l < itemstack2.count; ++l)
                    ---------- PRE 1.11 END ----------
                    ---------- SINCE 1.11 START ----------
                    for(l = 0; k > 0 && l < itemstack2.getCount(); ++l)
                    ---------- SINCE 1.11 END ----------
                    {
                        i1 = itemstack1.#FIELD_ITEMSTACK_GETDAMAGE#() - k;
                        itemstack1.setData(i1);
                        ++i;
                        k = Math.min(itemstack1.#FIELD_ITEMSTACK_5#(), itemstack1.#FIELD_ITEMSTACK_6#() / 4);
                    }
                    _whatever = l;
                }
                else
                {
                    if(!flag7 && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.#FIELD_ITEMSTACK_10#()))
                    {
                        invG.setItem(0, (ItemStack)#F_ITEMSTACK_NULL#);
                        #FIELD_CONTAINERANVIL_5# = 0;
                        return;
                    }
                    int j1;
                    if(itemstack1.#FIELD_ITEMSTACK_10#() && !flag7)
                    {
                        k = itemstack.#FIELD_ITEMSTACK_6#() - itemstack.#FIELD_ITEMSTACK_5#();
                        l = itemstack2.#FIELD_ITEMSTACK_6#() - itemstack2.#FIELD_ITEMSTACK_5#();
                        i1 = l + itemstack1.#FIELD_ITEMSTACK_6#() * 12 / 100;
                        int k1 = k + i1;
                        j1 = itemstack1.#FIELD_ITEMSTACK_6#() - k1;
                        if(j1 < 0)
                        {
                            j1 = 0;
                        }
                        if(j1 < itemstack1.getData())
                        {
                            itemstack1.setData(j1);
                            i += 2;
                        }
                    }
                    Map map1 = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_3#(itemstack2);
                    Iterator iterator = map1.keySet().iterator();
                    while(iterator.hasNext())
                    {
                        ---------- PRE 1.9 START ----------
                        int key = (Integer)iterator.next();
                        Enchantment enchantment = Enchantment.#FIELD_ENCHANTMENT_GETBYID#(key);
                        ---------- PRE 1.9 END ----------
                        ---------- SINCE 1.9 START ----------
                        Enchantment key = (Enchantment)iterator.next();
                        Enchantment enchantment = key;
                        ---------- SINCE 1.9 END ----------
                        if(enchantment != null)
                        {
                            j1 = map.containsKey(key) ? (Integer)map.get(key) : 0;
                            int l1 = (Integer)map1.get(key);
                            l1 = (j1 == l1) ? (l1 + 1) : Math.max(l1, j1);
                            boolean flag8 = enchantment.canEnchant(itemstack);
                            if(playerFree(_player) || itemstack.getItem() == Items.ENCHANTED_BOOK)
                            {
                                flag8 = true;
                            }
                            Iterator iterator1 = map.keySet().iterator();
                            while(iterator1.hasNext())
                            {
                                ---------- PRE 1.9 START ----------
                                int key2 = (Integer)iterator1.next();
                                Enchantment ench2 = Enchantment.#FIELD_ENCHANTMENT_GETBYID#(key2);
                                ---------- PRE 1.9 END ----------
                                ---------- SINCE 1.9 START ----------
                                Enchantment key2 = (Enchantment)iterator1.next();
                                Enchantment ench2 = key2;
                                ---------- SINCE 1.9 END ----------
                                if(key != key2 && !enchantment.#FIELD_ENCHANTMENT_1#(ench2))
                                {
                                    flag8 = false;
                                    ++i;
                                }
                            }
                            if(flag8)
                            {
                                if(l1 > enchantment.getMaxLevel())
                                {
                                    l1 = enchantment.getMaxLevel();
                                }
                                map.put(key, l1);
                                int k2 = 0;
                                ---------- PRE 1.9 START ----------
                                switch(enchantment.getRandomWeight())
                                {
                                    case 1:
                                        k2 = 8;
                                        break;
                                    case 2:
                                        k2 = 4;
                                        break;
                                    case 5:
                                        k2 = 2;
                                        break;
                                    case 10:
                                        k2 = 1;
                                        break;
                                }
                                ---------- PRE 1.9 END ----------
                                ---------- SINCE 1.9 START ----------
                                switch(enchantment.#FIELD_ENCHANTMENT_GETRARITY#())
                                {
                                    case COMMON:
                                        k2 = 1;
                                        break;
                                    case UNCOMMON:
                                        k2 = 2;
                                        break;
                                    case RARE:
                                        k2 = 4;
                                        break;
                                    case VERY_RARE:
                                        k2 = 8;
                                        break;
                                }
                                ---------- SINCE 1.9 END ----------
                                if(flag7)
                                {
                                    k2 = Math.max(1, k2 / 2);
                                }
                                i += k2 * l1;
                            }
                        }
                    }
                }
            }
            if(StringUtils.isBlank(_itemName))
            {
                if(itemstack.hasName())
                {
                    b1 = 1;
                    i += b1;
                    itemstack1.#FIELD_ITEMSTACK_11#();
                }
            }
            else if(!_itemName.equals(itemstack.getName()))
            {
                b1 = 1;
                i += b1;
                itemstack1.#FIELD_ITEMSTACK_9#(_itemName);
            }
            #FIELD_CONTAINERANVIL_5# = j + i;
            if(i <= 0)
            {
                itemstack1 = null;
            }
            if(b1 == i && b1 > 0 && #FIELD_CONTAINERANVIL_5# >= maxLevel)
            {
                #FIELD_CONTAINERANVIL_5# = maxLevel - 1;
            }
            if(#FIELD_CONTAINERANVIL_5# >= maxLevel && !playerFree(_player))
            {
                itemstack1 = null;
            }
            if(itemstack1 != null && itemstack1 != #F_ITEMSTACK_NULL#)
            {
                k = itemstack1.getRepairCost();
                if((itemstack2 != null && itemstack2 != #F_ITEMSTACK_NULL#) && k < itemstack2.getRepairCost())
                {
                    k = itemstack2.getRepairCost();
                }
                ---------- PRE 1.9 START ----------
                k = k * 2 + 1;
                ---------- PRE 1.9 END ----------
                ---------- SINCE 1.9 START ----------
                if(b1 != i || b1 == 0)
                {
                    k = k * 2 + 1;
                }
                ---------- SINCE 1.9 END ----------
                itemstack1.setRepairCost(k);
                EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_4#(map, itemstack1);
            }
            invG.setItem(0, itemstack1 == null ? #F_ITEMSTACK_NULL# : itemstack1);
            this.#FIELD_CONTAINER_9#();
        }
    }

    public void #FIELD_CONTAINER_5#(EntityHuman entityhuman)
    {
        for(int i = 0; i < VAnvil.#FIELD_CONTAINERANVIL_1#(this).getSize(); ++i)
        {
            ItemStack itemstack = VAnvil.#FIELD_CONTAINERANVIL_1#(this).splitWithoutUpdate(i);
            if(itemstack != null && itemstack != #F_ITEMSTACK_NULL#)
            {
                entityhuman.drop(itemstack, false); // Let's just put some random trues and falses here and there...
            }
        }
    }

    public ItemStack #FIELD_CONTAINER_10#(EntityHuman entityhuman, int i)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) #FIELD_CONTAINER_3#.get(i);
        if(slot != null && slot.#FIELD_SLOT_1#())
        {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            if(i == 2)
            {
                if(!#FIELD_CONTAINER_8#(itemstack1, 3, 39, true))
                {
                    return null;
                }
                slot.#FIELD_SLOT_2#(itemstack1, itemstack);
            }
            else if(i != 0 && i != 1)
            {
                if(i >= 3 && i < 39 && !#FIELD_CONTAINER_8#(itemstack1, 0, 2, false))
                {
                    return null;
                }
            }
            else if(!#FIELD_CONTAINER_8#(itemstack1, 3, 39, false))
            {
                return null;
            }
            ---------- PRE 1.11 START ----------
            if(itemstack1.count == 0)
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            if(itemstack1.getCount() == 0)
            ---------- SINCE 1.11 END ----------
            {
                slot.set((ItemStack)#F_ITEMSTACK_NULL#);
            }
            else
            {
                slot.#FIELD_SLOT_3#();
            }
            ---------- PRE 1.11 START ----------
            if(itemstack1.count == itemstack.count)
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            if(itemstack1.getCount() == itemstack.getCount())
            ---------- SINCE 1.11 END ----------
            {
                return null;
            }
            slot.#FIELD_SLOT_2#(entityhuman, itemstack1);
        }
        return itemstack;
    }

    public void #FIELD_CONTAINERANVIL_6#(String s)
    {
        _itemName = s;
        if(this.getSlot(2).#FIELD_SLOT_1#())
        {
            this.getSlot(2).getItem().#FIELD_ITEMSTACK_9#(_itemName);
        }
        #FIELD_CONTAINERANVIL_4#();
    }

    public static IInventory #FIELD_CONTAINERANVIL_1#(VAnvil anvil)
    {
        return getInv(anvil, "#FIELD_CONTAINERANVIL_3#");
    }

    public int getWhatever()
    {
        return _whatever;
    }

    public static int #FIELD_CONTAINERANVIL_7#(VAnvil anvil)
    {
        return anvil.getWhatever();
    }

    public IInventory getInv(String name)
    {
        return getInv(this, name);
    }

    public static IInventory getInv(VAnvil anvil, String name)
    {
        try
        {
            Field f = ContainerAnvil.class.getDeclaredField(name);
            f.setAccessible(true);
            return (IInventory)f.get(anvil);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void updatePlayerInventory()
    {
        _player.updateInventory(_player.activeContainer);
    }

    public static boolean playerFree(EntityHuman entityhuman)
    {
        return (entityhuman.abilities.canInstantlyBuild || Perm.has(entityhuman.world.getWorld().getName(), (Player)entityhuman.getBukkitEntity(), "vpack.use.anvil.free"));
    }

    ---------- SINCE 1.9 START ----------
    public CraftInventoryView getBukkitView()
    {
        return new CraftInventoryView(_player.getBukkitEntity(), new CraftInventoryAnvil(null, VAnvil.#FIELD_CONTAINERANVIL_1#(this), getInv("#FIELD_CONTAINERANVIL_2#")
            ---------- SINCE 1.11 START ----------
            , this
            ---------- SINCE 1.11 END ----------
        ), this);
    }
    ---------- SINCE 1.9 END ----------
}

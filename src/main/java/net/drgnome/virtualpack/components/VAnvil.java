// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import java.lang.reflect.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.*;
import net.drgnome.virtualpack.util.*;

public class VAnvil extends ContainerAnvil
{
    private int _exp = 0;
    private String _itemName;
    private final EntityPlayer _player;

    public VAnvil(EntityPlayer entityplayer)
    {
        super(entityplayer.inventory, null, 0, 0, 0, entityplayer);
        checkReachable = false;
        _player = entityplayer;
        for(int i = 0; i < #FIELD_CONTAINER_3#.size(); i++)
        {
            try
            {
                if(Class.forName("net.minecraft.server.v#MC_VERSION#.SlotAnvilResult").isInstance(#FIELD_CONTAINER_3#.get(i)))
                {
                    #FIELD_CONTAINER_3#.set(i, new VAnvilSlot(this, getInv("#FIELD_CONTAINERANVIL_2#"), 2, 134, 47));
                    break;
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    public VAnvil(EntityPlayer entityplayer, int x, int y, int z)
    {
        super(entityplayer.inventory, entityplayer.world, x, y, z, entityplayer);
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

    public void #FIELD_CONTAINERANVIL_4#()
    {
        calculate0();
        updatePlayerInventory();
    }

    public void calculate0()
    {
        final int maxLevel = Config.getInt(_player.getBukkitEntity(), "tools", "anvil", "maxlevel", Config.MODE_MAX);
        ItemStack itemstack = VAnvil.#FIELD_CONTAINERANVIL_1#(this).getItem(0);
        #FIELD_CONTAINERANVIL_5# = 0;
        int i = 0;
        byte b0 = 0;
        int j = 0;
        if(itemstack == null)
        {
            getInv("#FIELD_CONTAINERANVIL_2#").setItem(0, null);
            #FIELD_CONTAINERANVIL_5# = 0;
        }
        else
        {
            ItemStack itemstack1 = itemstack.cloneItemStack();
            ItemStack itemstack2 = VAnvil.#FIELD_CONTAINERANVIL_1#(this).getItem(1);
            Map map = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_3#(itemstack1);
            boolean flag = false;
            int k = b0 + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());
            _exp = 0;
            int l;
            int i1;
            int j1;
            int k1;
            int l1;
            Iterator iterator;
            Enchantment enchantment;
            if(itemstack2 != null)
            {
                flag = Item.#FIELD_ITEM_7#(itemstack2.getItem()) == Material.ENCHANTED_BOOK.getId() && ((ItemEnchantedBook)Item.#FIELD_ITEM_8#(Material.ENCHANTED_BOOK.getId())).#FIELD_ITEMENCHANTEDBOOK_2#(itemstack2).size() > 0;
                if(itemstack1.#FIELD_ITEMSTACK_10#() && itemstack1.getItem().#FIELD_ITEM_6#(itemstack, itemstack2))
                {
                    l = Math.min(itemstack1.#FIELD_ITEMSTACK_5#(), itemstack1.#FIELD_ITEMSTACK_6#() / 4);
                    if(l <= 0)
                    {
                        getInv("#FIELD_CONTAINERANVIL_2#").setItem(0, null);
                        #FIELD_CONTAINERANVIL_5# = 0;
                        return;
                    }
                    for(i1 = 0; l > 0 && i1 < itemstack2.count; ++i1)
                    {
                        j1 = itemstack1.#FIELD_ITEMSTACK_5#() - l;
                        itemstack1.setData(j1);
                        i += Math.max(1, l / 100) + map.size();
                        l = Math.min(itemstack1.#FIELD_ITEMSTACK_5#(), itemstack1.#FIELD_ITEMSTACK_6#() / 4);
                    }
                    _exp = i1;
                }
                else
                {
                    if(!flag && (Item.#FIELD_ITEM_7#(itemstack1.getItem()) != Item.#FIELD_ITEM_7#(itemstack2.getItem()) || !itemstack1.#FIELD_ITEMSTACK_10#()))
                    {
                        getInv("#FIELD_CONTAINERANVIL_2#").setItem(0, null);
                        #FIELD_CONTAINERANVIL_5# = 0;
                        return;
                    }
                    if(itemstack1.#FIELD_ITEMSTACK_10#() && !flag)
                    {
                        l = itemstack.#FIELD_ITEMSTACK_6#() - itemstack.#FIELD_ITEMSTACK_5#();
                        i1 = itemstack2.#FIELD_ITEMSTACK_6#() - itemstack2.#FIELD_ITEMSTACK_5#();
                        j1 = i1 + itemstack1.#FIELD_ITEMSTACK_6#() * 12 / 100;
                        int i2 = l + j1;
                        k1 = itemstack1.#FIELD_ITEMSTACK_6#() - i2;
                        if(k1 < 0)
                        {
                            k1 = 0;
                        }
                        if(k1 < itemstack1.getData())
                        {
                            itemstack1.setData(k1);
                            i += Math.max(1, j1 / 100);
                        }
                    }
                    Map map1 = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_3#(itemstack2);
                    iterator = map1.keySet().iterator();
                    while(iterator.hasNext())
                    {
                        j1 = ((Integer)iterator.next()).intValue();
                        enchantment = Enchantment.byId[j1];
                        k1 = map.containsKey(Integer.valueOf(j1)) ? ((Integer)map.get(Integer.valueOf(j1))).intValue() : 0;
                        l1 = ((Integer)map1.get(Integer.valueOf(j1))).intValue();
                        int j2;
                        if(k1 == l1)
                        {
                            ++l1;
                            j2 = l1;
                        }
                        else
                        {
                            j2 = Math.max(l1, k1);
                        }
                        l1 = j2;
                        int k2 = l1 - k1;
                        boolean flag1 = enchantment.canEnchant(itemstack);
                        if(playerFree(_player) || (Item.#FIELD_ITEM_7#(itemstack.getItem()) == Material.ENCHANTED_BOOK.getId()))
                        {
                            flag1 = true;
                        }
                        Iterator iterator1 = map.keySet().iterator();
                        while(iterator1.hasNext())
                        {
                            int l2 = ((Integer)iterator1.next()).intValue();
                            if(l2 != j1 && !enchantment.#FIELD_ENCHANTMENT_1#(Enchantment.byId[l2]))
                            {
                                flag1 = false;
                                i += k2;
                            }
                        }
                        if(flag1)
                        {
                            if(l1 > enchantment.getMaxLevel())
                            {
                                l1 = enchantment.getMaxLevel();
                            }
                            map.put(Integer.valueOf(j1), Integer.valueOf(l1));
                            int i3 = 0;
                            switch(enchantment.getRandomWeight())
                            {
                                case 1:
                                    i3 = 8;
                                    break;
                                case 2:
                                    i3 = 4;
                                case 3:
                                case 4:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                default:
                                    break;
                                case 5:
                                    i3 = 2;
                                    break;
                                case 10:
                                    i3 = 1;
                            }
                            i += i3 * k2;
                        }
                    }
                }
            }
            if(_itemName != null && !_itemName.equalsIgnoreCase(itemstack.getName()) && _itemName.length() > 0)
            {
                j = itemstack.#FIELD_ITEMSTACK_10#() ? 7 : itemstack.count * 5;
                i += j;
                if(itemstack.hasName())
                {
                    k += j / 2;
                }
                itemstack1.#FIELD_ITEMSTACK_9#(_itemName);
            }
            l = 0;
            for(iterator = map.keySet().iterator(); iterator.hasNext(); k += l + k1 * l1)
            {
                j1 = ((Integer)iterator.next()).intValue();
                enchantment = Enchantment.byId[j1];
                k1 = ((Integer)map.get(Integer.valueOf(j1))).intValue();
                l1 = 0;
                ++l;
                switch(enchantment.getRandomWeight())
                {
                    case 1:
                        l1 = 8;
                        break;
                    case 2:
                        l1 = 4;
                    case 3:
                    case 4:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    default:
                        break;
                    case 5:
                        l1 = 2;
                        break;
                    case 10:
                        l1 = 1;
                }
                if(flag)
                {
                    l1 = Math.max(1, l1 / 2);
                }
            }
            if(flag)
            {
                k = Math.max(1, k / 2);
            }
            #FIELD_CONTAINERANVIL_5# = k + i;
            if(i <= 0)
            {
                itemstack1 = null;
            }
            if(j == i && j > 0 && #FIELD_CONTAINERANVIL_5# >= maxLevel)
            {
                #FIELD_CONTAINERANVIL_5# = maxLevel - 1;
            }
            if(#FIELD_CONTAINERANVIL_5# >= maxLevel && !playerFree(_player))
            {
                itemstack1 = null;
            }
            if(itemstack1 != null)
            {
                i1 = itemstack1.getRepairCost();
                if(itemstack2 != null && i1 < itemstack2.getRepairCost())
                {
                    i1 = itemstack2.getRepairCost();
                }
                if(itemstack1.hasName())
                {
                    i1 -= 9;
                }
                if(i1 < 0)
                {
                    i1 = 0;
                }
                i1 += 2;
                itemstack1.setRepairCost(i1);
                EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_4#(map, itemstack1);
            }
            getInv("#FIELD_CONTAINERANVIL_2#").setItem(0, itemstack1);
            #FIELD_CONTAINER_9#();
        }
    }

    public void #FIELD_CONTAINER_5#(EntityHuman entityhuman)
    {
        for(int i = 0; i < VAnvil.#FIELD_CONTAINERANVIL_1#(this).getSize(); ++i)
        {
            ItemStack itemstack = VAnvil.#FIELD_CONTAINERANVIL_1#(this).splitWithoutUpdate(i);
            if(itemstack != null)
            {
                entityhuman.drop(itemstack, false); // What does this "false" do?
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
            if(itemstack1.count == 0)
            {
                slot.set((ItemStack) null);
            }
            else
            {
                slot.#FIELD_SLOT_3#();
            }
            if(itemstack1.count == itemstack.count)
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

    public static int #FIELD_CONTAINERANVIL_7#(VAnvil anvil)
    {
        return anvil._exp;
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
        return (entityhuman.abilities.canInstantlyBuild || Perm.has(entityhuman.world.getWorld().getName(), entityhuman.getUniqueID(), "vpack.use.anvil.free"));
    }
}

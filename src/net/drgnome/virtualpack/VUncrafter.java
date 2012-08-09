// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;

import net.minecraft.server.*;

public class VUncrafter extends ContainerChest
{
    EntityPlayer player;
    
    public VUncrafter(EntityPlayer player)
    {
        super(player.inventory, new VUncrafterInv());
        this.player = player;
        a = new ArrayList();
        b = new ArrayList();
        for(int i = 0; i < 2; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                a(new Slot(container, j + 9 * i, 8 + j * 18, 18));
            }
        }
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                a(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 67 + i * 18));
            }
        }
        for(int i = 0; i < 9; i++)
        {
            a(new Slot(player.inventory, i, 8 + i * 18, 125));
        }
    }
    
    public void a(EntityHuman entityhuman)
    {
        for(int i = 0; i < 18; i++)
        {
            ItemStack itemstack = container.splitWithoutUpdate(i);
            if(itemstack != null)
            {
                entityhuman.drop(itemstack);
            }
        }
    }
    
    public boolean b(EntityHuman entityhuman)
    {
        return true;
    }
    
    public ItemStack clickItem(int i, int j, boolean flag, EntityHuman entityhuman)
    {
        ItemStack itemstack = null;
        if(j > 1)
        {
            return null;
        }
        if((j == 0) || (j == 1))
        {
            PlayerInventory playerinventory = entityhuman.inventory;
            if(i == -999)
            {
                if(playerinventory.getCarried() != null)
                {
                    if(j == 0)
                    {
                        entityhuman.drop(playerinventory.getCarried());
                        playerinventory.setCarried((ItemStack)null);
                    }
                    if(j == 1)
                    {
                        entityhuman.drop(playerinventory.getCarried().a(1));
                        if(playerinventory.getCarried().count == 0)
                        {
                            playerinventory.setCarried((ItemStack)null);
                        }
                    }
                }
            }
            else if(flag)
            {
                ItemStack itemstack1 = b(i);
                if(itemstack1 != null)
                {
                    int k = itemstack1.id;
                    itemstack = itemstack1.cloneItemStack();
                    Slot slot = (Slot)this.b.get(i);
                    if((slot != null) && (slot.getItem() != null) && (slot.getItem().id == k))
                    {
                        b(i, j, flag, entityhuman);
                    }
                }
            }
            else
            {
                if(i < 0)
                {
                    return null;
                }
                Slot slot1 = (Slot)this.b.get(i);
                if(slot1 != null)
                {
                    slot1.d();
                    ItemStack itemstack2 = slot1.getItem();
                    ItemStack itemstack3 = playerinventory.getCarried();
                    if((i > 8) && (i < 18) && (itemstack3 != null))
                    {
                        ud();
                        return itemstack;
                    }
                    if(itemstack2 != null)
                    {
                        itemstack = itemstack2.cloneItemStack();
                    }
                    if(itemstack2 == null)
                    {
                        if((itemstack3 != null) && (slot1.isAllowed(itemstack3)))
                        {
                            int l = j == 0 ? itemstack3.count : 1;
                            if(l > slot1.a())
                            {
                                l = slot1.a();
                            }
                            slot1.set(itemstack3.a(l));
                            if(itemstack3.count == 0)
                            {
                                playerinventory.setCarried((ItemStack)null);
                            }
                        }
                    }
                    else if(itemstack3 == null)
                    {
                        int l = j == 0 ? itemstack2.count : (itemstack2.count + 1) / 2;
                        ItemStack itemstack4 = slot1.a(l);
                        playerinventory.setCarried(itemstack4);
                        if(itemstack2.count == 0)
                        {
                            slot1.set((ItemStack)null);
                        }
                        slot1.b(playerinventory.getCarried());
                    }
                    else if(slot1.isAllowed(itemstack3))
                    {
                        if((itemstack2.id == itemstack3.id) && ((!itemstack2.usesData()) || (itemstack2.getData() == itemstack3.getData())) && (ItemStack.equals(itemstack2, itemstack3)))
                        {
                            int l = j == 0 ? itemstack3.count : 1;
                            if(l > slot1.a() - itemstack2.count)
                            {
                                l = slot1.a() - itemstack2.count;
                            }
                            if(l > itemstack3.getMaxStackSize() - itemstack2.count)
                            {
                                l = itemstack3.getMaxStackSize() - itemstack2.count;
                            }
                            itemstack3.a(l);
                            if(itemstack3.count == 0)
                            {
                                playerinventory.setCarried((ItemStack)null);
                            }
                            itemstack2.count += l;
                        }
                        else if(itemstack3.count <= slot1.a())
                        {
                            slot1.set(itemstack3);
                            playerinventory.setCarried(itemstack2);
                        }
                    }
                    else if((itemstack2.id == itemstack3.id) &&
                            (itemstack3.getMaxStackSize() > 1) &&
                            ((!itemstack2.usesData()) || (itemstack2.getData() == itemstack3.getData())) &&
                            (ItemStack.equals(itemstack2, itemstack3)))
                    {
                        int l = itemstack2.count;
                        if((l > 0) && (l + itemstack3.count <= itemstack3.getMaxStackSize()))
                        {
                            itemstack3.count += l;
                            itemstack2 = slot1.a(l);
                            if(itemstack2.count == 0)
                            {
                                slot1.set((ItemStack)null);
                            }
                            slot1.b(playerinventory.getCarried());
                        }
                    }
                }
            }
        }
        ud();
        return itemstack;
    }
    
    private void ud()
    {
        player.updateInventory(player.activeContainer);
    }
}
// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import net.drgnome.virtualpack.VPack;
import net.drgnome.virtualpack.util.*;

// VirtualTileEntityFurnace is way too long, therefore VTE
public class VTEFurnace extends TileEntityFurnace implements VIInventory
{
    // To access the chests
    private VPack vpack;
    private ItemStack[] contents = new ItemStack[3];
    public int link = 0;
    // For custom stuff
    private double burnSpeed = 1D;
    private double meltSpeed = 1D;
    // I'm internally using "myCookTime" to not lose any precision, but for displaying the progress I still have to use "cookTime"
    private double myCookTime = 0D;
    // Call me paranoid, but this has to be checked
    private Item lastID;
    // Increases performance (or should at least)
    private long lastCheck = 0L;
    ---------- SINCE 1.11 START ----------
    private ProxyList<ItemStack> proxy;
    ---------- SINCE 1.11 END ----------

    ---------- SINCE 1.13 START ----------
    private static Method fuelTimeMeth;
    static
    {
        try
        {
            fuelTimeMeth = TileEntityFurnace.class.getDeclaredMethod("fuelTime", ItemStack.class);
            fuelTimeMeth.setAccessible(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    ---------- SINCE 1.13 END ----------

    // New VTE
    public VTEFurnace(VPack vpack)
    {
        this.vpack = vpack;
        bTime(0);
        cTime(0);
        cookTimeTotal(0);
        tfcf(0);
        ---------- SINCE 1.11 START ----------
        proxy = new ProxyList<ItemStack>(contents, #F_ITEMSTACK_NULL#);
        ---------- SINCE 1.11 END ----------
    }

    // Read from save
    public VTEFurnace(VPack vpack, String data[])
    {
        this(vpack);
        // Where to stop?
        for(int i = 0; i < Util.min(data.length, contents.length); i++)
        {
            contents[i] = Util.stringToItemStack(data[i]);
        }
        lastID = (contents[0] == null || contents[0] == #F_ITEMSTACK_NULL#) ? null : contents[0].getItem();
        // If the data array is long enough, we try to parse its entry, and if it's too short or the parsing fails, we'll leave it as it is.
        try
        {
            bTime(Util.tryParse(data[3], bTime()));
            try
            {
                tfcf(Integer.parseInt(data[4]));
            }
            catch(NumberFormatException e)
            {
            }
            myCookTime = Util.tryParse(data[5], myCookTime);
            cTime(Util.round(myCookTime));
            link = Util.tryParse(data[6], link);
            burnSpeed = Util.tryParse(data[7], getBurnSpeed(contents[1]));
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
        }
        // Note to self: No need to save this
        meltSpeed = getMeltSpeed(contents[0]);
        cookTimeTotal(#FIELD_TILEENTITYFURNACE_1#(contents[0]));
    }

    public String[] save()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < 3; i++)
        {
            list.add(Util.itemStackToString(contents[i]));
        }
        list.add(Integer.toString(bTime()));
        list.add(Integer.toString(tfcf()));
        list.add(Double.toString(myCookTime));
        list.add(Integer.toString(link));
        // I save this now, because you could lose burn speed if it's the last fuel item and the server gets restarted
        list.add(Double.toString(burnSpeed));
        return list.toArray(new String[0]);
    }

    public void tick(int ticks)
    {
        checkLink();
        Item newID = (contents[0] == null || contents[0] == #F_ITEMSTACK_NULL#) ? null : contents[0].getItem();
        // Has the item been changed?
        if(newID != lastID)
        {
            // Then reset the progress!
            myCookTime = 0.0D;
            lastID = newID;
            // And, most important: change the melt speed
            meltSpeed = getMeltSpeed(contents[0]);
        }
        // So, can we now finally burn?
        if(canHasBURN() && !isBurning() && (getFuelTime(contents[1]) > 0))
        {
            bTime(getFuelTime(contents[1]));
            cookTimeTotal(#FIELD_TILEENTITYFURNACE_1#(contents[0]));
            tfcf(bTime());
            // Before we remove the item: how fast does it burn?
            burnSpeed = getBurnSpeed(contents[1]);
            // If it's a container item (lava bucket), we only consume its contents (not like evil Notch!)
            if(contents[1].getItem().#FIELD_ITEM_1#()) // Derpnote
            {
                contents[1] = new ItemStack(contents[1].getItem().#FIELD_ITEM_2#());  // Derpnote
            }
            // If it's not a container, consume it! Om nom nom nom!
            else
            {
                ---------- PRE 1.11 START ----------
                contents[1].count--;
                if(contents[1].count <= 0)
                ---------- PRE 1.11 END ----------
                ---------- SINCE 1.11 START ----------
                int count = contents[1].getCount() - 1;
                contents[1].setCount(count);
                if(count <= 0)
                ---------- SINCE 1.11 END ----------
                // Let 0 be null
                {
                    contents[1] = null;
                }
            }
        }
        // Now, burning?
        if(isBurning())
        {
            // Then move on
            bTime(bTime() - ticks);
            // I'm using a double here because of the custom recipes.
            // The faster this fuel burns and the faster the recipe melts, the faster we're done
            myCookTime += burnSpeed * meltSpeed * ((double)ticks);
            // Finished burning?
            int time = #FIELD_TILEENTITYFURNACE_1#(contents[0]);
            while(myCookTime >= time)
            {
                myCookTime -= time;
                burn();
            }
        }
        // If it's not burning, we reset the burning progress!
        else if(!canHasBURN())
        {
            myCookTime = 0.0D;
        }
        // And for the display (I'm using floor rather than round to not cause the client to do shit when we not really reached cookTimeTotal):
        cTime(Util.floor(myCookTime));
    }

    protected void checkLink()
    {
        // If this furnace is linked, then we should see if there's a reason to interact
        if(isFine() || (link == 0) || (vpack == null) || (vpack.getInv(link) == null) || (lastCheck >= vpack.getInv(link).getLastUpdate()))
        {
            return;
        }
        VInv inv = vpack.getInv(link);
        // If we can't burn at the moment, we need different stuff
        if(!canHasBURN())
        {
            // Do we need a different ingredient?
            boolean get0 = false;
            // If there is none, then of course
            if(contents[0] == null)
            {
                get0 = true;
            }
            else
            {
                // Or if it can't be molten
                if(getBurnResult(contents[0]) == null)
                {
                    get0 = true;
                }
            }
            // So do we need a different ingredient?
            if(get0)
            {
                // Lets search for a meltable item
                ItemStack item;
                for(int i = 0; i < inv.getSize(); i++)
                {
                    item = inv.getItem(i);
                    if(getBurnResult(item) != null)
                    {
                        // We have to exchange the items, but we can't do it directly without messing everything up
                        item = Util.copy_old(item);
                        ItemStack item1 = Util.copy_old(contents[0]);
                        contents[0] = item;
                        inv.setItem(i, item1);
                        // And leave the loop
                        break;
                    }
                }
            }
            // Now, if there is any reason we can't burn, we're done and put the output item away (if there is any)
            if(!canHasBURN() && (contents[2] != null))
            {
                // Lets search for a place we can put our stuff
                ItemStack item;
                for(int i = 0; i < inv.getSize(); i++)
                {
                    item = inv.getItem(i);
                    // If there's no item: Lol, too easy ^^
                    if(item == #F_ITEMSTACK_NULL#)
                    {
                        inv.setItem(i, Util.copy_old(contents[2]));
                        contents[2] = null;
                        // And we can leave the loop
                        break;
                    }
                    // If there an item, then the materials have to match
                    else if(contents[2].doMaterialsMatch(item))
                    {
                        // Put away as much as possible
                        ---------- PRE 1.11 START ----------
                        int max = Util.min(contents[2].count, Util.min(item.getItem().getMaxStackSize(), getMaxStackSize()) - item.count);
                        item.count += max;
                        contents[2].count -= max;
                        if(contents[2].count <= 0)
                        ---------- PRE 1.11 END ----------
                        ---------- SINCE 1.11 START ----------
                        int icount = item.getCount();
                        int ccount = contents[2].getCount();
                        int max = Util.min(ccount, Util.min(item.getItem().getMaxStackSize(), getMaxStackSize()) - icount);
                        item.setCount(icount + max);
                        ccount -= max;
                        contents[2].setCount(ccount);
                        if(ccount <= 0)
                        ---------- SINCE 1.11 END ----------
                        // If we've put everything away
                        {
                            contents[2] = null;
                            // Then let's go away from here
                            break;
                        }
                    }
                }
            }
        }
        // Now, if we finally can burn, but we don't have fuel, then go and get some!
        if(canHasBURN() && !isBurning() && (getFuelTime(contents[1]) <= 0))
        {
            // Search for fuel
            ItemStack item;
            for(int i = 0; i < inv.getSize(); i++)
            {
                item = inv.getItem(i);
                // Is it fuel?
                if(getFuelTime(item) > 0)
                {
                    // Then take it!
                    item = Util.copy_old(item);
                    ItemStack item1 = Util.copy_old(contents[1]);
                    contents[1] = item;
                    inv.setItem(i, item1);
                    // And goodbye
                    break;
                }
            }
        }
        // If we couldn't do anything, don't check again until the chest contents are changed
        if(isFine())
        {
            lastCheck = 0;
        }
        else
        {
            lastCheck = inv.getLastUpdate();
        }
    }

    public boolean isFine()
    {
        return ((myCookTime > 0.0D) || (getFuelTime(contents[1]) > 0)) && canHasBURN();
    }

    // This needs a little addition
    public boolean isBurning()
    {
        return (bTime() > 0) && (burnSpeed > 0.0D) && canHasBURN();
    }

    private ItemStack getBurnResult(ItemStack stack)
    {
        if(stack == null || stack == #F_ITEMSTACK_NULL#)
        {
            return null;
        }
        // CUSTOM RECIPE HERE
        ---------- PRE 1.13 START ----------
        return RecipesFurnace.getInstance().getResult(stack); // Derpnote
        ---------- PRE 1.13 END ----------
        ---------- SINCE 1.13 START ----------
        FurnaceRecipe recipe = getRecipe(stack);
        return recipe != null ? recipe.#FIELD_IRECIPE_1#() : #F_ITEMSTACK_NULL#;
        ---------- SINCE 1.13 END ----------
    }

    private double getMeltSpeed(ItemStack item)
    {
        if(item == null || item == #F_ITEMSTACK_NULL#)
        {
            return 0.0D;
        }
        // CUSTOM RECIPE HERE
        return 1.0D;
    }

    private int getFuelTime(ItemStack item)
    {
        if(item == null || item == #F_ITEMSTACK_NULL#)
        {
            return 0;
        }
        Item type = item.getItem();
        // CUSTOM FUEL HERE
        // Lava should melt 128 items, not 100
        if(type == Items.LAVA_BUCKET)
        {
            return 25600;
        }
        else
        {
            return fuelTime(item);
        }
    }

    private double getBurnSpeed(ItemStack item)
    {
        if(item == null || item == #F_ITEMSTACK_NULL#)
        {
            return 0.0D;
        }
        // CUSTOM FUEL HERE
        return 1.0D;
    }

    private boolean canHasBURN()
    {
        // No ingredient, no recipe
        if(contents[0] == null)
        {
            return false;
        }
        ItemStack itemstack = getBurnResult(contents[0]);
        // No recipe, no burning
        if(itemstack == null)
        {
            return false;
        }
        // Free space? Let's burn!
        else if(contents[2] == null)
        {
            return true;
        }
        // Materials don't match? Too bad.
        else if(!contents[2].doMaterialsMatch(itemstack))
        {
            return false;
        }
        // As long as there is space, we can burn
        ---------- PRE 1.11 START ----------
        else if((contents[2].count + itemstack.count <= getMaxStackSize()) && (contents[2].count + itemstack.count <= contents[2].getMaxStackSize()))
        ---------- PRE 1.11 END ----------
        ---------- SINCE 1.11 START ----------
        else if((contents[2].getCount() + itemstack.getCount() <= getMaxStackSize()) && (contents[2].getCount() + itemstack.getCount() <= contents[2].getMaxStackSize()))
        ---------- SINCE 1.11 END ----------
        {
            return true;
        }
        return false;
    }

    public void burn()
    {
        // Can't burn? Goodbye
        if(!canHasBURN())
        {
            return;
        }
        ItemStack itemstack = getBurnResult(contents[0]);
        // Nothing in there? Then put something there.
        if(contents[2] == null)
        {
            contents[2] = Util.copy_old(itemstack);
        }
        // Burn ahead
        else if(contents[2].doMaterialsMatch(itemstack))
        {
            ---------- PRE 1.11 START ----------
            contents[2].count += itemstack.count;
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            contents[2].setCount(contents[2].getCount() + itemstack.getCount());
            ---------- SINCE 1.11 END ----------
        }
        // And consume the ingredient item
        // Goddamn, you have container functions, use them! Notch!
        if(contents[0].getItem().#FIELD_ITEM_1#()) // Derpnote
        {
            contents[0] = new ItemStack(contents[0].getItem().#FIELD_ITEM_2#()); // Derpnote
        }
        else
        {
            ---------- PRE 1.11 START ----------
            contents[0].count--;
            if(contents[0].count <= 0)
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            int count = contents[0].getCount() - 1;
            contents[0].setCount(count);
            if(count <= 0)
            ---------- SINCE 1.11 END ----------
            // Let 0 be null
            {
                contents[0] = null;
            }
        }
    }

    /***** The following methods are only here because they interact with the contents array, which is private *****/

    public ItemStack[] #F_GET_RAW_CONTENTS#()
    {
        return contents;
    }

    ---------- SINCE 1.11 START ----------
    public List<ItemStack> getContents()
    {
        return proxy;
    }
    ---------- SINCE 1.11 END ----------

    public int getSize()
    {
        return contents.length;
    }

    public ItemStack getItem(int i)
    {
        return contents[i] == null ? #F_ITEMSTACK_NULL# : contents[i];
    }

    public ItemStack splitStack(int i, int j)
    {
        if(contents[i] != null)
        {
            ItemStack itemstack;
            ---------- PRE 1.11 START ----------
            if(contents[i].count <= j)
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            if(contents[i].getCount() <= j)
            ---------- SINCE 1.11 END ----------
            {
                itemstack = contents[i];
                contents[i] = null;
                return itemstack == null ? #F_ITEMSTACK_NULL# : itemstack;
            }
            else
            {
                itemstack = contents[i].#FIELD_ITEMSTACK_3#(j); // Derpnote
                ---------- PRE 1.11 START ----------
                if(contents[i].count == 0)
                ---------- PRE 1.11 END ----------
                ---------- SINCE 1.11 START ----------
                if(contents[i].getCount() == 0)
                ---------- SINCE 1.11 END ----------
                {
                    contents[i] = null;
                }
                return itemstack == null ? #F_ITEMSTACK_NULL# : itemstack;
            }
        }
        else
        {
            return null;
        }
    }

    public ItemStack splitWithoutUpdate(int i)
    {
        if(contents[i] != null)
        {
            ItemStack itemstack = contents[i];
            contents[i] = null;
            return itemstack == null ? #F_ITEMSTACK_NULL# : itemstack;
        }
        else
        {
            return null;
        }
    }

    public void setItem(int i, ItemStack itemstack)
    {
        if(itemstack == #F_ITEMSTACK_NULL#)
        {
            itemstack = null;
        }
        contents[i] = itemstack;
        int max = getMaxStackSize();
        ---------- PRE 1.11 START ----------
        if(itemstack != null && itemstack.count > max)
            itemstack.count = max;
        ---------- PRE 1.11 END ----------
        ---------- SINCE 1.11 START ----------
        if(itemstack != null && itemstack.getCount() > max)
            itemstack.setCount(max);
        ---------- SINCE 1.11 END ----------
    }

    private int bTime() { return getProperty(0); }
    private void bTime(int i) { #F_SETPROPERTY#(0, i); }

    private int tfcf() { return getProperty(1); }
    private void tfcf(int i) { #F_SETPROPERTY#(1, i); }

    private int cTime() { return getProperty(2); }
    private void cTime(int i) { #F_SETPROPERTY#(2, i); }

    private int cookTimeTotal() { return getProperty(3); }
    private void cookTimeTotal(int i) { #F_SETPROPERTY#(3, i); }

    ---------- SINCE 1.13 START ----------
    // used by FIELD_TILEENTITYFURNACE_1
    private int proxyGetBurnTime(ItemStack stack)
    {
        FurnaceRecipe recipe = getRecipe(stack);
        return (recipe != null) ? recipe.#F_FURNACERECIPE_COOKINGTIME#() : 200;
    }

    private FurnaceRecipe getRecipe(ItemStack stack)
    {
        return (FurnaceRecipe)this.world.#F_WORLD_GETCRAFTINGMANAGER#().#F_CMNGR_RECIPE_FOR_INV#(new ProxyInv(stack), this.world);
    }

    private static class ProxyInv extends TileEntityFurnace
    {
        private final ItemStack _stack;

        private ProxyInv(ItemStack stack)
        {
            _stack = stack;
        }

        public ItemStack getItem(int i)
        {
            return i == 0 ? _stack : #F_ITEMSTACK_NULL#;
        }
    }

    public static int fuelTime(ItemStack stack)
    {
        try
        {
            return (Integer)fuelTimeMeth.invoke(null, stack);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }
    ---------- SINCE 1.13 END ----------

    // Compatibility
    public InventoryHolder getOwner()
    {
        return null;
    }

    public boolean #FIELD_IINVENTORY_1#(EntityHuman entityhuman) // Derpnote
    {
        return true;
    }

    public void onOpen(CraftHumanEntity who)
    {
    }

    public void onClose(CraftHumanEntity who)
    {
    }

    public List<HumanEntity> getViewers()
    {
        return new ArrayList<HumanEntity>();
    }
}

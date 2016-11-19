// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

---------- SINCE 1.11 START ----------

package net.drgnome.virtualpack.util;

import org.apache.commons.lang3.Validate;
import net.minecraft.server.v#MC_VERSION#.*;

public class ProxyList<E> extends NonNullList<E>
{
    protected final E[] elements;
    protected final E none;

    public ProxyList(E[] elements, E none)
    {
        super(null, null);
        this.elements = elements;
        this.none = none;
    }

    public E get(int n)
    {
        return elements[n] == null ? none : elements[n];
    }

    public E set(int n, E e)
    {
        Validate.<E>notNull(e);
        return elements[n] = e == none ? null : e;
    }

    public void add(int n, E e)
    {
        throw new UnsupportedOperationException("Inventory has fixed size");
    }

    public E remove(int n)
    {
        throw new UnsupportedOperationException("Inventory has fixed size");
    }

    public int size()
    {
        return elements.length;
    }

    public void clear()
    {
        for(int i = 0; i < elements.length; ++i)
        {
            elements[i] = null;
        }
    }
}

---------- SINCE 1.11 END ----------

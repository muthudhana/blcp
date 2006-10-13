package sparsearray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;
import java.io.*;

public class SparseArray<T> implements Serializable {

    public SparseArray()
    {
  //      sb = new SparseBitVector();
        ht = new Hashtable<Integer, T>();
    }

  //  private SparseBitVector sb = null;
    private Hashtable ht = null;
    private int maxIndex = -1;

    public Integer[] getIndicies()
    {
        ArrayList a = new ArrayList();
        Integer[] go = null;

        a.addAll( (Collection) ht.keySet() );
        go = new Integer[a.size()];
        go = (Integer[]) a.toArray( go );
        return go;
    }
    
    public Integer[] getIndiciesSorted()
    {
        ArrayList a = new ArrayList();
        Integer[] go = null;

        a.addAll( (Collection) ht.keySet() );
        go = new Integer[a.size()];
        go = (Integer[]) a.toArray( go );
        Arrays.sort(go);
        return go;
    }

    public void set(int key, T value)
    {
  //      sb.setBit( (long) key );
        if( key > maxIndex )
            maxIndex = key;
        ht.put( Integer.valueOf( key ), value );
    }

    public boolean remove(int key)
    {
//        if( !sb.isBitSet( (long) key ) )
//            return false;
//        else
//        {
//            sb.clearBit( (long) key );
            ht.remove( Integer.valueOf( key ) );
            if( maxIndex == key )
            {
                maxIndex = -1; // we'll calculate the new max index when needed
            }
            return true;
//        }
    }

    public T get(int key)
    {
//        if( sb.isBitSet( (long) key ) )
            return((T)ht.get(key));
//        else
//            return null;
    }

    public int size() {
//        if(maxIndex < 0 && sb.getPopCount() > 0){ //find max index
        if(maxIndex < 0 && ht.size() > 0){ //find max index
            Integer[] idx = getIndicies();
            Arrays.sort( idx );
            if(idx.length > 1){
                maxIndex = idx[idx.length - 1].intValue();
            } else {
                maxIndex = -2;
            }
        }
        return(maxIndex + 1);
    }

    public int getPopCount()
    {
//        return sb.getPopCount();
        return(ht.size());
    }

    public boolean contains(int key)
    {
//        return sb.isBitSet( (long) key );
        return(ht.containsKey(key));
    }
}

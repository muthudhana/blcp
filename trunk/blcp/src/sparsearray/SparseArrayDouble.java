/*
 * SparseArrayDouble.java
 *
 * Created on May 7, 2006, 2:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sparsearray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;
import java.io.*;
import gnu.trove.*;

public class SparseArrayDouble implements Serializable {

  public SparseArrayDouble() {
    this.ht = new TIntDoubleHashMap();
  }

  //  private SparseBitVector sb = null;
  private TIntDoubleHashMap ht = null;
  private int maxIndex = -1;

  public int[] getIndicies() {
    int[] keys = this.ht.keys();
    return (keys);
  }

  public int[] getIndiciesSorted() {
    int[] keys = this.getIndicies();
    Arrays.sort (keys);
    return (keys);
  }

  public void set (int key, double value) {
    if ( key > maxIndex )
      maxIndex = key;
    ht.put ( key, value );
  }

  public boolean remove (int key) {
    ht.remove (key);
    if ( maxIndex == key ) {
      maxIndex = -1; // we'll calculate the new max index when needed
    }
    return true;
  }

  public double get (int key) {
    return (ht.get (key) );
  }

  public int size() {
//        if(maxIndex < 0 && sb.getPopCount() > 0){ //find max index
    if (maxIndex < 0 && ht.size() > 0) { //find max index
      int[] idx = this.getIndiciesSorted();
      if (idx.length > 1) {
        maxIndex = idx[idx.length - 1];
      } else {
        maxIndex = -2;
      }
    }
    return (maxIndex + 1);
  }

  public int getPopCount() {
    return (ht.size() );
  }

  public boolean contains (int key) {
    return (ht.containsKey (key) );
  }
}


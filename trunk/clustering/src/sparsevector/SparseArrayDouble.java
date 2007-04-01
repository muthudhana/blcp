/*
 * @(#)SparseArrayDouble.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



package sparsevector;

import gnu.trove.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

/**
 * Class description
 *
 *
 * @version    Enter version here..., 04/01/07
 * @author     Mike Wiacek
 */
public class SparseArrayDouble implements Serializable {

  // private SparseBitVector sb = null;
  private TIntDoubleHashMap ht = null;
  private int maxIndex = -1;
  private boolean keyCacheDirty = true;
  private int[] keyCache = null;

  /**
   * Constructs ...
   *
   */
  public SparseArrayDouble () {
    this.ht = new TIntDoubleHashMap();
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public boolean contains (int key) {
    return (ht.containsKey(key));
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public boolean remove (int key) {
    ht.remove(key);

    if (maxIndex == key) {
      maxIndex = -1;    // we'll calculate the new max index when needed
    }

    this.keyCacheDirty = true;

    return true;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int size () {

//        if(maxIndex < 0 && sb.getPopCount() > 0){ //find max index
    if ((maxIndex < 0) && (ht.size() > 0)) {    // find max index
      int[] idx = this.getIndiciesSorted();

      if (idx.length > 1) {
        maxIndex = idx[idx.length - 1];
      } else {
        maxIndex = -2;
      }
    }

    return (maxIndex + 1);
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public double get (int key) {
    return (ht.get(key));
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int[] getIndicies () {
    if (keyCacheDirty == true) {
      this.keyCache = this.ht.keys();

      Arrays.sort(this.keyCache);

      this.keyCacheDirty = false;
    }

    return (this.keyCache);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int[] getIndiciesSorted () {
    return (this.getIndicies());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getPopCount () {
    return (ht.size());
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  public void set (int key,
                   double value) {
    if (key > maxIndex) {
      maxIndex = key;
    }

    this.keyCacheDirty = true;

    ht.put(key, value);
  }
}

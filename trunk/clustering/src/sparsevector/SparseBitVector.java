/*
 * @(#)SparseBitVector.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



/*
 * SparseBitVector.java
 *
 * Created on February 22, 2006, 12:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sparsevector;

import gnu.trove.*;

import java.io.*;

import java.util.*;

/**
 *
 * @author mike
 */
public class SparseBitVector implements Serializable {
  private int globalPopCount = 0;

  // private SkipList bitVector = null;
  private TLongLongHashMap bitVector = null;

  /** Creates a new instance of SparseBitVector */
  public SparseBitVector () {

    // this.bitVector = new SkipList(1000);
    this.bitVector = new TLongLongHashMap();
  }

  /**
   * Method description
   *
   *
   * @param bitNum
   */
  public void clearBit (long bitNum) {
    long bucketNum = (long) (bitNum / 32L);
    int offsetInBucket = (int) (bitNum % 32L);

    // long result = this.bitVector.search(bucketNum);
    Long result = this.bitVector.get(bucketNum);

    if ((result & (1L << offsetInBucket)) != 0) {

      // Remove this key
      --this.globalPopCount;

      long mask = ~(1L << offsetInBucket);

      result = result & mask;

      if (result == 0L) {

        // this.bitVector.delete((int)bucketNum);
        this.bitVector.remove(bucketNum);
      } else {

        // this.bitVector.insert((int)bucketNum, result);
        this.bitVector.put(bucketNum, result);
      }
    }

    return;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getPopCount () {
    return globalPopCount;
  }

  /**
   * Method description
   *
   *
   * @param bitNum
   *
   * @return
   */
  public boolean isBitSet (long bitNum) {
    long bucketNum = (long) (bitNum / 32L);
    int offsetInBucket = (int) (bitNum % 32L);

    // long result = this.bitVector.search(bucketNum);
    long result = this.bitVector.get(bucketNum);

    // if(result == SkipList.NOT_FOUND){
    if (result == 0L) {
      return (false);
    } else if ((result & (1L << offsetInBucket)) != 0) {
      return (true);
    } else {
      return (false);
    }
  }

  /**
   * Method description
   *
   *
   * @param bitNum
   */
  public void setBit (long bitNum) {
    long bucketNum = (long) (bitNum / 32L);
    int offsetInBucket = (int) (bitNum % 32L);

    // long result = this.bitVector.search(bucketNum);
    long result = this.bitVector.get(bucketNum);

    if ((result & (1L << offsetInBucket)) == 0) {
      ++this.globalPopCount;    // count this bit if we haven't already
    }

    // Add this bit!
    result = result | (1L << offsetInBucket);

    // this.bitVector.insert(bucketNum, result);
    this.bitVector.put(bucketNum, result);

    return;
  }
}

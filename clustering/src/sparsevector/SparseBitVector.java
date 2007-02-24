/*
 * SparseBitVector.java
 *
 * Created on February 22, 2006, 12:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sparsevector;
import java.util.*;
import java.io.*;
import gnu.trove.*;


/**
 *
 * @author mike
 */
public class SparseBitVector implements Serializable {

  private int globalPopCount = 0;
  // private SkipList bitVector = null;
  private TLongLongHashMap bitVector = null;

  /** Creates a new instance of SparseBitVector */
  public SparseBitVector() {
    // this.bitVector = new SkipList(1000);
    this.bitVector = new TLongLongHashMap();
  }

  public boolean isBitSet (long bitNum) {
    long bucketNum = (long) (bitNum / 32L);
    int offsetInBucket = (int) (bitNum % 32L);

    // long result = this.bitVector.search(bucketNum);
    long result = this.bitVector.get (bucketNum);

    //if(result == SkipList.NOT_FOUND){
    if (result == 0L) {
      return (false);
    } else if ( (result & (1L << offsetInBucket) ) != 0) {
      return (true);
    } else {
      return (false);
    }
  }

  public void setBit (long bitNum) {
    long bucketNum = (long) (bitNum / 32L);
    int offsetInBucket = (int) (bitNum % 32L);

    // long result = this.bitVector.search(bucketNum);
    long result = this.bitVector.get (bucketNum);

    if ( (result & (1L << offsetInBucket) ) == 0) {
      ++this.globalPopCount; // count this bit if we haven't already
    }

    // Add this bit!
    result = result | (1L << offsetInBucket);
    // this.bitVector.insert(bucketNum, result);
    this.bitVector.put (bucketNum, result);

    return;
  }

  public void clearBit (long bitNum) {
    long bucketNum = (long) (bitNum / 32L);
    int offsetInBucket = (int) (bitNum % 32L);

    // long result = this.bitVector.search(bucketNum);
    Long result = this.bitVector.get (bucketNum);

    if ( (result & (1L << offsetInBucket) ) != 0) {
      // Remove this key
      --this.globalPopCount;

      long mask = ~ (1L << offsetInBucket);
      result = result & mask;

      if (result == 0L) {
        // this.bitVector.delete((int)bucketNum);
        this.bitVector.remove (bucketNum);
      } else {
        // this.bitVector.insert((int)bucketNum, result);
        this.bitVector.put (bucketNum, result);
      }
    }
    return;
  }

  public int getPopCount() {
    return globalPopCount;
  }


}

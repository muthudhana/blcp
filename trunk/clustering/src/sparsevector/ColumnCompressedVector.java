/*
 * @(#)ColumnCompressedVector.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



/*
 * ColumnCompressedVector.java
 *
 * Created on March 6, 2006, 10:43 AM
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
public class ColumnCompressedVector implements Serializable {
  private static final long serialVersionUID = 123456789012345L;
  private TDoubleArrayList values = null;
  private SparseBitVector sb = null;
  private TIntArrayList rowIndex = null;
  private int maxElement = -1;

  /** Creates a new instance of ColumnCompressedVector */
  public ColumnCompressedVector () {
    this.values = new TDoubleArrayList();
    this.rowIndex = new TIntArrayList();
    this.sb = new SparseBitVector();
  }

  /**
   * Copy constructor
   *
   * @param origin
   */
  public ColumnCompressedVector (ColumnCompressedVector origin) {
    this.values = origin.getCopyOfValuesVector();
    this.rowIndex = origin.getCopyOfRowVector();

    for (int i = 0; i < this.rowIndex.size(); ++i) {
      int k = this.rowIndex.get(i);

      this.sb.setBit((long) k);
    }
  }

  /**
   * Method description
   *
   *
   * @param operand
   */
  public void add (ColumnCompressedVector operand) {
    TIntArrayList idx = operand.getIndiciesRef();

    for (int i = 0; i < idx.size(); ++i) {
      Double sum = this.get(idx.get(i));

      sum += operand.get(idx.get(i));

      this.set(idx.get(i), sum);
    }
  }

  /**
   * Method description
   *
   *
   * @param elementNum
   *
   * @return
   */
  public boolean contains (int elementNum) {
    return (sb.isBitSet(elementNum));
  }

  /**
   * Method description
   *
   *
   * @param ccv
   *
   * @return
   */
  public double distance (ColumnCompressedVector ccv) {
    ColumnCompressedVector copy = new ColumnCompressedVector(this);

    copy.subtract(ccv);

    return (copy.length());
  }

  /**
   * Method description
   *
   *
   * @param ccv
   *
   * @return
   */
  public double distanceSquared (ColumnCompressedVector ccv) {
    double dist = this.distance(ccv);

    return (dist * dist);
  }

  /**
   * Method description
   *
   *
   * @param rvalue
   *
   * @return
   */
  public double dotProduct (ColumnCompressedVector rvalue) {

    TIntArrayList rkeys = rvalue.getIndiciesRef();
    TIntArrayList lkeys = this.getIndiciesRef();

    double dotProduct = 0.0;

    if (rkeys.size() > lkeys.size()) {
      for (int i = 0; i < lkeys.size(); ++i) {
        dotProduct += this.get(lkeys.get(i)) * rvalue.get(lkeys.get(i));
      }
    } else {
      for (int i = 0; i < rkeys.size(); ++i) {
        dotProduct += this.get(rkeys.get(i)) * rvalue.get(rkeys.get(i));
      }
    }

    return (dotProduct);

  }

  /**
   * Method description
   *
   *
   * @return
   */
  public double length () {
    return (Math.sqrt(this.dotProduct(this)));
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public double lengthSquared () {
    return (this.dotProduct(this));
  }

  /**
   * Method description
   *
   */
  public void normalize () {
    double normFactor = 0.0;

    for (int i = 0; i < this.values.size(); ++i) {
      double val = this.values.get(i) * this.values.get(i);

      normFactor += val;
    }

    normFactor = Math.sqrt(normFactor);

    for (int i = 0; i < this.values.size(); ++i) {
      double val = this.values.get(i);

      val = val / normFactor;

      this.values.set(i, val);
    }

    return;
  }

  // Here to keep SparseVector objects based on ColumnCompressedVectors happy

  /**
   * Method description
   *
   *
   * @param key
   */
  public void remove (int key) {
    this.set(key, 0D);
  }

  /**
   * Method description
   *
   *
   * @param operand
   */
  public void scalarDivide (double operand) {
    this.scalarMultiply(1D / operand);
  }

  /**
   * Method description
   *
   *
   * @param operand
   */
  public void scalarMultiply (double operand) {
    TIntArrayList idx = this.getIndiciesRef();

    for (int i = 0; i < idx.size(); ++i) {
      Double product = this.get(idx.get(i));

      product *= operand;

      this.set(idx.get(i), product);
    }
  }

  /**
   * Returns the number of rows in this vector if it were a real vector.
   *
   * @return
   */
  public int size () {
    return (this.maxElement + 1);
  }

  /**
   * Method description
   *
   *
   * @param operand
   */
  public void subtract (ColumnCompressedVector operand) {
    TIntArrayList idx = operand.getIndiciesRef();

    for (int i = 0; i < idx.size(); ++i) {
      Double sum = this.get(idx.get(i));

      sum -= operand.get(idx.get(i));

      this.set(idx.get(i), sum);
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String toString () {
    String s = "Vector Contents:\n";
    int[] idx = this.getIndicies();

    for (int i = 0; i < idx.length; ++i) {
      s += "[" + idx[i] + "] = " + this.get(idx[i]) + "\n";
    }

    return (s);
  }

  /**
   * Method description
   *
   *
   * @param elementNum
   *
   * @return
   */
  public double get (int elementNum) {

    if (sb.isBitSet(elementNum) == false) {
      return (0D);
    }

    int numRows = this.rowIndex.size();

    for (int i = 0; i < numRows; ++i) {
      if (this.rowIndex.get(i) == elementNum) {
        return (this.values.get(i));
      }
    }

    // Should never get here... Should return fast on 0 values above.
    return (0D);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public TIntArrayList getCopyOfRowVector () {
    return new TIntArrayList(this.rowIndex.toNativeArray());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public TDoubleArrayList getCopyOfValuesVector () {
    return new TDoubleArrayList(this.values.toNativeArray());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int[] getIndicies () {
    return this.rowIndex.toNativeArray();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public TIntArrayList getIndiciesRef () {
    return this.rowIndex;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int[] getIndiciesSorted () {
    return this.getIndicies();
  }

  /**
   * Returns the number of elements of the underlying array that have a non
   * default value. In other words the number of elements that have been
   * explicity assigned a value! Elements that were explicity set to 0.0
   * are not counted in this value!
   *
   * Make sure you want .getPopCount() and NOT .size()
   *
   * @return
   */
  public int getPopCount () {
    return (sb.getPopCount());
  }

  /**
   * Method description
   *
   *
   * @param elementNum
   * @param value
   */
  public void set (int elementNum,
                   double value) {

    // CRITICAL SECTION -- THIS IS NOT THREAD SAFE!

    if (sb.isBitSet(elementNum) == true) {

      // we are updating...
      if (value == 0D) {

        // we are setting an existing value to 0
        sb.clearBit(elementNum);

        for (int i = 0; i < this.rowIndex.size(); ++i) {
          if (this.rowIndex.get(i) == elementNum) {
            this.values.set(i, 0D);

            break;
          }
        }
      } else {

        // we are performing a routine update!
        for (int i = 0; i < this.rowIndex.size(); ++i) {
          if (this.rowIndex.get(i) == elementNum) {
            this.values.set(i, value);

            break;
          }
        }
      }
    } else {
      if (value != 0D) {    // we are inserting a new element
        sb.setBit(elementNum);

        if (elementNum > this.maxElement) {
          this.maxElement = elementNum;
        }

        this.rowIndex.add(elementNum);

        int index = rowIndex.size() - 1;

        if (index >= 0) {
          this.values.add(value);
        } else {
          System.out.println("I SHOULD NEVER GET HERE!");
          System.exit(-1);
        }
      }
    }

    // END CRITICAL SECTION -- CODE ABOVE NOT THREAD SAFE!
  }
}

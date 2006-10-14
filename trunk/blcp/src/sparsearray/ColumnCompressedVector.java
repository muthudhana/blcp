/*
 * ColumnCompressedVector.java
 *
 * Created on March 6, 2006, 10:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sparsearray;
import java.util.*;
import java.io.*;

/**
 *
 * @author mike
 */
public class ColumnCompressedVector implements Serializable {

  private static final long serialVersionUID = 123456789012345L;

  private ArrayList<Double> values = null;
  private ArrayList<Integer> rowIndex = null;
  private SparseBitVector sb = null;

  private int maxElement = -1;

  /** Creates a new instance of ColumnCompressedVector */
  public ColumnCompressedVector() {
    this.values = new ArrayList<Double>();
    this.rowIndex = new ArrayList<Integer>();
    this.sb = new SparseBitVector();
  }

  /** Copy constructor */
  public ColumnCompressedVector (ColumnCompressedVector origin) {
    this.values = origin.getCopyOfValuesVector();
    this.rowIndex = origin.getCopyOfRowVector();
    Iterator i = this.rowIndex.iterator();
    while (i.hasNext() ) {
      Integer k = (Integer) i.next();
      this.sb.setBit (k);
    }
  }

  public ArrayList<Double> getCopyOfValuesVector() {
    return (new ArrayList<Double> (this.values) );
  }

  public ArrayList<Integer> getCopyOfRowVector() {
    return (new ArrayList<Integer> (this.rowIndex) );
  }

  public boolean contains (int elementNum) {
    return (sb.isBitSet (elementNum) );
  }

  /**
   * Returns the number of elements of the underlying array that have a non
   * default value. In other words the number of elements that have been
   * explicity assigned a value! Elements that were explicity set to 0.0
   * are not counted in this value!
   *
   * Make sure you want .getPopCount() and NOT .size()
   */
  public int getPopCount() {
    return (sb.getPopCount() );
  }

  public void add (ColumnCompressedVector operand) {
    ArrayList<Integer> idx = operand.getIndiciesRef();
    for (int i = 0; i < idx.size(); ++i) {
      Double sum = this.get (idx.get (i) );
      sum += operand.get (idx.get (i) );
      this.set (idx.get (i), sum);
    }
  }

  public void scalarMultiply (double operand) {
    ArrayList<Integer> idx = this.getIndiciesRef();
    for (int i = 0; i < idx.size(); ++i) {
      Double product = this.get (idx.get (i) );
      product *= operand;
      this.set (idx.get (i), product);
    }
  }

  public void scalarDivide (double operand) {
    this.scalarMultiply (1D / operand);
  }

  public void subtract (ColumnCompressedVector operand) {
    ArrayList<Integer> idx = operand.getIndiciesRef();
    for (int i = 0; i < idx.size(); ++i) {
      Double sum = this.get (idx.get (i) );
      sum -= operand.get (idx.get (i) );
      this.set (idx.get (i), sum);
    }
  }

  public double distanceSquared (ColumnCompressedVector ccv) {
    double dist = this.distance (ccv);
    return (dist * dist);
  }

  public double distance (ColumnCompressedVector ccv) {
    ColumnCompressedVector copy = new ColumnCompressedVector (this);
    copy.subtract (ccv);
    return (copy.length() );
  }

  public double length() {
    return (Math.sqrt (this.dotProduct (this) ) );
  }

  public double lengthSquared() {
    return (this.dotProduct (this) );
  }

  public void normalize() {
    Iterator itr = this.values.iterator();
    double normFactor = 0.0;
    while (itr.hasNext() ) {
      double val = (Double) itr.next();
      val *= val;
      normFactor += val;
    }

    normFactor = Math.sqrt (normFactor);

    for (int i = 0; i < this.values.size(); ++i) {
      double val = this.values.get (i);
      val = val / normFactor;
      this.values.set (i, val);
    }

    return;
  }

  public Double get (int elementNum) {

    if (sb.isBitSet (elementNum) == false) {
      return (0D);
    }

    int numRows = this.rowIndex.size();
    for (int i = 0; i < numRows; ++i) {
      if (this.rowIndex.get (i) == elementNum) {
        return (this.values.get (i) );
      }
    }

    // Should never get here... Should return fast on 0 values above.
    return (0D);
  }

  public ArrayList<Integer> getIndiciesRef() {
    return (this.rowIndex);
  }

  public Integer[] getIndicies() {
    Integer[] idx = new Integer[rowIndex.size() ];
    Iterator itr = rowIndex.iterator();
    int i = 0;
    while (itr.hasNext() ) {
      idx[i++] = (Integer) itr.next();
    }

    Arrays.sort (idx);

    return (idx);
  }

  /**
   * Returns the number of rows in this vector if it were a real vector.
   */
  public int size() {
    return (this.maxElement + 1);
  }

  public void set (int elementNum, Double value) {

    // CRITICAL SECTION -- THIS IS NOT THREAD SAFE!

    if (sb.isBitSet (elementNum) == true) {
      // we are updating...
      if (value == 0D) {
        // we are setting an existing value to 0
        sb.clearBit (elementNum);
        for (int i = 0; i < this.rowIndex.size(); ++i) {
          if (this.rowIndex.get (i) == elementNum) {
            this.values.set (i, 0D);
            break;
          }
        }
      } else {
        // we are performing a routine update!
        for (int i = 0; i < this.rowIndex.size(); ++i) {
          if (this.rowIndex.get (i) == elementNum) {
            this.values.set (i, value);
            break;
          }
        }
      }
    } else {
      if (value != 0D) { // we are inserting a new element
        sb.setBit (elementNum);

        if (elementNum > this.maxElement) {
          this.maxElement = elementNum;
        }

        this.rowIndex.add (elementNum);
        int index = rowIndex.size() - 1;

        if (index >= 0) {
          this.values.add (value);
        } else {
          System.out.println ("I SHOULD NEVER GET HERE!");
          System.exit (-1);
        }
      }
    }
    // END CRITICAL SECTION -- CODE ABOVE NOT THREAD SAFE!
  }

  public double dotProduct (ColumnCompressedVector rvalue) {

    ArrayList<Integer> rkeys = rvalue.getIndiciesRef();
    ArrayList<Integer> lkeys = this.getIndiciesRef();

    double dotProduct = 0.0;

    if (rkeys.size() > lkeys.size() ) {
      for (int i = 0; i < lkeys.size(); ++i) {
        dotProduct += this.get (lkeys.get (i) ) * rvalue.get (lkeys.get (i) );
      }
    } else {
      for (int i = 0; i < rkeys.size(); ++i) {
        dotProduct += this.get (rkeys.get (i) ) * rvalue.get (rkeys.get (i) );
      }
    }

    return (dotProduct);

  }

  public String toString() {
    String s = "Vector Contents:\n";
    Integer[] idx = this.getIndicies();
    for (int i = 0; i < idx.length; ++i) {
      s += "[" + idx[i] + "] = " + this.get (idx[i]) + "\n";
    }
    return (s);
  }

}

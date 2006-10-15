/*
 * OriginalSparseVector.java
 *
 * Created on February 26, 2006, 10:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sparsevector;

import java.util.Arrays;

/**
 *
 * @author mike
 */
public class OriginalSparseVector extends SparseArray<Double> {

  public OriginalSparseVector (OriginalSparseVector origin) {
    Integer[] idx = origin.getIndicies();
    for (int i = 0; i < idx.length; ++i) {
      this.set (idx[i], origin.get (idx[i]) );
    }
  }

  public void add (OriginalSparseVector operand) {
    Integer[] idx = operand.getIndicies();
    for (int i = 0; i < idx.length; ++i) {
      Double sum = this.get (idx[i]);
      sum += operand.get (idx[i]);
      this.set (idx[i], sum);
    }
  }

  public void subtract (OriginalSparseVector operand) {
    Integer[] idx = operand.getIndicies();
    for (int i = 0; i < idx.length; ++i) {
      Double sum = (Double) this.get (idx[i]);
      sum -= operand.get (idx[i]);
      this.set (idx[i], sum);
    }
  }

  public void normalize() {
    Integer[] idx = this.getIndicies();
    double normalizationFactor = 0;
    for (int i = 0; i < idx.length; ++i) {
      Double k = this.get (idx[i]);
      normalizationFactor += k * k;
    }
    normalizationFactor = Math.sqrt (normalizationFactor);
    for (int i = 0; i < idx.length; ++i) {
      this.set (idx[i], this.get (idx[i]) / normalizationFactor);
    }
  }

  public Double get (int key) {
    Double val = super.get (key);
    if (val == null) {
      return (0.0);
    } else {
      return (val);
    }
  }

  public double dotProduct (OriginalSparseVector rvalue) throws Exception {
    if (rvalue.size() != this.size() ) {
      throw new SparseVectorDimensionMismatch ("");
    }

    Integer[] rkeys = rvalue.getIndicies();
    Integer[] lkeys = this.getIndicies();

    double dotProduct = 0.0;

    if (rkeys.length > lkeys.length) {
      for (int i = 0; i < lkeys.length; ++i) {
        dotProduct += this.get (lkeys[i]) * rvalue.get (lkeys[i]);
      }
    } else {
      for (int i = 0; i < rkeys.length; ++i) {
        dotProduct += this.get (rkeys[i]) * rvalue.get (rkeys[i]);
      }
    }

    return (dotProduct);

  }

  public void set (int key, Double value) {
    if (value == 0) {  // 0 is a default value... remove if value = 0
      super.remove (key);
    } else {
      super.set (key, value);
    }
  }

  /** Creates a new instance of OriginalSparseVector */
  public OriginalSparseVector() {
  }

  public String toString() {
    String s = "Vector Contents:\n";
    Integer[] idx = this.getIndicies();
    Arrays.sort (idx);
    for (int i = 0; i < idx.length; ++i) {
      s += "[" + idx[i] + "] = " + this.get (idx[i]) + "\n";
    }
    return (s);
  }

}

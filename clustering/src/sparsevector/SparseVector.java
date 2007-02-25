/*
 * SparseVector.java
 *
 * Created on February 26, 2006, 10:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sparsevector;

import java.util.Arrays;
import java.io.*;

/**
 *
 * @author mike
 */
// public class SparseVector extends SparseArray<Double> implements Serializable{

public class SparseVector extends SparseArrayDouble implements Serializable {
  private static final long serialVersionUID = 1370252991810615959L;
  private double sumSquareCoordinates = 0.0;
//    private int popCount = 0;

  public SparseVector (SparseVector origin) {
    int[] idx = origin.getIndicies();
    for (int i = 0; i < idx.length; ++i) {
      this.set (idx[i], origin.get (idx[i]) );
    }
  }

  public void add (SparseVector operand) {
    int[] idx = operand.getIndicies();
    for (int i = 0; i < idx.length; ++i) {
      double sum = this.get (idx[i]);
      sum += operand.get (idx[i]);
      this.set (idx[i], sum);
    }
  }

  public void subtract (SparseVector operand) {
    int[] idx = operand.getIndicies();
    for (int i = 0; i < idx.length; ++i) {
      double sum = (Double) this.get (idx[i]);
      sum -= operand.get (idx[i]);
      this.set (idx[i], sum);
    }
  }

  public void scalarMultiply (double scalar) {
    int[] idx = this.getIndicies();
    for (int i = 0; i < idx.length; ++i) {
      double product = (Double) this.get (idx[i]);
      product *= scalar;
      this.set (idx[i], product);
    }
  }

  public void scalarDivide (double scalar) {
    this.scalarMultiply (1D / scalar);
  }

  public double distanceSquared (SparseVector sv) {
    double dist = this.distance (sv);
    return (dist * dist);
  }

  public double distance (SparseVector sv) {

    double dist = 0.0;

    int[] idx = this.getIndiciesSorted();
    int[] opIdx = sv.getIndiciesSorted();

    int i = 0;
    int j = 0;

//        System.out.println("Calculating distance!");

    while (i < idx.length || j < opIdx.length) {
      if (i < idx.length && j < opIdx.length) {
        if (idx[i] == opIdx[j]) {
          double t = this.get (idx[i]);
          double s = sv.get (opIdx[j]);
          dist += (t - s) * (t - s);
          ++i;
          ++j;
        } else if (idx[i] < opIdx[j]) {
          double t = this.get (idx[i]);
          dist += t * t;
          ++i;
        } else if (idx[i] > opIdx[j]) {
          double s = sv.get (opIdx[j]);
          dist += s * s;
          ++j;
        }

//                System.out.println("i = " + i);

      } else {

        /* One array or the other is finished, so let's just
         * eat up whats left and add it to dist.
         */

        if (i < idx.length && j >= opIdx.length) {
          for (; i < idx.length; ++i) {
            double val = this.get (idx[i]);
            dist += val * val;
          }
        }

        if (i >= idx.length && j < opIdx.length) {
          for (; j < opIdx.length; ++j) {
            double val = sv.get (opIdx[j]);
            dist += val * val;
          }
        }
      }
    }
// System.out.println("Distance calculated!");
    return (Math.sqrt (dist) );
//        SparseVector copy = new SparseVector(this);
//        copy.subtract(sv);
//        return(copy.length());
  }

  public void normalize() {
    int[] idx = this.getIndicies();
    double normalizationFactor = this.length();

    for (int i = 0; i < idx.length; ++i) {
      this.set (idx[i], this.get (idx[i]) / normalizationFactor);
    }
  }

  public double get (int key) {
    Double val = super.get (key);
    if (val == null) {
      return (0.0);
    } else {
      return (val);
    }
  }

  public double length() {
    return (Math.sqrt (this.sumSquareCoordinates) );
    // return(Math.sqrt(this.dotProduct(this)));
  }

  public double lengthSquared() {
    return (this.sumSquareCoordinates);
    // return(this.dotProduct(this));
  }

  public double dotProduct (SparseVector rvalue) {

    int[] rkeys = rvalue.getIndicies();
    int[] lkeys = this.getIndicies();

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

  public void set (int key, double value) {
    if (value == 0D) {  // 0 is a default value... remove if value = 0
      double curVal = this.get (key);
      this.sumSquareCoordinates -= curVal * curVal;
      super.remove (key);
    } else {
      double curVal = this.get (key);
      if (curVal != 0D) {
        this.sumSquareCoordinates -= curVal * curVal;
      }
      this.sumSquareCoordinates += value * value;
      super.set (key, value);
    }
  }

  /** Creates a new instance of SparseVector */
  public SparseVector() {
  }

  public String toString() {
    String s = "Vector Contents:\n";
    int[] idx = this.getIndicies();
    Arrays.sort (idx);
    for (int i = 0; i < idx.length; ++i) {
      s += "[" + idx[i] + "] = " + this.get (idx[i]) + "\n";
    }
    return (s);
  }

}

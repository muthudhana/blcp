/*
 * TermVarianceStructure.java
 *
 * Created on April 23, 2006, 1:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package math710;

import java.util.*;
import java.io.*;

/**
 *
 * @author mike
 */
public class TermVarianceStructure implements Comparable, Comparator, Serializable {
  public int termId = -1;
  public double variance = 0.0;

  public TermVarianceStructure() {}

  /**
   * This compare returns the opposite of what one would expect. If x is greater than y
   * then it returns negative, if y is greater than x it returns positive. If x == y then
   * it returns 0.  This is because this object is used to build a max priority queue!
   */
  public int compare (Object x, Object y) {
    TermVarianceStructure t = (TermVarianceStructure) x;
    TermVarianceStructure s = (TermVarianceStructure) y;
    return (Double.compare (s.variance, t.variance) );
  }

  public int compareTo (Object x) {
    return (this.compare (this, (TermVarianceStructure) x) );
  }
}

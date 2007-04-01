/*
 * @(#)TermVarianceStructure.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



/*
 * TermVarianceStructure.java
 *
 * Created on April 23, 2006, 1:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package clusterbase;

import java.io.*;

import java.util.*;

/**
 *
 * @author mike
 */
public class TermVarianceStructure
    implements Comparable, Comparator, Serializable {
  public int termId = -1;
  public double variance = 0.0;

  /**
   * Constructs ...
   *
   */
  public TermVarianceStructure () {}

  /**
   * This compare returns the opposite of what one would expect. If x is greater than y
   * then it returns negative, if y is greater than x it returns positive. If x == y then
   * it returns 0.  This is because this object is used to build a max priority queue!
   *
   * @param x
   * @param y
   *
   * @return
   */
  public int compare (Object x,
                      Object y) {
    TermVarianceStructure t = (TermVarianceStructure) x;
    TermVarianceStructure s = (TermVarianceStructure) y;

    return (Double.compare(s.variance, t.variance));
  }

  /**
   * Method description
   *
   *
   * @param x
   *
   * @return
   */
  public int compareTo (Object x) {
    return (this.compare(this, (TermVarianceStructure) x));
  }
}

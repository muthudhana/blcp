/*
 * @(#)SkipListElement.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



package sparsevector;

//
//File:        SkipListElement.java
//
//Language:    Java 1.02
//Description: Class for a single node in a "SkipList"
//        as proposed by William Pugh
//
//        To get faster data access to the nodes, you have to direct
//        access data.
//
//Author:  Thomas Wenger, Jan-7-1998
//

/**
 * Class description
 *
 *
 * @version    Enter version here..., 04/01/07
 * @author     Enter your name here...
 */
public class SkipListElement {
  SkipListElement forward[];    // array of forward pointers

  // /////////////////////////////////////////////////////////////////////////
  // accessible data members:
  // access is "friendly", this way all classes in this package can access
  // the data member directly: performance is increased
  long key;      // key data (sort and search criterion)
  long value;    // associated value

  // /////////////////////////////////////////////////////////////////////////
  // Constructor:
  // Constructs a new element of a skip list.
  // level, key and value of the new node are given
  //

  /**
   * Constructs ...
   *
   *
   * @param level
   * @param key
   * @param value
   */
  public SkipListElement (int level,
                          long key,
                          long value) {
    this.key = key;
    this.value = value;
    forward = new SkipListElement[level + 1];
  }

  // /////////////////////////////////////////////////////////////////////////
  // toString() overwrites java.lang.Object.toString()
  // composes a multiline-string describing this node:
  //

  /**
   * Method description
   *
   *
   * @return
   */
  public String toString () {
    String result = "";

    // element data:
    result += " k:" + key + ", v:" + value + ", l:" + getLevel() + ". fwd k: ";

    // key of forward pointed nodes:
    for (int i = 0; i <= getLevel(); i++) {
      if (forward[i] != null) {
        result += i + ": " + forward[i].key + ", ";
      } else {
        result += i + ": nil, ";
      }
    }

    return result;
  }

  // /////////////////////////////////////////////////////////////////////////
  // getLevel():
  // returns the level of this node (count starting at 0)
  //
  int getLevel () {
    return forward.length - 1;
  }
}

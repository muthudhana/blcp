/*
 * @(#)DocumentTimeStruct.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



package clusterbase;

import java.io.*;

/**
 * Class description
 *
 *
 * @version    Enter version here..., 04/01/07
 * @author     Mike Wiacek
 */
public class DocumentTimeStruct implements Comparable, Serializable {
  private String filename = null;
  private long timestamp = 0;

  /**
   * Constructs ...
   *
   *
   * @param doc
   */
  public DocumentTimeStruct (Document doc) {
    this.filename = doc.getFilename();
    this.timestamp = doc.getTimestamp();
  }

  /**
   * Creates a new instance of DocumentTimeStruct
   *
   * @param fname
   * @param ts
   */
  public DocumentTimeStruct (String fname,
                             long ts) {
    this.filename = new String(fname);
    this.timestamp = ts;
  }

  /**
   * Method description
   *
   *
   * @param obj
   *
   * @return
   */
  public int compareTo (Object obj) {
    DocumentTimeStruct t = (DocumentTimeStruct) obj;

    if (this.timestamp < t.getTimestamp()) {
      return -1;
    }

    if (this.timestamp == t.getTimestamp()) {
      return 0;
    }

    // t.timestamp > this.timestamp.
    return 1;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getFilename () {
    return this.filename;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public long getTimestamp () {
    return this.timestamp;
  }
}

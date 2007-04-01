/*
 * @(#)BirchClusterOptions.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



package blc;

import java.io.*;

import java.util.*;

enum ClusteringApproach implements Serializable{NOTHING,
                                                GREEDY_ALLOCATION_FORWARD,
                                                GREEDY_ALLOCATION_BACKWARD,
                                                BEST_FIT_ALLOCATION,
                                                REASONABLE_EFFORT_FORWARD,
                                                REASONABLE_EFFORT_BACKWARD}

enum ClusteringOrder implements Serializable{NOTHING, TIMESTAMP_FORWARD,
                                             TIMESTAMP_REVERSE, RANDOM}

enum TermReductionApproach implements Serializable{USE_TERM_REDUCTION,
                                                   NO_TERM_REDUCTION}

/**
 * Class description
 *
 *
 * @version    Enter version here..., 04/01/07
 * @author     Mike Wiacek
 */
public class BirchClusterOptions implements Serializable {
  private ClusteringOrder clusteringOrder = ClusteringOrder.NOTHING;
  private TermReductionApproach termReductionApproach =
    TermReductionApproach.NO_TERM_REDUCTION;
  private int termLimit = -1;
  private Hashtable<String, String> miscStringSettings = null;
  private double maxTrialsForReasonableEffort = -1.0;
  private int maxClusterSize = -1;
  private ClusteringApproach clusteringApproach = ClusteringApproach.NOTHING;
  private double capacityFraction = -1.0;

  /**
   * Constructs ...
   *
   */
  public BirchClusterOptions () {
    this.miscStringSettings = new Hashtable<String, String>();
  }

  /**
   * Method description
   *
   *
   * @param filename
   *
   * @return
   *
   * @throws Exception
   */
  public static BirchClusterOptions deserializeBirchClusterOptions (
      String filename) throws Exception {
    FileInputStream fis = new FileInputStream(filename);
    ObjectInputStream ois = new ObjectInputStream(fis);
    BirchClusterOptions bco = (BirchClusterOptions) ois.readObject();

    ois.close();
    fis.close();

    return bco;
  }

  /**
   * Method description
   *
   *
   * @param bco
   * @param outputFileName
   *
   * @throws Exception
   */
  public static void serializeBirchClusterOptions (BirchClusterOptions bco,
                                                   String outputFileName)
                                                   throws Exception {
    if (bco.verify() == false) {
      throw new Exception("Specified BirchClusterOptions object is invalid!");
    }

    FileOutputStream fos = new FileOutputStream(outputFileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);

    oos.writeObject(bco);
    oos.flush();
    oos.close();
    fos.close();
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  public void store (String key,
                     String value) {
    this.miscStringSettings.put(key, value);

    return;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String toString () {
    StringBuffer sb = new StringBuffer();

    sb.append("Birch Cluster Options\n");
    sb.append("Clustering Order: " + this.clusteringOrder + "\n");
    sb.append("Term Reduction Approach: " + this.termReductionApproach + "\n");

    if (this.termReductionApproach ==
        TermReductionApproach.USE_TERM_REDUCTION) {
      sb.append("  Terms reduced to: " + this.termLimit + " terms.\n");
    }

    sb.append("Clustering Approach: " + this.clusteringApproach + "\n");

    if ((this.clusteringApproach ==
         ClusteringApproach.REASONABLE_EFFORT_FORWARD) || (this
           .clusteringApproach == ClusteringApproach
           .REASONABLE_EFFORT_BACKWARD)) {
      sb.append("  Reasonable effort percentage: " +
                this.maxTrialsForReasonableEffort + "\n");
    }

    sb.append("Capacity Fraction: " + this.capacityFraction + "\n");
    sb.append("Max Cluster Size: " + this.maxClusterSize + "\n");

    return sb.toString();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean verify () {

    // Verify a clusteringApproach has been specified.
    if (this.clusteringApproach == ClusteringApproach.NOTHING) {
      return false;
    } else if ((this.clusteringApproach ==
                ClusteringApproach.REASONABLE_EFFORT_FORWARD) || (this
                  .clusteringApproach == ClusteringApproach
                  .REASONABLE_EFFORT_BACKWARD)) {

      // If we are using a resonable effort approach, make sure the percentage
      // of trials to conduct is specified.
      if ((this.maxTrialsForReasonableEffort < 0.0) ||
          (this.maxTrialsForReasonableEffort > 1)) {
        return false;
      }
    }

    // Make sure a clusteringOrder is specified.
    if (this.clusteringOrder == ClusteringOrder.NOTHING) {
      return false;
    }

    // Make sure term reduction settings are specified.
    if (this.termReductionApproach ==
        TermReductionApproach.USE_TERM_REDUCTION) {

      // If we are using term reduction, then make sure we have a good limit.
      if (this.termLimit <= 0) {
        return false;
      }
    }

    // Make sure the two most important settings are valid!
    if ((this.capacityFraction > 1.0) || (this.capacityFraction <= 0.0) ||
        (this.maxClusterSize <= 0)) {
      return false;
    }

    // All settings in this object appear valid, return true.
    return true;
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public String get (String key) {
    String value = this.miscStringSettings.get(key);

    if (value == null) {
      return "";
    } else {
      return value;
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getCSV () {
    StringBuffer sb = new StringBuffer();

    sb.append(this.clusteringOrder + ",");
    sb.append(this.termReductionApproach + ",");

    if (this.termReductionApproach ==
        TermReductionApproach.USE_TERM_REDUCTION) {
      sb.append(this.termLimit + ",");
    } else {
      sb.append(",");
    }

    sb.append(this.clusteringApproach + ",");

    if ((this.clusteringApproach ==
         ClusteringApproach.REASONABLE_EFFORT_FORWARD) || (this
           .clusteringApproach == ClusteringApproach
           .REASONABLE_EFFORT_BACKWARD)) {
      sb.append(this.maxTrialsForReasonableEffort + ",");
    } else {
      sb.append(",");
    }

    sb.append(this.capacityFraction + ",");
    sb.append(this.maxClusterSize + ",");

    return sb.toString();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public double getCapacityFraction () {
    return this.capacityFraction;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ClusteringApproach getClusteringApproach () {
    return this.clusteringApproach;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ClusteringOrder getClusteringOrder () {
    return this.clusteringOrder;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getMaxClusterSize () {
    return this.maxClusterSize;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getMaxTermLimit () {
    return this.termLimit;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public double getReasonableEffortValue () {
    return this.maxTrialsForReasonableEffort;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getSQL () {
    StringBuffer sb = new StringBuffer();

    sb.append("clusteringOrder = \"" + this.clusteringOrder + "\", ");
    sb.append("termReductionApproach = \"" + this.termReductionApproach +
              "\", ");

    if (this.termReductionApproach ==
        TermReductionApproach.USE_TERM_REDUCTION) {
      sb.append("termLimit = " + this.termLimit + ", ");
    }

    sb.append("clusteringApproach = \"" + this.clusteringApproach + "\", ");

    if ((this.clusteringApproach ==
         ClusteringApproach.REASONABLE_EFFORT_FORWARD) || (this
           .clusteringApproach == ClusteringApproach
           .REASONABLE_EFFORT_BACKWARD)) {
      sb.append("maxTrialsForReasonableEffort = " +
                this.maxTrialsForReasonableEffort + ", ");
    }

    sb.append("capacityFraction = " + this.capacityFraction + ", ");
    sb.append("maxClusterSize = " + this.maxClusterSize + ", ");

    return sb.toString();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public TermReductionApproach getTermReductionApproach () {
    return this.termReductionApproach;
  }

  /**
   * Method description
   *
   *
   * @param cf
   */
  public void setCapacityFraction (double cf) {
    this.capacityFraction = cf;
  }

  /**
   * Method description
   *
   *
   * @param c
   *
   * @return
   */
  public ClusteringApproach setClusteringApproach (ClusteringApproach c) {
    this.clusteringApproach = c;

    return this.clusteringApproach;
  }

  /**
   * Method description
   *
   *
   * @param c
   *
   * @return
   */
  public ClusteringOrder setClusteringOrder (ClusteringOrder c) {
    this.clusteringOrder = c;

    return this.clusteringOrder;
  }

  /**
   * Method description
   *
   *
   * @param maxSize
   */
  public void setMaxClusterSize (int maxSize) {
    this.maxClusterSize = maxSize;
  }

  /**
   * Setting this value <= 0 will disable term reduction completely.
   * Setting it to a value > 0 will enable term reduction.
   *
   * @param termLimit
   *
   * @return
   */
  public TermReductionApproach setMaxTermLimit (int termLimit) {
    this.termLimit = termLimit;

    if (termLimit <= 0) {
      return this.setTermReductionApproach(
          TermReductionApproach.NO_TERM_REDUCTION);
    } else {
      return this.setTermReductionApproach(
          TermReductionApproach.USE_TERM_REDUCTION);
    }

  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public ClusteringApproach setReasonableEffortValue (double value) {
    this.maxTrialsForReasonableEffort = value;

    return this.getClusteringApproach();
  }

  /**
   * Method description
   *
   *
   * @param t
   *
   * @return
   */
  public TermReductionApproach setTermReductionApproach (
      TermReductionApproach t) {
    this.termReductionApproach = t;

    return this.termReductionApproach;
  }
}

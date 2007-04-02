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
 * This object is used to represent the configured settings for the BIRCH
 * clustering engine.
 *
 * @version    1.0, 04/01/07
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
   * Create a new BirchClusterOptions object.
   *
   */
  public BirchClusterOptions () {
    this.miscStringSettings = new Hashtable<String, String>();
  }

  /**
   * Load a serialized object from disk.
   *
   *
   * @param filename Filename of the serialize BirchClusterOptions object.
   *
   * @return An instance of the serialized BirchClusterOptions object.
   *
   * @throws Exception If there is a file system or serialization error.
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
   * Take a live BirchClusterOptions object and write it to disk.
   *
   *
   * @param bco BirchClusterOptions object to serialize.
   * @param outputFileName Filename of where to store the serialized object.
   *
   * @throws Exception If there is a file system or serialization error.
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
   * This is currently unused, but is fully functional. For those settings
   * that don't have method calls, store values in an internal hash table.
   *
   * @param key Identifier of setting name.
   * @param value Value to store for the responding key.
   */
  public void store (String key,
                     String value) {
    this.miscStringSettings.put(key, value);

    return;
  }

  /**
   * @return Returns a human readable description of this instance.
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
   * Evaluate the settings configured in this object and determine if they
   * corespond to a valid clustering configuration.
   *
   *
   * @return true if this object is represents a valid configuration, else false
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
   * See BirchClusterOptions.get() for more information
   *
   *
   * @param key Setting name to retrieve from our internal settings hash table.
   *
   * @return Stored setting value
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
   * @return A comma delimited value representing the settings of this object.
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
   * @return Stored capacity fraction.
   */
  public double getCapacityFraction () {
    return this.capacityFraction;
  }

  /**
   * @return Stored clustering approach.
   */
  public ClusteringApproach getClusteringApproach () {
    return this.clusteringApproach;
  }

  /**
   * @return Stored clustering order.
   */
  public ClusteringOrder getClusteringOrder () {
    return this.clusteringOrder;
  }

  /**
   * @return Maximum allowed cluster size.
   */
  public int getMaxClusterSize () {
    return this.maxClusterSize;
  }

  /**
   * @return The maximum number of terms to used in the clustering process.
   */
  public int getMaxTermLimit () {
    return this.termLimit;
  }

  /**
   * @return Return the threshold value for reasonable effort clustering.
   */
  public double getReasonableEffortValue () {
    return this.maxTrialsForReasonableEffort;
  }

  /**
   * @return A snippet of SQL representing this object.
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
   * @return Stored term reduction approach.
   */
  public TermReductionApproach getTermReductionApproach () {
    return this.termReductionApproach;
  }

  /**
   * @param cf Stored capacity fraction.
   */
  public void setCapacityFraction (double cf) {
    this.capacityFraction = cf;
  }

  /**
   * @param c ClusteringApproach to be used for clustering.
   *
   * @return The clustering approach that was just set.
   */
  public ClusteringApproach setClusteringApproach (ClusteringApproach c) {
    this.clusteringApproach = c;

    return this.clusteringApproach;
  }

  /**
   * @param c ClusteringOrder to be used for clustering.
   *
   * @return The clustering order that was just set.
   */
  public ClusteringOrder setClusteringOrder (ClusteringOrder c) {
    this.clusteringOrder = c;

    return this.clusteringOrder;
  }

  /**
   * Set the maximum number of documents any cluster can represent.
   *
   *
   * @param maxSize Maximum number of documents a cluster may represent.
   */
  public void setMaxClusterSize (int maxSize) {
    this.maxClusterSize = maxSize;
  }

  /**
   * Setting this value <= 0 will disable term reduction completely.
   * Setting it to a value > 0 will enable term reduction.
   *
   * @param termLimit If > 0, term are limited to that value, else no reduction 
   *
   * @return New term reduction approach.
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
   * @param value Fraction of eligible clusters to check for potential clusters.
   *
   * @return Current clustering approach.
   */
  public ClusteringApproach setReasonableEffortValue (double value) {
    this.maxTrialsForReasonableEffort = value;

    return this.getClusteringApproach();
  }

  /**
   * To enable term reduction, call this function and then call 
   * BirchClusterOptions.setMaxTermLimit() with a paramter > 0.
   *
   * @param t TermReductionApproach to use.
   *
   * @return Current TermReductionApproach.
   */
  public TermReductionApproach setTermReductionApproach (
      TermReductionApproach t) {
    this.termReductionApproach = t;

    return this.termReductionApproach;
  }
}

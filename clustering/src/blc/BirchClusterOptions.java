package blc;

import java.util.*;
import java.io.*;

enum ClusteringOrder implements Serializable {
    NOTHING,
    TIMESTAMP_FORWARD,
    TIMESTAMP_REVERSE,
    RANDOM
}

enum TermReductionApproach implements Serializable {
    USE_TERM_REDUCTION,
    NO_TERM_REDUCTION
}

enum ClusteringApproach implements Serializable {
    NOTHING,
    GREEDY_ALLOCATION,
    BEST_FIT_ALLOCATION,
    REASONABLE_EFFORT
}

public class BirchClusterOptions implements Serializable {
  private ClusteringOrder clusteringOrder = ClusteringOrder.NOTHING;
  private TermReductionApproach termReductionApproach = 
      TermReductionApproach.NO_TERM_REDUCTION;
  private int termLimit = -1;
  
  private ClusteringApproach clusteringApproach = 
      ClusteringApproach.NOTHING;
  private double maxTrialsForReasonableEffort = -1.0;
  
  private double capacityFraction = -1.0;
  private int maxClusterSize = -1;
  
  private Hashtable<String, String> miscStringSettings = null;

  public BirchClusterOptions() {
    this.miscStringSettings = new Hashtable<String, String>();
  }
  
  public void store(String key, String value) {
    this.miscStringSettings.put(key, value);
    return;
  }
  
  public String get(String key) {
    String value = this.miscStringSettings.get(key);
    if (value == null) {
      return "";
    } else {
      return value;
    }
  }
  
  public ClusteringOrder getClusteringOrder() {
    return this.clusteringOrder;
  }
  
  public ClusteringOrder setClusteringOrder(ClusteringOrder c) {
    this.clusteringOrder = c;
    return this.clusteringOrder;
  }
  
  public TermReductionApproach getTermReductionApproach() {
    return this.termReductionApproach;
  }
  
  public TermReductionApproach setTermReductionApproach(
      TermReductionApproach t) {
    this.termReductionApproach = t;
    return this.termReductionApproach;
  }
  
  /**
   * Setting this value <= 0 will disable term reduction completely.
   * Setting it to a value > 0 will enable term reduction.
   */
  public TermReductionApproach setMaxTermLimit(int termLimit) {
    this.termLimit = termLimit;
    if (termLimit <= 0) {
      return this.setTermReductionApproach(
          TermReductionApproach.NO_TERM_REDUCTION);
    } else { 
      return this.setTermReductionApproach(
          TermReductionApproach.USE_TERM_REDUCTION);
    }
        
  }
  
  public int getMaxTermLimit() {
    return this.termLimit;
  }
  
  public ClusteringApproach getClusteringApproach() {
    return this.clusteringApproach;
  }
  
  public ClusteringApproach setClusteringApproach(ClusteringApproach c) {
    this.clusteringApproach = c;
    return this.clusteringApproach;
  }
  
  public double getReasonableEffortValue() {
    return this.maxTrialsForReasonableEffort;
  }
  
  public ClusteringApproach setReasonableEffortValue(double value) {
    this.maxTrialsForReasonableEffort = value;
    return this.setClusteringApproach(clusteringApproach.REASONABLE_EFFORT);
  }
  
  public double getCapacityFraction() {
    return this.capacityFraction;
  }
  
  public void setCapacityFraction(double cf) {
    this.capacityFraction = cf;
  }
   
  public int getMaxClusterSize() {
    return this.maxClusterSize;
  }
  
  public void setMaxClusterSize(int maxSize) {
    this.maxClusterSize = maxSize;
  }
  
  public boolean verify() { 
    // Verify a clusteringApproach has been specified.
    if (this.clusteringApproach == ClusteringApproach.NOTHING) {
      return false;
    } else if (this.clusteringApproach == ClusteringApproach.REASONABLE_EFFORT) 
        {
      // If we are using a resonable effort approach, make sure the percentage
      // of trials to conduct is specified.
      if (this.maxTrialsForReasonableEffort < 0.0 || 
          this.maxTrialsForReasonableEffort > 1) {
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
    if (this.capacityFraction > 1.0 || this.capacityFraction <= 0.0 || 
        this.maxClusterSize <= 0) {
      return false;
    }
    
    // All settings in this object appear valid, return true.
    return true;
  }
  
  public static void serializeBirchClusterOptions(BirchClusterOptions bco,
      String outputFileName) throws Exception {
    if (bco.verify() == false) {
      throw new Exception("Specified BirchClusterOptions object is invalid!");
    }
    
    FileOutputStream fos = new FileOutputStream(outputFileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(bco);
    oos.flush();
    oos.close();
  }
  
  public static BirchClusterOptions deserializeBirchClusterOptions(
      String filename) throws Exception {
    FileInputStream fis = new FileInputStream(filename);
    ObjectInputStream ois = new ObjectInputStream(fis);
    BirchClusterOptions bco = (BirchClusterOptions) ois.readObject();
    return bco;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Birch Cluster Options\n");
    sb.append("Clustering Order: " + this.clusteringOrder + "\n");
    sb.append("Term Reduction Approach: " + this.termReductionApproach + "\n");
    if (this.termReductionApproach == TermReductionApproach.USE_TERM_REDUCTION) 
        {
      sb.append("  Terms reduced to: " + this.termLimit + " terms.\n");
    }
    sb.append("Clustering Approach: " + this.clusteringApproach + "\n");
    if (this.clusteringApproach == ClusteringApproach.REASONABLE_EFFORT) {
      sb.append("  Reasonable effort percentage: " + 
          this.maxTrialsForReasonableEffort + "\n");
    }
    sb.append("Capacity Fraction: " + this.capacityFraction + "\n");
    sb.append("Max Cluster Size: " + this.maxClusterSize + "");
    return sb.toString();
  }
}

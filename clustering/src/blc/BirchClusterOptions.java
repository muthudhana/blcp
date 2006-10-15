package blc;

import java.util.*;
import java.io.*;

enum ClusteringOrder implements Serializable {
    TIMESTAMP_FORWARD,
    TIMESTAMP_REVERSE,
    RANDOM
}

enum TermReductionApproach implements Serializable {
    USE_TERM_REDUCTION,
    NO_TERM_REDUCTION
}

enum ClusteringApproach implements Serializable {
    GREEDY_ALLOCATION,
    BEST_FIT_ALLOCATION,
    REASONABLE_EFFORT
}

public class BirchClusterOptions implements Serializable {
  private ClusteringOrder clusteringOrder = ClusteringOrder.TIMESTAMP_FORWARD;
  private TermReductionApproach termReductionApproach = 
      TermReductionApproach.NO_TERM_REDUCTION;
  private int termLimit = -1;
  
  private ClusteringApproach clusteringApproach = 
      ClusteringApproach.GREEDY_ALLOCATION;
  private int maxTrialsForReasonableEffort = -1;
  
  private double capacityFraction = -1;
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
  
  public TermReductionApproach setMaxTermLimit(int termLimit) {
    this.termLimit = termLimit;
    return this.setTermReductionApproach(
        TermReductionApproach.USE_TERM_REDUCTION);
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
  
  public int getReasonableEffortValue() {
    return this.maxTrialsForReasonableEffort;
  }
  
  public ClusteringApproach setReasonableEffortValue(int value) {
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
    if (termReductionApproach == TermReductionApproach.USE_TERM_REDUCTION) {
      if (termLimit < 0) {
        return false;
      }
    }
    
    if (clusteringApproach == ClusteringApproach.REASONABLE_EFFORT) {
      if (maxTrialsForReasonableEffort < 0) {
        return false;
      }
    }
    
    if (capacityFraction < 0) {
      return false;
    }
    
    if (maxClusterSize < 0) {
      return false;
    } 
    
    return true;
  }
}

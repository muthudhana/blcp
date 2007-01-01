package blc;

import gnu.getopt.*;
import java.io.*;
import clusterbase.IStemmer;
import clusterbase.StopList;

enum DriverAction {
  NOTHING,
  BUILD_DOCUMENTS,
  SET_OPTIONS,
  SHOW_OPTIONS,
  CLUSTER
}

public class Driver {
  
  public DriverAction driverAction = null;
  public String input = null;
  public String output = null;
  public String clusterOptionsPath = null;
  public String bkmPath = null;
  public BirchClusterOptions clusterOptions = new BirchClusterOptions();
  
  public Driver() {
  }
  
  private int parseAndSerializeDocuments(String in, String out,
      StopList stopList, IStemmer stemmer) {
    BirchDocumentFactory bdf = new BirchDocumentFactory(stopList, stemmer);
    return bdf.createSerializedDocuments(in, 0, out, 1);
  }
  
  private void createClusterOptions(String outputPath) {
    System.out.println("Serializing Cluster Options!");
    System.out.println(this.clusterOptions);
    try {
      BirchClusterOptions.serializeBirchClusterOptions(
          this.clusterOptions, outputPath);
    } catch (Exception e){
      System.out.println(e);
      System.exit(-1);
    }
  }
  
  /**
   * We have 4 possible options with our new driver
   * 1) Read in a serialzied BirchClusterObject and print it out.
   * 2) Create a new BirchClusterObject, configure it, and serialize it out.
   * 3) Given a directory of raw files: parse, serialize, and write them out.
   * 4) Given serialized documents from #3 and a BirchClusterOptions object from
   *    #2, cluster the documents using a provided directory as scratch space.
   */
  
  public static void main(String[] args) {
    Driver driver = new Driver();
    
    driver.driverAction = DriverAction.NOTHING;
    
    String arg;
    int c = -1;
    
    Getopt opts = new Getopt("blcp", args, "BOCi:o:s:b:r:l:a:t:c:m:");
    
    for(int i = 0; i < args.length; ++i)
      System.out.println(args[i]);
    
    while ((c = opts.getopt()) != -1) {
      switch (c) {
        case 'B': // Make a new project directory
          driver.driverAction = DriverAction.BUILD_DOCUMENTS;
          break;
        case 'O':
          driver.driverAction = DriverAction.SET_OPTIONS;
          break;
        case 'P':
          driver.driverAction = DriverAction.SHOW_OPTIONS;
          break;
        case 'C':
          driver.driverAction = DriverAction.CLUSTER;
          break;
        case 'i':
          if (opts.getOptarg().length() > 0) {
            driver.input = opts.getOptarg();
          }
          break;
        case 'o':
          if (opts.getOptarg().length() > 0) {
            driver.output = opts.getOptarg();
          }
          break;
        case 's':
          if (opts.getOptarg().length() > 0) {
            driver.clusterOptionsPath = opts.getOptarg();
          }
          break;
        case 'b':
          if (opts.getOptarg().length() > 0) {
            driver.bkmPath = opts.getOptarg();
          }
        case 'r':
          if (opts.getOptarg().length() > 0) {
            String a = opts.getOptarg();
            if (a.equalsIgnoreCase("TIMESTAMP_FORWARD")) {
              driver.clusterOptions.setClusteringOrder(
                  ClusteringOrder.TIMESTAMP_FORWARD);
            } else if (a.equalsIgnoreCase("TIMESTAMP_REVERSE")) {
              driver.clusterOptions.setClusteringOrder(
                  ClusteringOrder.TIMESTAMP_REVERSE);
            } else if (a.equalsIgnoreCase("RANDOM")) {
              driver.clusterOptions.setClusteringOrder(ClusteringOrder.RANDOM);
            }
          }
          break;
        case 'l':
          int termLimit = Integer.parseInt(opts.getOptarg());
          if (termLimit > 0) {
            driver.clusterOptions.setMaxTermLimit(termLimit);
            driver.clusterOptions.setTermReductionApproach(
                TermReductionApproach.USE_TERM_REDUCTION);
          } else {
            driver.clusterOptions.setTermReductionApproach(
                TermReductionApproach.NO_TERM_REDUCTION);
          }
          break;
        case 'a':
          String a = opts.getOptarg();
          if (a.equalsIgnoreCase("GREEDY_ALLOCATION")) {
            driver.clusterOptions.setClusteringApproach(
                ClusteringApproach.GREEDY_ALLOCATION);
          } else if (a.equalsIgnoreCase("BEST_FIT_ALLOCATION")) {
            driver.clusterOptions.setClusteringApproach(
                ClusteringApproach.BEST_FIT_ALLOCATION);
          } else if (a.equalsIgnoreCase("REASONABLE_EFFORT")) {
            driver.clusterOptions.setClusteringApproach(
                ClusteringApproach.REASONABLE_EFFORT);
          }
          break;
        case 't':
          double reasonableEffort = Double.parseDouble(opts.getOptarg());
          if (reasonableEffort > 0.0 && reasonableEffort <= 1.0) {
            driver.clusterOptions.setReasonableEffortValue(reasonableEffort);
            driver.clusterOptions.setClusteringApproach(
                ClusteringApproach.REASONABLE_EFFORT);
          }
          break;
        case 'c':
          double capacityFraction = Double.parseDouble(opts.getOptarg());
          if (capacityFraction > 0.0 && capacityFraction <= 1.0) {
            driver.clusterOptions.setCapacityFraction(capacityFraction);
          }
          break;
        case 'm':
          int maxClusterSize = Integer.parseInt(opts.getOptarg());
          if (maxClusterSize > 0) {
            driver.clusterOptions.setMaxClusterSize(maxClusterSize);
          }
          break;
      }
    }
    
    switch (driver.driverAction) {
      case NOTHING:
        break;
      case BUILD_DOCUMENTS:
        break;
      case SET_OPTIONS:
        break;
      case SHOW_OPTIONS:
        break;
      case CLUSTER:
        break;
    }
  }
}

package blc;

import gnu.getopt.*;
import java.io.*;
import clusterbase.*;

/**
 * Basic Usage Notes: 
 *
 * Create a Birch Cluster Options object and save to a file:
 * java -jar "C:\Documents and Settings\mike\My Documents\Java Projects\clustering\dist\Clustering.jar" -O -s "c:\go.bco" -r TIMESTAMP_FORWARD -l 2500 -a GREEDY_ALLOCATION -c 0.8 -m 100
 *
 * Print out the options stored in a saved Birch Cluster Options object:
 * java -jar "C:\Documents and Settings\mike\My Documents\Java Projects\clustering\dist\Clustering.jar" -P -s "c:\go.bco"
 *
 * Build the serialized documents:
 * java -jar "C:\Documents and Settings\mike\My Documents\Java Projects\clustering\dist\Clustering.jar" -B -o c:\corpus\serialized\ -i c:\corpus\src -x "C:\Documents and Settings\mike\My Documents\Java Projects\clustering\stopList.txt"
 * 
 * Take serialized documents, and build a BirchKmeans global dictionary and required statistics for clustering from them:
 * java -jar "C:\Documents and Settings\mike\My Documents\Java Projects\clustering\dist\Clustering.jar" -D -i c:\corpus\serialized\ -o c:\corpus\serialized\ -b c:\corpus\bkm.dat 
 *
 * Take the serialized phase 2 documents from the -D operation and the corresponding BirchKmeans object and cluster them:
 * java -jar "C:\Documents and Settings\mike\My Documents\Java Projects\clustering\dist\Clustering.jar" -C -i c:\corpus\serialized\ -o c:\corpus\serialized\ -s c:\go.bco -b c:\corpus\bkm.dat
*/

enum DriverAction {
  NOTHING,
  BUILD_DICTIONARY,
  BUILD_DOCUMENTS,
  SET_OPTIONS,
  SHOW_OPTIONS,
  NORMALIZE_VECTORS,
  CLUSTER
}

public class Driver {
  
  public DriverAction driverAction = null;
  public String input = null;
  public String output = null;
  public String clusterOptionsPath = null;
  public String bkmInputPath = null;
  public String bkmOutputPath = null;
  public BirchClusterOptions clusterOptions = new BirchClusterOptions();
  public String stopListFileName = null;
  public boolean verbose = false;
  
  public Driver() {
  }
  
  public int parseAndSerializeDocuments(String in, String out,
    StopList stopList, IStemmer stemmer) {
    BirchDocumentFactory bdf = new BirchDocumentFactory(stopList, stemmer);
    return bdf.createSerializedDocuments(in, out, 1);
  }
  
  public void createClusterOptions(String outputPath) {
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
    
    Getopt opts = new Getopt("blcp", args, "BOCPDNi:o:s:b:k:r:l:a:t:c:m:x:v");
    
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
        case 'D':
          driver.driverAction = DriverAction.BUILD_DICTIONARY;
          break;
        case 'N':
          driver.driverAction = DriverAction.NORMALIZE_VECTORS;
          break;
        case 'i':
          if (opts.getOptarg().length() > 0) {
            driver.input = opts.getOptarg();
          }
          break;
        case 'v':
          driver.verbose = true;
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
            driver.bkmInputPath = opts.getOptarg();
          }
          break;
        case 'k':
          if (opts.getOptarg().length() > 0) {
            driver.bkmOutputPath = opts.getOptarg();
          }
          break;
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
            } else {
              System.out.println("Unknown clustering order!");
              System.exit(-1);
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
          } else if (a.equalsIgnoreCase("REASONABLE_EFFORT_FORWARD")) {
            driver.clusterOptions.setClusteringApproach(
              ClusteringApproach.REASONABLE_EFFORT_FORWARD);
          } else if (a.equalsIgnoreCase("REASONABLE_EFFORT_BACKWARD")) {
            driver.clusterOptions.setClusteringApproach(
              ClusteringApproach.REASONABLE_EFFORT_BACKWARD);
          } else {
            System.out.println("Invalid clustering approach!");
            System.exit(-1);
          }    
          break;
        case 't':
          double reasonableEffort = Double.parseDouble(opts.getOptarg());
          if (reasonableEffort > 0.0 && reasonableEffort <= 1.0) {
            driver.clusterOptions.setReasonableEffortValue(reasonableEffort);
          } else {
            System.out.println("Reasonable effort value must be (0,1.0]");
            System.exit(-1);
          }
          break;
        case 'c':
          double capacityFraction = Double.parseDouble(opts.getOptarg());
          if (capacityFraction > 0.0 && capacityFraction <= 1.0) {
            driver.clusterOptions.setCapacityFraction(capacityFraction);
          } else {
            System.out.println("Invalid capacity fraction!");
            System.exit(-1);
          }
          break;
        case 'm':
          int maxClusterSize = Integer.parseInt(opts.getOptarg());
          if (maxClusterSize > 0) {
            driver.clusterOptions.setMaxClusterSize(maxClusterSize);
          } else {
            System.out.println("Invalid maximum cluster size!");
            System.exit(-1);
          }
          break;
        case 'x':
          driver.stopListFileName = opts.getOptarg();
          break;
        default:
          System.out.println("Unknown options: '" + c + "'");
          System.exit(-1);
      }
    }
    
    switch (driver.driverAction) {
      case NOTHING:
        System.out.println("No valid action selected!");
        break;
      case BUILD_DICTIONARY:
        if (driver.input == null || driver.output == null ||
            driver.bkmInputPath == null || driver.bkmOutputPath == null) {
          System.out.println(
            "Invalid options for building the global dictionary!");
          System.exit(-1);
        }
   
        BirchKmeans bkm = null;
        
        // See if driver.bkmPath points to an existing BirchKmeans object
        // if it does, load it (we'll just update it), otherwise make a new one.
        
        try {
          bkm = BirchKmeans.deserializeBirchKmeans(driver.bkmInputPath);
        } catch (FileNotFoundException ex) {
          // File not found, make a new BKM Object!
          bkm = new BirchKmeans();
        } catch (Exception ex) {
          // Fatal Exception
          ex.printStackTrace();
          System.exit(-1);
        }
      
        bkm.buildGlobalDictionaryFromSerializedRawDocuments(driver.input,
            driver.output);

        try {
          BirchKmeans.serializeBirchKMeans(bkm, driver.bkmOutputPath);
        } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(-1);
        }
        break;
      case BUILD_DOCUMENTS:
        if (driver.stopListFileName == null || driver.input == null ||
            driver.output == null) {
          System.out.println("stoplistfilename = " + driver.stopListFileName);
          System.out.println("input = " + driver.input);
          System.out.println("out = " + driver.output);
          System.out.println("Invalid options for building parsed documents!");
          System.exit(-1);
        }
        IStemmer st = new Stemmer();
        StopList sl = null;
        try {
          sl = new StopList(st, driver.stopListFileName);
        } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(-1);
        }
        driver.parseAndSerializeDocuments(driver.input, driver.output, sl, st);
        break;
      case SET_OPTIONS:
        if (driver.clusterOptionsPath == null) {
          System.out.println("Invalid options for setting options!");
          System.exit(-1);
        }
        try {
          BirchClusterOptions.serializeBirchClusterOptions(
            driver.clusterOptions, driver.clusterOptionsPath);
        } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(-1);
        }
        break;
      case SHOW_OPTIONS:
        if (driver.clusterOptionsPath == null) {
          System.out.println("Invalid options for showing options!");
          System.exit(-1);
        }
        BirchClusterOptions bco = null;
        try {
          bco = BirchClusterOptions.deserializeBirchClusterOptions(
            driver.clusterOptionsPath);
        } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(-1);
        }
        System.out.println(bco);
        break;
      case NORMALIZE_VECTORS:
      case CLUSTER:
        BirchClusterOptions options = null;
        if (driver.clusterOptionsPath != null) {
          try {
            options = BirchClusterOptions.deserializeBirchClusterOptions(
              driver.clusterOptionsPath);
          } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
          }
        } else { // See if enough options were specified as parameters!
          if (driver.clusterOptions.verify() == false) {
            System.out.println("Insufficient options are set in order to " +
                "proceed!");
            System.exit(-1);
          } else {
            options = driver.clusterOptions;
          }
        }
        
        BirchKmeans birch = null;
        
        if (driver.bkmInputPath == null) {
          System.out.println("Path to BirchKmeans object is not specified!");
          System.exit(-1);
        }
        try {
          birch = BirchKmeans.deserializeBirchKmeans(driver.bkmInputPath);
        }  catch (Exception ex) {
          // Fatal Exception
          ex.printStackTrace();
          System.exit(-1);
        }
        
        try {
          // set final options before clustering
          birch.setClusterOptions(options); 
        } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(-1);
        }
  
        if (driver.verbose) {
          birch.setVerboseOuput(true);
        }
        
        if (driver.driverAction == DriverAction.NORMALIZE_VECTORS) {
          if (driver.input == null) {
            System.out.println("Please specify appropriate input " +
                "directory in order to finish clustering!");
            System.exit(-1);
          }
          if (driver.bkmOutputPath == null) {
            System.out.println("Please specify the output location of the " +
                "BirchKmeans object using -k");
            System.exit(-1);
          }
          birch.useGlobalDictionaryAndBuildNormalizedVectors(driver.input);
          try {
            BirchKmeans.serializeBirchKMeans(birch, driver.bkmOutputPath);
          } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
          }
        } else if (driver.driverAction == DriverAction.CLUSTER) {
          birch.clusterDocuments();
          System.out.println(birch);
        }
        break;
    }
  }
}

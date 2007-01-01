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
  
  public Driver() {
    
  }

  private int parseAndSerializeDocuments(String in, String out,
      StopList stopList, IStemmer stemmer) {
    BirchDocumentFactory bdf = new BirchDocumentFactory(stopList, stemmer);
    return bdf.createSerializedDocuments(in, 0, out, 1);
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
    
    Getopt opts = new Getopt("blcp", args, "BOCi:o:s:");
    
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
          
//        case 'P': // Location of project
//          if (opts.getOptarg().length() > 0) {
//            driver.projectPath = opts.getOptarg(); 
//          } else {
//            System.out.println("Project location not valid!");
//            return;
//          }
//          break;
//        case 'p': // Parse corpus files
//            if (driver.driverAction == DriverAction.NOTHING) {
//              driver.driverAction = DriverAction.PARSE_FILES;
//            }
//            driver.unparsedFiles = opts.getOptarg();
//          break;
//        case 'x':
//          arg = opts.getOptarg();
//          System.out.println("Switch '" + arg + "' enabled.");
//          if (arg != null) {
//            System.out.println("  Parameter = " + arg);
//          }
//          break;
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

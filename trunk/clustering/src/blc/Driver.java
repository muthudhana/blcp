package blc;

import gnu.getopt.*;
import java.io.*;

enum DriverAction {
  NOTHING,
} 

public class Driver {

  public DriverAction driverAction = null;
  
  public Driver() {
    
  }

  private boolean parseAndSerializeDocuments() {
    
    return true;
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
    
    String arg;
    int c = -1;
    
    Getopt opts = new Getopt("blcp", args, "N:p:P:");
    
    for(int i = 0; i < args.length; ++i)
      System.out.println(args[i]);
    
    while ((c = opts.getopt()) != -1) {
      switch (c) {
        case 'N': // Make a new project directory
           if (driver.driverAction == DriverAction.NOTHING) {
             driver.driverAction = DriverAction.MAKE_NEW_PROJECT;
           }
          break;
        case 'P': // Location of project
          if (opts.getOptarg().length() > 0) {
            driver.projectPath = opts.getOptarg(); 
          } else {
            System.out.println("Project location not valid!");
            return;
          }
          break;
        case 'p': // Parse corpus files
            if (driver.driverAction == DriverAction.NOTHING) {
              driver.driverAction = DriverAction.PARSE_FILES;
            }
            driver.unparsedFiles = opts.getOptarg();
          break;
        case 'x':
          arg = opts.getOptarg();
          System.out.println("Switch '" + arg + "' enabled.");
          if (arg != null) {
            System.out.println("  Parameter = " + arg);
          }
          break;
      }
    }
  }
}

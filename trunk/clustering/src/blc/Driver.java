package blc;

import gnu.getopt.*;
import java.io.*;

enum DriverAction {
  NOTHING,
  MAKE_NEW_PROJECT,
  PARSE_FILES,
  ADD_DOCUMENTS_TO_MODEL,
  SET_TERM_REDUCTION,
  FINALIZE_CORPUS,
  SET_CLUSTER_OPTIONS,
  CLUSTER
} 

public class Driver {

  public DriverAction driverAction = null;
  public String projectPath = null;
  public String unparsedFiles = null;
  
  public Driver() {
    
  }

  private boolean parseAndSerializeDocuments() {
    
    return true;
  }
  
  private boolean bindDocumentToModel() {
 
    return true;
  }
  
  private boolean createNewModel() {
    
    return true;
  }
  
  private boolean setModelOptions() {
    
    return true;
  }
  
  private boolean clusterDocumentsInModel() {
    
    return true;
  }  

  public boolean initializeNewProject(String arg) {
    File f = new File(arg);
    if (f.mkdirs() == false) {
      System.out.println("Fatal Error creating new project directory!");
      return false;
    }
    f = new File(arg + File.separator + "rawDocs" + File.separator);
    if (f.mkdirs() == false) {
      System.out.println("Fatal Error making new project rawDocs");
      return false;
    }
    f = new File(arg + File.separator + "corpus" + File.separator);
    if (f.mkdirs() == false) {
      System.out.println("Fatal Error making new project corpus");
      return false;
    }
    f = new File(arg + File.separator + "models" + File.separator);
    if (f.mkdirs() == false) {
      System.out.println("Fatal Error making new project models");
      return false;
    }
    return true;
  }
  
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

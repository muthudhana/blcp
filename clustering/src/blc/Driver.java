package blc;

import gnu.getopt.*;
import java.io.*;

public class Driver {
  
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
  
  enum DriverAction {
    MAKE_NEW_PROJECT,
    PARSE_FILES,
    ADD_DOCUMENTS_TO_MODEL,
    SET_TERM_REDUCTION,
    FINALIZE_CORPUS,
    SET_CLUSTER_OPTIONS,
    CLUSTER
  }
  
  public static void main(String[] args) { 
    String arg;
    int c = -1;
    boolean actionSelected = false;
    File f = null;

    Getopt opts = new Getopt("blcp", args, "N:");
    
    for(int i = 0; i < args.length; ++i)
      System.out.println(args[i]);
    
    while ((c = opts.getopt()) != -1) {
      switch (c) {
        case 'N': // Make a new project directory
            if (actionSelected == true) {
              System.exit(-1);
            } else {
              actionSelected = true;
            }
            arg = opts.getOptarg();
            f = new File(arg);
            if (f.mkdirs() == false) {
              System.out.println("Fatal Error creating new project directory!");
              return;
            }
            f = new File(arg + File.separator + "rawDocs" + File.separator);
            if (f.mkdirs() == false) {
              System.out.println("Fatal Error making new project rawDocs");
              return; 
            }
            f = new File(arg + File.separator + "corpus" + File.separator);
            if (f.mkdirs() == false) {
              System.out.println("Fatal Error making new project corpus");
              return;
            }
            f = new File(arg + File.separator + "models" + File.separator);
            if (f.mkdirs() == false) {
              System.out.println("Fatal Error making new project models");
              return;
            }
          break;
        case 'P': // Parse corpus files
            if (actionSelected == true) {
              System.exit(-1);
            } else {
              actionSelected = true;
            }
            arg = opts.getOptarg();
            f = new File(arg);
            if (f.mkdirs()) {
              System.out.println("New project directory created!");
            }
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

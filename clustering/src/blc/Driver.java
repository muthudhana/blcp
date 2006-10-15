package blc;

import gnu.getopt.*;

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
  
  public static void main(String[] args) {
    Getopt opts = new Getopt("blcp", args, "");
    
    String arg;
    int c = -1;
    
    while ((c = opts.getopt()) != -1) {
      switch (c) {
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

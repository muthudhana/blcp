package blc;

import java.util.*;
import java.io.*;
import clusterbase.Document;
import clusterbase.IStemmer;
import clusterbase.StopList;

public class BirchDocumentFactory {
  
  protected IStemmer stemmer = null;
  protected StopList stopList = null;
  
  public BirchDocumentFactory(StopList stopList, IStemmer stemmer) {
    this.stopList = stopList;
    this.stemmer = stemmer;
  }
  
  public int createSerializedDocuments (String srcDirectory,
      String outDirectory, int fileNum) {
      return(this.createSerializedDocuments(srcDirectory, 1, outDirectory, 
          fileNum));
  }
  
  /**
   * This function parses documents in a directory tree. The Document objects it
   * creates are not linked to any particular model.
   *
   * The output files will be created in the provided outputDirectory with the
   * same relative heirarchy as seen in the source directory.
   */
  public int createSerializedDocuments (String directory, int sourceId, 
      String outputDirectory, int fileNum) {

    File f = new File(directory);
    File[] files = f.listFiles();
    
    int numDocumentsAdded = 0;

    File output = new File(outputDirectory);
    if (output.exists() == false) {
      output.mkdirs();
    }
    
    for (int i = 0; i < files.length; ++i) {
      if (files[i].isDirectory()) {
        int filesAdded = this.createSerializedDocuments(files[i].toString(),
            sourceId, outputDirectory + File.separator + 
            files[i].getName(), fileNum);
        numDocumentsAdded += filesAdded;
        fileNum += filesAdded;
      }

      if (files[i].isFile()) {
        try {
          Document doc = new Document (files[i].getAbsolutePath(),
              this.stopList, this.stemmer, sourceId);
          
          Document.serializeDocument (doc, outputDirectory + 
              File.separator + files[i].getName() + ".bp1");
          System.out.println ("Creating serialized document #" + fileNum);
          
          ++fileNum;
          ++numDocumentsAdded;
        } catch (Exception ex) {
          ex.printStackTrace();
          System.exit (-1);
        }
      }
    }
    return numDocumentsAdded;
  }
}
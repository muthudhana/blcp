/*
 * @(#)BirchDocumentFactory.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



package blc;

import clusterbase.Document;
import clusterbase.IStemmer;
import clusterbase.StopList;

import java.io.*;

import java.util.*;

/**
 * Create serialized documents, this includes parsing, stemming, etc.
 *
 *
 * @version    1.0, 04/01/07
 * @author     Mike Wiacek
 */
public class BirchDocumentFactory {
  protected IStemmer stemmer = null;
  protected StopList stopList = null;
  private boolean verboseOutput = false;

  /**
   * @param stopList StopList object that removes common English words.
   * @param stemmer Interface to an object that implements a stemmer function.
   */
  public BirchDocumentFactory (StopList stopList,
                               IStemmer stemmer) {
    this.stopList = stopList;
    this.stemmer = stemmer;
  }

  /**
   * @return True if we should print verbose information to the logs.
   */
  public boolean beVerbose () {
    return this.verboseOutput;
  }

  /**
   * This function parses documents in a directory tree. The Document objects it
   * creates are not linked to any particular model.
   *
   * The output files will be created in the provided outputDirectory with the
   * same relative heirarchy as seen in the source directory.
   *
   * This calls the overloaded createSerializedDocument with a common source id
   * of 1.
   * 
   *
   * @param srcDirectory  Directory to start scanning for documents.
   * @param outDirectory  Directory to output serialized documents.
   * @param fileNum Used to track number of processed files through recursion.
   *
   * @return Number of files processed in total.
   */
  public int createSerializedDocuments (String srcDirectory,
                                        String outDirectory,
                                        int fileNum) {
    return (this.createSerializedDocuments(srcDirectory, 1, outDirectory,
                                           fileNum));
  }

  /**
   * This function parses documents in a directory tree. The Document objects it
   * creates are not linked to any particular model.
   *
   * The output files will be created in the provided outputDirectory with the
   * same relative heirarchy as seen in the source directory.
   *
   * @param directory Directory to start scanning for documents.
   * @param sourceId Tag documents with a unique source identifier, not used.
   * @param outputDirectory Directory to output serialized documents.
   * @param fileNum Used to track number of processed files through recursion.
   *
   * @return Number of files processed in total.
   */
  public int createSerializedDocuments (String directory,
                                        int sourceId,
                                        String outputDirectory,
                                        int fileNum) {

    File f = new File(directory);
    File[] files = f.listFiles();

    int numDocumentsAdded = 0;

    File output = new File(outputDirectory);

    if (output.exists() == false) {
      output.mkdirs();
    }

    for (int i = 0; i < files.length; ++i) {
      if (files[i].isDirectory()) {
        int filesAdded =
          this.createSerializedDocuments(files[i].toString(), sourceId,
                                         outputDirectory + File.separator +
                                         files[i].getName(), fileNum);

        numDocumentsAdded += filesAdded;
        fileNum += filesAdded;
      }

      if (files[i].isFile()) {
        try {
          Document doc = new Document(files[i].getAbsolutePath(),
                                      this.stopList, this.stemmer, sourceId);

          Document.serializeDocument(doc,
                                     outputDirectory + File.separator +
                                     files[i].getName() + ".bp1");

          if (this.beVerbose()) {
            System.out.println("Creating serialized document #" + fileNum);
          }

          ++fileNum;
          ++numDocumentsAdded;
        } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(-1);
        }
      }
    }

    return numDocumentsAdded;
  }

  /**
   * @param verboseOutputEnabled If true, enable verbose output of any actions.
   *
   * @return True if verbose output is enabled.
   */
  public boolean setVerboseOuput (boolean verboseOutputEnabled) {
    this.verboseOutput = verboseOutputEnabled;

    return this.verboseOutput;
  }
}

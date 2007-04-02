/*
 * @(#)BirchCluster.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



package blc;

import clusterbase.Document;

import sparsevector.SparseVector;

import java.io.*;

import java.util.*;

/**
 * This class represents a BIRCH cluster and contains methods to interact
 * with the cluster.
 *
 *
 * @version    1.0, 04/01/07
 * @author     Mike Wiacek
 */
public class BirchCluster {
  private static final long serialVersionUID = 1234567890L;
  private SparseVector s = null;
  private double sumSquaredLengths = 0.0;
  private int numberOfDocuments = 0;
  private ArrayList<String> includedFiles = null;
  private int dimensionSize = 0;
  private double cachedQuality = 0.0;

  /**
   * Make a new BIRCH cluster.
   *
   * @param numDistinctTerms The number of distinct terms in the corpus
   */
  public BirchCluster (int numDistinctTerms) {
    this.s = new SparseVector();
    this.sumSquaredLengths = 0D;
    this.numberOfDocuments = 0;
    this.cachedQuality = 0.0;
    this.includedFiles = new ArrayList<String>();
    this.dimensionSize = numDistinctTerms;
  }

  /**
   * Adds the document in the vector space model provided as parameters
   * to this BIRCH like cluster.  The new cluster quality is returned.
   *
   * Not thread safe
   *
   * @param vsm BirchKmeans object that this cluster belongs to
   * @param doc Document to be added to this cluster.
   *
   * @return New quality of this cluster with the document added.
   */
  public double addDocument (BirchKmeans vsm,
                             Document doc) {
    SparseVector normalizedVector = doc.getNormalizedVector(vsm);

    double newQuality = this.calculateChangeInQuality(normalizedVector) +
                        this.cachedQuality;

    this.numberOfDocuments++;

    this.s.add(normalizedVector);

    this.sumSquaredLengths += normalizedVector.lengthSquared();
    this.cachedQuality = newQuality;

    this.includedFiles.add(doc.getFilename());

    return this.cachedQuality;
  }

  /**
   * Adds the document in the vector space model provided as parameters
   * to this BIRCH like cluster.  This overloaded version allows the caller
   * to specify the quality of the resulting cluster.  Make sure the caller
   * calculates this value properly.  This will save several cycles by avoiding
   * the penalty of calculating a new quality value, when the caller might have
   * this information already available.
   *
   * Not thread safe
   *
   * @param vsm BirchKmeans object that this cluster belongs to
   * @param doc Document to be added to this cluster.
   * @param forcedNewQuality Force the new quality of the cluster.
   *
   * @return Returns forcedNewQuality
   */
  public double addDocument (BirchKmeans vsm,
                             Document doc,
                             double forcedNewQuality) {

    SparseVector normalizedVector = doc.getNormalizedVector(vsm);

    double newQuality = forcedNewQuality;

    this.numberOfDocuments++;

    this.s.add(normalizedVector);

    this.sumSquaredLengths += normalizedVector.lengthSquared();
    this.cachedQuality = newQuality;

    this.includedFiles.add(doc.getFilename());

    return this.cachedQuality;
  }

  /**
   * Calculate what the change in quality would be if a document with the
   * provided normalized vector was added to this cluster.
   *
   * @param normalizedVector A unit length sparse vector
   *
   * @return What the change in quality would be if a new document was added.
   */
  public double calculateChangeInQuality (SparseVector normalizedVector) {
    SparseVector docVec = normalizedVector;
    SparseVector sCopy = this.s;

    double newQuality = 0.0;

    sCopy.scalarDivide(this.numberOfDocuments);
    sCopy.subtract(docVec);

    if (this.numberOfDocuments == 0) {
      newQuality = 0;
    } else {
      newQuality = (1.0 * this.numberOfDocuments) /
                   (1.0 * (this.numberOfDocuments + 1));
      newQuality *= sCopy.lengthSquared();
    }

    /*
     *  Undo our changes to this.s  since sCopy is not really a copy!
     * This should help memory usage since we don't actually need to
     * allocate a new object, we can perform some algebra to put back
     * what we changed.
     */
    sCopy.add(docVec);
    sCopy.scalarMultiply(this.numberOfDocuments);

    return newQuality;
  }

  /**
   * Load a serialized BirchCluster object in from disk.
   *
   * @param filename Location on the filesystem of a serialized BirchCluster
   *
   * @return BirchCluster object.
   *
   * @throws Exception On unable to load a serialized object.
   */
  public static BirchCluster deserializeBirchKmeans (String filename)
  throws Exception {
    FileInputStream fis = new FileInputStream(filename);
    ObjectInputStream ois = new ObjectInputStream(fis);
    BirchCluster bc = (BirchCluster) ois.readObject();

    ois.close();
    fis.close();

    return bc;
  }

  /**
   * Serialize a provided BirchCluster object to disk.
   *
   *
   * @param bc  BirchCluster object to serialize out.
   * @param outputFileName Location of where to serialize the object.
   *
   * @throws Exception Error in case of filesystem or serialization error. 
   */
  public static void serializeBirchCluster (BirchCluster bc,
                                            String outputFileName)
                                            throws Exception {
    FileOutputStream fos = new FileOutputStream(outputFileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);

    oos.writeObject(bc);
    oos.flush();
    oos.close();
    fos.close();
  }

  /**
   * Returns a textual description of the state of this object.
   *
   *
   * @return 
   */
  public String toString () {
    StringBuffer sb = new StringBuffer();

    sb.append("Birch Cluster Statistics\n");
    sb.append("Number of Documents: " + this.getNumberOfDocuments() + "\n");
    sb.append("Cluster Quality:     " + this.getQuality() + "\n");
    sb.append("Cluster Sparsity:    " + this.getSparsity() + "\n");

    return sb.toString();
  }

  /**
   * @return Number of documents this cluster represents.
   */
  public int getNumberOfDocuments () {
    return this.numberOfDocuments;
  }

  /**
   * @return Return the number of non-zero elements in our centroid vector.
   */
  public int getNumberOfNonZeroElementsInSummaryVector () {
    return this.s.getPopCount();
  }

  /**
   * @return Current quality of this cluster.
   */
  public double getQuality () {
    return this.cachedQuality;
  }

  /**
   * @return Sparsity of this cluster's centroid vector.
   */
  public double getSparsity () {
    return (1.0 * this.getNumberOfNonZeroElementsInSummaryVector()) /
           (double) this.dimensionSize;
  }
}

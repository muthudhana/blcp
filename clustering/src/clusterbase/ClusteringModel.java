/*
 * @(#)ClusteringModel.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



package clusterbase;

import sparsevector.SparseVector;

import java.io.*;

import java.util.*;

/**
 * Class description
 *
 *
 * @version    Enter version here..., 04/01/07
 * @author     Mike Wiacek
 */
abstract public class ClusteringModel implements Serializable {
  private static final long serialVersionUID = 1023456780L;

  /** numDocumentsContainingTerm[i] = # of docs containing term i */
  protected SparseVector numDocumentsContainingTerm = null;

  /** termDictionary stores the vector ID of a term. This is authoritative! */
  private Hashtable<String, Integer> termDictionary = null;

  /** This is the total number of terms seen in the entire collection! */
  protected int numberOfTerms = 0;

  /** globalFrequency contains the # of times a term appears in all documents */
  protected SparseVector numGlobalTermOccurences = null;

  /** nextTermId is the id of the NEXT term that will be registered! */
  private int nextTermId = 0;

  /** Creates a new instance of ClusteringModel */
  public ClusteringModel () {
    this.termDictionary = new Hashtable<String, Integer>();
    this.numGlobalTermOccurences = new SparseVector();
    this.numDocumentsContainingTerm = new SparseVector();
  }

  /**
   * Updates the number of times we've seen a particular term throughout an
   * entire corpus.
   *
   * @param termId
   * @param freq
   *
   * @return
   */
  protected int incrementGlobalTermOccurence (int termId,
                                              int freq) {
    int oldFreq = (int) this.numGlobalTermOccurences.get(termId);

    this.numGlobalTermOccurences.set(termId, 1.0 * oldFreq + freq);

    // count each term as appropriate for a global term count.
    this.numberOfTerms += freq;

    return oldFreq + freq;
  }

  /**
   * Method description
   *
   *
   * @param termId
   *
   * @return
   */
  protected int incrementNumDocumentsContainingTerm (int termId) {
    int oldCount = (int) this.numDocumentsContainingTerm.get(termId);

    this.numDocumentsContainingTerm.set(termId, oldCount + 1.0);

    return oldCount + 1;
  }

  /**
   * Returns the numerical term id of the provided term. If the term is new,
   * a new value is assigned to it and returned.
   *
   * @param term
   *
   * @return
   */
  public int registerTerm (String term) {
    if (this.termDictionary.containsKey(term)) {
      return this.termDictionary.get(term);
    } else {
      this.termDictionary.put(term, this.nextTermId++);

      return this.nextTermId - 1;
    }
  }

  /**
   * Returns the frequency of a particular term throughout the entire
   * collection.  The parameter will automatically be converted to the
   * correct case and run through the appropriate stemmer.
   *
   * @param termId
   *
   * @return
   */
  public int getFrequencyOfTerm (int termId) {
    return (int) (this.numGlobalTermOccurences.get(termId));
  }

  /**
   * Method description
   *
   *
   * @param termId
   *
   * @return
   */
  abstract public double getGlobalTermWeight (int termId);

  /**
   * Returns the total number of unique terms in the collection after stemming.
   *
   * @return
   */
  public int getNumberOfDistinctTerms () {
    return this.termDictionary.size();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  abstract public int getNumberOfDocuments ();

  /**
   * Returns the number of documents that contain a particular term.
   *
   * @param termId
   *
   * @return
   */
  public int getNumberOfDocumentsContainingTerm (int termId) {
    return (int) (this.numDocumentsContainingTerm.get(termId));
  }

  /**
   * Returns the total number of terms found in the collection.
   *
   * @return
   */
  public int getNumberOfTerms () {
    return this.numberOfTerms;
  }

  /**
   * Returns ther numerical term id of the provided term. A return
   * value of -1 means no such term exists in the dictionary.
   *
   * @param term
   *
   * @return
   */
  public int getTermId (String term) {
    if (this.termDictionary.containsKey(term)) {
      return this.termDictionary.get(term);
    } else {
      return -1;
    }
  }

  /**
   * Returns the variance of the term identified by the provided termId
   *
   * @param j
   *
   * @return
   */
  protected double getTermVariance (int j) {
    double variance = 0.0;
    int numDocsContainingTerm = this.getNumberOfDocumentsContainingTerm(j);
    int numDocs = this.getNumberOfDocuments();
    double average = numDocsContainingTerm * 1.0 / numDocs;

    for (int i = 0; i < numDocsContainingTerm; ++i) {
      variance += (1.0 - average) * (1.0 - average);
    }

    for (int i = 0; i < (numDocs - numDocsContainingTerm); ++i) {
      variance += (0.0 - average) * (0.0 - average);
    }

    variance = variance / (numDocs * 1.0);

    return variance;
  }
}

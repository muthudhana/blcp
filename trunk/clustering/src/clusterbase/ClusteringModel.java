package clusterbase;

import java.util.*;
import java.io.*;
import sparsevector.SparseVector;

abstract public class ClusteringModel implements Serializable {

  private static final long serialVersionUID = 1023456780L;
  
  /** numDocumentsContainingTerm[i] = # of docs containing term i */
  protected SparseVector numDocumentsContainingTerm = null;

  /** termDictionary stores the vector ID of a term. This is authoritative! */
  private Hashtable<String, Integer> termDictionary = null;

  /** nextTermId is the id of the NEXT term that will be registered! */
  private int nextTermId = 0;

  /** globalFrequency contains the # of times a term appears in all documents */
  protected SparseVector numGlobalTermOccurences = null;

  /** This is the total number of terms seen in the entire collection! */
  protected int numberOfTerms = 0;

  /** Creates a new instance of ClusteringModel */
  public ClusteringModel() 
      throws Exception {
    this.termDictionary = new Hashtable<String, Integer>();
    this.numGlobalTermOccurences = new SparseVector();
    this.numDocumentsContainingTerm = new SparseVector();
  }

  /**
   * Updates the number of times we've seen a particular term throughout an
   * entire corpus.
   */
  protected int incrementGlobalTermOccurence(int termId, int freq) {
    int oldFreq = (int) this.numGlobalTermOccurences.get (termId);
    this.numGlobalTermOccurences.set (termId, 1.0 * oldFreq + freq);
    // count each term as appropriate for a global term count.
    this.numberOfTerms += freq;  
    return oldFreq + freq;
  }

  protected int incrementNumDocumentsContainingTerm(int termId) {
    int oldCount = (int) this.numDocumentsContainingTerm.get(termId);
    this.numDocumentsContainingTerm.set(termId, oldCount + 1.0);
    return oldCount + 1;
  }

  /**
   * Returns the frequency of a particular term throughout the entire
   * collection.  The parameter will automatically be converted to the
   * correct case and run through the appropriate stemmer.
   */
  public int getFrequencyOfTerm(int termId) {
    return (int) (this.numGlobalTermOccurences.get(termId));
  }

  /**
   * Returns the total number of unique terms in the collection after stemming.
   */
  public int getNumberOfDistinctTerms() {
    return this.termDictionary.size();
  }

  /**
   * Returns the total number of terms found in the collection.
   */
  public int getNumberOfTerms() {
    return this.numberOfTerms;
  }

  /**
   * Returns ther numerical term id of the provided term. A return
   * value of -1 means no such term exists in the dictionary.
   */
  public int getTermId(String term) {
    if (this.termDictionary.containsKey(term)) {
      return this.termDictionary.get(term);
    } else {
      return -1;
    }
  }

  /**
   * Returns the numerical term id of the provided term. If the term is new,
   * a new value is assigned to it and returned.
   */
  public int registerTerm(String term) {
    if (this.termDictionary.containsKey(term)) {
      return this.termDictionary.get(term);
    } else {
      this.termDictionary.put(term, this.nextTermId++);
      return this.nextTermId - 1;
    }
  }

  /**
   * Returns the number of documents that contain a particular term.
   */
  public int getNumberOfDocumentsContainingTerm(int termId) {
    return (int) (this.numDocumentsContainingTerm.get(termId));
  }

  /**
   * Returns the variance of the term identified by the provided termId
   */
  protected double getTermVariance(int j) {
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

  abstract public double getGlobalTermWeight(int termId);

  abstract public int getNumberOfDocuments();
}
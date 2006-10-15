/*
 * VectorSpaceModel.java
 *
 * Created on February 13, 2006, 1:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package clusterbase;

import java.util.*; // need Hashtable and ArrayList
import java.io.*;  // need File
import sparsevector.SparseMatrix;
import sparsevector.SparseVector;

/**
 *
 * @author mike
 */
public class VectorSpaceModel extends ClusteringModel {

  protected ArrayList<Document> documents = null;

  /** Creates a new instance of VectorSpaceModel */
  public VectorSpaceModel (IStemmer stemmer, String stopListFileName) throws Exception {
    super (stemmer, stopListFileName);
    this.documents = new ArrayList<Document>();
  }

  public Document getDocument (int i) {
    return (this.documents.get (i) );
  }

  public int addDirectory (String directory, int sourceId) {
    File f = new File (directory);
    File[] files = f.listFiles();

    int numDocumentsAdded = 0;

    for (int i = 0; i < files.length; ++i) {

      if (files[i].isDirectory() ) {
        numDocumentsAdded += this.addDirectory (files[i].toString(), sourceId);
      }

      if (files[i].isFile() ) {
        this.addDocument (files[i].toString(), sourceId);
        ++numDocumentsAdded;
      }
    }
    return (numDocumentsAdded);
  }

  public boolean addDocument (Document doc) {
    return (this.incorporateDocument (doc) );
  }


  public boolean addDocument (String document, int sourceId) {

    Document doc = null;

    System.out.println ("Adding document: " + document);
    System.out.println ("That will be document #" + this.documents.size() );

    try {
      doc = new Document (document, this.stopList, this.stemmer, sourceId);
    } catch (Exception e) {
      System.out.println (e);
      return (false);
    }

    return (this.incorporateDocument (doc) );
  }

  private boolean incorporateDocument (Document doc) {

    if (this.documents.contains (doc) == true) {
      return (false);
    }

    this.documents.add (doc);

    if (doc.setModel (this) == false) {
      return (false);
    }

    SparseVector documentFrequencyVector = doc.getFrequencyVector();
    int[] idx = documentFrequencyVector.getIndicies();

    for (int i = 0; i < idx.length; ++i) {
      int j = idx[i];
      this.incrementNumDocumentsContainingTerm (j);
      this.incrementGlobalTermOccurence (j, (int) documentFrequencyVector.get (j) );
    }
    return (true);
  }

  public int getNumberOfNonZeroEntries() {
    int num = 0;

    for (int i = 0; i < this.documents.size(); ++i) {
      num += ( (this.documents.get (i) ).getNormalizedVector (this) ).getPopCount();
    }

    return (num);
  }

  public int getNumberOfTotalCoordinates() {
    return (this.getNumberOfDistinctTerms() * this.getNumberOfDocuments() );
  }

  /**
   * Return number of documents in the collection
   */
  public int getNumberOfDocuments() {
    return (this.documents.size() );
  }


  /**
   * Returns a SparseMatrix object representing the term document matrix of this Vector Space Model object.
   * Document vectors are weighted according to the provided parameters.
   */
  protected SparseMatrix calculateTermDocumentMatrix() {
    SparseMatrix sm = new SparseMatrix();

    for (int i = 0; i < this.documents.size(); ++i) {
      sm.addColumn (this.documents.get (i).getNormalizedVector (this) );
    }

    return (sm);
  }

  public double getGlobalTermWeight (int termId) {
    return (Math.log (this.documents.size() / this.getNumberOfDocumentsContainingTerm (termId) ) / Math.log (2) );
  }

  public int[] useNHighVarianceTerms (int n) {
    int[] termList = new int[n];
    PriorityQueue<TermVarianceStructure> pq = new PriorityQueue<TermVarianceStructure> (this.getNumberOfDistinctTerms() );
    int numTerms = this.getNumberOfDistinctTerms();
    for (int i = 0; i < numTerms; ++i) {
      TermVarianceStructure t = new TermVarianceStructure();
      t.termId = i;
      t.variance = this.getTermVariance (i);
      pq.add (t);
    }

    for (int i = 0; i < n; ++i) {
      TermVarianceStructure t = pq.poll();
//            System.out.println(i + ") Term Id = " + t.termId + " Variance = " + t.variance);
      termList[i] = t.termId;
    }

    Iterator i = this.documents.iterator();
    while (i.hasNext() ) {
      Document d = (Document) i.next();
      d.setTermList (termList);
    }

    return (termList);
  }
}

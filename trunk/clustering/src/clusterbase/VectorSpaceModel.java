/*
 * @(#)VectorSpaceModel.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



/*
 * VectorSpaceModel.java
 *
 * Created on February 13, 2006, 1:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package clusterbase;

import sparsevector.SparseMatrix;
import sparsevector.SparseVector;

import java.io.*;      // need File

import java.util.*;    // need Hashtable and ArrayList

/**
 *
 * @author mike
 */
public class VectorSpaceModel extends ClusteringModel {
  protected ArrayList<Document> documents = null;
  protected IStemmer stemmer = null;
  protected StopList stopList = null;

  /**
   * Creates a new instance of VectorSpaceModel
   *
   * @param stemmer
   * @param stopListFileName
   *
   * @throws Exception
   */
  public VectorSpaceModel (IStemmer stemmer,
                           String stopListFileName) throws Exception {
    super();

    this.stemmer = stemmer;
    this.stopList = new StopList(this.stemmer, stopListFileName);
    this.documents = new ArrayList<Document>();
  }

  /**
   * Method description
   *
   *
   * @param directory
   * @param sourceId
   *
   * @return
   */
  public int addDirectory (String directory,
                           int sourceId) {
    File f = new File(directory);
    File[] files = f.listFiles();

    int numDocumentsAdded = 0;

    for (int i = 0; i < files.length; ++i) {

      if (files[i].isDirectory()) {
        numDocumentsAdded += this.addDirectory(files[i].toString(), sourceId);
      }

      if (files[i].isFile()) {
        this.addDocument(files[i].toString(), sourceId);

        ++numDocumentsAdded;
      }
    }

    return (numDocumentsAdded);
  }

  /**
   * Method description
   *
   *
   * @param doc
   *
   * @return
   */
  public boolean addDocument (Document doc) {
    return (this.incorporateDocument(doc));
  }

  /**
   * Method description
   *
   *
   * @param document
   * @param sourceId
   *
   * @return
   */
  public boolean addDocument (String document,
                              int sourceId) {

    Document doc = null;

    System.out.println("Adding document: " + document);
    System.out.println("That will be document #" + this.documents.size());

    try {
      doc = new Document(document, this.stopList, this.stemmer, sourceId);
    } catch (Exception e) {
      System.out.println(e);

      return (false);
    }

    return (this.incorporateDocument(doc));
  }

  /**
   * Returns a SparseMatrix object representing the term document matrix of this Vector Space Model object.
   * Document vectors are weighted according to the provided parameters.
   *
   * @return
   */
  protected SparseMatrix calculateTermDocumentMatrix () {
    SparseMatrix sm = new SparseMatrix();

    for (int i = 0; i < this.documents.size(); ++i) {
      sm.addColumn(this.documents.get(i).getNormalizedVector(this));
    }

    return (sm);
  }

  /**
   * Method description
   *
   *
   * @param doc
   *
   * @return
   */
  private boolean incorporateDocument (Document doc) {

    if (this.documents.contains(doc) == true) {
      return (false);
    }

    this.documents.add(doc);

    if (doc.setModel(this) == false) {
      return (false);
    }

    SparseVector documentFrequencyVector = doc.getFrequencyVector();
    int[] idx = documentFrequencyVector.getIndicies();

    for (int i = 0; i < idx.length; ++i) {
      int j = idx[i];

      this.incrementNumDocumentsContainingTerm(j);
      this.incrementGlobalTermOccurence(
          j, (int) documentFrequencyVector.get(j));
    }

    return (true);
  }

  /**
   * Method description
   *
   *
   * @param n
   *
   * @return
   */
  public int[] useNHighVarianceTerms (int n) {
    int[] termList = new int[n];
    PriorityQueue<TermVarianceStructure> pq =
      new PriorityQueue<TermVarianceStructure>(this.getNumberOfDistinctTerms());
    int numTerms = this.getNumberOfDistinctTerms();

    for (int i = 0; i < numTerms; ++i) {
      TermVarianceStructure t = new TermVarianceStructure();

      t.termId = i;
      t.variance = this.getTermVariance(i);

      pq.add(t);
    }

    for (int i = 0; i < n; ++i) {
      TermVarianceStructure t = pq.poll();

//            System.out.println(i + ") Term Id = " + t.termId + " Variance = " + t.variance);
      termList[i] = t.termId;
    }

    Iterator i = this.documents.iterator();

    while (i.hasNext()) {
      Document d = (Document) i.next();

      d.setTermList(termList);
    }

    return (termList);
  }

  /**
   * Method description
   *
   *
   * @param i
   *
   * @return
   */
  public Document getDocument (int i) {
    return (this.documents.get(i));
  }

  /**
   * Method description
   *
   *
   * @param termId
   *
   * @return
   */
  public double getGlobalTermWeight (int termId) {
    return (Math.log(
        this.documents.size() /
        this.getNumberOfDocumentsContainingTerm(termId)) / Math.log(2));
  }

  /**
   * Return number of documents in the collection
   *
   * @return
   */
  public int getNumberOfDocuments () {
    return (this.documents.size());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getNumberOfNonZeroEntries () {
    int num = 0;

    for (int i = 0; i < this.documents.size(); ++i) {
      num += ((this.documents.get(i)).getNormalizedVector(this)).getPopCount();
    }

    return (num);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getNumberOfTotalCoordinates () {
    return (this.getNumberOfDistinctTerms() * this.getNumberOfDocuments());
  }
}

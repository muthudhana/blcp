/*
 * @(#)Document.java   04/01/07
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
public class Document implements Serializable {
  private static final long serialVersionUID = -6907142671647031026L;
  public String filename = null;
  private SparseVector terms = new SparseVector();
  private int[] termList = null;
  private int sourceId = 0;
  private int numberOfTerms = 0;
  private int maxFrequencyOfAnyTerm = 0;
  transient private SparseVector lastNormalizedVector = null;
  private Hashtable<String, Integer> documentVocabulary = null;
  transient private ClusteringModel cmForLastNormVec = null;
  transient private boolean cachedVectorUsesSubsetOfTerms = false;
  public long timestamp = 0;

  /**
   * Creates a new instance of Document
   *
   * @param document
   * @param stopList
   * @param stemmer
   * @param sourceId
   */
  public Document (String document,
                   StopList stopList,
                   IStemmer stemmer,
                   int sourceId) {

    // Uncomment the next 2 lines and comment out the 3rd to use the
    // tokenizer.
    ITokenizer tok = new EmailTokenizer(stemmer, document);

    this.timestamp = ((EmailTokenizer) tok).getTimestamp();

    // ITokenizer tok = new Tokenizer(stemmer, document);

    this.sourceId = sourceId;
    this.filename = new String(document);
    this.documentVocabulary = new Hashtable<String, Integer>();
    this.termList = null;

    System.out.println("Parsing file: " + document);

    String s = null;

    try {
      while ((s = tok.nextToken()).length() > 0) {
        if (stopList.containsTerm(s) == true) {

          // If the current token is on our stop list
          // continue to the next token immediately.
          // We treat this as if this token never existed!
          continue;
        }

        Integer freq = this.documentVocabulary.get(s);

        if (freq == null) {
          this.documentVocabulary.put(s, 1);
        } else {
          this.documentVocabulary.put(s, freq + 1);
        }

        ++this.numberOfTerms;
      }
    } catch (Exception ex) {
      FileOutputStream out;
      PrintStream p;

      try {
        System.out.println("Exception has occured... press any key...");
        System.in.read();

        out = new FileOutputStream("c:\\EXCEPTION.txt");
        p = new PrintStream(out);

        ex.printStackTrace(p);
        p.close();
        out.close();
        System.out.println("Working on document: " + document);
      } catch (Exception e) {
        System.err.println("Error writing to file");
      }

      System.exit(-1);
    }
  }

  /**
   * Method description
   *
   */
  public void clearTermList () {
    this.termList = null;
  }

  /**
   * Method description
   *
   *
   * @param termId
   *
   * @return
   */
  public boolean containsTerm (int termId) {
    return this.terms.contains(termId);
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   *
   * @throws Exception
   */
  public static Document deserializeDocument (String file) throws Exception {
    int len = (int) (new File(file).length());
    FileInputStream fis = new FileInputStream(file);
    byte buf[] = new byte[len];

    fis.read(buf);

    ByteArrayInputStream bais = new ByteArrayInputStream(buf);
    ObjectInputStream ois = new ObjectInputStream(bais);
    Document doc = (Document) ois.readObject();

    ois.close();
    bais.close();
    fis.close();

    return doc;
  }

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   */
  private SparseVector internalGetNormalizedVector (ClusteringModel v) {
    SparseVector documentVector = new SparseVector();

    this.setModel(v);

    if ((this.cachedVectorUsesSubsetOfTerms == false) &&
        (this.cmForLastNormVec == v) && (this.lastNormalizedVector != null)) {
      return this.lastNormalizedVector;
    }

    /* Update cached copy records */
    this.cachedVectorUsesSubsetOfTerms = false;
    this.cmForLastNormVec = v;

    this.lastNormalizedVector = null;

    int numTerms = v.getNumberOfDistinctTerms();

    // Local weighting Scheme is simply FREQ
    documentVector.add(this.terms);

    // Now that all local terms are weighted, let's build an array of
    // their indicies in our documentVector...
    int[] idx = documentVector.getIndicies();

    // Perform global weighting -- IDFB
    for (int i = 0; i < idx.length; ++i) {
      double globalWeightFactor = v.getGlobalTermWeight(idx[i]);

      documentVector.set(idx[i],
                         documentVector.get(idx[i]) * globalWeightFactor);
    }

    // Normalize this document! -- COSN
    double sum = documentVector.length();

    if (sum == 0) {
      sum = 1;    // Handle empty documents! Keep division well defined! :-)
    }

    double Nj = 1;

    Nj = 1 / sum;

    for (int i = 0; i < idx.length; ++i) {
      documentVector.set(idx[i], documentVector.get(idx[i]) * Nj);
    }

    this.lastNormalizedVector = documentVector;

    return documentVector;
  }

  /**
   * Method description
   *
   *
   * @param v
   * @param termList
   *
   * @return
   */
  private SparseVector internalGetNormalizedVector (ClusteringModel v,
                                                    int[] termList) {
    SparseVector documentVector = new SparseVector();

    this.setModel(v);

    if ((this.cachedVectorUsesSubsetOfTerms == true) &&
        (this.cmForLastNormVec == v) && (this.lastNormalizedVector != null)) {
      return this.lastNormalizedVector;
    }

    /* Update cached copy records */
    this.cachedVectorUsesSubsetOfTerms = true;
    this.cmForLastNormVec = v;
    this.lastNormalizedVector = null;

    int numTerms = termList.length;

    for (int i = 0; i < numTerms; ++i) {
      double lFreq = this.terms.get(termList[i]);

      if (lFreq != 0D) {
        double globalWeightFactor = v.getGlobalTermWeight(termList[i]);

        documentVector.set(i, lFreq * globalWeightFactor);
      }
    }

    double Nj = 0.0;

    if (documentVector.length() == 0) {
      Nj = 1.0;
    } else {
      Nj = 1.0 / documentVector.length();
    }

    for (int i = 0; i < numTerms; ++i) {
      double curVal = documentVector.get(i);

      curVal = curVal * Nj;

      documentVector.set(i, curVal);
    }

    this.lastNormalizedVector = documentVector;

    return documentVector;
  }

  /**
   * Method description
   *
   *
   * @param doc
   * @param outputFileName
   *
   * @throws Exception
   */
  public static void serializeDocument (Document doc,
                                        String outputFileName)
                                        throws Exception {
    FileOutputStream fos = new FileOutputStream(outputFileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);

    oos.writeObject(doc);
    oos.flush();
    oos.close();
    fos.close();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getFilename () {
    return this.filename;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public SparseVector getFrequencyVector () {
    return this.terms;
  }

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   */
  public SparseVector getNormalizedVector (ClusteringModel v) {
    if (this.termList == null) {
      return this.internalGetNormalizedVector(v);
    } else {
      return this.internalGetNormalizedVector(v, this.termList);
    }
  }

  /**
   * Returns the total number of terms found in the document.
   *
   * @return
   */
  public int getNumberOfTerms () {
    return this.numberOfTerms;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getSourceId () {
    return this.sourceId;
  }

  /**
   * Returns the number of times a particular term appears in the document.
   *
   * @param termId
   *
   * @return
   */
  public int getTermFrequency (int termId) {
    return termId;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public long getTimestamp () {
    return this.timestamp;
  }

  /**
   * Permanently bind this Document object to a ClusteringModel.
   * This is permanent, and cannot be undone.
   *
   * @param vsm
   *
   * @return
   */
  public boolean setModel (ClusteringModel vsm) {
    if (this.documentVocabulary == null) {
      return false;
    }

    this.terms = new SparseVector();

    for (Enumeration e =
        this.documentVocabulary.keys(); e.hasMoreElements(); ) {
      String key = (String) e.nextElement();
      int termId = vsm.registerTerm(key);

      this.terms.set(termId, (double) this.documentVocabulary.get(key));
    }

    this.documentVocabulary = null;

    return true;
  }

  /**
   * Method description
   *
   *
   * @param termList
   */
  public void setTermList (int[] termList) {
    this.termList = termList;
  }
}

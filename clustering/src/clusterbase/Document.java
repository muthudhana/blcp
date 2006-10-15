package clusterbase;

import java.util.*;
import java.io.*;
import sparsevector.SparseVector;

public class Document implements Serializable {

  private static final long serialVersionUID = -6907142671647031026L;

  public String filename = null;
  private SparseVector terms = new SparseVector();
  private int numberOfTerms = 0;
  private int maxFrequencyOfAnyTerm = 0;
  private int sourceId = 0;
  private int[] termList = null;
  private Hashtable<String, Integer> documentVocabulary = null;
  transient private ClusteringModel cmForLastNormVec = null;
  transient private SparseVector lastNormalizedVector = null;
  transient private boolean cachedVectorUsesSubsetOfTerms = false;
  public long timestamp = 0;

  /** Creates a new instance of Document */
  public Document(String document, StopList stopList, IStemmer stemmer, 
      int sourceId) {
    // Tokenize using porter
    ITokenizer tok = new EmailTokenizer (stemmer, document);
    
    this.sourceId = sourceId;
    this.filename = new String (document);
    this.documentVocabulary = new Hashtable<String, Integer>();
    this.termList = null;

    /* comment out this block if not doing birch! */
    this.timestamp = ((EmailTokenizer) tok).getTimestamp();
    /* end block */

    System.out.println ("Parsing file: " + document);

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

  public int getSourceId() {
    return this.sourceId;
  }

  public String getFilename() {
    return this.filename;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  /***
   * Permanently bind this Document object to a ClusteringModel. 
   * This is permanent, and cannot be undone.
   */
  public boolean setModel (ClusteringModel vsm) {
    if (this.documentVocabulary == null) {
      return false;
    }

    this.terms = new SparseVector();
    
    for (Enumeration e = this.documentVocabulary.keys(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      int termId = vsm.registerTerm(key);
      this.terms.set(termId, (double) this.documentVocabulary.get(key));
    }

    this.documentVocabulary = null;

    return true;
  }

  /**
   * Returns the total number of terms found in the document.
   */
  public int getNumberOfTerms() {
    return this.numberOfTerms;
  }

  /**
   * Returns the number of times a particular term appears in the document.
   */
  public int getTermFrequency(int termId) {
    return termId;
  }


  public boolean containsTerm(int termId) {
    return this.terms.contains(termId);
  }

  public void setTermList(int[] termList) {
    this.termList = termList;
  }

  public void clearTermList() {
    this.termList = null;
  }

  public SparseVector getNormalizedVector(ClusteringModel v) {
    if (this.termList == null) {
      return this.internalGetNormalizedVector(v);
    } else {
      return this.internalGetNormalizedVector(v, this.termList);
    }
  }

  private SparseVector internalGetNormalizedVector(ClusteringModel v, 
      int[] termList) {
    SparseVector documentVector = new SparseVector();
    this.setModel(v);

    if (this.cachedVectorUsesSubsetOfTerms == true 
        && this.cmForLastNormVec == v && this.lastNormalizedVector != null) {
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
        documentVector.set (i, lFreq * globalWeightFactor);
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

  private SparseVector internalGetNormalizedVector (ClusteringModel v) {
    SparseVector documentVector = new SparseVector();
    this.setModel (v);

    if (this.cachedVectorUsesSubsetOfTerms == false &&
        this.cmForLastNormVec == v && this.lastNormalizedVector != null) {
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
      double globalWeightFactor = v.getGlobalTermWeight (idx[i]);
      documentVector.set (idx[i], 
          documentVector.get(idx[i]) * globalWeightFactor);
    }

    // Normalize this document! -- COSN
    double sum = documentVector.length();

    if (sum == 0) {
      sum = 1;  // Handle empty documents! Keep well division defined! :-)
    }

    double Nj = 1;
    Nj = 1 / sum;

    for (int i = 0; i < idx.length; ++i) {
      documentVector.set(idx[i], documentVector.get(idx[i]) * Nj);
    }

    this.lastNormalizedVector = documentVector;
    return documentVector;
  }

  public SparseVector getFrequencyVector() {
    return this.terms;
  }

  public static void serializeDocument(Document doc, String outputFileName)
      throws Exception {
    FileOutputStream fos = new FileOutputStream(outputFileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(doc);
    oos.flush();
    oos.close();
    fos.close();
  }

  public static Document deserializeDocument (String file) throws Exception {
    FileInputStream fis = new FileInputStream(file);
    ObjectInputStream ois = new ObjectInputStream(fis);
    Document doc = (Document) ois.readObject();
    ois.close();
    fis.close();
    return doc;
  }
}

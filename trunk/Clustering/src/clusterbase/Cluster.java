/*
 * Cluster.java
 *
 * Created on March 25, 2006, 11:46 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package clusterbase;

import java.util.*;
import sparsevector.SparseMatrix;
import sparsevector.SparseVector;
/**
 *
 * @author mike
 */
public class Cluster {

  private ArrayList<Integer> docMap = null;
  private ArrayList<Document> docs = null;
  private VectorSpaceModel vsm = null;

  /** Creates a new instance of Cluster */
  public Cluster (VectorSpaceModel vsm) {
    this.docMap = new ArrayList<Integer>();
    this.docs = new ArrayList<Document>();
    this.vsm = vsm;
  }

  public int addDocument (Document doc) {
    return (this.addDocument (doc, Integer.MAX_VALUE) );
  }

  public int addDocument (Document doc, int trackingId) {
    /* THIS CODE IS NOT THREAD SAFE! */
    this.docs.add (this.docs.size(), doc);
    this.docMap.add (this.docs.size() - 1, trackingId);
    return (this.docs.size() - 1);
  }

  public Document getDocument (int docNum) {
    return (this.docs.get (docNum) );
  }

  public int getDocumentId (int docNum) {
    return (this.docMap.get (docNum) );
  }

  public int getNumberOfDocuments() {
    return (this.docs.size() );
  }

  /**
   * Returns a SparseMatrix object representing the term document matrix of this Vector Space Model object.
   * Document vectors are weighted according to the provided parameters.
   */
  protected SparseMatrix calculateTermDocumentMatrix() {
    SparseMatrix sm = new SparseMatrix();
    int minNumRow = 0;

    for (int i = 0; i < this.docs.size(); ++i) {
      SparseVector sv = this.docs.get (i).getNormalizedVector (this.vsm);
      if (sv.size() > minNumRow) {
        minNumRow = sv.size();
      }
      sm.addColumn (sv);
    }

    // ensure that our sparse matrix is always as big as the biggest column
    // of our original TDM.
    sm.setMinNumRows (minNumRow);

    return (sm);
  }

}

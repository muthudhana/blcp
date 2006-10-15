/*
 * PDDP.java
 *
 * Created on March 21, 2006, 5:13 PM
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
public class PDDP extends VectorSpaceModel {

  /** Creates a new instance of PDDP */
  public PDDP(IStemmer stemmer, String stopListFileName) throws Exception {
    super(stemmer, stopListFileName);
  }

  public double getPartitionQuality (Cluster[] c) {

    double quality = 0.0;

    for (int i = 0; i < c.length; ++i) {
      SparseMatrix tdm = c[i].calculateTermDocumentMatrix();
      SparseVector cI = tdm.getMeanColumnVector();
      SparseVector x = null;

      try {
        x = tdm.getDominantEigenvector();
      } catch (Exception e) {
        e.printStackTrace();
        System.exit (-1);
      }

      x.scalarMultiply (x.dotProduct (cI) );
      cI = x;

      for (int j = 0; j < tdm.numColumns(); ++j) {
        SparseVector a = new SparseVector (tdm.getColumn (j) );
        a.subtract (cI);
        quality += a.lengthSquared();
      }
    }
    return (quality);
  }

  public double getLowerQualityBound (Cluster[] c) {

    double bound = 0.0;

    for (int i = 0; i < c.length; ++i) {
      SparseMatrix tdm = c[i].calculateTermDocumentMatrix();
      SparseVector x = null;
      try {
        x = tdm.getDominantEigenvector();
      } catch (Exception e) {
        e.printStackTrace();
        System.exit (-1);
      }

      for (int j = 0; j < tdm.numColumns(); ++j) {
        SparseVector Aj = new SparseVector (tdm.getColumn (j) );
        SparseVector xCopy = new SparseVector (x);
        xCopy.scalarMultiply (xCopy.dotProduct (Aj) );
        Aj.subtract (xCopy);
        bound += Aj.lengthSquared();
      }
    }

    return (bound);
  }

  /**
   *  Returns a confusion matrix of the documents. The number of clusters
   *  to generate is the parameter to this method.  This is simply a wrapper
   *  around the actual clustering method doCluster.  doCluster can requires
   *  bookkeeping information and it calls itself recursively.  This public
   *  method, builds an initial set of the bookkeeping information and begins
   *  PDDP clustering.
   */
  public Cluster[] cluster (int numClusters) {

    /* Build our initial cluster */
    Cluster c = new Cluster (this);
    for (int i = 0; i < this.getNumberOfDocuments(); ++i) {
      c.addDocument (this.documents.get (i), i);
    }

    Cluster[] partition = new Cluster[numClusters];
    partition = this.doCluster (c, numClusters);

    for (int i = 0; i < numClusters; ++i) {

      int sources[] = new int[4];

      for (int j = 0; j < partition[i].getNumberOfDocuments(); ++j) {
        Document doc = partition[i].getDocument (j);
        sources[doc.getSourceId() ]++;
      }

      System.out.print ("PDDP Generated Cluster " + i + " contains ");

      for (int j = 1; j < sources.length; ++j) {
        System.out.print (sources[j] + " documents with source id of " + j + ", ");
      }
      System.out.println ("");
    }

//        System.out.println("Lower bound on quality of generated partition is: " + this.getLowerQualityBound(partition));
//        System.out.println("Partition quality is: " + this.getPartitionQuality(partition));

    return (partition);
  }


  private Cluster[] doCluster (Cluster cluster, int numClusters) {

    Cluster[] partition = new Cluster[numClusters];
    Cluster left = new Cluster (this);
    Cluster right = new Cluster (this);

    if (numClusters == 1) {
      partition[0] = cluster;
      return (partition);
    }

    try {
      SparseMatrix tdm = cluster.calculateTermDocumentMatrix();
      SparseVector eigenVector = tdm.getDominantEigenvector();
      eigenVector.normalize();
      SparseVector c = tdm.getMeanColumnVector();
      c.normalize();

      double mean = 0;
      for (int i = 0; i < cluster.getNumberOfDocuments(); ++i) {
        mean += this.getProjection (eigenVector, tdm.getColumn (i) );
      }
      mean = mean / cluster.getNumberOfDocuments();

      for (int i = 0; i < cluster.getNumberOfDocuments(); ++i) {
        if (this.getProjection (eigenVector, tdm.getColumn (i) ) >= mean) {
          right.addDocument (cluster.getDocument (i), cluster.getDocumentId (i) );
        } else {
          left.addDocument (cluster.getDocument (i), cluster.getDocumentId (i) );
        }
      }

      //  System.out.println("Right cluster has " + right.getNumberOfDocuments() + " documents");
      //  System.out.println("Left cluster has " + left.getNumberOfDocuments() + " documents");

      Cluster[] recursiveClusters = null;

      if (right.getNumberOfDocuments() >= left.getNumberOfDocuments() ) {
        recursiveClusters = this.doCluster (right, numClusters - 1);
        partition[0] = left;
      } else {
        recursiveClusters = this.doCluster (left, numClusters - 1);
        partition[0] = right;
      }

      for (int i = 0; i < numClusters - 1; ++i) {
        partition[i + 1] = recursiveClusters[i];
      }

      return (partition);

    } catch (Exception e) {
      System.out.println (e);
      e.printStackTrace();
    }

    System.out.println ("UNREACHABLE CODE EXECUTED!");
    /* we should never get here */
    return (new Cluster[0]);
  }

  private double getProjection (SparseVector direction, SparseVector operand) throws Exception {
    return (direction.dotProduct (operand) );
  }


}

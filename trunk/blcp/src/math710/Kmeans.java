///*
// * Kmeans.java
// *
// * Created on March 28, 2006, 12:35 PM
// *
// * To change this template, choose Tools | Template Manager
// * and open the template in the editor.
// */
//
package math710;
import java.util.*;
import sparsearray.*;

//
///**
// *
// * @author mike
// */

public class Kmeans {

  private final double TOL = 0.0;

  private ClusteringModel baseClusteringModel = null;
  private SparseVector[] centroids = null;
  private ArrayList<Document>[] assignedClusters = null;
  private double[] clusterQualities = null;

  private SparseVector[] newCentroids = null;
  private ArrayList<Document>[] newClusters = null;
  private double[] newQualities = null;

  private int incSourceCluster = -1;
  private int incDocumentIdx = -1;
  private int incDstCluster = -1;
  private double incMaxToBeat = 0.0;


  /**
   * While documents provide a getNormalizedVector function, I want this
   * K-Means object to be able to handle full document vectors as returned
   * by that function and subsets of those vectors such as those given by
   * high variance term selection methods.  This wrapper function examines
   * parameters provided in the constructor and returns the appropriate vector.
   */
  private SparseVector getVector (Document d) {
    return (d.getNormalizedVector (this.baseClusteringModel) );
  }

  /** Creates a new instance of Kmeans */
  public Kmeans (ClusteringModel cm, Cluster[] partition) {

    this.baseClusteringModel = cm;

    this.centroids = new SparseVector[partition.length];
    this.assignedClusters = new ArrayList[partition.length];
    this.clusterQualities = new double[partition.length];

    this.newCentroids = new SparseVector[partition.length];
    this.newClusters = new ArrayList[partition.length];
    this.newQualities = new double[partition.length];

    for (int i = 0; i < this.centroids.length; ++i) {
      this.centroids[i] = new SparseVector();
      this.assignedClusters[i] = new ArrayList<Document>();
      for (int j = 0; j < partition[i].getNumberOfDocuments(); ++j) {
        this.centroids[i].add (this.getVector (partition[i].getDocument (j) ) );
        this.assignedClusters[i].add (partition[i].getDocument (j) );
      }
      this.centroids[i].scalarDivide (partition[i].getNumberOfDocuments() );
      this.clusterQualities[i] = this.calculateClusterQuality (this.assignedClusters[i], this.centroids[i]);
    }
  }

  private SparseVector calculateCentroid (ArrayList<Document> docs) {
    SparseVector centroid = new SparseVector();
    Iterator i = docs.iterator();

    while (i.hasNext() ) {
      Document d = (Document) i.next();
      centroid.add (this.getVector (d) );
    }

    centroid.scalarDivide (docs.size() );

    return (centroid);
  }

  private double calculatePartitionQuality (ArrayList<Document>[] docs, SparseVector[] centroid) {
    double quality = 0.0;
    for (int i = 0; i < docs.length; ++i) {
      quality += this.calculateClusterQuality (docs[i], centroid[i]);
    }
    return (quality);
  }

  private double calculateClusterQuality (ArrayList<Document> docs, SparseVector centroid) {
    double quality = 0.0;
    SparseVector c = centroid;
    for (int i = 0; i < docs.size(); ++i) {
      Document doc = docs.get (i);
      quality += c.distanceSquared (this.getVector (doc) );
    }
    return (quality);
  }

  public void cluster (int maxIterations) {

    if (maxIterations <= 0) {
      return;
    }

    System.out.println ("\nQuality of initial partition as provided by PDDP is: " + this.calculatePartitionQuality (this.assignedClusters, this.centroids) + "\n");

    for (int numChanged = 0, itr = 0; numChanged > 0 || itr == 0; ++itr) {

      numChanged = 0;

      while (true) {

        int numReassigned = this.doBatchKmeans();

        System.out.println ("After an iteration of Batch K-Means, " + numReassigned + " documents were moved.");

        double oldQuality = 0.0;
        double newQuality = 0.0;
        for (int b = 0; b < this.centroids.length; ++b) {
          oldQuality += this.clusterQualities[b];
          newQuality += this.newQualities[b];
        }

        double qualityDelta =  oldQuality - newQuality;
        System.out.println ("Change in quality is: " + qualityDelta);

        if (qualityDelta < this.TOL) {
          System.out.println ("Benefit of change is below tolerance... Switching to incremental...\n");
          break;
        }

        if (numReassigned == 0) {
          System.out.println ("Batch K-Means has made no changes! Switching to incremental...\n");
          break;
        }

        // We like the new results. Let's make them authoritative
        for (int k = 0; k < this.assignedClusters.length; ++k) {
          this.assignedClusters[k] = this.newClusters[k];
          this.centroids[k] = this.newCentroids[k];
          this.clusterQualities[k] = this.newQualities[k];
        }

        numChanged = numReassigned; // Record the fact we made a change!
      }

      double qual = 0.0;
      for (int i = 0; i < this.clusterQualities.length; ++i) {
        qual += this.clusterQualities[i];
      }
      System.out.println ("Quality of partition generated by Batch K-Means: " + qual);

      /* Do Incremental K-Means here */
      System.out.println ("\nContinuing with Incremental K-Means...\n");

      if (this.doIncrementalKmeans() > 0) {

        double oldQuality = 0.0;
        double newQuality = 0.0;
        for (int b = 0; b < this.centroids.length; ++b) {
          oldQuality += this.clusterQualities[b];
          newQuality += this.newQualities[b];
        }

        double qualityDelta =  oldQuality - newQuality;
        System.out.println ("Incremental step change in quality is: " + qualityDelta);

        if (qualityDelta < this.TOL) {
          System.out.println ("Incremental K-Means was unable to improve cluster qualities...\n");
          break;
        }

        // We like the new results. Let's make them authoritative
        for (int k = 0; k < this.assignedClusters.length; ++k) {
          this.assignedClusters[k] = this.newClusters[k];
          this.centroids[k] = this.newCentroids[k];
          this.clusterQualities[k] = this.newQualities[k];
        }
        ++numChanged; // We made a change!
      } else {
        System.out.println ("Batch K-Means had no suggestions for documents to move incrementally...\n");
        break;
      }
    }

    System.out.println ("Batch & Incremental K-Means Complete!\n");

    double qual = 0.0;
    for (int i = 0; i < this.clusterQualities.length; ++i) {
      qual += this.clusterQualities[i];
    }
    System.out.println ("Quality of partition generated by Batch K-Means and Incremental K-Means: " + qual + "\n");

    for (int i = 0; i < this.centroids.length; ++i) {
      Hashtable<Integer, Integer> cfm = new Hashtable<Integer, Integer>();
      System.out.println ("Cluster " + i + " has " + this.assignedClusters[i].size() + " documents");
      Iterator itr = this.assignedClusters[i].iterator();
      while (itr.hasNext() ) {
        Document d = (Document) itr.next();
        Integer cur = cfm.get (d.getSourceId() );
        if (cur == null) {
          cfm.put (d.getSourceId(), new Integer (1) );
        } else {
          cfm.put (d.getSourceId(), cur + 1);
        }
      }

      Enumeration e = cfm.keys();
      while (e.hasMoreElements() ) {
        Integer val = (Integer) e.nextElement();
        System.out.println ("Cluster " + i + " has " + cfm.get (val) + " documents from source " + val);
      }

      System.out.println ("");
    }

    System.out.println ("");

  }

  /**
   * This method relies on variables created during doBatchKmeans() as a result,
   * that method must be called first!  This method returns the number of documents
   * moved, which is essentially 1 if it works or 0 if it doesn't.
   */
  private int doIncrementalKmeans() {

    for (int i = 0; i < this.centroids.length; ++i) {
      this.newClusters[i] = new ArrayList<Document> (this.assignedClusters[i]);
      this.newCentroids[i] = this.centroids[i];
      this.newQualities[i] = this.clusterQualities[i];
      System.out.println ("Current Quality of cluster " + i + " is: " + this.clusterQualities[i]);
    }

    if (this.incSourceCluster != this.incDstCluster) {

      Document doc = this.newClusters[this.incSourceCluster].get (this.incDocumentIdx);
      SparseVector docVec = this.getVector (doc);

      /* Calculate new centroids first */
      this.newCentroids[this.incSourceCluster].scalarMultiply (this.assignedClusters[this.incSourceCluster].size() );
      this.newCentroids[this.incSourceCluster].subtract (docVec);
      this.newCentroids[this.incSourceCluster].scalarDivide (this.assignedClusters[this.incSourceCluster].size() - 1);

      this.newCentroids[this.incDstCluster].scalarMultiply (this.assignedClusters[this.incDstCluster].size() );
      this.newCentroids[this.incDstCluster].add (docVec);
      this.newCentroids[this.incDstCluster].scalarDivide (this.assignedClusters[this.incDstCluster].size() + 1);

      /* Now calculate new qualities at these centroids */
      this.newQualities[this.incSourceCluster] = this.calculateClusterQuality (this.newClusters[this.incSourceCluster], this.newCentroids[this.incSourceCluster]);
      this.newQualities[this.incDstCluster] = this.calculateClusterQuality (this.newClusters[this.incDstCluster], this.newCentroids[this.incDstCluster]);

      /* Pull the selected document out of its cluster and put it into the new one */
      this.newClusters[this.incDstCluster].add (doc);
      this.newClusters[this.incSourceCluster].remove (this.incDocumentIdx);
      System.out.println ("Incremental proposes moving a single document from cluster " + this.incSourceCluster + " to cluster " + this.incDstCluster);

      return (1);

    } else {
      return (0);
    }
  }


  /**
   * Performs one iteration of batch k-means. Returns the number of documents that
   * were moved during this iteration. This method also updates the global variables
   * newClusters[] and newCentroids[] to the values. It's up to the caller to copy these
   * over the current assignedClusters[] and centroids[] arrays if desired.  Initial centroids of
   * each initial cluster must be built in the constructor.
   */
  private int doBatchKmeans() {

    System.out.println ("\nBegining a new iteration of K-Means...");

    int numReassigned = 0;

    /* Clear records for incremental k-means */
    this.incSourceCluster = -1;
    this.incDocumentIdx = -1;
    this.incDstCluster = -1;
    this.incMaxToBeat = 0D;

    for (int i = 0; i < this.centroids.length; ++i) {
      this.newClusters[i] = new ArrayList<Document>();
      this.newCentroids[i] = new SparseVector();
      this.newQualities[i] = 0.0;
//            System.out.println("Current Quality of cluster " + i + " is: " + this.clusterQualities[i]);
    }

    for (int clusterNum = 0; clusterNum < this.centroids.length; ++clusterNum) { // iterate over clusters
      for (int docNum = 0; docNum < this.assignedClusters[clusterNum].size(); ++docNum) { // iterate over docs

        /* Store the document the loops have selected in the 'doc' variable.
         * Store is vector in the 'docVec' variable for easy access. */
        Document doc = this.assignedClusters[clusterNum].get (docNum);
        SparseVector docVec = this.getVector (doc);

        int bestClusterNum = clusterNum; // Assume we are already in the best cluster.
        double distanceToCurrentCentroid = this.centroids[clusterNum].distanceSquared (docVec);
        double squareDistanceOfBestCluster =  distanceToCurrentCentroid;

        for (int i = 0; i < this.centroids.length; ++i) {

          double distance = 0.0;

          // see which centroid is closest to docVec
          if (clusterNum == i) { // We know the distance in its' current cluster.
            distance = distanceToCurrentCentroid;
          } else {
            distance = this.centroids[i].distanceSquared (docVec);
            double incChange =
              ( (this.assignedClusters[clusterNum].size() / (this.assignedClusters[clusterNum].size() - 1) ) * distanceToCurrentCentroid) -
              ( (this.assignedClusters[i].size() / (this.assignedClusters[i].size() + 1) ) * (distance) );

            if (incChange > this.incMaxToBeat) {
              this.incMaxToBeat = incChange;
              this.incDocumentIdx = docNum;
              this.incDstCluster = i;
              this.incSourceCluster = clusterNum;
            }
          }

          if (distance < squareDistanceOfBestCluster) {
//                        System.out.println("\nDocument is " + doc.getFilename());
//                        System.out.println("Currently assigned to cluster:       " + bestClusterNum);
//                        System.out.println("Moving document to cluster:          " + i);
//                        System.out.println("Square distance to current centroid: " + squareDistanceOfBestCluster);
//                        System.out.println("Square distance to new centroid:     " + distance);
//                        System.out.println("Difference between the two is:       " + (double)(squareDistanceOfBestCluster - distance));
            squareDistanceOfBestCluster = distance;
            bestClusterNum = i;
          }
        }

        if (bestClusterNum != clusterNum) {  // we moved a document!
          ++numReassigned;
        }

        this.newClusters[bestClusterNum].add (doc);
        this.newCentroids[bestClusterNum].add (docVec);
      }
    }

    // Calculate the centroids of the clusters
    for (int i = 0; i < newClusters.length; ++i) {
      this.newCentroids[i].scalarDivide (this.newClusters[i].size() );
      this.newQualities[i] = this.calculateClusterQuality (this.newClusters[i], this.newCentroids[i]);
      System.out.println ("Quality of new cluster " + i + " is: " + this.newQualities[i]);
    }

    return (numReassigned);
  }

}

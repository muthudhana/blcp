package blc;

import clusterbase.Document;
import java.util.*;
import java.io.*;
import sparsevector.SparseVector;

public class BirchCluster {
  private static final long serialVersionUID = 1234567890L;
  private SparseVector s = null;
  private double sumSquaredLengths = 0.0;
  private int numberOfDocuments = 0;
  private double cachedQuality = 0.0;
  private ArrayList<String> includedFiles = null;

  public BirchCluster() {
    this.s = new SparseVector();
    this.sumSquaredLengths = 0D;
    this.numberOfDocuments = 0;
    this.cachedQuality = 0.0;
    this.includedFiles = new ArrayList<String>();
  }

  public double getQuality() {
    return this.cachedQuality;
  }

  public double calculateChangeInQuality(SparseVector normalizedVector) {
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

    /* Undo our changes to this.s  since sCopy is not really a copy!
     * This should help memory usage since we don't actually need to
     * allocate a new object, we can perform some algebra to put back
     * what we changed.
     */
    sCopy.add (docVec);
    sCopy.scalarMultiply (this.numberOfDocuments);

    return newQuality;
  }

  /***
   * Adds the document in the vector space model provided as parameters
   * to this BIRCH like cluster.  The new cluster quality is returned.
   *
   * Not thread safe
   */
  public double addDocument(BirchKmeans vsm, Document doc) {
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

  /***
   * Adds the document in the vector space model provided as parameters
   * to this BIRCH like cluster.  This overloaded version allows the caller
   * to specify the quality of the resulting cluster.  Make sure the caller
   * calculates this value properly.  This will save several cycles by avoiding
   * the penalty of calculating a new quality value, when the caller might have
   * this information already available.
   *
   * Not thread safe
   */
  public double addDocument(BirchKmeans vsm, Document doc,
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

  public int getNumberOfDocuments() {
    return this.numberOfDocuments;
  }

  public static void serializeBirchCluster(BirchCluster bc, 
      String outputFileName) throws Exception {
    FileOutputStream fos = new FileOutputStream(outputFileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(bc);
    oos.flush();
    oos.close();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Birch Cluster Statistics\n");
    sb.append("Number of Documents: " + this.getNumberOfDocuments() + "\n");
    sb.append("Cluster Quality:     " + this.getQuality() + "\n");
    sb.append("\n");
    return sb.toString();
  }
  
  public static BirchCluster deserializeBirchKmeans(String filename) 
      throws Exception {
    FileInputStream fis = new FileInputStream(filename);
    ObjectInputStream ois = new ObjectInputStream(fis);
    BirchCluster bc = (BirchCluster) ois.readObject();
    return bc;
  }
}

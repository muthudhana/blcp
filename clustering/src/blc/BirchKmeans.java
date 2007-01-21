package blc;

import clusterbase.ClusteringModel;
import clusterbase.Document;
import clusterbase.DocumentTimeStruct;
import clusterbase.TermVarianceStructure;
import java.util.*; // need Hashtable and ArrayList
import java.io.*;  // need File
import sparsevector.SparseVector;

public class BirchKmeans extends ClusteringModel implements Serializable {
  
  private static final long serialVersionUID = 124958239691558337L;
  
  private ArrayList<BirchCluster> clusters = new ArrayList<BirchCluster>();
  private Hashtable<String, Integer> documentNames = null;
  private SparseVector globalVectorSum = null;
  private double globalSumOfSquaredLengths = 0.0;
  private int numberOfDocuments = 0;
  private int numNonZeroEntries = 0;
  private PriorityQueue<DocumentTimeStruct> pq = null;
  private int[] termReductionList = null;
  private java.util.Random myRand = null;
  
  private boolean blockNewDocumentsAndTermReduction = false;
  
  public BirchClusterOptions clusterOptions = null;
  
  /** Creates a new instance of BirchKmeans */
  public BirchKmeans() {
    super();
    
    this.globalVectorSum = new SparseVector();
    this.globalSumOfSquaredLengths = 0.0;
    this.numberOfDocuments = 0;
    this.documentNames = new Hashtable<String, Integer>();
    this.pq = new PriorityQueue<DocumentTimeStruct>();
    this.termReductionList = null;
    
    this.myRand = new java.util.Random();
    this.myRand.setSeed(new java.util.Date().getTime());
  }
  
  public BirchClusterOptions getClusterOptions() {
    return this.clusterOptions;
  }
  
  public void setClusterOptions(BirchClusterOptions bc) {
    this.clusterOptions = bc;
  }
  
  public int buildGlobalDictionaryFromSerializedRawDocuments(
      String serializedFilesPath, String outputDirectory) {
    
    if (this.blockNewDocumentsAndTermReduction == true) {
      return -1;
    }
    
    File f = new File(serializedFilesPath);
    File[] files = f.listFiles();
    
    int numDocumentsAdded = 0;
    
    // If the location we want to store new files doesn't exist, create it
    File output = new File(outputDirectory);
    if (output.exists() == false) {
      output.mkdirs();
    }
    
    for (int i = 0; i < files.length; ++i) {
      // If we reach a directory, recursively descend through it.
      if (files[i].isDirectory()) {
        numDocumentsAdded +=
            this.buildGlobalDictionaryFromSerializedRawDocuments(
            files[i].getAbsolutePath(), outputDirectory + File.separator +
            files[i].getName());
        continue; // nothing more to do on this iteration
      }
      
      if (files[i].isFile() &&
          files[i].getAbsolutePath().endsWith(".bp1") == true) {
        try {
          Document doc = Document.deserializeDocument(
              files[i].getAbsolutePath());
          
          System.out.println("Phase 1 Incorporated document: " +
              files[i].getAbsolutePath());
          
          this.incorporateDocumentStatisticsIntoModel(doc);
          
          String p2FileName = outputDirectory + File.separator +
              files[i].getName();
          p2FileName = p2FileName.replaceAll("bp1$", "bp2");
          Document.serializeDocument(doc, p2FileName);
          
          ++numDocumentsAdded;
        } catch (Exception ex) {
          ex.printStackTrace();
          System.out.println("First half of incorporating document!");
          System.exit(-1);
        }
      }
    }
    return numDocumentsAdded;
  }
  
  /**
   * This method takes the serialized files created during Phase 1, and uses the
   * complete global dictionary to generate normalized frequency vectors for
   * each document.  This step also builds a priority queue which is keyed on
   * the timestamp of each document. The order in which these documents appear
   * in the priority queue, will be the order in which they are analyzed during
   * actual clustering.
   */
  public int useGlobalDictionaryAndBuildNormalizedVectors(
      String serializedFilesPath) {
    
    // Clustering options must be configured before this point!
    if (this.clusterOptions == null) {
      return -1;
    }
    
    if (this.clusterOptions.getTermReductionApproach() == 
        TermReductionApproach.USE_TERM_REDUCTION) {
      this.useNHighVarianceTerms(this.clusterOptions.getMaxTermLimit());
    }
    
    // No new documents can be loaded and term reduction settings are locked
    this.blockNewDocumentsAndTermReduction = true;
    
    File f = new File(serializedFilesPath);
    File[] files = f.listFiles();
    
    int numDocumentsAdded = 0;
    
    for (int i = 0; i < files.length; ++i) {
      // If we reach a directory, recursively descend through it.
      if (files[i].isDirectory()) {
        numDocumentsAdded += this.useGlobalDictionaryAndBuildNormalizedVectors(
            files[i].getAbsolutePath());
        continue; // nothing more to do on this iteration
      }
      
      if (files[i].isFile() &&
          files[i].getAbsolutePath().endsWith(".bp2") == true) {
        File d = files[i];
        System.out.println("Phase 2 Incorporated and loaded: " +
            d.getAbsolutePath());
        
        try {
          Document doc = Document.deserializeDocument(d.getAbsolutePath());
          
          // Build queue of messages sorted by time!
          pq.add(new DocumentTimeStruct(files[i].getAbsolutePath(),
              doc.getTimestamp()));
          
          SparseVector sv = this.getNormalizedDocumentVector(doc);
          
          this.globalVectorSum.add(sv);
          this.globalSumOfSquaredLengths += sv.lengthSquared();
          this.numNonZeroEntries += this.globalVectorSum.getPopCount();
          
          ++numDocumentsAdded;
          
        } catch (Exception ex) {
          ex.printStackTrace();
          System.out.println("Second half of incorporating document!");
          System.exit(-1);
        }
      }
    }
    
    System.out.println("Number of documents = " +
        this.getNumberOfDocuments());
    System.out.println("Number of unique terms = " +
        this.getNumberOfDistinctTerms());
    System.out.println("Number of total terms = " +
        this.getNumberOfTerms());
    
    return numDocumentsAdded;
  }
  
  /**
   * Since BirchKmeans may use a reduced term list, we need a bit of logic to
   * ensure that when documentVectors are needed, the correct one is obtained.
   * This method determines if we are in fact using a reduced term list, and if
   * so, returns the appropriate document vector.
   */
  private SparseVector getNormalizedDocumentVector(Document doc) {
    if (this.termReductionList != null) {
      doc.setTermList(this.termReductionList);
    }
    return doc.getNormalizedVector(this);
  }
  
  /**
   * Returns the number of non-zero coordinates across all parsed documents.
   */
  public int getNumberOfNonZeroEntries() {
    return this.numNonZeroEntries;
  }
  
  /**
   * This method updates model statistics using values from the document
   * provided. Calling it more than once should have no effect since
   * doc.getFilename() is stored and if it exists, this function aborts.
   */
  private boolean incorporateDocumentStatisticsIntoModel(Document doc) {
    doc.setModel(this);
    
    if (isDocumentIncorporated(doc)) {
      return false;
    }
    
    this.documentNames.put(doc.getFilename(), new Integer(1));
    
    SparseVector documentFrequencyVector = doc.getFrequencyVector();
    int[] idx = documentFrequencyVector.getIndicies();
    
    for (int i = 0; i < idx.length; ++i) {
      int j = idx[i];
      this.incrementNumDocumentsContainingTerm(j);
      this.incrementGlobalTermOccurence(j,
          (int) documentFrequencyVector.get(j));
    }
    
    ++this.numberOfDocuments;
    return true;
  }
  
  /***
   * This function returns true if the provided document was included when
   * calculateBirchDataFromSerializedFiles was called.  It returns false
   * otherwise.
   */
  private boolean isDocumentIncorporated(Document doc) {
    if (this.documentNames.get(doc.getFilename()) != null) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Calculating the upper quality bound using the cluster density factor is
   * extremely resource intensive. In an effort to deal with this,
   * clusterDocuments() is a light wrapper around the function
   * clusterDocumentsInternal(). 
   */
  public int clusterDocuments() {
    if (this.clusterOptions == null) {
      return -1;
    }
      
    double upperQualityBound = this.clusterOptions.getCapacityFraction() *
        (this.getGlobalQuality() / this.getNumberOfDocuments());
    
    System.out.println("Global quality = " + this.getGlobalQuality() );
    System.out.println("num docs = " + this.numberOfDocuments);
    System.out.println("upper = " + upperQualityBound);
    
    if (this.clusterOptions.getClusteringOrder() == ClusteringOrder.RANDOM) {
      // reseed in case we are in a serialized object.
      this.myRand.setSeed((new java.util.Date()).getTime());
      ArrayList<DocumentTimeStruct> al = new ArrayList<DocumentTimeStruct>(pq);
      System.out.println("Al size " + al.size());
      
      while (al.size() > 0) {
        int randIndex = this.myRand.nextInt(al.size());
        Document doc = null;
        try {
          doc = Document.deserializeDocument(al.get(randIndex).getFilename());
          al.remove(randIndex); // remove this document from the arraylist.
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        System.out.println("Clustering document: " + doc.getFilename() );
        System.out.println("Timestamp: " + doc.getTimestamp() );
        this.clusterDocument(doc);
      }
      return this.clusters.size();
    } else if (this.clusterOptions.getClusteringOrder() ==
        ClusteringOrder.TIMESTAMP_FORWARD) {
      while (pq.size() > 0) {
        DocumentTimeStruct dts = pq.poll();
        Document doc = null;
        
        try {
          doc = Document.deserializeDocument(dts.getFilename());
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        
        System.out.println("Clustering document: " + doc.getFilename());
        System.out.println("Timestamp: " + doc.getTimestamp());
        this.clusterDocument(doc);
      }
      return this.clusters.size();
    } else if (this.clusterOptions.getClusteringOrder() ==
        ClusteringOrder.TIMESTAMP_REVERSE) {
      ArrayList<DocumentTimeStruct> al = new ArrayList<DocumentTimeStruct>();
      
      // Convert the priority queue into a sorted array list.
      while (pq.peek() != null) {
        al.add(pq.poll());
      }
      
      // Since these are sorted in natural temporal ordering, we now reverse
      // the entire array in place.
      for (int i = 0; i < Math.floor(al.size() / 2); ++i) {
        int farIdx = al.size() - (i + 1);
        DocumentTimeStruct tmp = al.get(farIdx);
        al.set(farIdx, al.get(i));
        al.set(i, tmp);
      }
      
      Iterator<DocumentTimeStruct> itr = al.iterator();
      
      while (itr.hasNext()) {
        DocumentTimeStruct dts = itr.next();
        Document doc = null;
        try {
          doc = Document.deserializeDocument(dts.getFilename());
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        System.out.println("Clustering document: " + doc.getFilename() );
        System.out.println("Timestamp: " + doc.getTimestamp() );
        this.clusterDocument(doc);
      }
      return this.clusters.size();
    }
    
    // Unreachable in theory . =]
    return -1;
  }
  
  /* Not thread safe */
  private int clusterDocument(Document doc) {
    int bestClusterIdx = -1;
    double qualityDeltaForBestCluster = Double.MAX_VALUE;
    double qualityForBestCluster = Double.MAX_VALUE;
    int maxClusterSize = this.clusterOptions.getMaxClusterSize();
    double upperQualityBound = this.clusterOptions.getCapacityFraction() *
        (this.getGlobalQuality() / this.getNumberOfDocuments());
    
    System.out.println("Pop Count of Doc Vec = " +
        this.getNormalizedDocumentVector(doc).getPopCount());
    
    switch (this.clusterOptions.getClusteringApproach()) {
      case BEST_FIT_ALLOCATION:
        for (int i = 0; i < this.clusters.size(); ++i) {
          BirchCluster c = this.clusters.get(i);
          if ((c.getNumberOfDocuments() + 1) > maxClusterSize) {
            continue;
          }
          
          double proposedChangeInQuality = c.calculateChangeInQuality(
              this.getNormalizedDocumentVector(doc));
          double currentQuality = c.getQuality();
          double newQuality = proposedChangeInQuality + currentQuality;
          
          if (proposedChangeInQuality < qualityDeltaForBestCluster &&
              newQuality < upperQualityBound * (c.getNumberOfDocuments() + 1)) {
            bestClusterIdx = i;
            qualityDeltaForBestCluster = proposedChangeInQuality;
            qualityForBestCluster = newQuality;
          }
        }
        break;
      case GREEDY_ALLOCATION:
        for (int i = 0; i < this.clusters.size(); ++i) {
          BirchCluster c = this.clusters.get(i);
          if ((c.getNumberOfDocuments() + 1) > maxClusterSize) {
            continue;
          }
          
          double proposedChangeInQuality = c.calculateChangeInQuality(
              this.getNormalizedDocumentVector(doc));
          double currentQuality = c.getQuality();
          double newQuality = proposedChangeInQuality + currentQuality;
          
          if (newQuality < upperQualityBound * (c.getNumberOfDocuments() + 1)) {
            bestClusterIdx = i;
            qualityDeltaForBestCluster = proposedChangeInQuality;
            qualityForBestCluster = newQuality;
            break; // we are greedy, first match wins
          }
        }        
        break;
      case REASONABLE_EFFORT_FORWARD:
        int clustersToCheck = (int)Math.ceil(
            this.clusterOptions.getReasonableEffortValue() * 
            this.clusters.size());
        
        for (int i = 0; i < this.clusters.size() && clustersToCheck > 0; ++i) {
          BirchCluster c = this.clusters.get(i);
          if ((c.getNumberOfDocuments() + 1) > maxClusterSize) {
            continue;
          }
          
          // For reasonable effort, we only really care about clusters that
          // aren't already full, so let's not count them.  By decrementing
          // clustersToCheck here, we only count those clusters that could
          // theoretically accept an additional document against our reasonable
          // effort condition.
          --clustersToCheck;
          
          double proposedChangeInQuality = c.calculateChangeInQuality(
              this.getNormalizedDocumentVector(doc));
          double currentQuality = c.getQuality();
          double newQuality = proposedChangeInQuality + currentQuality;
          
          if (proposedChangeInQuality < qualityDeltaForBestCluster &&
              newQuality < upperQualityBound * (c.getNumberOfDocuments() + 1)) {
            bestClusterIdx = i;
            qualityDeltaForBestCluster = proposedChangeInQuality;
            qualityForBestCluster = newQuality;
          }
        }
        break;
      case REASONABLE_EFFORT_BACKWARD:
        int clustersToCheckB = (int)Math.ceil(
            this.clusterOptions.getReasonableEffortValue() * 
            this.clusters.size());
        
        for (int i = this.clusters.size() - 1; i >= 0 && clustersToCheckB > 0; 
            --i) {
          BirchCluster c = this.clusters.get(i);
          if ((c.getNumberOfDocuments() + 1) > maxClusterSize) {
            continue;
          }
          
          // For reasonable effort, we only really care about clusters that
          // aren't already full, so let's not count them.  By decrementing
          // clustersToCheck here, we only count those clusters that could
          // theoretically accept an additional document against our reasonable
          // effort condition.
          --clustersToCheckB;
          
          double proposedChangeInQuality = c.calculateChangeInQuality(
              this.getNormalizedDocumentVector(doc));
          double currentQuality = c.getQuality();
          double newQuality = proposedChangeInQuality + currentQuality;
          
          if (proposedChangeInQuality < qualityDeltaForBestCluster &&
              newQuality < upperQualityBound * (c.getNumberOfDocuments() + 1)) {
            bestClusterIdx = i;
            qualityDeltaForBestCluster = proposedChangeInQuality;
            qualityForBestCluster = newQuality;
          }
        }
        break;
    }

    if (bestClusterIdx >= 0) {
      this.clusters.get(bestClusterIdx).addDocument(
          this, doc, qualityForBestCluster);
      System.out.println("Added document to EXISTING cluster #" +
          bestClusterIdx);
    } else { // this was a bad fit for all clusters
      BirchCluster bc = new BirchCluster();
      bc.addDocument(this, doc);
      // These next two lines should be atomic.
      this.clusters.add(bc);
      bestClusterIdx = this.clusters.size() - 1;
      System.out.println("Added document to NEW cluster #" + bestClusterIdx);
    }
    return bestClusterIdx;
  }
  
  public double getGlobalTermWeight(int termId) {
    double gtw = Math.log(this.numberOfDocuments /
        this.getNumberOfDocumentsContainingTerm(termId));
    return gtw / Math.log(2);
  }
  
  /**
   * Return number of documents in the collection
   */
  public int getNumberOfDocuments() {
    return this.numberOfDocuments;
  }
  
  public int getNumberOfClusters() {
    return this.clusters.size();
  }
  
  public double getGlobalQuality() {
    double globalQuality = this.globalSumOfSquaredLengths;
    SparseVector s = new SparseVector(this.globalVectorSum);
    s.scalarDivide(this.getNumberOfDocuments());
    globalQuality = globalQuality - (s.lengthSquared() *
        this.getNumberOfDocuments() );
    return globalQuality;
  }
  
  public static void serializeBirchKMeans(BirchKmeans bkm,
      String outputFileName) throws Exception {
    FileOutputStream fos = new FileOutputStream(outputFileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(bkm);
    oos.flush();
    oos.close();
  }
  
  public static BirchKmeans deserializeBirchKmeans(String filename)
  throws Exception, FileNotFoundException {
    FileInputStream fis = new FileInputStream(filename);
    ObjectInputStream ois = new ObjectInputStream(fis);
    BirchKmeans bkm = (BirchKmeans) ois.readObject();
    return bkm;
  }
  
  /**
   * This method will ensure that this BirchKmeans object/instance will only
   * process the top N terms with the highest variance. It is called
   * automatically based on the settings in this object's clusterOptions object.
   * Do not call it manually.
   */
  private void useNHighVarianceTerms(int n) {
    if (this.blockNewDocumentsAndTermReduction == true) {
      return;
    }
    
    int numTerms = this.getNumberOfDistinctTerms();
    if (numTerms < n) {
      System.out.println("Asked to reduce terms to " + n + " but" +
        " there are only " + numTerms + " total, aborting!");
      System.exit(-1);
    }
    
    this.termReductionList = new int[n];
    double smallestVarianceInQueue = Double.MAX_VALUE;
  
    PriorityQueue<TermVarianceStructure> pq =
        new PriorityQueue<TermVarianceStructure> (numTerms);
    
    for (int i = 0; i < numTerms; ++i) {
      TermVarianceStructure t = new TermVarianceStructure();
      t.termId = i;
      t.variance = this.getTermVariance(i);
      pq.add(t);
    }
    
    for (int i = 0; i < n; ++i) {
      TermVarianceStructure t = pq.poll();
      this.termReductionList[i] = t.termId;
    }
    return;
  }
  
  public double getGeneratedPartitionQuality() {
    double quality = 0.0;
    
    for (int i = 0; i < this.clusters.size(); ++i) {
      quality += this.clusters.get(i).getQuality();
    }
    return (quality);
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer("BirchKmeans Object Info\n");
  
    sb.append("# of documents = " + this.getNumberOfDocuments() + "\n");
    sb.append("# of distinct terms = " + this.getNumberOfDistinctTerms() +
        "\n");
    sb.append("# of total terms = " + this.getNumberOfTerms() + "\n");
    sb.append(this.clusterOptions);
    
    Iterator<BirchCluster> itr = this.clusters.iterator();
   
    double avgQuality = 0.0;
    double avgClusterSize = 0.0;
   
    while(itr.hasNext()){
      BirchCluster bc = itr.next();
      avgQuality += bc.getQuality();
      avgClusterSize += bc.getNumberOfDocuments();
      sb.append(bc.toString());
    }
    
    sb.append("\nAverage Cluster Quality = " + 
        avgQuality / this.clusters.size() + "\n");
    sb.append("Average # of documents per cluster = " + 
        avgClusterSize / this.clusters.size() + "\n");
    sb.append("Total number of clusters = " + this.clusters.size() + "\n");
    sb.append("Global Quality = " + this.getGlobalQuality() + "\n");
    
    return sb.toString();
  }
}

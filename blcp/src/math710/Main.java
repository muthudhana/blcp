/*
 * Main.java
 *
 * Created on January 31, 2006, 10:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package math710;
import java.io.*;
import java.util.*;
import sparsearray.*;

/**
 *
 * @author mike
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
        
    }
    
    public static void Project4(String[] args) {
        
        if(args.length < 2){
            System.out.println("Usage: ");
            System.out.println("java -Xms250m -Xmx750m -jar MATH710.jar stoplist.txt directory1 diectory2 ... ");
        }
        
        PDDP vsm = null;
        
        try {
            vsm = new PDDP(new Stemmer(), args[0]);
        } catch (Exception e){
            System.out.println(e);
            System.out.println("Unable to load stop list!");
            return;
        }
        
        long docStart = new Date().getTime();
        
        int sourceId = 1;
        
        for(int i = 1; i < args.length; ++i){
            
            File f = new File(args[i]);
            
            int singleFileSourceId = 0;
            
            if(f.exists() == false){
                continue;
            }
            
            if(f.isFile()){
                // Skip files, we specify in the usage statement we only want directories.
                // vsm.addDocument(args[i], singleFileSourceId);
            } else if (f.isDirectory()){
                vsm.addDirectory(args[i], sourceId++);
            }
        }
        
        long docStop = new Date().getTime();
        
        System.out.println("\nAll documents are loaded!...\nLoading and parsing took: " + (docStop - docStart) + " ms");
        
        System.out.println("Model currently has " + vsm.getNumberOfDistinctTerms() + " distinct terms!");
        System.out.println("Model currently has " + vsm.getNumberOfTerms() + " total terms!");
        System.out.println("Model currently has " + vsm.getNumberOfDocuments() + " documents!");
        
         vsm.useNHighVarianceTerms(1000);
        
        System.out.println("\nBegining Clustering Analysis\n");
        long start = new Date().getTime();
        
        System.out.println("Calculating PDDP Partitions...");
        Cluster[] c = vsm.cluster(sourceId - 1);
        
/*
        // randomly build clusters...
        // To enable, uncomment this big block of code, and then comment out
        // the line immediately preceding it.
 
        Cluster[] c = new Cluster[3];
        int numDocs = vsm.getNumberOfDocuments();
 
        java.util.Random myRand = new java.util.Random();
        myRand.setSeed((new java.util.Date()).getTime());
 
        c[0] = new Cluster(vsm);
        c[1] = new Cluster(vsm);
        c[2] = new Cluster(vsm);
 
        for(int i = 0; i < numDocs; ++i){
            int cluster = myRand.nextInt(3);
            c[cluster].addDocument(vsm.getDocument(i));
        }
 
 */
        System.out.println("");
        for(int i = 0; i < 3; ++i){
            System.out.println("Initial Partion #" + i + " has " + c[i].getNumberOfDocuments() + " documents.");
        }
        
        System.out.println("\nUsing the initial partition as input to Batch K-Means...");
        System.out.println("Running Batch K-Means...");
        
        Kmeans km = new Kmeans(vsm, c);
        km.cluster(35);
        
        long stop = new Date().getTime();
        System.out.println("Total time for complete analysis is: " + (stop - start) + " ms");
    }



/* IGNORE EVERYTHING BELOW THIS LINE PLEASE... ITS JUST TESTING CODE! */

//    public static void Wmain(String[] args){
//        ColumnCompressedVector cv = new ColumnCompressedVector();
//        ColumnCompressedVector cx = new ColumnCompressedVector();
//        cv.set(0, 2D);
//        cv.set(1, -5D);
//        cv.set(2, -1D);
//
//        cx.set(0, 3D);
//        cx.set(1, 2D);
//        cx.set(2, -3D);
//
//        System.out.println(cv);
//        cv.normalize();
//        System.out.println(cv);
//
//    }
//
//    public static void RRmain(String[] args){
//        SparseMatrix sm = new SparseMatrix();
//        SparseVector sv = new SparseVector();
//
//        sm.addNColumns(2);
//
//        try{
//
//            sm.set(0, 0, 2D);  sm.set(0, 1, 3D);// sm.set(0, 2, 6D);
//            sm.set(1, 0, 1D);  sm.set(1, 1, 5D);// sm.set(1, 2, 6D);
//            // sm.set(2, 0, 2D);  sm.set(2, 1, -1D); sm.set(2, 2, 8D);
//
//            System.out.println("Eigenvector notes method: " + sm.getDominantEigenvector());
//
//        } catch (Exception e){
//            System.out.println(e);
//        }
//
//        // System.out.println(sv);
//    }
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        PDDP vsm = null;
//
//        try {
//            vsm = new PDDP(new Stemmer(), "f:\\My Documents\\Java Projects\\MATH710\\stopList.txt");
//        } catch (Exception e){
//            System.out.println(e);
//            System.out.println("Unable to load stop list!");
//            return;
//        }
//
//        long docStart = new Date().getTime();
//
//        int sourceId = 1;
//
//        for(int i = 1; i < args.length; ++i){
//
//            File f = new File(args[i]);
//
//            int singleFileSourceId = 0;
//
//            if(f.exists() == false){
//                continue;
//            }
//
//            if(f.isFile()){
//                vsm.addDocument(args[i], singleFileSourceId);
//            } else if (f.isDirectory()){
//                vsm.addDirectory(args[i], sourceId++);
//            }
//        }
//
//        long docStop = new Date().getTime();
//
//        System.out.println("All documents are loaded!...\nLoading and parsing took: " + (docStop - docStart) + " ms");
//
//        System.out.println("Vector Space Model currently has " + vsm.getNumberOfDistinctTerms() + " distinct terms!");
//        System.out.println("Vector Space Model currently has " + vsm.getNumberOfTerms() + " total terms!");
//        System.out.println("Vector Space Model currently has " + vsm.getNumberOfDocuments() + " documents!");
//
//        System.out.println("\nCalculating weighted vectors...");
//
//        int numNonZero = vsm.getNumberOfNonZeroEntries();
//
//        System.out.println("Calculations complete!!!\n");
//
//        System.out.println("Vector Space Model currently has " + numNonZero + " non-zero entries!");
//        System.out.println("Vector Space Model currently has " + vsm.getNumberOfTotalCoordinates() + " total coordinates!");
//        System.out.println("Sparsity of the data is " + 100.0D * (double)numNonZero/vsm.getNumberOfTotalCoordinates() + "%");
//
//        System.out.println("Begining Clustering Analysis");
//        long start = new Date().getTime();
//
//        //Cluster[] c = vsm.cluster(3);
//        // randomly build clusters...
//
//        Cluster[] c = new Cluster[3];
//        int numDocs = vsm.getNumberOfDocuments();
//
//        java.util.Random myRand = new java.util.Random();
//        myRand.setSeed((new java.util.Date()).getTime());
//
//        c[0] = new Cluster(vsm);
//        c[1] = new Cluster(vsm);
//        c[2] = new Cluster(vsm);
//
//        for(int i = 0; i < numDocs; ++i){
//            int cluster = myRand.nextInt(3);
//            c[cluster].addDocument(vsm.getDocument(i));
//        }
//
//        for(int i = 0; i < 3; ++i){
//            System.out.println("Cluster " + i + " has " + c[i].getNumberOfDocuments() + " documents.");
//        }
//
//        Kmeans km = new Kmeans(vsm, c);
//        km.cluster(200);
//
//        long stop = new Date().getTime();
//        System.out.println("Total time for clustering analysis is: " + (stop - start) + " ms.");
//
//    }
//
//    public static void FFFGDFGFmain(String[] args){
//        BirchKmeans bkm = null;
//        Document doc = null;
//        try {
//            bkm = BirchKmeans.deserializeBirchKmeans("c:\\SerializedOut\\BKM.dat");
//            doc = Document.deserializeDocument("c:\\SerializedOut\\10001.p1dat");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return;
//        }
//
//        System.out.println("# of documents: " + bkm.getNumberOfDocuments());
//        System.out.println("# of non-zero coordinates: " + bkm.getNumberOfNonZeroEntries());
//        System.out.println("# of total coordinates: " + bkm.getNumberOfDistinctTerms() * bkm.getNumberOfDocuments());
//    }
//
//    public static void RUNBIRCHmain(String[] args){
//        BirchKmeans bkm = null;
//
//        System.out.println("Loading birch k-means");
//        try {
//            bkm = BirchKmeans.deserializeBirchKmeans("c:\\SerializedOut\\BKM.dat");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        System.out.println("BirchKmeans loaded...");
//        System.out.println("Preparing to cluster documents...");
//
//        bkm.clusterDocuments("c:\\SerializedOut\\badeer-r\\", 100, 0.8);
//
//    }
//
////    public static void main(String[] args){
////        Document doc = null;
////        try {
////            doc = new Document("f:\\My Documents\\Java Projects\\MATH710\\enron\\maildir\\allen-p\\_sent_mail\\1", new StopList(new Stemmer(), "f:\\My Documents\\Java Projects\\MATH710\\stopList.txt"), new Stemmer(), 1);
////        } catch (Exception ex) {
////            ex.printStackTrace();
////        }
////        System.out.println("Document has " + doc.getNumberOfTerms() + " terms.");
////    }
//
    /** previous run command: java -Xms250m -Xmx750m -jar MATH710.jar "f:\My Documents\Java Projects\MATH710\stopList.txt" "f:\My Documents\Java Projects\MATH710\enron\maildir" */
    public static void INITBIRCHmain(String[] args){

        BirchKmeans bkm = null;

        System.out.println("Loading...");

        System.out.println("Creating new BirchKmeans object...");
        try {
            bkm = new BirchKmeans(new Stemmer(), "f:\\My Documents\\Java Projects\\MATH710\\stopList.txt");
        } catch (Exception e){
            System.out.println(e);
            System.out.println("Unable to load stop list!");
            return;
        }
        System.out.println("Object loaded...");


        String[] dirs = new String[1];
        dirs[0] = "c:\\EnronSample\\";
        int numAdded = 0;
           
        bkm.createSerializedDocuments("f:\\My Documents\\Java Projects\\MATH710\\enron\\maildir\\", 1, dirs[0], 1);
            
        numAdded = bkm.incorporateSerializedDocumentsPhase1(dirs[0]);
        System.out.println("PHASE 1 INCORPORATED " + numAdded + " documents!");
            
        try {
            System.out.println("Saving BKM after phase 1 but BEFORE term reduction!");
            BirchKmeans.serializeBirchKMeans(bkm, dirs[0] + File.separatorChar + "BKM.dat.pre-phase2");
            System.out.println("BKM Saved successfully...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
            
//        try {
//            bkm = BirchKmeans.deserializeBirchKmeans(dirs[0] + File.separatorChar + "BKM.dat.pre-phase2");
//            System.out.println("Deserialization of BKM pre-phase2 complete!");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            System.exit(-1);
//        }
//        
        bkm.useNHighVarianceTerms(1500);

        numAdded = bkm.incorporateSerializedDocumentsPhase2(dirs[0]);
        System.out.println("PHASE 2 INCORPORATED " + numAdded + " documents!");

        try {
            BirchKmeans.serializeBirchKMeans(bkm, dirs[0] + File.separatorChar + "BKM.dat");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
//
//    // use serialized data and cluster... FINALLY!
     public static void main(String[] args){

        BirchKmeans bkm = null;
        String dirs = "c:\\EnronSample\\";
        
        int maxClusterSize = 250;
        double capacityFraction = 0.8;
        
        System.out.println("Loading...");
        try {
            bkm = BirchKmeans.deserializeBirchKmeans(dirs + File.separatorChar + "BKM.dat");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Deserialization of BirchKmeans object complete...");
        System.out.println("Initiating Clustering Algorithm at " + new Date().getTime());
        System.out.println("Max Cluster Size: " + maxClusterSize);
        System.out.println("Capcity Fraction: " + capacityFraction);
        System.out.println("I have generated " + bkm.clusterDocuments(maxClusterSize, capacityFraction) + " clusters!");
        System.out.println("Partition Quality: " + bkm.getGeneratedPartitionQuality());
        System.out.println("Completed at " + new Date().getTime());

    }
//
//      public static void NOGOmain(String[] args) {
//        PDDP vsm = null;
//        String outputDirectory = "C:\\SerializedOut\\";
//
//        try {
//            vsm = new PDDP(new Stemmer(), args[0]);
//        } catch (Exception e){
//            System.out.println(e);
//            System.out.println("Unable to load stop list!");
//            return;
//        }
//
//        long docStart = new Date().getTime();
//
//        int sourceId = 1;
//
//        for(int i = 1; i < args.length; ++i){
//
//            File f = new File(args[i]);
//
//            int singleFileSourceId = 0;
//
//            if(f.exists() == false){
//                continue;
//            }
//
//            if(f.isFile()){
//                vsm.addDocument(args[i], singleFileSourceId);
//            } else if (f.isDirectory()){
//                // vsm.addDirectoryAndSerialize(args[i], sourceId++, outputDirectory, 1);
//            }
//        }
//    }
//
//       public static void Rmain(String[] args) {
//        VectorSpaceModel vsm = null;
//
//        if(args.length < 2){
//            System.out.println("Usage: ");
//            System.out.println("java -jar MATH710.jar stoplist.txt document1 document2 ... ");
//            System.out.println("Or alternately: ");
//            System.out.println("java -jar MATH710.jar stoplist.txt directory1 directory2 ... ");
//            System.out.println("Or any combination of the first two: ");
//            System.out.println("java -jar MATH710.jar stoplist.txt document1 directory2 ... ");
//            System.out.println("");
//            System.out.println("The only rule is the first argument must be the stop list.");
//            System.out.println("Subsequent entries can be files or directories for the corpus.");
//            System.out.println("Currently all weighting is FREQ, IDFB, COSN.");
//            System.out.println("");
//            return;
//        }
//
//        try {
//            vsm = new VectorSpaceModel(new Stemmer(), args[0]);
//        } catch (Exception e){
//            System.out.println(e);
//            System.out.println("Unable to load stop list!");
//            return;
//        }
//
//        long docStart = new Date().getTime();
//
//        for(int i = 0; i < args.length; ++i){
//
//            File f = new File(args[i]);
//
//            if(f.exists() == false){
//                continue;
//            }
//
//            if(f.isFile()){
//                vsm.addDocument(args[i]);
//            } else if (f.isDirectory()){
//                vsm.addDirectory(args[i]);
//            }
//        }
//
//        long docStop = new Date().getTime();
//
//        System.out.println("All documents are loaded!...\nLoading and parsing took: " + (docStop - docStart) + " ms");
//
//        System.out.println("Vector Space Model currently has " + vsm.getNumberOfDistinctTerms() + " distinct terms!");
//        System.out.println("Vector Space Model currently has " + vsm.getNumberOfTerms() + " total terms!");
//        System.out.println("Vector Space Model currently has " + vsm.getNumberOfDocuments() + " documents!");
//
//        System.out.println("\nCalculating weighted vectors...");
//
//        int numNonZero = vsm.getNumberOfNonZeroEntries();
//
//        System.out.println("Calculations complete!!!\n");
//
//        System.out.println("Vector Space Model currently has " + numNonZero + " non-zero entries!");
//        System.out.println("Vector Space Model currently has " + vsm.getNumberOfTotalCoordinates() + " total coordinates!");
//        System.out.println("Sparsity of the data is " + 100.0D * (double)numNonZero/vsm.getNumberOfTotalCoordinates() + "%");
//
//        System.out.println("Begining doTest");
//        long start = new Date().getTime();
//        vsm.doTest();
//        long stop = new Date().getTime();
//        System.out.println("Total time for doTest is: " + (stop - start));
//
//    }
}

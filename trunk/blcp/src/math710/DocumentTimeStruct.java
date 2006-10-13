/*
 * DocumentTimeStruct.java
 *
 * Created on May 2, 2006, 5:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package math710;

import java.io.*;

/**
 *
 * @author mike
 */
public class DocumentTimeStruct implements Comparable, Serializable {
    
    private String filename = null;
    private long timestamp = 0;
    
    /** Creates a new instance of DocumentTimeStruct */
    public DocumentTimeStruct(String fname, long ts) {
        this.filename = new String(fname);
        this.timestamp = ts;
    }
    
    public DocumentTimeStruct(Document doc){
        this.filename = doc.getFilename();
        this.timestamp = doc.getTimestamp();
    }
    
    public long getTimestamp(){
        return(this.timestamp);
    }
    
    public String getFilename(){
        return(this.filename);
    }
    
    public int compareTo(Object obj){
        DocumentTimeStruct t = (DocumentTimeStruct)obj;
        if(this.timestamp < t.timestamp){
            return(-1);
        }
        if(this.timestamp == t.timestamp){
            return(0);
        }
        // t.timestamp > this.timestamp.
        return(1);
    }
}

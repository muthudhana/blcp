package clusterbase;

import java.io.*;

public class DocumentTimeStruct implements Comparable, Serializable {
  private String filename = null;
  private long timestamp = 0;

  /** Creates a new instance of DocumentTimeStruct */
  public DocumentTimeStruct(String fname, long ts) {
    this.filename = new String(fname);
    this.timestamp = ts;
  }

  public DocumentTimeStruct(Document doc) {
    this.filename = doc.getFilename();
    this.timestamp = doc.getTimestamp();
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public String getFilename() {
    return this.filename;
  }

  public int compareTo(Object obj) {
    DocumentTimeStruct t = (DocumentTimeStruct) obj;
    if (this.timestamp < t.getTimestamp()) {
      return -1;
    }
    if (this.timestamp == t.getTimestamp()) {
      return 0;
    }
    // t.timestamp > this.timestamp.
    return 1;
  }
}

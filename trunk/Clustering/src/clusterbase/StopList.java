/*
 * StopList.java
 *
 * Created on February 13, 2006, 1:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package clusterbase;

import java.util.*; // need Hashtable
import java.io.*;

/**
 * Heavily based on Document.java however its much simpler.  Simply
 * loads a stop list file and provides means to see if a term appears
 * on the list.  It should be stemmed and tokenized in the same meanner
 * as a Document.
 *
 * @author mike
 */
public class StopList implements Serializable {

  private IStemmer stemmer = null;
  private String filename = null;
  private Hashtable<String, Integer> terms = new Hashtable<String, Integer>();

  /** Creates a new instance of Document */
  public StopList (IStemmer stemmer, String document) throws Exception {

    Tokenizer tok = new Tokenizer (stemmer, document); // Tokenize using porter
    this.stemmer = stemmer;
    this.filename = new String (document);

    String s = null;

    while ( (s = tok.nextToken() ).length() > 0) {
      terms.put (s, 1);
    }
  }

  public boolean containsTerm (String t) {
    t = this.stemmer.stem (t);
    return (this.terms.containsKey (t) );
  }
}

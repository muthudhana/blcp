/*
 * SparseVectorDimensionMismatch.java
 *
 * Created on March 5, 2006, 2:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sparsearray;

/**
 *
 * @author mike
 */
public class SparseVectorDimensionMismatch extends Exception {

  String message = null;

  /** Creates a new instance of SparseVectorDimensionMismatch */
  public SparseVectorDimensionMismatch() {}

  public SparseVectorDimensionMismatch (String message) {
    this.message = message;
  }

  public String toString() {
    if (message != null) {
      return (message);
    } else {
      return ("SparseVectorDimensionMismatch");
    }
  }
}

/*
 * ITokenizer.java
 *
 * Created on February 13, 2006, 3:32 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package math710;

/**
 *
 * @author mike
 */
public interface ITokenizer {

  String nextToken() throws Exception;
  String nextTokenUnstemmed() throws Exception;

}

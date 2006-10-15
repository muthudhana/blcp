/*
 * Tokenizer.java
 *
 * Created on January 31, 2006, 10:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package clusterbase;

import java.io.*;

/**
 *
 * @author mike
 */
public class Tokenizer implements ITokenizer {

  public final static int MIN_TOKEN_LENGTH = 4;
  private File file = null;
  private StringBuilder nextToken = null;
  private FileReader fr = null;
  private IStemmer stemmer = null;

  /**
   * Tokenize the file specified by the parameter.
   */
  public Tokenizer (IStemmer stemmer, String filename) {
    this.file = new File (filename);
    this.nextToken = new StringBuilder();
    this.stemmer = stemmer;
  }

  public String nextToken() throws Exception {
    return (this.stemmer.stem (this.nextTokenUnstemmed() ) );
  }

  public String nextTokenUnstemmed() throws Exception {
    if (this.fr == null) {
      // We must open the the file if possible!
      this.fr = new FileReader (this.file);
    }

    // Trim the nextToken buffer to be empty.
    this.nextToken.delete (0, this.nextToken.length() );

    int c = '\0';

    while (true) { // This is necessary because lame ass sun's jvm
      // doesn't know how to optimize tail recursion.
      // Continue reading until end of file!
      while ( (c = this.fr.read() ) > -1) {
        char t = (char) c;

        // We build an FSM to parse this file!

        /*
         * If t contains whitespace and we have no
         * characters in our buffer. Just keep eating
         * up the space!
         */
        if (Character.isWhitespace (t) && this.nextToken.length() == 0) {
          continue;
        }

        /*
         * If t contains whitespace and we have characters
         * in our buffer, we are finished reading the current
         * token. Break out of our FSM and see if it is valid.
         */
        if (Character.isWhitespace (t) && this.nextToken.length() > 0) {
          break;
        }

        if (Character.isLetter (t) ) {
          t = Character.toLowerCase (t);
          this.nextToken.append (t);
          continue;
        }

        if (t == '-') {
          this.nextToken.append ('-');
          continue;
        }

        /* If we have gotten this far we have a character that is not
         * in our allowed set of "A-Z a-z -" so we make a choice. If
         * we have data in our nextToken buffer, then we stop and
         * perform tests on it.  If however we have nothing in our
         * buffer, we can keep going until we have a token.
         */
        if (this.nextToken.length() == 0) {
          continue;
        }

        if (this.nextToken.length() > 0) {
          break;
        }
      }

      if (this.nextToken.length() >= Tokenizer.MIN_TOKEN_LENGTH) {
        // If we have a buffer of length > Tokenizer.MIN_TOKEN_LENGTH we have valid token!
        return (this.nextToken.toString() );
      } else if (this.nextToken.length() > 0 && this.nextToken.length() < Tokenizer.MIN_TOKEN_LENGTH) {
        // If we have a buffer of length >0 but < 2 we have an invalid
        // token. Ideally this would be a tail recursive call to this
        // function, but Sun's lame JVM doesn't support tail recursion
        // optimization.  Since it doesn't, this would overlfow the stack.
        // my solution is to wrap most of this function in a while(true)
        // loop.  Once a token is obtained, a return statement will break
        // out of it.  Since you are here, the token in nextToken is invalid.
        // simply clear out the token, and keep going!

        // Trim the nextToken buffer to be empty.
        this.nextToken.delete (0, this.nextToken.length() );

      } else {
        // We reached the end of file. Return an empty token.
        return (new String() );
      }
    }
  }
}

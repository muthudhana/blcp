/*
 * @(#)EmailTokenizer.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



package clusterbase;

import java.io.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * Class description
 *
 *
 * @version    Enter version here..., 04/01/07
 * @author     Mike Wiacek
 */
public class EmailTokenizer implements ITokenizer {
  public final static int MIN_TOKEN_LENGTH = 4;
  private File file = null;
  private StringBuilder nextToken = null;
  private FileReader fr = null;
  private IStemmer stemmer = null;
  private long messageTimestamp = 0;

  /**
   * Tokenize the file specified by the parameter.
   *
   * @param stemmer
   * @param filename
   */
  public EmailTokenizer (IStemmer stemmer,
                         String filename) {
    this.file = new File(filename);
    this.nextToken = new StringBuilder();
    this.stemmer = stemmer;
    this.fr = null;

    this.eatMessageHeaders();
  }

  /**
   * Advance the file cursor to the begining of message headers.
   */
  private void eatMessageHeaders () {
    if (this.fr == null) {
      try {

        // We must open the the file if possible!
        this.fr = new FileReader(this.file);
      } catch (FileNotFoundException ex) {
        ex.printStackTrace();
      }
    }

    for (int i = 0, length = 0; true; ++i) {
      int scanCode = this.nextChar();

      if (scanCode == '\n') {
        if (length == 0) {
          break;
        } else {
          length = 0;
        }
      } else if ((scanCode != '\r') && (scanCode != '\n')) {
        if ((length == 0) && (scanCode == 'D')) {
          ++length;

          scanCode = this.nextChar();

          if (scanCode == 'a') {
            ++length;

            scanCode = this.nextChar();

            if (scanCode == 't') {
              ++length;

              scanCode = this.nextChar();

              if (scanCode == 'e') {
                ++length;

                for (int j = 0; j < 2; ++j) {
                  this.nextChar();
                }

                this.parseTimestamp();
              }
            }
          }
        } else {
          ++length;
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private int nextChar () {
    try {
      return this.fr.read();
    } catch (IOException ex) {
      ex.printStackTrace();

      return 0;
    }
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws Exception
   */
  public String nextToken () throws Exception {
    return this.stemmer.stem(this.nextTokenUnstemmed());
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws Exception
   */
  public String nextTokenUnstemmed () throws Exception {
    if (this.fr == null) {

      // We must open the the file if possible!
      this.fr = new FileReader(this.file);
    }

    // Trim the nextToken buffer to be empty.
    this.nextToken.delete(0, this.nextToken.length());

    int c = '\0';

    while (true) {

      // This is necessary because sun's lame jvm
      // doesn't know how to optimize tail recursion.
      // Continue reading until end of file!
      while ((c = this.fr.read()) > -1) {
        char t = (char) c;

        // We build an FSM to parse this file!

        /*
         * If t contains whitespace and we have no
         * characters in our buffer. Just keep eating
         * up the space!
         */
        if (Character.isWhitespace(t) && (this.nextToken.length() == 0)) {
          continue;
        }

        /*
         * If t contains whitespace and we have characters
         * in our buffer, we are finished reading the current
         * token. Break out of our FSM and see if it is valid.
         */
        if (Character.isWhitespace(t) && (this.nextToken.length() > 0)) {
          break;
        }

        if (Character.isLetter(t)) {
          t = Character.toLowerCase(t);

          this.nextToken.append(t);

          continue;
        }

        if (t == '-') {
          this.nextToken.append('-');

          continue;
        }

        /*
         *  If we have gotten this far we have a character that is not
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
        return this.nextToken.toString();
      } else if ((this.nextToken.length() > 0) &&
                 (this.nextToken.length() < Tokenizer.MIN_TOKEN_LENGTH)) {

        // If we have a buffer of length >0 but < 2 we have an invalid
        // token. Ideally this would be a tail recursive call to this
        // function, but Sun's lame JVM doesn't support tail recursion
        // optimization.  Since it doesn't, this would overlfow the stack.
        // my solution is to wrap most of this function in a while(true)
        // loop.  Once a token is obtained, a return statement will break
        // out of it.  Since you are here, the token in nextToken is invalid.
        // simply clear out the token, and keep going!

        // Trim the nextToken buffer to be empty.
        this.nextToken.delete(0, this.nextToken.length());
      } else {

        // We reached the end of file. Return an empty token.
        return new String();
      }
    }
  }

  /**
   * Method description
   *
   */
  private void parseTimestamp () {
    if (this.messageTimestamp > 0) {    // First date field is authoritative
      return;
    }

    StringBuffer sb = new StringBuffer();
    int scanCode = 0;

    while ((scanCode = this.nextChar()) != ')') {
      sb.append((char) scanCode);
    }

    SimpleDateFormat format =
      new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss ZZZZZ (zzz");

    // Parse the date
    try {
      Date date = format.parse(sb.toString());

      this.messageTimestamp = date.getTime();

      System.out.println("Parsed date and timestamp    : " + date.toString() +
                         " = " + this.messageTimestamp);
    } catch (ParseException pe) {
      pe.printStackTrace();
      System.out.println("Filename is: " + this.file.getAbsolutePath());

      this.messageTimestamp = Long.MAX_VALUE;
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public long getTimestamp () {
    return this.messageTimestamp;
  }
}

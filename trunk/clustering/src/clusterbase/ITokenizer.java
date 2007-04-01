/*
 * @(#)ITokenizer.java   04/01/07
 * 
 * Copyright (c) 2007 Michael Wiacek, <mike@iroot.net>
 *
 * All rights reserved.
 *
 */



package clusterbase;

/**
 * Interface description
 *
 *
 * @version        Enter version here..., 04/01/07
 * @author         Mike Wiacek
 */
public interface ITokenizer {
  String nextToken () throws Exception;

  String nextTokenUnstemmed () throws Exception;
}

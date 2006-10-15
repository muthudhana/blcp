package clusterbase;

public interface ITokenizer {
  String nextToken() throws Exception;
  String nextTokenUnstemmed() throws Exception;
}

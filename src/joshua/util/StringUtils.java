package joshua.util;

public class StringUtils {

  public static SuffixMatchResult suffixMatch(String[] sent, int sentStartInclude, int sentEndExclusive, 
                                                String[] pattern, int patternStartInclude, int patternEndExclusive){
    if (sent == null || sent.length == 0 || pattern == null || pattern.length == 0 || 
        patternStartInclude == patternEndExclusive){
      return new SuffixMatchResult(-1, 0);
    }

    SuffixMatchResult result = new SuffixMatchResult();
    int startPos = -1, maxLen = 0;
    
    for (int i = sentEndExclusive - 1; i >=sentStartInclude; i--){
      int curLen = 0;
      for (int j  = patternEndExclusive - 1, pos = i; j >= patternStartInclude && pos >= sentStartInclude; j--, pos--){
        if (sent[pos].equals(pattern[j])){
          curLen ++;
          
          if ((j==sentStartInclude || pos==patternStartInclude) && curLen > maxLen){
            startPos = i;
            maxLen = curLen;
            break;
          }
          
          continue;
        }
        
        if (curLen > maxLen){
          startPos = i;
          maxLen = curLen;
        }
        break;
      }
    }
    
    result.startPos = startPos;
    result.len = maxLen;
    return result;
  }
  
}

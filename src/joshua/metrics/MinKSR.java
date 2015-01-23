package joshua.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import joshua.util.StringUtils;
import joshua.util.SuffixMatchResult;

import org.apache.commons.lang3.ArrayUtils;

public class MinKSR extends EvaluationMetric {
  private static final Logger logger = Logger.getLogger(BLEU.class.getName());
  
  // The maximum n-gram we care about
  protected int maxGramLength;
  
  protected int[][] refWordCount;
  protected String[][][] refWords;
  
  protected int separatorLength = 0;
  protected double minKSRatioNormal = 2;
  protected int KS4SelectIME = 1;
  protected int KS4SelectPrediction = 1;
   
  private Set<String> puncSet = new HashSet<>();
  private Set<String> alphaSet = new HashSet<>();
  private Set<String> numberSet = new HashSet<>();
  
  private String puncStr;
  private String alphaStr;
  private String numericStr;
  private String alphanumericStr;

  public MinKSR(){
    this(4);
  }
  
  public MinKSR(String[] MinKSR_options) {
    this(Integer.parseInt(MinKSR_options[0]));
  }
  
  public MinKSR(int mxGrmLn){
    if (mxGrmLn >= 1) {
      maxGramLength = mxGrmLn;
    } else {
      logger.severe("Maximum gram length must be positive");
      System.exit(1);
    }
    
    initialize();
  }
  
  public static List<String> toList(char[] array){
    if (array == null) return null;
    List<String> list = new ArrayList<>();
    for (char c : array){
      list.add(String.valueOf(c));
    }
    
    return list;
  }
  
  protected void initSymbol(){
    puncStr="~`!@#$%^&*()-=_+[]{}\\|;':\",./<>?/·！￥…（）—、【】：“”；‘’，《》。？";
    puncSet.addAll(toList(puncStr.toCharArray()));
    
    alphaStr="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    alphaSet.addAll(toList(alphaStr.toCharArray()));
    
    numericStr="0123456789";
    alphaSet.addAll(toList(numericStr.toCharArray()));
    
    alphanumericStr = alphaStr+numericStr;

  }
  
  @Override
  protected void initialize() {
    
    
    this.metricName = "MinKSR";
    toBeMinimized = false;
    
    //3: minKeys_normal keys_TINPE minKeys_TINPE 2:length(c) length(r)
    suffStatsCount = 3 + 2;
    
    set_refWordCount();
  }

  @Override
  public double bestPossibleScore() {
    return 1;
  }

  @Override
  public double worstPossibleScore() {
    return 0;
  }

  @Override
  public int[] suffStats(String cand_str, int i) {
    int[][] stats = new int[refsPerSen][suffStatsCount];
    
    if (!cand_str.equals("")) {
      String[] words = cand_str.split("\\s+");
      for (int r = 0; r < refsPerSen; r++){
        set_prec_suffStats(stats[r], words, i, r);
        stats[r][suffStatsCount - 2] = words.length;
        stats[r][suffStatsCount - 1] = refWordCount[i][0];
      }
    } else {
      String[] words = new String[0];
      for (int r = 0; r < refsPerSen; r++){
        set_prec_suffStats(stats[r], words, i, r);
        stats[r][suffStatsCount - 2] = 0;
        stats[r][suffStatsCount - 1] = refWordCount[i][0];
      }
    }
    
    int r_stats = 0;
    double r_max = 0;
    for (int r = 0; r < refsPerSen; r++){
      double r_max_curr = score(stats[r]);
      if (r_max_curr > r_max){
        r_max = r_max_curr;
        r_stats = r;
      }
    }
    
    return stats[r_stats];
  }

  @Override
  public double score(int[] stats) {
    if (stats.length != suffStatsCount) {
      logger.severe("Mismatch between stats.length and suffStatsCount (" + stats.length + " vs. "
          + suffStatsCount + ") in BLEU.score(int[])");
      System.exit(2);
    }

    double minKSR = 0.0;
    
    int minKeysNormal = stats[0];
    int keysTINPE = stats[1];
    int minKeysTINPE = stats[2];
    double c_len = stats[suffStatsCount - 2];
//    if ( Math.abs(c_len) < Math.pow(10, -10)){
    if ( c_len < 1){
      return 0;
    }
    
    double r_len = stats[suffStatsCount - 1];

    if (minKeysNormal != 0){ 
      minKSR = (minKeysNormal - keysTINPE) * 1.0 / (minKeysNormal - minKeysTINPE);
    }else{
      minKSR = 0;
    }
    
    double BP = 1.0;
    if (c_len > r_len)
      BP = Math.exp(1 - (c_len / r_len));
    // if c_len <= r_len, no penalty applies
        
    double score = BP * minKSR;
    return score;
  }

  @Override
  public void printDetailedScore_fromStats(int[] stats, boolean oneLiner) {
    double minKSR = 0.0;

    int minKeysNormal = stats[0];
    int keysTINPE = stats[1];
    int minKeysTINPE = stats[2];
    double c_len = stats[suffStatsCount - 2];
    double r_len = stats[suffStatsCount - 1];

    if (minKeysNormal != 0){ 
      minKSR = (minKeysNormal - keysTINPE) * 1.0 / (minKeysNormal - minKeysTINPE);
    }else{
      minKSR = 0;
    }
    
    double BP = 1.0;
    if (c_len > r_len)
      BP = Math.exp(1 - (c_len / r_len));
    // if c_len <= r_len, no penalty applies
    
    System.out.println("minKeysNormal = "+f4.format(minKeysNormal));
    System.out.println("keysTINPE = "+f4.format(keysTINPE));
    System.out.println("minKeysTINPE = "+f4.format(minKeysTINPE));
    
    if (oneLiner) {
      System.out.print("(overall=" + f4.format(Math.exp(minKSR)) + "), ");
    } else {
      System.out.println("MinKSR_Original = " + f4.format(minKSR));
      System.out.println("");
    }

    if (oneLiner) {
      System.out.print("BP=" + f4.format(BP) + ", ");
    } else {
      System.out.println("Length of candidate corpus = " + (int) c_len);
      System.out.println("Effective length of reference corpus = " + (int) r_len);
      System.out.println("MinKSR_BP = " + f4.format(BP));
      System.out.println("");
    }

    System.out.println("  => MinKSR = " + f4.format(BP * minKSR));
  }

  protected void set_refWordCount() {
    refWords = new String[numSentences][refsPerSen][];
    refWordCount = new int[numSentences][refsPerSen];
    
    String ref_str;
    for (int i = 0; i < numSentences; ++i) {
      for (int r = 0; r < refsPerSen; ++r) {
        ref_str = refSentences[i][r];
        if (!ref_str.equals("")) {
          refWords[i][r] = ref_str.split("\\s+");
          refWordCount[i][r]=refWords[i][r].length;
        } else {
          refWords[i][r] = new String[0];
          refWordCount[i][r] = 0;
        }
      }
    }
  }
  
  protected int wordCount(String cand_str) {
    if (!cand_str.equals("")) {
      return cand_str.split("\\s+").length;
    } else {
      return 0;
    }
  }
  
  protected void set_prec_suffStats(int[] stats, String[] words, int i, int r){
    if (words != null && words.length != 0){
      stats[0] = minKeysNormal(words, i, r);
      stats[1] = keysTINPE(words, i, r);
      stats[2] = minKeysTINPE(words, i, r);
    }else{
      stats[0] = minKeysNormal(words, i, r);
      stats[1] = stats[0];
      stats[2] = minKeysTINPE(words, i, r);
    }
    
    return;
  }
  
  protected int minKeysNormal(String[] words, int i, int r){
    int sum = 0;
    if (refWords[i][r] == null || refWords[i][r].length == 0) return sum;
    
    for (int w = 0; w < refWords[i][r].length; w++){
      sum += Math.max(Math.ceil(refWords[i][r][w].length() * minKSRatioNormal), 1);
      sum += separatorLength + KS4SelectIME;
    }
    sum -= separatorLength;
    return sum;
  }
  
  protected int keysTINPE(String[] words, int i, int r){
    int sum = 0;
    
    String[] refws = refWords[i][r];
    int refLen = refws.length;
    if (refws == null || refws.length == 0) return sum;
    
    int sentLen = words.length;
    int j = 0;
    for (int k = 0; k < maxGramLength && k < words.length && k < refLen; k++){
      if (!refws[k].equals(words[k])) break;
      
      j++;
    }
    if (j > 0) sum++;
    
    while (j < refLen){
      SuffixMatchResult matchResult = StringUtils.suffixMatch(words, 0, sentLen, refws, 0, j);
      if (matchResult.startPos >= 0){
        int matched = 0;
        for (int m = 1; m <= maxGramLength && j+m-1<refLen && matchResult.startPos+m<sentLen; m++){
          if (!refws[j+m-1].equals(words[matchResult.startPos+m])) break;
          
          matched++;
        }
        if (matched > 0){
          sum += KS4SelectPrediction;
          j += matched;
          continue;
        }
      }
      
      int index = ArrayUtils.indexOf(words, refws[j]);
      if (index == ArrayUtils.INDEX_NOT_FOUND){
        sum += refws[j].length()*minKSRatioNormal + KS4SelectIME;
        
        j++;
        continue;
      }
      
      int charNum = refws[j].length();
      int k = 1;
      for (; k <= maxGramLength && j+k < refLen; k++){
        if (StringUtils.suffixMatch(words, 0, sentLen, refws, 0, j+k).startPos != ArrayUtils.INDEX_NOT_FOUND)  break;
        
        index = ArrayUtils.indexOf(words, refws[j+k]);
        if (index == ArrayUtils.INDEX_NOT_FOUND) break;
        
        charNum += refws[j+k].length();
      }
      j += k;
      sum += charNum + KS4SelectIME;
    }
    
    return sum;
  }
  
  protected int keysTINPE_bak(String[] words, int i, int r){
    int sum = 0;
    
    String[] refws = refWords[i][r];
    if (refws == null || refws.length == 0) return sum;
    
    List<int[]> commonSubIndexList = commonSubIndexArray(refws, words);
    
    int[] flagList = new int [refws.length];
    for (int[] indexPair : commonSubIndexList){
      for (int ii=indexPair[0]; ii<indexPair[1]; ii++){
        if ((ii==indexPair[0] && indexPair[0] != 0)
              || (indexPair[0] == 0 && !refws[0].equals(words[0]))) flagList[ii] = 1;//anchor word
        else  flagList[ii] = 2;//n-gram prediction
      }
    }
    
    int curAnchor = -2;
    for (int ii = 0, len = refws.length; ii < len; ii++){
      if (flagList[ii] == 1){//anchor
        curAnchor = ii;
        sum += refws[ii].length() + KS4SelectIME;
      }else if (flagList[ii] == 2){//n-gram prediction
        if (ii == 0) curAnchor = -1;
        if (ii - curAnchor > 4){
          curAnchor = ii - 1;
        }
        
        if (ii-curAnchor == 1) sum += KS4SelectPrediction;
        
        continue;        
      }else{//normal
        curAnchor = -2;
        sum += refws[ii].length() * minKSRatioNormal + KS4SelectIME;
      }
      
      if (ii != len - 1) sum += separatorLength;
    }
    
    return sum;
  }
  
  protected int minKeysTINPE(String[] words, int i, int r){
    int sum = 0;
    if (refWords[i][r] == null || refWords[i][r].length == 0) return sum;
    
    int blocks = refWords[i][r].length/maxGramLength;
    
    if (refWords[i][r].length%maxGramLength == 0){
      sum += blocks * (KS4SelectPrediction + separatorLength) - separatorLength;
    }else{
      sum += blocks * (KS4SelectPrediction + separatorLength) + KS4SelectPrediction;
    }
    
    return sum;
  }
  
  /**
   * aArray在bArray中的共同子串下标
   * @param aArray
   * @param bArray
   * @return
   */
  public static <T> List<int[]> commonSubIndexArray(T[] aArray, T[] bArray){
    List<int[]> anchorIndexList = new ArrayList<>();
    if (ArrayUtils.isEmpty(aArray) || ArrayUtils.isEmpty(bArray)) return anchorIndexList;
    int i = 0, aLen = aArray.length, bLen = bArray.length;
    
    
    List<Integer> indexList = null;
    while (i < aLen){
       indexList = allIndexOf(aArray[i], bArray);
       if (indexList.isEmpty()){
         i++;
         continue;
       }
       
       int maxLen = 0;
       int[] anchorIndex = new int[2];
       for (int index : indexList){
         int j = i, k = index, currLen = 0;
         while (j < aLen && k < bLen){
           if (aArray[j].equals(bArray[k])){
             currLen ++;
           }else break;
           
           j++; k++;
         }
         
         if (currLen <= maxLen) continue;
         maxLen = currLen;
         
         anchorIndex[0] = i;
         anchorIndex[1] = j;
       }
       
       anchorIndexList.add(anchorIndex);
       i += maxLen;
    }
    
    return anchorIndexList;
  }

  public static <T> List<Integer> allIndexOf(T item, T[] array){
    List<Integer> resultList = new ArrayList<>();
    if (ArrayUtils.isEmpty(array)) return resultList;
    
    int i = 0, len = array.length;
    while (i < len){
      i = ArrayUtils.indexOf(array, item, i);
      if (i == -1) break;
      
      resultList.add(i);
      i++;
    }
    
    return resultList;
  }

}

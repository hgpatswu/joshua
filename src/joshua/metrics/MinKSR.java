package joshua.metrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

public class MinKSR extends EvaluationMetric {
  private static final Logger logger = Logger.getLogger(BLEU.class.getName());
  
  // The maximum n-gram we care about
  protected int maxGramLength;
  
  protected int[][] refWordCount;
  protected String[][][] refWords;
  
  protected int separatorLength = 0;
  protected double minKSRatioNormal = 3;
  protected int KS4SelectIME = 1;
  protected int KS4SelectPrediction = 1;
    
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
        stats[r][suffStatsCount - 2] = words.length;
//        stats[r][suffStatsCount - 1] = refWordCount[i][0];
        stats[r][suffStatsCount - 1]=0;
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
//    if (Math.abs(c_len) < Math.pow(10, -9)) return 0;
    if ( c_len < 1 ) return 0;
    if (c_len > r_len)
      BP = Math.exp(1 - (c_len / r_len));
    // if c_len < r_len, no penalty applies
        
    return BP * minKSR;
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
    if (c_len < r_len)
      BP = Math.exp(1 - (r_len / c_len));
    // if c_len > r_len, no penalty applies
    
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
      stats[0] = 0;
      stats[1] = 0;
      stats[2] = 0;
    }
    
    return;
  }
  
  protected int minKeysNormal(String[] words, int i, int r){
    int sum = 0;
    if (refWords[i][r] == null || refWords[i][r].length == 0) return sum;
    
    for (int w = 0; w < refWords[i][r].length; w++){
      sum += Math.max(Math.floor(refWords[i][r][w].length() * minKSRatioNormal), 1);
      sum += separatorLength + KS4SelectIME;
    }
    sum -= separatorLength;
    return sum;
  }
  
  protected int keysTINPE(String[] words, int i, int r){
    int sum = 0;
    
    String[] refws = refWords[i][r];
    if (refws == null || refws.length == 0) return sum;
    int startIndex = 0;
    while (startIndex < refws.length 
        && startIndex < words.length
        && startIndex < maxGramLength
        && refws[startIndex].equals(words[startIndex])){
      startIndex++;      
    }
    if (startIndex > 0) sum += KS4SelectPrediction + separatorLength;
    
    while (startIndex < refws.length){
      if (startIndex == 0 || ArrayUtils.contains(words, refws[startIndex - 1]) == false){
        sum += Math.max(Math.floor(refws[startIndex].length() * minKSRatioNormal), 1);

        if (startIndex < refws.length -1 ) {
          startIndex++;
          sum += separatorLength + KS4SelectIME;
        }else{
          startIndex++;
          sum += KS4SelectIME;
        }
        continue;
      }
            
      int tempIndex = startIndex;
      int tempAnchorIndex = ArrayUtils.indexOf(words,  refws[startIndex-1]) + 1;
      
      int anchorIndex = tempAnchorIndex;
      int currentPredictionLength = 0;
//      while (tempAnchorIndex != -1){
//        
//      }
      while (tempIndex < refws.length
          && tempAnchorIndex < words.length
          && refws[tempIndex].equals(words[tempAnchorIndex])
          && tempIndex-startIndex < maxGramLength ){
        
        tempIndex++;
        tempAnchorIndex++;
      }
      
      if (tempIndex == startIndex){
        sum += Math.max(Math.floor(refws[startIndex].length() * minKSRatioNormal), 1);
        sum += separatorLength + KS4SelectIME;
        startIndex++;
        continue;
      }
      
      sum += KS4SelectPrediction;
      if (tempIndex < refws.length){
        sum += separatorLength;
        startIndex = tempIndex;
        continue;
      }
      
      break;
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
}

package joshua;

import joshua.metrics.BLEU;
import joshua.metrics.EvaluationMetric;
import joshua.metrics.MinKSR;
import joshua.metrics.TER;

public class MinKSRTest {

  public static void main(String[] args) {
    MinKSRTest.simpleTest();
  }

  public static void simpleTest() {

//    String ref1 =  "中国 考虑 改革 公务员 福利 制度";
    String ref1 = "通过 对话 实现 政治 解决 是 唯一 正确 的 途径 ， 这 也 是 国际 社会 的 共识 。 ";
//    String ref1 = "a b c c de de de de de f g h";
//    String ref2 = "好 逐渐 情况 经济 中国";
//    String test1 = "中国 考虑 改变 官员 福利 制度";
//    String test1 = "中国 考虑 改变 才能 官员 福利 制度";
//    String test1 = "考虑 改革 中国 官员 福利 制度";
//      String test1 = "公务员 福利 制度 中国 考虑 改变";
//    String test1 = "福利 中国 考虑 改革 才能 公务员 制度";
    
    String test1="“通过 对话 是 实现 政治 解决 的 唯一 正确 途径 和 国际 社会 的 共识 。";
//    String test1="唯一 正确 途径 也 是 国际 社会 的 共识 是 实现 通过 对话 的 政治 解决 。";
//    String test1="通过 对话 实现 政治 解决 是 唯一 正确 的 途径 ， 这 也 是 国际 社会 的 共识 。";
    
//    String test1 = "a b c c de de de de de f g h";
//    String test2 = "好 逐渐 情况 经济 中国";
    
    // refSentences[i][r] stores the r'th reference of the i'th sentence
    String[][] refSentences = new String[1][1];
    refSentences[0][0] = ref1;
//    refSentences[1][0] = ref2;
//    refSentences[0][1] = ref2;
    
    EvaluationMetric.set_numSentences(1);
    EvaluationMetric.set_refsPerSen(1);
    EvaluationMetric.set_refSentences(refSentences);
    
    MinKSR minksr = new MinKSR();
    BLEU bleu = new BLEU();
    String[] terOptions = {"nocase", "punc", "20", "50", "tercom.7.25.jar", "5"};
    TER ter = new TER(terOptions);
    
    // testSentences[i] stores the candidate translation for the i'th sentence
    String[] testSentences = new String[1];
    testSentences[0] = test1;
//    testSentences[1] = test2;
    try {
      // Check BLEU score matches
      double actualScore = minksr.score(testSentences);
      
      double actualScoreBleu = bleu.score(testSentences);
      
      double actualScoreTer = ter.score(testSentences);
      // Check sufficient statistics match
      int[] actualSS = minksr.suffStats(testSentences);
      
      int[] actualSSBleu = bleu.suffStats(testSentences);
      
      int[] actualSSTer = ter.suffStats(testSentences);
//      int[] expectedSS = {14,27,8,26,5,25,3,24,27,23};


      System.out.println("BLEU");
      for (int a : actualSSBleu){
        System.out.print(a + " ");
      }
      System.out.println(actualScoreBleu);
      
      System.out.println("MinKSR");
      for (int a : actualSS){
        System.out.print(a + " ");
      }      
      System.out.println(actualScore);
      
      System.out.println("TER");
      for (int a : actualSSTer){
        System.out.print(a + " ");
      }
      System.out.println(actualScoreTer);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

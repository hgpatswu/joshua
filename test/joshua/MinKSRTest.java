package joshua;

import joshua.metrics.BLEU;
import joshua.metrics.EvaluationMetric;
import joshua.metrics.MinKSR;

import org.testng.Assert;

public class MinKSRTest {

  public static void main(String[] args) {
    MinKSRTest.simpleTest();
  }

  public static void simpleTest() {

    String ref1 =  "辛格 将 作为 其 中国 同僚 李克强 的 宾客 于 10月 22 至 24日 对 中国 进行 正式 访问";
//    String ref2 = "好 逐渐 情况 经济 中国";
//    String test = "中国 考虑 改变 才能 官员 福利 制度";
    String test1 = "辛格 将 作为 其 中国 同僚 李克强 的 宾客 于 10月 22 至 24日 对 中国 进行 正式 访问";
//    String test2 = "好 逐渐 情况 经济 中国";
    
    // refSentences[i][r] stores the r'th reference of the i'th sentence
    String[][] refSentences = new String[1][2];
    refSentences[0][0] = ref1;
//    refSentences[1][0] = ref2;
//    refSentences[0][1] = ref2;
    
    EvaluationMetric.set_numSentences(1);
    EvaluationMetric.set_refsPerSen(1);
    EvaluationMetric.set_refSentences(refSentences);
    
    MinKSR minksr = new MinKSR();
    
    // testSentences[i] stores the candidate translation for the i'th sentence
    String[] testSentences = new String[1];
    testSentences[0] = test1;
//    testSentences[1] = test2;
    try {
      // Check BLEU score matches
      double actualScore = minksr.score(testSentences);

      // Check sufficient statistics match
      int[] actualSS = minksr.suffStats(testSentences);
//      int[] expectedSS = {14,27,8,26,5,25,3,24,27,23};


      for (int a : actualSS){
        System.out.print(a + " ");
      }
      
      System.out.println(actualScore);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

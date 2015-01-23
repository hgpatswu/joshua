package joshua.util;

public class SuffixMatchResult implements Comparable<SuffixMatchResult> {

  public int startPos = -1;
  
  public int len = 0;
  
  public SuffixMatchResult(int startPos, int len){
    this.startPos = startPos;
    this.len = len;
  }
  
  public SuffixMatchResult(){
    
  }
  
   @Override
  public int compareTo(SuffixMatchResult o) {
    return Integer.compare(len,  o.len);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + len;
    result = prime * result + startPos;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SuffixMatchResult other = (SuffixMatchResult) obj;
    if (len != other.len)
      return false;
    if (startPos != other.startPos)
      return false;
    return true;
  }

}

package pe.edu.pucp.johannmorales.thesis.algorithm.genetic.structures;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class BitArrayTest {

  @Test
  public void flip_ShouldSetTrue() {
    assertThat(BitArray.create("101").flip(1)).isEqualTo(BitArray.create("111"));
    assertThat(BitArray.create("011").flip(2)).isEqualTo(BitArray.create("111"));
  }

  @Test
  public void flip_ShouldSetFalse() {
    assertThat(BitArray.create("010").flip(1)).isEqualTo(BitArray.create("000"));
    assertThat(BitArray.create("100").flip(2)).isEqualTo(BitArray.create("000"));
  }

  @Test
  public void setFrom_ShouldSetTrueFromStart() {
    assertThat(BitArray.create("000").setFrom(0, true)).isEqualTo(BitArray.create("111"));
    assertThat(BitArray.create("100").setFrom(0, true)).isEqualTo(BitArray.create("111"));
  }

  @Test
  public void setFrom_ShouldSetFalseFromStart() {
    assertThat(BitArray.create("111").setFrom(0, false)).isEqualTo(BitArray.create("000"));
    assertThat(BitArray.create("011").setFrom(0, false)).isEqualTo(BitArray.create("000"));
  }

  @Test
  public void setFrom_ShouldSetTrueFromPosition() {
    assertThat(BitArray.create("000").setFrom(1, true)).isEqualTo(BitArray.create("110"));
    assertThat(BitArray.create("010").setFrom(1, true)).isEqualTo(BitArray.create("110"));
  }

  @Test
  public void setFrom_ShouldSetFalseFromPosition() {
    assertThat(BitArray.create("111").setFrom(1, false)).isEqualTo(BitArray.create("001"));
    assertThat(BitArray.create("101").setFrom(1, false)).isEqualTo(BitArray.create("001"));
  }

  @Test
  public void setTo_ShouldSetTrueToEnd() {
    assertThat(BitArray.create("000").setTo(3, true)).isEqualTo(BitArray.create("111"));
    assertThat(BitArray.create("100").setTo(3, true)).isEqualTo(BitArray.create("111"));
  }

  @Test
  public void setTo_ShouldSetFalseToEnd() {
    assertThat(BitArray.create("111").setTo(3, false)).isEqualTo(BitArray.create("000"));
    assertThat(BitArray.create("011").setTo(3, false)).isEqualTo(BitArray.create("000"));
  }

  @Test
  public void setTo_ShouldSetTrueToPosition() {
    assertThat(BitArray.create("000").setTo(2, true)).isEqualTo(BitArray.create("011"));
    assertThat(BitArray.create("010").setTo(2, true)).isEqualTo(BitArray.create("011"));
  }

  @Test
  public void setTo_ShouldSetFalseToPosition() {
    assertThat(BitArray.create("111").setTo(2, false)).isEqualTo(BitArray.create("100"));
    assertThat(BitArray.create("101").setTo(2, false)).isEqualTo(BitArray.create("100"));
  }

  @Test
  public void test() {
    int n = 150;
    System.out.println(Long.toBinaryString(~0L).length());
    for (int i = 0; i < n; i++) {
      System.out.println(StringUtils.leftPad("" + i, 3) + "  " + BitArray.create(n).flip(i));
    }
  }

  @Test
  public void test3() {
    int n = 150;
    BitArray bitArray = BitArray.createMax(n);
    for (int i = 0; i < n; i++) {
      String binary = bitArray.flip(i).toBinaryString();
      for (int j = 0; j < n; j++) {
        char c = binary.charAt(binary.length() - j - 1);
        if (j != i) {
          assertThat(c).isEqualTo('1');
        } else {
          assertThat(c).isEqualTo('0');
        }
      }
    }
  }


  @Test
  public void test2() {
    int n = 200;
    System.out.println(Long.toBinaryString(~0L).length());
    for (int i = 0; i < n; i++) {
      System.out
          .println(StringUtils.leftPad("" + i, 3) + "  " + BitArray.createMax(n).setFrom(i, false)
              .toBinaryString());
      System.out
          .println(StringUtils.leftPad("" + i, 3) + "  " + BitArray.createMax(n).setTo(i, false)
              .toBinaryString());
    }
  }

}
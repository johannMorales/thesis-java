package pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.structures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.math.BigDecimal;
import org.junit.Test;

public class VectorTest {

  @Test
  public void createConstant_ShouldReturnSameValueForEachComponent() {
    Vector v = Vector.createConstant(4, BigDecimal.TEN);
    for (int i = 0; i < 4; i++) {
      assertThat(v.get(i)).isEqualTo(BigDecimal.TEN);
    }
  }

  @Test
  public void get_WithInvalidDimension_ShouldThrowIndexOutOfBoundsException() {
    Vector v = Vector.createConstant(4, BigDecimal.TEN);
    assertThatExceptionOfType(IndexOutOfBoundsException.class)
        .isThrownBy(() -> v.get(10));
    assertThatExceptionOfType(IndexOutOfBoundsException.class)
        .isThrownBy(() -> v.get(4));
    assertThatExceptionOfType(IndexOutOfBoundsException.class)
        .isThrownBy(() -> v.get(-1));
  }

  @Test
  public void set_WithInvalidDimension_ShouldThrowIndexOutOfBoundsException() {
    Vector v = Vector.createConstant(4, BigDecimal.TEN);
    assertThatExceptionOfType(IndexOutOfBoundsException.class)
        .isThrownBy(() -> v.set(10, BigDecimal.ZERO));
    assertThatExceptionOfType(IndexOutOfBoundsException.class)
        .isThrownBy(() -> v.set(4, BigDecimal.ZERO));
    assertThatExceptionOfType(IndexOutOfBoundsException.class)
        .isThrownBy(() -> v.set(-1, BigDecimal.ZERO));
  }

}
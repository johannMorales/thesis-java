package pe.edu.pucp.johannmorales.thesis.algorithm.common;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
public class Result {

  private Integer iteration;

  private Long executionTime;

  private BigDecimal fitness;

}

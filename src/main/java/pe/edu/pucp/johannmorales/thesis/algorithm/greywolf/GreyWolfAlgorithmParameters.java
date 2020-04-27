package pe.edu.pucp.johannmorales.thesis.algorithm.greywolf;

import java.util.Random;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class GreyWolfAlgorithmParameters {

  private Integer iterations;
  private Integer populationSize;
  private Integer dimensions;
  private Random random;
}

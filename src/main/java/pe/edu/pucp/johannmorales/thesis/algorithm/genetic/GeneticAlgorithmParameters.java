package pe.edu.pucp.johannmorales.thesis.algorithm.genetic;

import java.util.Random;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GeneticAlgorithmParameters {

  private Integer generations;

  private Integer populationSize;

  private Double ratioSurvive;

  private Double ratioRecombination;

  private Double ratioMutation;

  private Random random;

}

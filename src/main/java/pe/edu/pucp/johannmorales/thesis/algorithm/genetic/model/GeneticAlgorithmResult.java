package pe.edu.pucp.johannmorales.thesis.algorithm.genetic.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import pe.edu.pucp.johannmorales.thesis.algorithm.common.Result;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.structures.BitArray;

@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
public class GeneticAlgorithmResult extends Result {

  private BitArray chromosome;


}

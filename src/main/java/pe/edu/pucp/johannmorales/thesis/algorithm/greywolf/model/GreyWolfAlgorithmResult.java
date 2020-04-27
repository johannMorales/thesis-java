package pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import pe.edu.pucp.johannmorales.thesis.algorithm.common.Result;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.structures.Vector;

@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
public class GreyWolfAlgorithmResult extends Result {

  private Vector wolf;


}

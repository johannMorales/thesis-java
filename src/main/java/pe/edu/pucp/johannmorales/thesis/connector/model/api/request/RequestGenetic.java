package pe.edu.pucp.johannmorales.thesis.connector.model.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestGenetic {

  private Integer population;
  private Integer generations;
  private Double ratioCrossover;
  private Double ratioMutation;
  private Double ratioSurvive;

}

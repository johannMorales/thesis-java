package pe.edu.pucp.johannmorales.thesis.connector.model.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestProblem {

  private RequestGenetic genetic;

  private RequestProblemParameters problem;

  private RequestGreyWolf greyWolf;

  private RequestLoadedData loadedData;

}

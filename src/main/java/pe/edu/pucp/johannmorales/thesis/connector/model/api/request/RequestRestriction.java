package pe.edu.pucp.johannmorales.thesis.connector.model.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestRestriction {

  private Long workareatypeIdA;
  private Long workareatypeIdB;
  private Double distance;

}

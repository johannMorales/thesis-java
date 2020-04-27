package pe.edu.pucp.johannmorales.thesis.connector.model.api.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponsePeriod {

  private Long id;
  private String name;
  private List<ResponseProcess> processes;
  private List<ResponseWorkAreaType> workAreaTypes;

}

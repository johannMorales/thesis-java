package pe.edu.pucp.johannmorales.thesis.connector.model.api.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Response {

  private List<ResponsePeriod> periods;

}

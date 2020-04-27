package pe.edu.pucp.johannmorales.thesis.connector.model.api.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestWorkAreaStatic {

  private Long workareatypeId;
  private BigDecimal x;
  private BigDecimal y;

}

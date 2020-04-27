package pe.edu.pucp.johannmorales.thesis.connector.model.api.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestWorkAreaType {

  private Long id;
  private String name;
  private Boolean isStatic;
  private BigDecimal w;
  private BigDecimal h;
  private BigDecimal mhc;
  private BigDecimal rc;
  private Integer amount;

}

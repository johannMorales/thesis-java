package pe.edu.pucp.johannmorales.thesis.connector.model.api.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseWorkAreaType {

  public Long id;
  public String name;
  public Boolean isStatic;
  public BigDecimal w;
  public BigDecimal h;

  private List<ResponseWorkArea> solutionGreyWolf;
  private List<ResponseWorkArea> solutionGenetic;

}

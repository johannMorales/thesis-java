package pe.edu.pucp.johannmorales.thesis.flp.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Problem {


  private List<Period> periods;
  private List<WorkArea> workAreas;


  public BigDecimal objectiveFunction() {
    BigDecimal result = BigDecimal.ZERO;

    for (Period period : periods) {
      for (Process process : period.getProcesses()) {
      }
    }

    return result;
  }
}

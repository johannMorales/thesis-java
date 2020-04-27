package pe.edu.pucp.johannmorales.thesis.flp.model;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Period {

  private Long id;
  private String name;
  private List<Process> processes;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Period period = (Period) o;
    return id.equals(period.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

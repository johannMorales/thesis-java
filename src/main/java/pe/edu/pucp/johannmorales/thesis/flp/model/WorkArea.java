package pe.edu.pucp.johannmorales.thesis.flp.model;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkArea {

  private Long id;
  private Double x;
  private Double y;
  private Period period;
  private Process process;
  private WorkAreaType type;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkArea workArea = (WorkArea) o;
    return Objects.equals(id, workArea.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}

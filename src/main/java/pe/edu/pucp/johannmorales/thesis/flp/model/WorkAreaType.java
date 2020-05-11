package pe.edu.pucp.johannmorales.thesis.flp.model;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WorkAreaType {

  private Long id;
  private Boolean isStatic;
  private Integer amount;
  private Double w;
  private Double h;
  private Double mhc;
  private Double rc;
  private List<WorkArea> staticWorkAreas;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkAreaType workArea = (WorkAreaType) o;
    return Objects.equals(id, workArea.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}

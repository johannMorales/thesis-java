package pe.edu.pucp.johannmorales.thesis.flp.model;

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

  private Double x;
  private Double y;
  private WorkAreaType type;

  public Double cX() {
    return x + type.getW() / 2;
  }

  public Double cY() {
    return y + type.getH() / 2;
  }

}

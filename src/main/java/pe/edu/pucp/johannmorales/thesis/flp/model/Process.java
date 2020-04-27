package pe.edu.pucp.johannmorales.thesis.flp.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Process {

  private Long id;
  private List<WorkAreaType> workAreasTypes;

}

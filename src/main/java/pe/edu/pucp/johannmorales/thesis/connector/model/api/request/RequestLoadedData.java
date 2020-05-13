package pe.edu.pucp.johannmorales.thesis.connector.model.api.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestLoadedData {

  private List<RequestPeriod> periods;
  private List<RequestProcess> processes;
  private List<RequestProcessPeriod> processxperiod;
  private List<RequestWorkAreaType> workareatypes;
  private List<RequestProcessWorkAreaType> workareatypexprocess;
  private List<RequestWorkAreaStatic> workareasstatic;
  private List<RequestRestriction> restrictionsMin;
  private List<RequestRestriction> restrictionsMax;

}

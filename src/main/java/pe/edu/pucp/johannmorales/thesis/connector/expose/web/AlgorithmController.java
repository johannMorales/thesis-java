package pe.edu.pucp.johannmorales.thesis.connector.expose.web;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.model.GeneticAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.model.GreyWolfAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProblem;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.Response;
import pe.edu.pucp.johannmorales.thesis.connector.service.AlgorithmService;

@Log4j2
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AlgorithmController {

  private AlgorithmService algorithmService;

  @PostMapping("genetic")
  public Response runGenetic(@RequestBody RequestProblem request) {
    log.info("{}", request.getGenetic());
    log.info("{}", request.getGreyWolf());
    return algorithmService.run(request);
  }

  @PostMapping("greywolf-test")
  public GreyWolfAlgorithmResult[] greyWolfTest(@RequestBody RequestProblem request) {
    log.info("{}", request.getGreyWolf());
    return algorithmService.runGwTest(request);
  }


  @PostMapping("genetic-test")
  public GeneticAlgorithmResult[] geneticTest(@RequestBody RequestProblem request) {
    log.info("{}", request.getGenetic());
    return algorithmService.runGATest(request);
  }

}

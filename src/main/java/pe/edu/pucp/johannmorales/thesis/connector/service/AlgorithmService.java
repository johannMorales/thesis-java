package pe.edu.pucp.johannmorales.thesis.connector.service;

import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.model.GeneticAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.model.GreyWolfAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProblem;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.Response;

public interface AlgorithmService {

  Response run(RequestProblem request);

  GreyWolfAlgorithmResult[] runGwTest(RequestProblem request);

  GeneticAlgorithmResult[] runGATest(RequestProblem request);
}

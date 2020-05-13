package pe.edu.pucp.johannmorales.thesis.connector.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.GeneticAlgorithmParameters;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.model.GeneticAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.GreyWolfAlgorithmParameters;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.model.GreyWolfAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestLoadedData;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestPeriod;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProblem;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProcess;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProcessPeriod;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProcessWorkAreaType;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestRestriction;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestWorkAreaStatic;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestWorkAreaType;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.Response;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.ResponsePeriod;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.ResponseProcess;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.ResponseWorkArea;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.ResponseWorkAreaType;
import pe.edu.pucp.johannmorales.thesis.flp.FLP;
import pe.edu.pucp.johannmorales.thesis.flp.model.Period;
import pe.edu.pucp.johannmorales.thesis.flp.model.Process;
import pe.edu.pucp.johannmorales.thesis.flp.model.WorkArea;
import pe.edu.pucp.johannmorales.thesis.flp.model.WorkAreaType;

@Log4j2
@Service
public class AlgorithmServiceImpl implements AlgorithmService {

  @Override
  public Response run(RequestProblem req) {
    RequestLoadedData request = req.getLoadedData();
    List<Period> periods = new ArrayList<>();
    Map<Long, Period> periodsById = new HashMap<>();
    Map<Long, RequestPeriod> requestPeriodsById = new HashMap<>();

    Map<Period, List<WorkAreaType>> workAreaTypesByPeriod = new HashMap<>();
    Map<Period, Set<WorkAreaType>> workAreaTypesByPeriodHelper = new HashMap<>();

    Map<Long, Process> processesById = new HashMap<>();
    Map<Long, RequestProcess> requestProcessesById = new HashMap<>();
    List<Process> processes = new ArrayList<>();

    Map<Long, WorkAreaType> workAreaTypeById = new HashMap<>();
    Map<Long, RequestWorkAreaType> requestWorkAreaTypeById = new HashMap<>();
    Map<Process, Set<WorkAreaType>> workAreaTypesByProcess = new HashMap<>();

    Map<WorkAreaType, Map<WorkAreaType, Double>> minDistanceMap = new HashMap<>();
    Map<WorkAreaType, Map<WorkAreaType, Double>> maxDistanceMap = new HashMap<>();

    buildStructures(req, request, periods, periodsById, requestPeriodsById, workAreaTypesByPeriod,
        workAreaTypesByPeriodHelper, processesById, requestProcessesById, processes,
        workAreaTypeById,
        requestWorkAreaTypeById,
        workAreaTypesByProcess,
        minDistanceMap,
        maxDistanceMap);

    int facilities = 0;
    for (Period period : periods) {
      for (WorkAreaType workAreaType : workAreaTypesByPeriod.get(period)) {
        if (!workAreaType.getIsStatic()) {
          facilities += workAreaType.getAmount();
        }
      }
    }

    FLP flp = getFlp(req, periods, workAreaTypesByPeriod, facilities, minDistanceMap,
        maxDistanceMap);

    GeneticAlgorithmResult[] results = flp.runGenetic(GeneticAlgorithmParameters.builder()
        .generations(req.getGenetic().getGenerations())
        .random(new Random())
        .populationSize(req.getGenetic().getPopulation())
        .ratioMutation(req.getGenetic().getRatioMutation())
        .ratioRecombination(req.getGenetic().getRatioCrossover())
        .ratioSurvive(req.getGenetic().getRatioSurvive())
        .build());

    GeneticAlgorithmResult result = results[results.length - 1];
    log.info("GA {}", result);

    GreyWolfAlgorithmResult[] resultsGW = flp.runGreyWolf(GreyWolfAlgorithmParameters.builder()
        .iterations(req.getGreyWolf().getIterations())
        .populationSize(req.getGreyWolf().getPopulation())
        .dimensions(facilities * 2)
        .random(new Random())
        .build());

    GreyWolfAlgorithmResult resultGW = resultsGW[resultsGW.length - 1];

    log.info("GW {}", resultGW);

    List<Pair<Double, Double>> gaResultCoordinates = flp.decodeBitArray(result.getChromosome());
    List<Pair<Double, Double>> gwResultCoordinates = flp.decodeVector(resultGW.getWolf());

    Response response = new Response();
    response.setPeriods(new ArrayList<>());

    for (Period period : periods) {
      RequestPeriod requestPeriod = requestPeriodsById.get(period.getId());
      ResponsePeriod rp = new ResponsePeriod();
      rp.setId(requestPeriod.getId());
      rp.setName(requestPeriod.getName());
      rp.setWorkAreaTypes(new ArrayList<>());
      rp.setProcesses(new ArrayList<>());
      response.getPeriods().add(rp);

      int pairCounter = 0;

      Map<Long, ResponseWorkAreaType> responseWorkAreaTypeMap = new HashMap<>();
      if (workAreaTypesByPeriod.get(period) != null) {
        for (WorkAreaType workAreaType : workAreaTypesByPeriod.get(period)) {
          RequestWorkAreaType rwat = requestWorkAreaTypeById.get(workAreaType.getId());
          ResponseWorkAreaType responseWorkAreaType = new ResponseWorkAreaType();
          responseWorkAreaType.setH(rwat.getH());
          responseWorkAreaType.setIsStatic(rwat.getIsStatic());
          responseWorkAreaType.setW(rwat.getW());
          responseWorkAreaType.setName(rwat.getName());
          responseWorkAreaType.setId(rwat.getId());

          rp.getWorkAreaTypes().add(responseWorkAreaType);
          responseWorkAreaTypeMap.put(rwat.getId(), responseWorkAreaType);

          if (workAreaType.getIsStatic()) {
            List<ResponseWorkArea> rwa = workAreaType.getStaticWorkAreas()
                .stream()
                .map(i -> ResponseWorkArea.builder().x(i.getX()).y(i.getY()).build())
                .collect(Collectors.toList());
            responseWorkAreaType.setSolutionGenetic(rwa);
            responseWorkAreaType.setSolutionGreyWolf(rwa);
          } else {
            responseWorkAreaType.setSolutionGenetic(new ArrayList<>());
            responseWorkAreaType.setSolutionGreyWolf(new ArrayList<>());
            for (int i = 0; i < rwat.getAmount(); i++) {
              Pair<Double, Double> pair = gaResultCoordinates.get(pairCounter);
              ResponseWorkArea rwa = new ResponseWorkArea();
              rwa.setX(pair.getLeft());
              rwa.setY(pair.getRight());
              responseWorkAreaType.getSolutionGenetic().add(rwa);

              Pair<Double, Double> pair2 = gwResultCoordinates.get(pairCounter++);
              ResponseWorkArea rwa2 = new ResponseWorkArea();
              rwa2.setX(pair2.getLeft());
              rwa2.setY(pair2.getRight());
              responseWorkAreaType.getSolutionGreyWolf().add(rwa2);
            }
          }


        }
      }

      for (Process process : period.getProcesses()) {
        RequestProcess requestProcess = requestProcessesById.get(process.getId());
        ResponseProcess rpx = new ResponseProcess();
        rpx.setId(requestProcess.getId());
        rpx.setWorkAreaTypes(new ArrayList<>());
        rpx.setName(requestProcess.getName());
        rp.getProcesses().add(rpx);

        if (workAreaTypesByProcess.get(process) != null) {
          for (WorkAreaType workAreaType : workAreaTypesByProcess.get(process)) {
            ResponseWorkAreaType responseWorkAreaType = responseWorkAreaTypeMap
                .get(workAreaType.getId());
            rpx.getWorkAreaTypes().add(responseWorkAreaType);
          }
        }
      }
    }

    return response;
  }

  private void buildStructures(RequestProblem req, RequestLoadedData request, List<Period> periods,
      Map<Long, Period> periodsById, Map<Long, RequestPeriod> requestPeriodsById,
      Map<Period, List<WorkAreaType>> workAreaTypesByPeriod,
      Map<Period, Set<WorkAreaType>> workAreaTypesByPeriodHelper, Map<Long, Process> processesById,
      Map<Long, RequestProcess> requestProcessesById, List<Process> processes,
      Map<Long, WorkAreaType> workAreaTypeById,
      Map<Long, RequestWorkAreaType> requestWorkAreaTypeById,
      Map<Process, Set<WorkAreaType>> workAreaTypesByProcess,
      Map<WorkAreaType, Map<WorkAreaType, Double>> minDistanceMap,
      Map<WorkAreaType, Map<WorkAreaType, Double>> maxDistanceMap) {
    for (RequestPeriod rp : request.getPeriods()) {
      requestPeriodsById.put(rp.getId(), rp);
      Period p = new Period();
      p.setId(rp.getId());
      p.setProcesses(new ArrayList<>());
      periods.add(p);
      periodsById.put(p.getId(), p);
    }

    for (RequestProcess rq : request.getProcesses()) {
      requestProcessesById.put(rq.getId(), rq);
      Process p = new Process();
      p.setId(rq.getId());
      p.setWorkAreasTypes(new ArrayList<>());
      processes.add(p);
      processesById.put(p.getId(), p);
    }

    for (RequestWorkAreaType rwat : request.getWorkareatypes()) {
      requestWorkAreaTypeById.put(rwat.getId(), rwat);
      WorkAreaType wat = new WorkAreaType();
      wat.setId(rwat.getId());
      wat.setIsStatic(rwat.getIsStatic());
      wat.setAmount(rwat.getAmount());
      wat.setH(rwat.getH().doubleValue());
      wat.setW(rwat.getW().doubleValue());
      wat.setRc(rwat.getRc().doubleValue());
      wat.setMhc(rwat.getMhc().doubleValue());
      workAreaTypeById.put(wat.getId(), wat);
    }

    for (RequestProcessPeriod rpp : request.getProcessxperiod()) {
      if (periodsById.get(rpp.getPeriodId()) == null) {
        log.error("Couldn't find period with id {}", rpp.getPeriodId());
      }
      if (processesById.get(rpp.getProcessId()) == null) {
        log.error("Couldn't find process with id {}", rpp.getProcessId());
      }
      periodsById.get(rpp.getPeriodId()).getProcesses().add(processesById.get(rpp.getProcessId()));
    }

    for (RequestProcessWorkAreaType rpwat : request.getWorkareatypexprocess()) {
      if (processesById.get(rpwat.getProcessId()) == null) {
        log.error("Couldn't find process with id {}", rpwat.getProcessId());
      }
      if (workAreaTypeById.get(rpwat.getWorkareatypeId()) == null) {
        log.error("Couldn't find workAreaType with id {}", rpwat.getWorkareatypeId());
      }
      Process process = processesById.get(rpwat.getProcessId());
      WorkAreaType workAreaType = workAreaTypeById.get(rpwat.getWorkareatypeId());

      if (!workAreaTypesByProcess.containsKey(process)) {
        workAreaTypesByProcess.put(process, new HashSet<>());
      }
      process.getWorkAreasTypes().add(workAreaType);
      workAreaTypesByProcess.get(process).add(workAreaType);
    }

    for (RequestWorkAreaStatic requestWorkAreaStatic : req.getLoadedData().getWorkareasstatic()) {
      WorkAreaType wat = workAreaTypeById.get(requestWorkAreaStatic.getWorkareatypeId());
      if (wat.getStaticWorkAreas() == null) {
        wat.setStaticWorkAreas(new ArrayList<>());
      }
      wat.getStaticWorkAreas().add(WorkArea
          .builder()
          .type(wat)
          .x(requestWorkAreaStatic.getX())
          .y(requestWorkAreaStatic.getY())
          .build());
    }

    // Extract which unique work-area-types are used by period
    for (Period period : periods) {
      if (!workAreaTypesByPeriodHelper.containsKey(period)) {
        workAreaTypesByPeriodHelper.put(period, new HashSet<>());
        workAreaTypesByPeriod.put(period, new ArrayList<>());
      }
      List<WorkAreaType> workAreaTypeList = workAreaTypesByPeriod.get(period);
      Set<WorkAreaType> workAreaTypeSet = workAreaTypesByPeriodHelper.get(period);
      for (Process process : period.getProcesses()) {
        for (WorkAreaType workAreasType : process.getWorkAreasTypes()) {
          if (!workAreaTypeSet.contains(workAreasType)) {
            workAreaTypeSet.add(workAreasType);
            workAreaTypeList.add(workAreasType);
          }
        }
      }
    }

    for (RequestRestriction restriction : req.getLoadedData().getRestrictionsMin()) {
      WorkAreaType watA = workAreaTypeById.get(restriction.getWorkareatypeIdA());
      WorkAreaType watB = workAreaTypeById.get(restriction.getWorkareatypeIdB());

      if (!minDistanceMap.containsKey(watA)) {
        minDistanceMap.put(watA, new HashMap<>());
      }

      if (!minDistanceMap.containsKey(watB)) {
        minDistanceMap.put(watB, new HashMap<>());
      }

      if (minDistanceMap.get(watA).containsKey(watB)) {
        if (minDistanceMap.get(watA).get(watB) < restriction.getDistance()) {
          minDistanceMap.get(watA).put(watB, restriction.getDistance());
        }
      } else {
        minDistanceMap.get(watA).put(watB, restriction.getDistance());
      }

      if (maxDistanceMap.get(watA).containsKey(watB)) {
        if (maxDistanceMap.get(watA).get(watB) > restriction.getDistance()) {
          maxDistanceMap.get(watA).put(watB, restriction.getDistance());
        }
      } else {
        maxDistanceMap.get(watA).put(watB, restriction.getDistance());
      }
    }
  }

  @Override
  public GreyWolfAlgorithmResult[] runGwTest(RequestProblem req) {
    RequestLoadedData request = req.getLoadedData();
    List<Period> periods = new ArrayList<>();
    Map<Long, Period> periodsById = new HashMap<>();
    Map<Long, RequestPeriod> requestPeriodsById = new HashMap<>();

    Map<Period, List<WorkAreaType>> workAreaTypesByPeriod = new HashMap<>();
    Map<Period, Set<WorkAreaType>> workAreaTypesByPeriodHelper = new HashMap<>();

    Map<Long, Process> processesById = new HashMap<>();
    Map<Long, RequestProcess> requestProcessesById = new HashMap<>();
    List<Process> processes = new ArrayList<>();

    Map<Long, WorkAreaType> workAreaTypeById = new HashMap<>();
    Map<Long, RequestWorkAreaType> requestWorkAreaTypeById = new HashMap<>();
    Map<Process, Set<WorkAreaType>> workAreaTypesByProcess = new HashMap<>();

    Map<WorkAreaType, Map<WorkAreaType, Double>> minDistanceMap = new HashMap<>();
    Map<WorkAreaType, Map<WorkAreaType, Double>> maxDistanceMap = new HashMap<>();

    buildStructures(req, request, periods, periodsById, requestPeriodsById, workAreaTypesByPeriod,
        workAreaTypesByPeriodHelper, processesById, requestProcessesById, processes,
        workAreaTypeById,
        requestWorkAreaTypeById, workAreaTypesByProcess, minDistanceMap, maxDistanceMap);

    int facilities = 0;
    for (Period period : periods) {
      for (WorkAreaType workAreaType : workAreaTypesByPeriod.get(period)) {
        if (!workAreaType.getIsStatic()) {
          facilities += workAreaType.getAmount();
        }
      }
    }

    FLP flp = getFlp(req, periods, workAreaTypesByPeriod, facilities, minDistanceMap,
        maxDistanceMap);

    GreyWolfAlgorithmResult[] resultsGW = flp.runGreyWolf(GreyWolfAlgorithmParameters.builder()
        .iterations(req.getGreyWolf().getIterations())
        .populationSize(req.getGreyWolf().getPopulation())
        .dimensions(facilities * 2)
        .random(new Random())
        .build());

    for (GreyWolfAlgorithmResult greyWolfAlgorithmResult : resultsGW) {
      greyWolfAlgorithmResult.setWolf(null);
    }

    return resultsGW;
  }

  @Override
  public GeneticAlgorithmResult[] runGATest(RequestProblem req) {
    RequestLoadedData request = req.getLoadedData();
    List<Period> periods = new ArrayList<>();
    Map<Long, Period> periodsById = new HashMap<>();
    Map<Long, RequestPeriod> requestPeriodsById = new HashMap<>();

    Map<Period, List<WorkAreaType>> workAreaTypesByPeriod = new HashMap<>();
    Map<Period, Set<WorkAreaType>> workAreaTypesByPeriodHelper = new HashMap<>();

    Map<Long, Process> processesById = new HashMap<>();
    Map<Long, RequestProcess> requestProcessesById = new HashMap<>();
    List<Process> processes = new ArrayList<>();

    Map<Long, WorkAreaType> workAreaTypeById = new HashMap<>();
    Map<Long, RequestWorkAreaType> requestWorkAreaTypeById = new HashMap<>();
    Map<Process, Set<WorkAreaType>> workAreaTypesByProcess = new HashMap<>();

    Map<WorkAreaType, Map<WorkAreaType, Double>> minDistanceMap = new HashMap<>();
    Map<WorkAreaType, Map<WorkAreaType, Double>> maxDistanceMap = new HashMap<>();

    buildStructures(req, request, periods, periodsById, requestPeriodsById, workAreaTypesByPeriod,
        workAreaTypesByPeriodHelper, processesById, requestProcessesById, processes,
        workAreaTypeById,
        requestWorkAreaTypeById, workAreaTypesByProcess, minDistanceMap, maxDistanceMap);

    int facilities = 0;
    for (Period period : periods) {
      for (WorkAreaType workAreaType : workAreaTypesByPeriod.get(period)) {
        if (!workAreaType.getIsStatic()) {
          facilities += workAreaType.getAmount();
        }
      }
    }

    FLP flp = getFlp(req, periods, workAreaTypesByPeriod, facilities, minDistanceMap,
        maxDistanceMap);

    GeneticAlgorithmResult[] results = flp.runGenetic(GeneticAlgorithmParameters.builder()
        .generations(req.getGenetic().getGenerations())
        .random(new Random())
        .populationSize(req.getGenetic().getPopulation())
        .ratioMutation(req.getGenetic().getRatioMutation())
        .ratioRecombination(req.getGenetic().getRatioCrossover())
        .ratioSurvive(req.getGenetic().getRatioSurvive())
        .build());

    for (GeneticAlgorithmResult result : results) {
      result.setChromosome(null);
    }

    return results;
  }

  private FLP getFlp(
      RequestProblem req,
      List<Period> periods,
      Map<Period, List<WorkAreaType>> workAreaTypesByPeriod,
      int facilities,
      Map<WorkAreaType, Map<WorkAreaType, Double>> minDistanceMap,
      Map<WorkAreaType, Map<WorkAreaType, Double>> maxDistanceMap
  ) {
    int value = req.getProblem().getMaxX();

    int bitSizeX = 0;
    while (value > 0) {
      bitSizeX++;
      value = value >> 1;
    }

    value = req.getProblem().getMaxY();
    int bitSizeY = 0;
    while (value > 0) {
      bitSizeY++;
      value = value >> 1;
    }

    return FLP.builder()
        .bitSizeX(bitSizeX)
        .bitSizeY(bitSizeY)
        .facilitiesNumber(facilities)
        .periods(periods)
        .workAreaTypeByPeriod(workAreaTypesByPeriod)
        .maxX(req.getProblem().getMaxX())
        .maxY(req.getProblem().getMaxY())
        .minDistanceMap(minDistanceMap)
        .maxDistanceMap(maxDistanceMap)
        .build();
  }

}

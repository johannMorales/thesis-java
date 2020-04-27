package pe.edu.pucp.johannmorales.thesis.connector.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.GeneticAlgorithmParameters;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.model.GeneticAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestLoadedData;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestPeriod;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProblem;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProcess;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProcessPeriod;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestProcessWorkAreaType;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.request.RequestWorkAreaType;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.Response;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.ResponsePeriod;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.ResponseProcess;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.ResponseWorkArea;
import pe.edu.pucp.johannmorales.thesis.connector.model.api.response.ResponseWorkAreaType;
import pe.edu.pucp.johannmorales.thesis.flp.FLP;
import pe.edu.pucp.johannmorales.thesis.flp.model.Period;
import pe.edu.pucp.johannmorales.thesis.flp.model.Process;
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

    int facilities = 0;
    for (Period period : periods) {
      for (WorkAreaType workAreaType : workAreaTypesByPeriod.get(period)) {
        facilities += workAreaType.getAmount();
      }
    }

    FLP flp = FLP.builder()
        .bitSizeX(8)
        .bitSizeY(8)
        .facilitiesNumber(facilities)
        .maxX(100)
        .maxY(100)
        .build();

    GeneticAlgorithmResult[] results = flp.runGenetic(GeneticAlgorithmParameters.builder()
        .generations(req.getGenetic().getGenerations())
        .random(new Random(18121997))
        .populationSize(req.getGenetic().getPopulation())
        .ratioMutation(req.getGenetic().getRatioMutation())
        .ratioRecombination(req.getGenetic().getRatioCrossover())
        .ratioSurvive(req.getGenetic().getRatioSurvive())
        .build());

    GeneticAlgorithmResult result = results[results.length - 1];

    System.out.println(result);
    List<Pair<Double, Double>> resultCoordinates = flp.decodeBitArray(result.getChromosome());

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
          responseWorkAreaType.setSolutionGenetic(new ArrayList<>());
          rp.getWorkAreaTypes().add(responseWorkAreaType);
          responseWorkAreaTypeMap.put(rwat.getId(), responseWorkAreaType);
          for (int i = 0; i < rwat.getAmount(); i++) {
            Pair<Double, Double> pair = resultCoordinates.get(pairCounter++);
            ResponseWorkArea rwa = new ResponseWorkArea();
            rwa.setX(pair.getLeft());
            rwa.setY(pair.getRight());
            responseWorkAreaType.getSolutionGenetic().add(rwa);
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

}

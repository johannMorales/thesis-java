package pe.edu.pucp.johannmorales.thesis.flp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.GeneticAlgorithm;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.GeneticAlgorithmParameters;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.model.GeneticAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.structures.BitArray;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.GreyWolfAlg;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.GreyWolfAlgorithmParameters;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.model.GreyWolfAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.flp.model.Period;
import pe.edu.pucp.johannmorales.thesis.flp.model.Process;
import pe.edu.pucp.johannmorales.thesis.flp.model.WorkArea;
import pe.edu.pucp.johannmorales.thesis.flp.model.WorkAreaType;
import pe.edu.pucp.johannmorales.thesis.flp.util.StrUtils;

@Slf4j
@Getter
@Setter
@Builder
public final class FLP {

  private Integer maxX;
  private Integer maxY;
  private Integer facilitiesNumber;
  private Integer bitSizeX;
  private Integer bitSizeY;
  private List<Period> periods;
  private Map<Period, List<WorkAreaType>> workAreaTypeByPeriod;

  public GreyWolfAlgorithmResult[] runGreyWolf(GreyWolfAlgorithmParameters parameters) {
    double[] lower = new double[facilitiesNumber * 2];
    double[] upper = new double[facilitiesNumber * 2];
    for (int i = 0; i < upper.length; i++) {
      if (i % 2 == 0) {
        upper[i] = getMaxY();
      } else {
        upper[i] = getMaxX();
      }
    }
    GreyWolfAlg greyWolfAlg = new GreyWolfAlg(list -> {
      List<Pair<Double, Double>> list2 = new ArrayList<>();
      for (int i = 0; i < list.length; i += 2) {
        list2.add(Pair.of(list[i], list[i + 1]));
      }

      return calculateFitness(list2, true);
    }, lower, upper, parameters.getIterations(), parameters.getPopulationSize());

    return greyWolfAlg.solution();
  }

  public GeneticAlgorithmResult[] runGenetic(GeneticAlgorithmParameters parameters) {
    GeneticAlgorithm ga = new GeneticAlgorithm(parameters,
        getChromosomeFacilityBitSize() * getFacilitiesNumber()) {
      @Override
      protected Double calculateFitness(BitArray bitArray) {
        return FLP.this.calculateFitness(decodeBitArray(bitArray), false);
      }

      @Override
      protected Collection<BitArray> mutation(List<BitArray> population) {
        LinkedList<BitArray> result = new LinkedList<>();
        for (int i = 0; i < getAmountMutation(); i++) {
          BitArray parent = population.get(getRandomProvider().nextInt(population.size()));
          BitArray mutant = parent.duplicate();
          int n = (int) (1) + getRandomProvider()
              .nextInt(getChromosomeBitSize() - 1);
          for (int c = 0; c < n; c++) {
            mutant = mutant.flip(getRandomProvider().nextInt(getChromosomeBitSize()));
          }

          if (isValidSolution(decodeBitArray(mutant))) {
            result.add(mutant);
          }
        }
        return result;
      }

      @Override
      protected Collection<BitArray> crossover(List<BitArray> population) {
        LinkedList<BitArray> result = new LinkedList<>();
        for (int i = 0; i < getAmountRecombination(); i++) {
          BitArray parent1 = population.get(getRandomProvider().nextInt(population.size()));
          BitArray parent2 = population.get(getRandomProvider().nextInt(population.size()));
          int c = 1 + getRandomProvider().nextInt(getChromosomeBitSize() - 2);
          BitArray son1 = parent1.setFrom(c, false).or(parent2.setTo(c, false));
          BitArray son2 = parent2.setFrom(c, false).or(parent1.setTo(c, false));
          if (FLP.this.isValidSolution(decodeBitArray(son1))) {
            result.add(son1);
          }
          if (FLP.this.isValidSolution(decodeBitArray(son2))) {
            result.add(son2);
          }
        }
        return result;
      }

      @Override
      protected List<BitArray> initializePopulation() {
        List<BitArray> result = new ArrayList<>();
        for (int i = 0; i < getPopulationSize(); i++) {
          StringBuilder sb = new StringBuilder();
          for (int n = 0; n < facilitiesNumber; n++) {
            long x = getRandomProvider().nextInt(maxX);
            long y = getRandomProvider().nextInt(maxY);
            sb.append(StringUtils.leftPad(Long.toBinaryString(x), bitSizeX, '0'));
            sb.append(StringUtils.leftPad(Long.toBinaryString(y), bitSizeY, '0'));
          }
          result.add(BitArray.create(sb.toString()));
        }
        return result;
      }
    };

    return ga.run();
  }

  public List<Pair<Double, Double>> decodeBitArray(BitArray bitArray) {
    List<Pair<Double, Double>> results = new ArrayList<>();
    for (String s : StrUtils.split(bitArray.toBinaryString(), getChromosomeFacilityBitSize())) {
      double x = (double) (fromBinaryString(StringUtils.left(s, bitSizeX)));
      double y = (double) (fromBinaryString(StringUtils.right(s, bitSizeY)));
      results.add(Pair.of(x, y));
    }
    return results;
  }

  private Long fromBinaryString(String str) {
    long result = 0L;
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == '1') {
        result += 1 << (str.length() - i - 1);
      }
    }
    return result;
  }

  private int getChromosomeFacilityBitSize() {
    return bitSizeX + bitSizeY;
  }

  private double calculateFitness(List<Pair<Double, Double>> list, boolean checkhardrestrictions) {
    double fitness = 0.0;
    Map<Period, Map<WorkAreaType, List<WorkArea>>> map = buildStructure(list);
    double mhc = calculateMaterialHandlingCost(map);
    double rc = calculateRelocationCost(map);

    if (checkhardrestrictions && !isValidSolutionWithStructure(map)) {
      return Double.MAX_VALUE;
    } else {
      return mhc * rc;
    }
  }

  private Map<Period, Map<WorkAreaType, List<WorkArea>>> buildStructure(
      List<Pair<Double, Double>> list) {
    Map<Period, Map<WorkAreaType, List<WorkArea>>> map = new HashMap<>();

    int listCounter = 0;
    for (Period period : periods) {
      Map<WorkAreaType, List<WorkArea>> mapThisPeriod = new HashMap<>();

      List<WorkArea> workAreasThisPeriod = new ArrayList<>();
      for (WorkAreaType workAreaType : workAreaTypeByPeriod.get(period)) {
        List<WorkArea> workAreasForMapThisType = new ArrayList<>();
        if (workAreaType.getIsStatic()) {
          workAreasThisPeriod.addAll(workAreaType.getStaticWorkAreas());
          workAreasForMapThisType.addAll(workAreaType.getStaticWorkAreas());
        } else {
          for (int i = 0; i < workAreaType.getAmount(); i++) {
            Pair<Double, Double> ddp = list.get(listCounter++);
            WorkArea workArea = new WorkArea();
            workArea.setX(ddp.getLeft());
            workArea.setY(ddp.getRight());
            workArea.setType(workAreaType);
            workAreasThisPeriod.add(workArea);
            workAreasForMapThisType.add(workArea);
          }
        }
        mapThisPeriod.put(workAreaType, workAreasForMapThisType);
      }
      map.put(period, mapThisPeriod);
    }
    return map;
  }

  private boolean findIfOverlap(List<WorkArea> workAreasThisPeriod) {
    for (int i = 0; i < workAreasThisPeriod.size(); i++) {
      WorkArea waA = workAreasThisPeriod.get(i);
      for (int i1 = i + 1; i1 < workAreasThisPeriod.size(); i1++) {
        WorkArea waB = workAreasThisPeriod.get(i1);
        if (doOverlap(waA, waB)) {
          return true;
        }
      }
    }
    return false;
  }

  private double calculateRelocationCost(Map<Period, Map<WorkAreaType, List<WorkArea>>> map) {
    double rc = 0.0;
    for (int i = 0; i < periods.size(); i++) {
      Period period1 = periods.get(i);
      for (int i1 = 0; i1 < workAreaTypeByPeriod.get(period1).size(); i1++) {
        WorkAreaType wat = workAreaTypeByPeriod.get(periods.get(i)).get(i1);
        for (int j = i + 1; j < periods.size(); j++) {
          Period period2 = periods.get(j);
          if (workAreaTypeByPeriod.get(period2).contains(wat)) {
            int amount = wat.getAmount();
            for (Integer n = 0; n < wat.getAmount(); n++) {
              WorkArea waA = map.get(period1).get(wat).get(n);
              WorkArea waB = map.get(period2).get(wat).get(n);
              double d = Math.abs(waA.getCenterX() - waB.getCenterX()) + Math
                  .abs(waA.getCenterY() + waB.getCenterY());
              rc += d * wat.getRc();
            }
          }
        }
      }
    }
    return rc;
  }

  private double calculateMaterialHandlingCost(Map<Period, Map<WorkAreaType, List<WorkArea>>> map) {
    double mhc = 0.0;
    for (Period period : periods) {
      for (Process process : period.getProcesses()) {
        for (int i = 0; i < process.getWorkAreasTypes().size(); i++) {
          WorkAreaType wat1 = process.getWorkAreasTypes().get(i);
          for (int i1 = i; i1 < process.getWorkAreasTypes().size(); i1++) {
            WorkAreaType wat2 = process.getWorkAreasTypes().get(i1);
            for (WorkArea waA : map.get(period).get(wat1)) {
              for (WorkArea waB : map.get(period).get(wat2)) {
                double d = Math.abs(waA.getCenterX() - waB.getCenterX()) + Math
                    .abs(waA.getCenterY() + waB.getCenterY());
                mhc += d * waA.getType().getMhc();
              }
            }
          }
        }
      }
    }
    return mhc;
  }

  private boolean doOverlap(WorkArea waA, WorkArea waB) {
    if (waA.getX() >= waB.getX() + waB.getType().getW() || waB.getX() >= waA.getX() + waA.getType()
        .getW()) {
      return false;
    }

    if (waA.getY() + waA.getType().getH() <= waB.getY() || waB.getY() + waB.getType().getH() <= waA
        .getY()) {
      return false;
    }

    return true;
  }

  public List<Pair<Double, Double>> decodeVector(double[] wolf) {
    List<Pair<Double, Double>> list = new ArrayList<>();
    for (Integer i = 0; i < wolf.length; i += 2) {
      list.add(Pair.of(wolf[i], wolf[i + 1]));
    }
    return list;
  }

  private boolean isValidSolution(List<Pair<Double, Double>> list) {
    Map<Period, Map<WorkAreaType, List<WorkArea>>> map = this.buildStructure(list);
    return isValidSolutionWithStructure(map);
  }

  private boolean isValidSolutionWithStructure(Map<Period, Map<WorkAreaType, List<WorkArea>>> map) {
    for (Entry<Period, Map<WorkAreaType, List<WorkArea>>> periodMapEntry : map.entrySet()) {
      List<WorkArea> workAreas = new ArrayList<>();
      for (List<WorkArea> ignored : periodMapEntry.getValue().values()) {
        workAreas.addAll(ignored);
      }
      if (findIfOverlap(workAreas)) {
        return false;
      }
    }
    return true;
  }
}

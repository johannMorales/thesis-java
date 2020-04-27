package pe.edu.pucp.johannmorales.thesis.flp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.GeneticAlgorithm;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.GeneticAlgorithmParameters;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.model.GeneticAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.structures.BitArray;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.GreyWolfAlgorithm;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.GreyWolfAlgorithmParameters;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.model.GreyWolfAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.structures.Vector;
import pe.edu.pucp.johannmorales.thesis.flp.util.StrUtils;

@Getter
@Setter
@Builder
public final class FLP {

  private Integer maxX;
  private Integer maxY;
  private Integer facilitiesNumber;
  private Integer bitSizeX;
  private Integer bitSizeY;

  public GreyWolfAlgorithmResult[] runGreyWolf(GreyWolfAlgorithmParameters parameters) {
    GreyWolfAlgorithm greyWolfAlgorithm = new GreyWolfAlgorithm(parameters) {
      @Override
      protected BigDecimal calculateFitness(Vector wolf) {
        return null;
      }

      @Override
      protected Vector[] initialize() {
        return new Vector[0];
      }
    };
    return greyWolfAlgorithm.run();
  }

  public GeneticAlgorithmResult[] runGenetic(GeneticAlgorithmParameters parameters) {
    GeneticAlgorithm ga = new GeneticAlgorithm(parameters,
        getChromosomeFacilityBitSize() * getFacilitiesNumber()) {
      @Override
      protected Double calculateFitness(BitArray bitArray) {
        return FLP.this.calculateFitness(decodeBitArray(bitArray));
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
          result.add(mutant);
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
          result.add(parent1.setFrom(c, false).or(parent2.setTo(c, false)));
          result.add(parent2.setFrom(c, false).or(parent1.setTo(c, false)));
        }
        return result;
      }

      @Override
      protected List<BitArray> initializePopulation() {
        List<BitArray> result = new ArrayList<>();
        for (int i = 0; i < getPopulationSize(); i++) {
          StringBuilder sb = new StringBuilder();
          for (int n = 0; n < facilitiesNumber; n++) {
//            long x = getRandomProvider().nextInt(maxX);
            long x = 0;
//            long y = getRandomProvider().nextInt(maxY);
            long y = 0;
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

  private double calculateFitness(List<Pair<Double, Double>> list) {
    double fitness = 0.0;
    double value = 5.0;
    for (Pair<Double, Double> pair : list) {
      value += 3.0;
      if (pair.getLeft() == value && pair.getRight() == value) {
        continue;
      }
      Double right = pair.getRight().equals(value)
          ? 1.0
          : Math.pow(pair.getRight() - value, 2);

      Double left = pair.getLeft().equals(value)
          ? 1.0
          : Math.pow(pair.getLeft() - value, 2);

      fitness = fitness + right * left;
    }
    return fitness;
  }

}

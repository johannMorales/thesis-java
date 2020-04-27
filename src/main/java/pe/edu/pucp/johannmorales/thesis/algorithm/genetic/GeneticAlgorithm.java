package pe.edu.pucp.johannmorales.thesis.algorithm.genetic;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.Getter;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.model.GeneticAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.algorithm.genetic.structures.BitArray;

@Getter
public abstract class GeneticAlgorithm {

  private Random randomProvider;
  private Integer chromosomeBitSize;
  private Integer amountRecombination;
  private Integer amountMutation;
  private Integer amountSurvive;
  private Integer populationSize;
  private Integer generations;

  public GeneticAlgorithm(GeneticAlgorithmParameters parameters, int bitsize) {
    amountSurvive = (int) Math
        .round(parameters.getPopulationSize() * parameters.getRatioSurvive());
    amountRecombination = (int) Math
        .round(parameters.getPopulationSize() * parameters.getRatioRecombination());
    amountMutation = (int) Math
        .round(parameters.getPopulationSize() * parameters.getRatioMutation());
    generations = parameters.getGenerations();
    chromosomeBitSize = bitsize;
    randomProvider = parameters.getRandom();
    populationSize = parameters.getPopulationSize();
  }

  public final GeneticAlgorithmResult[] run() {
    List<BitArray> population;
    Map<BitArray, BigDecimal> fitnessCache = new HashMap<>();

    GeneticAlgorithmResult[] results = new GeneticAlgorithmResult[generations];

    long initialTime = System.currentTimeMillis();

    population = initializePopulation();
    Comparator<BitArray> comparator = Comparator
        .comparing(c -> this.obtainFitness(c, fitnessCache));

    population.sort(comparator);

    for (int i = 1; i <= generations; i++) {
      population = population.subList(0, amountSurvive);
      population.addAll(crossover(population));
      population.addAll(mutation(population));
      population.sort(comparator);
      population = population.subList(0, Math.min(population.size(), populationSize));

      results[i - 1] = GeneticAlgorithmResult
          .builder()
          .fitness(obtainFitness(population.get(0), fitnessCache))
          .executionTime(System.currentTimeMillis() - initialTime)
          .iteration(i)
          .chromosome(population.get(0))
          .build();
    }

    return results;
  }

  private BigDecimal obtainFitness(BitArray chromosome, Map<BitArray, BigDecimal> cache) {
    if (cache.containsKey(chromosome)) {
      return cache.get(chromosome);
    } else {
      BigDecimal value = BigDecimal.valueOf(calculateFitness(chromosome));
      cache.put(chromosome, value);
      return value;
    }
  }

  protected abstract Double calculateFitness(BitArray bitArray);

  protected abstract Collection<BitArray> mutation(List<BitArray> population);

  protected abstract Collection<BitArray> crossover(List<BitArray> population);

  protected abstract List<BitArray> initializePopulation();

}

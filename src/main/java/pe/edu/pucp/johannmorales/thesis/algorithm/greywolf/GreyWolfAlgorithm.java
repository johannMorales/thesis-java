package pe.edu.pucp.johannmorales.thesis.algorithm.greywolf;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import lombok.Getter;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.model.GreyWolfAlgorithmResult;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.structures.Vector;

@Getter
public abstract class GreyWolfAlgorithm {

  private Integer dimensions;
  private Integer populationSize;
  private Integer iterations;
  private Random randomProvider;

  protected GreyWolfAlgorithm(final GreyWolfAlgorithmParameters parameters) {
    this.dimensions = parameters.getDimensions();
    this.iterations = parameters.getIterations();
    this.populationSize = parameters.getPopulationSize();
    this.randomProvider = parameters.getRandom();
  }

  public GreyWolfAlgorithmResult[] run() {
    final Map<Vector, BigDecimal> fitnessCache = new HashMap<>();
    final GreyWolfAlgorithmResult[] results = new GreyWolfAlgorithmResult[iterations];

    Vector[] pack = initialize();
    Arrays.sort(pack, Comparator.comparing(this::calculateFitness));

    Vector alpha = pack[0];
    Vector beta = pack[1];
    Vector delta = pack[2];

    long startTime = System.currentTimeMillis();
    for (int i = 1; i <= iterations; i++) {
      Vector a = obtainAMin(i);
      for (int wolf = 3; wolf < pack.length; wolf++) {
        pack[wolf] = updatePosition(pack[wolf], alpha, beta, delta, a);
      }
      Arrays.sort(pack, Comparator.comparing(this::calculateFitness));
      alpha = pack[0];
      beta = pack[1];
      delta = pack[2];
      results[i - 1] = GreyWolfAlgorithmResult.builder()
          .wolf(alpha)
          .fitness(this.calculateFitness(alpha))
          .executionTime(System.currentTimeMillis() - startTime)
          .iteration(i)
          .build();
    }

    return results;
  }

  private BigDecimal obtainFitness(Vector wolf, Map<Vector, BigDecimal> cache) {
    if (!cache.containsKey(wolf)) {
      cache.put(wolf, calculateFitness(wolf));
    }
    return cache.get(wolf);
  }

  protected abstract BigDecimal calculateFitness(Vector wolf);

  protected abstract Vector[] initialize();

  private Vector encirclePrey(Vector wolf, Vector prey, Vector A, Vector C) {
    Vector D = C.dotProduct(prey).subtract(wolf).abs();
    return D.subtract(A.dotProduct(C));
  }

  private Vector updatePosition(Vector wolf, Vector alpha, Vector beta, Vector delta, Vector a) {

    Vector A1 = obtainAMay(a);
    Vector C1 = obtainC();
    Vector X1 = encirclePrey(wolf, alpha, A1, C1);

    Vector A2 = obtainAMay(a);
    Vector C2 = obtainC();
    Vector X2 = encirclePrey(wolf, beta, A2, C2);

    Vector A3 = obtainAMay(a);
    Vector C3 = obtainC();
    Vector X3 = encirclePrey(wolf, delta, A3, C3);

    return Vector.addAll(X1, X2, X3).divideInPlace(3);
  }

  private Vector obtainAMin(Integer iteration) {
    double value = 2.0 - 2.0 / iterations * iteration;
    return Vector.createConstant(dimensions, BigDecimal.valueOf(value));
  }

  private Vector obtainAMay(Vector a) {
    Vector r = Vector.createRandom(dimensions, randomProvider);
    return Vector.createConstant(dimensions, BigDecimal.valueOf(2))
        .dotProduct(a)
        .dotProduct(r)
        .subtract(a);
  }

  private Vector obtainC() {
    return Vector.createConstant(dimensions, BigDecimal.valueOf(2))
        .dotProduct(Vector.createRandom(dimensions, randomProvider));
  }

}

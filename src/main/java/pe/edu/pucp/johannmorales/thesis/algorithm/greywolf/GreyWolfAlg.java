package pe.edu.pucp.johannmorales.thesis.algorithm.greywolf;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.model.GreyWolfAlgorithmResult;

@Slf4j
public class GreyWolfAlg {

  public interface f_xj {

    double func(double x[]);
  }


  double r1;
  double r2;
  int N;
  int D;
  int maxiter;
  double[] alfa;
  double[] beta;
  double[] delta;
  double[] Lower;
  double[] Upper;
  f_xj ff;
  double XX[][];
  double[] BESTVAL;
  double[] iterdep;
  double a[];
  double A1[];
  double C1[];
  double A2[];
  double C2[];
  double A3[];
  double C3[];

  public GreyWolfAlg(f_xj iff, double iLower[], double iUpper[], int imaxiter, int iN) {
    maxiter = imaxiter;
    ff = iff;
    Lower = iLower;
    Upper = iUpper;
    N = iN;
    D = Upper.length;
    a = new double[D];
    XX = new double[N][D];
    alfa = new double[D];
    beta = new double[D];
    delta = new double[D];
    A1 = new double[D];
    C1 = new double[D];
    A2 = new double[D];
    C2 = new double[D];
    A3 = new double[D];
    C3 = new double[D];
    BESTVAL = new double[maxiter];
    iterdep = new double[maxiter];

  }

  double[][] sort_and_index(double[][] XXX) {
    Arrays.sort(XXX, Comparator.comparing(list -> ff.func(list)));
    return XXX;
  }


  void init() {
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < D; j++) {
        XX[i][j] = Lower[j] + (Upper[j] - Lower[j]) * Math.random();
      }
    }

    XX = sort_and_index(XX);
    for (int i = 0; i < D; i++) {
      alfa[i] = XX[0][i];
      beta[i] = XX[1][i];
      delta[i] = XX[2][i];
    }

  }


  double[][] simplebounds(double s[][]) {
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < D; j++) {
        if (s[i][j] < Lower[j]) {
          s[i][j] = Lower[j] + ((Upper[j] - Lower[j]) * Math.random());
        }
        if (s[i][j] > Upper[j]) {
          s[i][j] = Lower[j] + ((Upper[j] - Lower[j]) * Math.random());
        }
      }
    }
    return s;
  }

  double[] simplebounds(double s[], int i) {
    if (s[i] < Lower[i]) {
      s[i] = Lower[i] + ((Upper[i] - Lower[i]) * Math.random());
    }
    if (s[i] > Upper[i]) {
      s[i] = Lower[i] + ((Upper[i] - Lower[i]) * Math.random());
    }
    return s;
  }


  public GreyWolfAlgorithmResult[] solution() {
    GreyWolfAlgorithmResult[] results = new GreyWolfAlgorithmResult[maxiter];
    init();
    Long time1 = System.currentTimeMillis();

    int iter = 1;
    while (iter < maxiter) {
      double a = 2.0 - ((double) iter * (2.0 / (double) maxiter));

      for (int i = 0; i < N; i++) {
        double[] X1 = new double[D];
        double[] X2 = new double[D];
        double[] X3 = new double[D];

        for (int j = 0; j < D; j++) {
          double r1 = Math.random();
          double r2 = Math.random();

          // ALpha influenc
          A1[j] = 2.0 * a * r1 - a;
          C1[j] = 2.0 * r2;

          X1[j] = alfa[j] - A1[j] * (Math.abs(C1[j] * alfa[j] - XX[i][j]));
          X1 = simplebounds(X1, j);
          r1 = Math.random();
          r2 = Math.random();

          // Beta influenc
          A2[j] = 2.0 * a * r1 - a;
          C2[j] = 2.0 * r2;

          X2[j] = beta[j] - A2[j] * (Math.abs(C2[j] * beta[j] - XX[i][j]));
          X2 = simplebounds(X2, j);
          r1 = Math.random();
          r2 = Math.random();

          // Delta influenc
          A3[j] = 2.0 * a * r1 - a;
          C3[j] = 2.0 * r2;

          X3[j] = delta[j] - A3[j] * (Math.abs(C3[j] * delta[j] - XX[i][j]));
          X3 = simplebounds(X3, j);

          XX[i][j] = (X1[j] + X2[j] + X3[j]) / 3.0;

        }
      }
      XX = simplebounds(XX);
      XX = sort_and_index(XX);

      System.arraycopy(XX[0], 0, alfa, 0, D);
      System.arraycopy(XX[1], 0, beta, 0, D);
      System.arraycopy(XX[2], 0, delta, 0, D);

      BESTVAL[iter] = ff.func(XX[0]);

      results[iter] = GreyWolfAlgorithmResult.builder()
          .iteration(iter)
          .executionTime(System.currentTimeMillis() - time1)
          .fitness(BigDecimal.valueOf(ff.func(alfa)))
          .wolf(alfa)
          .build();
      iter++;
    }

    return results;
  }

}
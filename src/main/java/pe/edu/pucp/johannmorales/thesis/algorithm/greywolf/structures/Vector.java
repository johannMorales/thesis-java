package pe.edu.pucp.johannmorales.thesis.algorithm.greywolf.structures;

import java.util.Arrays;
import java.util.Random;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;


public class Vector {

  private static int DECIMALS = 10;

  @Getter
  private Integer dimension;
  private Boolean isConstant;
  private Double constantValue;
  private Double[] components;

  public static Vector createConstant(Integer dimensions, Double value) {
    Vector vector = new Vector();
    vector.dimension = dimensions;
    vector.isConstant = true;
    vector.constantValue = value;
    return vector;
  }

  public static Vector create(Integer dimensions) {
    Vector vector = new Vector();
    vector.dimension = dimensions;
    vector.isConstant = false;
    vector.constantValue = null;
    vector.components = new Double[dimensions];
    Arrays.fill(vector.components, 0.0);
    return vector;
  }

  public static Vector createRandom(Integer dimension, Random random) {
    Vector vector = new Vector();
    vector.dimension = dimension;
    vector.isConstant = false;
    vector.constantValue = null;
    vector.components = new Double[dimension];
    Double value = random.nextDouble();
    Arrays.fill(vector.components, value);
    return vector;
  }

  public void set(Integer dimension, Double value) {
    if (dimension >= this.dimension || dimension < 0) {
      throw new IndexOutOfBoundsException();
    }
    checkModifyable();
    this.components[dimension] = value;
  }

  public Double get(Integer dimension) {
    if (dimension >= this.dimension || dimension < 0) {
      throw new IndexOutOfBoundsException();
    }
    if (this.isConstant) {
      return this.constantValue;
    } else {
      return this.components[dimension];
    }
  }

  public Vector dotProduct(Vector other) {
    if (this.isConstant && other.isConstant) {
      return Vector.createConstant(dimension, this.constantValue * (other.constantValue));
    } else {
      Vector result = Vector.create(dimension);
      result.isConstant = false;
      result.constantValue = null;
      for (Integer i = 0; i < dimension; i++) {
        result.set(i, this.get(i) * other.get(i));
      }
      return result;
    }
  }

  public Vector abs() {
    if (this.isConstant) {
      return Vector.createConstant(dimension, Math.abs(this.constantValue));
    } else {
      Vector result = Vector.create(dimension);
      for (Integer i = 0; i < dimension; i++) {
        result.set(i, Math.abs(this.get(i)));
      }
      return result;
    }
  }

  private void checkModifyable() {
    if (this.isConstant) {
      throw new UnsupportedOperationException("Cant modify constant vector");
    }
  }

  public static Vector addAll(Vector... vectors) {
    int size = vectors[0].getDimension();
    Vector result = new Vector();
    result.isConstant = false;
    result.components = new Double[size];
    result.constantValue = null;
    result.dimension = size;

    for (int i = 0; i < size; i++) {
      Double sum = 0.0;
      for (int j = 0; j < vectors.length; j++) {
        sum = sum + (vectors[j].components[i]);
      }
      result.components[i] = sum;
    }
    return result;
  }

  public Vector divideInPlace(Integer divide) {
    for (int i = 0; i < this.components.length; i++) {
      components[i] = components[i]
          / (Double.valueOf(divide));
    }
    return this;
  }

  public Vector subtract(final Vector other) {
    Vector result = this.duplicate();
    result.minusInPlace(other);
    return result;
  }

  public void minusInPlace(final Vector other) {
    if (this.isConstant) {
      throw new UnsupportedOperationException();
    } else {
      for (int i = 0; i < this.dimension; i++) {
        components[i] = components[i]
            - (other.isConstant ? other.constantValue : other.components[i]);
      }
    }
  }

  public Vector multiply(int i) {
    Vector result = this.duplicate();
    result.multiplyInPlace(i);
    return result;
  }

  private Vector duplicate() {
    if (this.isConstant) {
      return Vector.createConstant(dimension, constantValue);
    } else {
      Vector v = new Vector();
      v.dimension = dimension;
      v.isConstant = false;
      v.components = new Double[dimension];
      System.arraycopy(components, 0, v.components, 0, v.components.length);
      return v;
    }
  }

  public void multiplyInPlace(int i) {
    if (isConstant) {
      constantValue = constantValue * ((double) i);
    } else {
      for (int integer = 0; integer < dimension; integer++) {
        components[i] = components[i] * ((double) i);
      }
    }
  }

  @Override
  public String toString() {
    return "Vector(" + StringUtils.join(components, "; ") + ")";
  }
}

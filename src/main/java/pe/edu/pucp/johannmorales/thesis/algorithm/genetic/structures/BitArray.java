package pe.edu.pucp.johannmorales.thesis.algorithm.genetic.structures;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public class BitArray {

  private static int BLOCK_BIT_SIZE = 64;
  private Long[] blocks;
  private Integer size;

  public static BitArray create(String value) {
    int len = value.length();
    BitArray bitArray = new BitArray();
    bitArray.blocks = new Long[len / BLOCK_BIT_SIZE + (len % BLOCK_BIT_SIZE == 0 ? 0 : 1)];
    Arrays.fill(bitArray.blocks, 0L);
    for (int i = len - 1; i >= 0; i--) {
      if (value.charAt(i) == '1') {
        bitArray = bitArray.set(value.length() - i - 1);
      }
    }
    bitArray.size = value.length();
    return bitArray;
  }

  public static BitArray create(Integer size) {
    BitArray bitArray = new BitArray();
    bitArray.blocks = new Long[size / BLOCK_BIT_SIZE + (size % BLOCK_BIT_SIZE == 0 ? 0 : 1)];
    Arrays.fill(bitArray.blocks, 0L);
    bitArray.size = size;
    return bitArray;
  }

  public static BitArray createMax(int size) {
    BitArray bitArray = new BitArray();
    bitArray.blocks = new Long[size / BLOCK_BIT_SIZE + (size % BLOCK_BIT_SIZE == 0 ? 0 : 1)];
    Arrays.fill(bitArray.blocks, ~0L);
    bitArray.size = size;
    return bitArray;
  }

  public BitArray duplicate() {
    BitArray result = new BitArray();
    result.blocks = new Long[blocks.length];
    System.arraycopy(blocks, 0, result.blocks, 0, blocks.length);
    result.size = size;
    return result;
  }

  public BitArray flip(Integer position) {
    BitArray result = this.duplicate();
    int blockModified = position / BLOCK_BIT_SIZE;
    int bitModified = position % BLOCK_BIT_SIZE;
    result.blocks[blockModified] = result.blocks[blockModified] ^ (1L << bitModified);
    return result;
  }

  public Boolean get(Integer position) {
    int blockModified = position / BLOCK_BIT_SIZE;
    int bitModified = position % BLOCK_BIT_SIZE;
    return (this.blocks[blockModified] >> bitModified & 1L) == 1L;
  }

  public BitArray set(Integer position) {
    BitArray result = this.duplicate();
    result.setInPlace(position);
    return result;
  }

  public void setInPlace(Integer position) {
    int block = position / BLOCK_BIT_SIZE;
    int bitModified = position % BLOCK_BIT_SIZE;
    this.blocks[block] = this.blocks[block] | (1L << bitModified);
  }

  public BitArray set(Integer position, Boolean value) {
    BitArray result = this.duplicate();
    int blockModified = position / BLOCK_BIT_SIZE;
    int bitModified = position % BLOCK_BIT_SIZE;
    if (value) {
      result.blocks[blockModified] = result.blocks[blockModified] | (1L << bitModified);
    } else {
      result.blocks[blockModified] = result.blocks[blockModified] & ~(1L << bitModified);
    }
    return result;
  }

  public BitArray setTo(Integer position, Boolean value) {
    BitArray result = this.duplicate();
    if (position == 0) {
      return result;
    }
    int block = position / BLOCK_BIT_SIZE;
    int bit = position % BLOCK_BIT_SIZE;
    Long mask = ~0L << (BLOCK_BIT_SIZE - bit) >>> (BLOCK_BIT_SIZE - bit);

    if (!value) {
      if (position % BLOCK_BIT_SIZE == 0 && position > 0) {
        result.blocks[block] = ~0L;
      } else {
        result.blocks[block] = result.blocks[block] & ~mask;
      }
      for (int i = 0; i < block; i++) {
        result.blocks[i] = 0L;
      }
    } else {
      result.blocks[block] = result.blocks[block] | mask;
      for (int i = 0; i < block; i++) {
        result.blocks[i] = ~0L;
      }
    }
    return result;
  }

  public BitArray setFrom(Integer position, Boolean value) {
    BitArray r = this.duplicate();

    int block = position / BLOCK_BIT_SIZE;
    int bit = position % BLOCK_BIT_SIZE;
    Long mask = ~0L >>> bit << bit;

    if (value) {
      r.blocks[block] = r.blocks[block] | mask;
      for (int i = block + 1; i < blocks.length; i++) {
        r.blocks[i] = ~0L;
      }
      int shift = BLOCK_BIT_SIZE - size % BLOCK_BIT_SIZE;
      r.blocks[blocks.length - 1] = r.blocks[blocks.length - 1] & (~0L << shift >>> shift);
    } else {
      r.blocks[block] = r.blocks[block] & ~mask;
      for (int i = block + 1; i < blocks.length; i++) {
        r.blocks[i] = 0L;
      }
    }

    return r;
  }

  public BitArray or(final BitArray other) {
    BitArray result = BitArray.create(this.size);
    for (int i = 0; i < blocks.length; i++) {
      result.blocks[i] = this.blocks[i] | other.blocks[i];
    }
    return result;
  }

  public BitArray and(final BitArray other) {
    BitArray result = BitArray.create(this.size);
    for (int i = 0; i < blocks.length; i++) {
      result.blocks[i] = this.blocks[i] & other.blocks[i];
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Long bit : blocks) {
      sb.insert(0, StringUtils.leftPad(Long.toBinaryString(bit), BLOCK_BIT_SIZE, '0'));
    }
    return "BitArray[" + StringUtils.right(sb.toString(), size) + ']';
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BitArray) {
      BitArray other = (BitArray) obj;
      for (int i = 0; i < this.blocks.length; i++) {
        if (!this.blocks[i].equals(other.blocks[i])) {
          return false;
        }
      }
      return true;
    }
    return super.equals(obj);
  }

  public String toBinaryString() {
    StringBuilder sb = new StringBuilder();
    for (Long bit : blocks) {
      sb.insert(0, StringUtils.leftPad(Long.toBinaryString(bit), BLOCK_BIT_SIZE, '0'));
    }
    return StringUtils.right(sb.toString(), size);
  }

}

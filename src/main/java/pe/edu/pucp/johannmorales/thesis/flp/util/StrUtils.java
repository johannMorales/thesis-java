package pe.edu.pucp.johannmorales.thesis.flp.util;

import org.apache.commons.lang3.StringUtils;

public class StrUtils {

  public static String[] split(String text, int size) {
    int amount = text.length() / size + (text.length() % size == 0 ? 0 : 1);
    String[] result = new String[amount];
    for (int i = 0; i < amount; i++) {
      result[i] = StringUtils.substring(text, size * i, size * i + size);
    }

    return result;
  }

}

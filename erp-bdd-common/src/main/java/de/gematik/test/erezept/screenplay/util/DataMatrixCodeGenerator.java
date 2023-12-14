/*
 * Copyright 2023 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.screenplay.util;

import static java.text.MessageFormat.format;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.DataMatrixWriter;
import de.gematik.test.erezept.fhir.values.AccessCode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class DataMatrixCodeGenerator {

  private static final int WIDTH = 500;
  private static final int HEIGHT = WIDTH;

  private DataMatrixCodeGenerator() {
    throw new AssertionError();
  }

  @SneakyThrows
  public static ByteArrayOutputStream writeToStream(String taskId, AccessCode accessCode) {
    val bufferedImage = getBufferedImage(taskId, accessCode);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", os);
    os.flush();
    os.close();
    return os;
  }

  public static void writeToFile(String taskId, AccessCode accessCode, String filePath) {
    val dmc = generateDmc(taskId, accessCode);
    writeToFile(dmc, filePath);
  }

  public static void writeToFile(String taskId, AccessCode accessCode, File file) {
    val dmc = generateDmc(taskId, accessCode);
    writeToFile(dmc, file);
  }

  public static void writeToFile(BitMatrix dmc, String filePath) {
    writeToFile(dmc, new File(filePath));
  }

  @SneakyThrows
  public static void writeToFile(BitMatrix dmc, File file) {
    val bufferedImage = getBufferedImage(dmc);
    if (file.mkdirs()) {
      ImageIO.write(bufferedImage, "png", file);
    } else {
      throw new IOException(format("Unable to write File {0} ", file.getAbsolutePath()));
    }
  }

  public static BitMatrix generateDmc(String taskId, AccessCode accessCode) {
    val taskReference = format("Task/{0}/$accept?ac={1}", taskId, accessCode.getValue());
    val inner = new JsonArray();
    inner.add(taskReference);
    val obj = new JsonObject();
    obj.add("urls", inner);
    val content = new Gson().toJson(obj);

    log.info(content);
    Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
    hints.put(EncodeHintType.GS1_FORMAT, "true");

    val writer = new DataMatrixWriter();
    return writer.encode(content, BarcodeFormat.DATA_MATRIX, WIDTH, HEIGHT, hints);
  }

  public static BufferedImage getBufferedImage(String taskId, AccessCode accessCode) {
    val dmc = generateDmc(taskId, accessCode);
    return getBufferedImage(dmc);
  }

  public static BufferedImage getBufferedImage(BitMatrix dmc) {
    return MatrixToImageWriter.toBufferedImage(dmc);
  }
}

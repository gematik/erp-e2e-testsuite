/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.screenplay.util;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.io.IOException;
import java.nio.file.Path;
import lombok.val;
import org.junit.jupiter.api.Test;

class DataMatrixCodeGeneratorTest {

  @Test
  void createDataMatrixCode() {
    val prescriptionId = PrescriptionId.random().getValue();
    val ac = AccessCode.random();

    val out = DataMatrixCodeGenerator.writeToStream(prescriptionId, ac);
    assertNotNull(out);
    assertTrue(out.toByteArray().length > 0);
  }

  @Test
  void writeDataMatrixCode() {
    val prescriptionId = PrescriptionId.random().getValue();
    val ac = AccessCode.random();
    val filePath = Path.of("target", "dmcs", format("dmc_{0}.png", prescriptionId));

    assertFalse(filePath.toFile().exists());
    DataMatrixCodeGenerator.writeToFile(prescriptionId, ac, filePath.toFile());
    assertTrue(filePath.toFile().exists());
  }

  @Test
  void shouldWriteAndReadDataMatrixCode() throws NotFoundException {
    val prescriptionId = PrescriptionId.random().getValue();
    val ac = AccessCode.random();

    val dmcImage = DataMatrixCodeGenerator.getBufferedImage(prescriptionId, ac);

    val tmpSource = new BufferedImageLuminanceSource(dmcImage);
    val tmpBitmap = new BinaryBitmap(new HybridBinarizer(tmpSource));
    val tmpBarcodeReader = new MultiFormatReader();

    val expected = format("{0}/$accept?ac={1}", prescriptionId, ac.getValue());
    val result = tmpBarcodeReader.decode(tmpBitmap);
    assertTrue(result.getText().contains(expected));
  }

  @Test
  void shouldSneakilyThrowOnWritingError() {
    try (val ms =
        mockConstruction(
            ObjectMapper.class,
            (mock, context) ->
                when(mock.writeValueAsString(any()))
                    .thenThrow(new RuntimeException("testing error")))) {
      val prescriptionId = PrescriptionId.random().getValue();
      val ac = AccessCode.random();

      assertThrows(
          RuntimeException.class,
          () -> DataMatrixCodeGenerator.getBufferedImage(prescriptionId, ac));
    }
  }

  @Test
  void shouldNotOverwriteDataMatrixCode() {
    val prescriptionId = PrescriptionId.random().getValue();
    val ac = AccessCode.random();

    val filePath = Path.of("target", "dmcs", format("dmc_{0}.png", prescriptionId));
    assertDoesNotThrow(
        () -> DataMatrixCodeGenerator.writeToFile(prescriptionId, ac, filePath.toString()));
    assertThrows(
        IOException.class,
        () -> DataMatrixCodeGenerator.writeToFile(prescriptionId, ac, filePath.toString()));
  }

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(DataMatrixCodeGenerator.class));
  }
}

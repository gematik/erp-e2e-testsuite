package de.gematik.test.erezept.primsys.data;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

  class PrescribeRequestDtoTest {
  @Test
    void shouldReturnPrescribeRequestDtoBuilder(){

      val builder = PrescribeRequestDto.forKvnr("X110407071");
      assertNotNull(builder);
  }
  @Test
    void shouldBuildPrescribeRequestDto(){
      val builder = PrescribeRequestDto.forKvnr("X110407071");
      val coverageDto = mock(CoverageDto.class);
      val medication = mock(PznMedicationDto.class);
      val medicationRequest = mock(MedicationRequestDto.class);

      val dto = builder.coveredBy(coverageDto).medication(medication).medicationRequest(medicationRequest).build();
      assertNotNull(dto);
  }
}

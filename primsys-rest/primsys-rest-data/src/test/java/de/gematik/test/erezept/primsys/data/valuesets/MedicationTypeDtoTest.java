package de.gematik.test.erezept.primsys.data.valuesets;

import de.gematik.test.erezept.primsys.exceptions.InvalidCodeValueException;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MedicationTypeDtoTest {
    @Test
    void shouldDecodeFromCode(){
        val result = MedicationTypeDto.fromCode("PZN");
        val result2 = MedicationTypeDto.fromCode("INGREDIENT");
        assertEquals(MedicationTypeDto.PZN, result);
        assertEquals(MedicationTypeDto.INGREDIENT, result2);
    }
    @Test
    void shouldThrowOnInvalidCode(){
        assertThrows(InvalidCodeValueException.class, ()-> MedicationTypeDto.fromCode("invalidCode"));
    }
    @Test
    void shouldThrowOnNullCode(){
        assertThrows(NullPointerException.class, ()-> MedicationTypeDto.fromCode(null));
    }
}

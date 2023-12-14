package de.gematik.test.erezept.primsys.data.valuesets;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

 class StandardSizeDtoTest {
    @Test
    void shouldDecodeFromCode(){
        val result = StandardSizeDto.fromCode("KTP");
        val result2 = StandardSizeDto.fromCode("Normgröße 1");
        val result3 = StandardSizeDto.fromCode("SONSTIGES");
        assertEquals(StandardSizeDto.KTP, result);
        assertEquals(StandardSizeDto.N1, result2);
        assertEquals(StandardSizeDto.SONSTIGES, result3);
    }
    @Test
    void shouldThrowOnNullCode(){
        assertThrows(NullPointerException.class, ()-> StandardSizeDto.fromCode(null));
    }
}

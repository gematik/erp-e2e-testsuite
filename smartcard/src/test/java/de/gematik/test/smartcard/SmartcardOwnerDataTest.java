package de.gematik.test.smartcard;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SmartcardOwnerDataTest {
    private static SmartcardArchive sca;

    @BeforeAll
    static void setup() {
        sca = SmartcardFactory.getArchive();
    }
    @Test
    void shouldReturnOwnerName(){
        val hbaRsa = sca.getHbaCards().get(0);
        String expected = hbaRsa.getOwner().getOwnerName();
        assertEquals(expected, hbaRsa.getOwner().toString());
    }
    @Test
    void shouldReturnOwnerNameWithTitle(){
        val mockOwner = SmartcardOwnerData.builder().givenName("Bernd").surname("Claudius").title("Dr.").build();
        assertEquals("Dr. Bernd, Claudius", mockOwner.getOwnerName());
    }

}

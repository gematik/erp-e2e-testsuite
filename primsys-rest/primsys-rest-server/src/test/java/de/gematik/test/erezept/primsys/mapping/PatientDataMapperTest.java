package de.gematik.test.erezept.primsys.mapping;
import lombok.val;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class PatientDataMapperTest {

    @Test
    void shouldCreateRandomDto(){
        val dto = PatientDataMapper.randomDto();
        assertNotNull(dto.getKvnr());
        assertNotNull(dto.getFirstName());
        assertNotNull(dto.getLastName());
        assertNotNull(dto.getBirthDate());
        assertNotNull(dto.getCity());
        assertNotNull(dto.getPostal());
        assertNotNull(dto.getStreet());
    }

}

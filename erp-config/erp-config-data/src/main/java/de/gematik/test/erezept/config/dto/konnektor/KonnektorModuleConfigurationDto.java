package de.gematik.test.erezept.config.dto.konnektor;

import de.gematik.test.erezept.config.dto.BaseConfigurationDto;
import lombok.Data;

import java.util.List;

/** This class is intended to be used only if the konnektor-client module is used separately */
@Data
public class KonnektorModuleConfigurationDto implements BaseConfigurationDto {

    private List<KonnektorConfiguration> konnektors;
}

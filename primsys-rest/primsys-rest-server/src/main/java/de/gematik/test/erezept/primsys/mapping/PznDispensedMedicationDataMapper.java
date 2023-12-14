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

package de.gematik.test.erezept.primsys.mapping;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PznMedicationBatchDto;
import lombok.val;

import java.util.Date;

public class PznDispensedMedicationDataMapper extends DataMapper<PznDispensedMedicationDto, ErxMedicationDispense> {

    private final KVNR forKvnr;
    private final PrescriptionId forPrescription;
    private final String performerTelematikId;

    public PznDispensedMedicationDataMapper(PznDispensedMedicationDto dto, KVNR forKvnr, PrescriptionId forPrescription, String performerTelematikId)  {
        super(dto);
        this.forKvnr = forKvnr;
        this.forPrescription = forPrescription;
        this.performerTelematikId = performerTelematikId;
    }
    
    @Override
    protected void complete() {
        // simply reuse the PznMedicationDataMapper here!
        PznMedicationDataMapper.from(dto);
        ensure(dto::getBatch, dto::setBatch, PznMedicationBatchDto::new);
        
        val batch = dto.getBatch();
        ensure(batch::getLotNumber, batch::setLotNumber, GemFaker::fakerLotNumber);
        ensure(batch::getExpiryDate, batch::setExpiryDate, GemFaker::fakerFutureExpirationDate);
    }

    @Override
    protected ErxMedicationDispense convertInternal() {
    return ErxMedicationDispenseBuilder.forKvnr(forKvnr)
        .performerId(performerTelematikId)
        .prescriptionId(forPrescription)
        .medication(PznMedicationDataMapper.from(dto).convert())
        .status("completed")
        .whenPrepared(new Date())
        .batch(dto.getBatch().getLotNumber(), dto.getBatch().getExpiryDate())
        .build();
    }

    public static PznDispensedMedicationDataMapper from(PznDispensedMedicationDto dto, KVNR forKvnr, PrescriptionId forPrescription, String performerTelematikId) {
        return new PznDispensedMedicationDataMapper(dto, forKvnr, forPrescription, performerTelematikId);
    }

    public static PznDispensedMedicationDataMapper random() {
        val dto = randomDto();
        return new PznDispensedMedicationDataMapper(dto, KVNR.random(), PrescriptionId.random(), GemFaker.fakerTelematikId());
    }

    public static PznDispensedMedicationDto randomDto() {
        return new PznDispensedMedicationDto();
    }
}

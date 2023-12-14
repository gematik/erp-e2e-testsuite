package de.gematik.test.erezept.client.usecases;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class AuditEventGetByIdCommand extends BaseCommand<ErxAuditEventBundle> {

  public AuditEventGetByIdCommand(PrescriptionId prescriptionId) {
    super(ErxAuditEventBundle.class, HttpRequestMethod.GET, "Task", prescriptionId.getValue());
    queryParameters.add(new QueryParameter("_revinclude", "AuditEvent:entity.what"));
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}

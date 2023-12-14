package de.gematik.test.erezept.primsys;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.konnektor.cfg.KonnektorFactory;
import de.gematik.test.smartcard.Algorithm;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.stubbing.Answer;

public abstract class TestWithActorContext {

  private static final Random rnd = new Random();
  private static final SmartcardArchive sca = SmartcardFactory.getArchive();
  protected static final FhirParser fhir = new FhirParser();

  @BeforeEach
  void setUp() {
    resetSingleton(ActorContext.class, "instance");
    this.initDefaultContextMock();
  }

  /**
   * As Configuration is a Singleton, we need to reset the instance after each testcase!
   *
   * @param clazz
   * @param fieldName
   */
  @SneakyThrows
  protected void resetSingleton(Class<?> clazz, String fieldName) {
    Field instance;
    instance = clazz.getDeclaredField(fieldName);
    instance.setAccessible(true);
    instance.set(null, null);
  }

  public void initDefaultContextMock() {
    val configDto = ConfigurationReader.forPrimSysConfiguration().create();
    val factory = spy(PrimSysRestFactory.fromDto(configDto, sca));

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClient);
      when(erpClient.getFhir()).thenReturn(fhir);
      when(erpClient.encode(any(), any()))
          .thenAnswer(
              (Answer<String>)
                  invocationOnMock -> {
                    val args = invocationOnMock.getArguments();
                    return fhir.encode((IBaseResource) args[0], (EncodingType) args[1]);
                  });

      val mockedDoctors = this.createDoctorActors(configDto);
      val mockedPharmacies = this.createPharmacyActors(configDto);
      when(factory.createDoctorActors()).thenReturn(mockedDoctors);
      when(factory.createPharmacyActors()).thenReturn(mockedPharmacies);

      ActorContext.init(factory);
    }
  }

  public List<Doctor> createDoctorActors(PrimsysConfigurationDto dto) {
    return dto.getActors().getDoctors().stream().map(this::createMockDoctor).toList();
  }

  public List<Pharmacy> createPharmacyActors(PrimsysConfigurationDto dto) {
    return dto.getActors().getPharmacies().stream().map(this::createMockedPharmacy).toList();
  }

  public Doctor createMockDoctor(DoctorConfiguration cfg) {
      val env = createActiveEnvironment();
      cfg.setAlgorithm(Algorithm.RSA_2048.getAlgorithmName());
      val softKonn = KonnektorFactory.createSoftKon();
      return spy(new Doctor(cfg, env, softKonn, sca));
  }

  public Pharmacy createMockedPharmacy(PharmacyConfiguration cfg) {
      val env = createActiveEnvironment();
      cfg.setAlgorithm(Algorithm.RSA_2048.getAlgorithmName());
      val softKonn = KonnektorFactory.createSoftKon();
      return spy(new Pharmacy(cfg, env, softKonn, sca));
  }
  
  public EnvironmentConfiguration createActiveEnvironment() {
    val activeEnvironment = new EnvironmentConfiguration();
    activeEnvironment.setName("Unit-Test Mock");
    activeEnvironment.setTslBaseUrl("https://download-ref.tsl.ti-dienste.de/ECC/");

    val ti = new BackendRouteConfiguration();
    ti.setDiscoveryDocumentUrl(
        "http://127.0.0.1:8590/auth/realms/idp/.well-known/openid-configuration");
    ti.setFdBaseUrl("http://127.0.0.1:3000");
    ti.setSubscriptionServiceUrl("wss://127.0.0.1:3000/subscription");
    ti.setUserAgent("eRp-Testsuite");
    activeEnvironment.setTi(ti);

    val internet = new BackendRouteConfiguration();
    internet.setDiscoveryDocumentUrl(
        "http://127.0.0.1:8590/auth/realms/idp/.well-known/openid-configuration");
    internet.setFdBaseUrl("http://127.0.0.1:3000");
    internet.setSubscriptionServiceUrl("wss://127.0.0.1:3000/subscription");
    internet.setUserAgent("eRp-Testsuite");
    internet.setXapiKey("xapikey");
    activeEnvironment.setTi(internet);

    return activeEnvironment;
  }
}

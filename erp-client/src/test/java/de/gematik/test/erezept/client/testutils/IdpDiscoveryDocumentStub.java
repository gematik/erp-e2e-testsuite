/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.client.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.BC;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

/**
 * Mocks the IDP endpoints required by {@code IdpClient.initialize()}: the signed discovery document
 * and the two public-key endpoints it references. The actual OAuth2 flow is <b>not</b> mocked.
 */
@Getter
public class IdpDiscoveryDocumentStub {

  public static final String DISCOVERY_DOCUMENT_PATH = "/.well-known/openid-configuration";
  public static final String PUK_IDP_SIG_PATH = "/idpSig/jwk.json";
  public static final String PUK_IDP_ENC_PATH = "/idpEnc/jwk.json";
  public static final String AUTHORIZATION_PATH = "/sign_response";
  public static final String TOKEN_PATH = "/token";
  public static final String SSO_PATH = "/sso_response";

  private final String baseUrl;
  private final String discoveryDocument;
  private final String pukIdpSig;
  private final String pukIdpEnc;

  @SneakyThrows
  public IdpDiscoveryDocumentStub(String baseUrl) {
    this.baseUrl = baseUrl;

    // the discovery document is signed with P-256 (ES256) to avoid registering brainpool with
    // jose4j
    val sigKeyPair = generateKeyPair("secp256r1");
    val sigCertificate =
        VauCertificateGenerator.generateX509Certificate(
            sigKeyPair.getPrivate(), sigKeyPair.getPublic());

    // the IdpClient expects brainpoolP256r1 for the encryption key
    val encKeyPair = generateKeyPair("brainpoolP256r1");

    val claims = new JwtClaims();
    claims.setClaim("authorization_endpoint", baseUrl + AUTHORIZATION_PATH);
    claims.setClaim("token_endpoint", baseUrl + TOKEN_PATH);
    claims.setClaim("sso_endpoint", baseUrl + SSO_PATH);
    claims.setClaim("uri_puk_idp_sig", baseUrl + PUK_IDP_SIG_PATH);
    claims.setClaim("uri_puk_idp_enc", baseUrl + PUK_IDP_ENC_PATH);
    claims.setIssuer(baseUrl);
    claims.setIssuedAtToNow();
    claims.setExpirationTimeMinutesInTheFuture(60 * 24);

    val jws = new JsonWebSignature();
    jws.setPayload(claims.toJson());
    jws.setKey(sigKeyPair.getPrivate());
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
    jws.setCertificateChainHeaderValue(sigCertificate);
    this.discoveryDocument = jws.getCompactSerialization();

    this.pukIdpSig =
        format(
            "'{'\"x5c\":[\"{0}\"],\"kid\":\"puk_idp_sig\",\"use\":\"sig\",\"kty\":\"EC\",\"crv\":\"BP-256\"'}'",
            Base64.getEncoder().encodeToString(sigCertificate.getEncoded()));

    val encPublicKey = (ECPublicKey) encKeyPair.getPublic();
    this.pukIdpEnc =
        format(
            "'{'\"kid\":\"puk_idp_enc\",\"use\":\"enc\",\"kty\":\"EC\",\"crv\":\"BP-256\",\"x\":\"{0}\",\"y\":\"{1}\"'}'",
            encodeCoordinate(encPublicKey.getW().getAffineX()),
            encodeCoordinate(encPublicKey.getW().getAffineY()));
  }

  public String getDiscoveryDocumentUrl() {
    return baseUrl + DISCOVERY_DOCUMENT_PATH;
  }

  /** Registers the stubs on the currently configured static WireMock instance. */
  public void register() {
    stubFor(
        get(urlPathEqualTo(DISCOVERY_DOCUMENT_PATH))
            .willReturn(aResponse().withStatus(200).withBody(discoveryDocument)));
    stubFor(
        get(urlPathEqualTo(PUK_IDP_SIG_PATH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(pukIdpSig)));
    stubFor(
        get(urlPathEqualTo(PUK_IDP_ENC_PATH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(pukIdpEnc)));
  }

  @SneakyThrows
  private static KeyPair generateKeyPair(String curve) {
    val generator = KeyPairGenerator.getInstance("EC", BC.getSecurityProvider());
    generator.initialize(new java.security.spec.ECGenParameterSpec(curve));
    return generator.generateKeyPair();
  }

  // EcKeyUtility.genECPublicKey decodes with a signed BigInteger, so keep the sign byte
  private static String encodeCoordinate(BigInteger coordinate) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(coordinate.toByteArray());
  }
}

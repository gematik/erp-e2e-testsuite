package de.gematik.test.erezept.client.testutils;

import de.gematik.test.erezept.client.vau.protocol.VauVersion;
import de.gematik.test.erezept.crypto.BC;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

public class VauCertificateGenerator {

  @SneakyThrows
  public static X509Certificate generateRandomVauCertificate() {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC", BC.getSecurityProvider());
    keyPairGenerator.initialize(new ECGenParameterSpec(VauVersion.V1.getCurve()));
    val keyPair = keyPairGenerator.generateKeyPair();

    val privateKey = keyPair.getPrivate();
    val publicKey = keyPair.getPublic();
    return generateX509Certificate(privateKey, publicKey);
  }

  @SneakyThrows
  public static X509Certificate generateX509Certificate(
      @NonNull PrivateKey privateKey, @NonNull PublicKey publicKey) {
    val now = Instant.now();
    val notBefore = Date.from(now);
    val until = new Date(LocalDate.now().plusYears(100).toEpochDay());
    val contentSigner =
        new JcaContentSignerBuilder("SHA256withECDSA")
            .setProvider(BC.getSecurityProvider())
            .build(privateKey);
    val x500Name = new X500Name("CN=Common Name,O=Organization,L=City,ST=State");
    val certificateBuilder =
        new JcaX509v3CertificateBuilder(
            x500Name,
            BigInteger.valueOf(now.toEpochMilli()),
            notBefore,
            until,
            x500Name,
            publicKey);
    return new JcaX509CertificateConverter()
        .setProvider(BC.getSecurityProvider())
        .getCertificate(certificateBuilder.build(contentSigner));
  }
}

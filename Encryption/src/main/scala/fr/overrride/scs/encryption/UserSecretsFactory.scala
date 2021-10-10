package fr.overrride.scs.encryption

import fr.overrride.scs.common.fs.PathOps.SuperPath
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.io.{FileInputStream, OutputStream}
import java.nio.file.{Files, Path}
import java.security.cert.{CertificateFactory, X509Certificate}
import java.security.{KeyStore, PrivateKey, Security}

object UserSecretsFactory {

    private final val KeyStoreName    = "keystore.p12"
    private final val CertificateName = "certificate.cer"

    def create(secretsFolder: Path, password: String, organization: UserOrganization): UserSecrets = {
        if (password.length < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.")
        if (Files.notExists(secretsFolder / KeyStoreName) || Files.notExists(secretsFolder / CertificateName))
            createStore(secretsFolder, password, organization)
        System.setProperty("crypto.policy", "unlimited")
        Security.addProvider(new BouncyCastleProvider())
        val factory     = CertificateFactory.getInstance("X.509", "BC")
        val certificate = factory.generateCertificate(new FileInputStream(s"$secretsFolder/$CertificateName")).asInstanceOf[X509Certificate]
        val pwdBytes    = password.toCharArray
        val keyStore    = KeyStore.getInstance("PKCS12")
        keyStore.load(new FileInputStream(s"$secretsFolder/$KeyStoreName"), pwdBytes)
        val key = keyStore.getKey("cloud", pwdBytes).asInstanceOf[PrivateKey]
        new UserSecrets(certificate, key)
    }

    private def createStore(secretsFolder: Path, password: String, organization: UserOrganization): Unit = {
        println("Creating Keystore and certificate...")
        val process = run("keytool", "-keystore", s"$secretsFolder/$KeyStoreName", "-genkey", "-alias", "cloud", "-keyalg", "RSA", "-keypass", password, "-storepass", password, "-validity", "365")
        organization.writeData(process.getOutputStream)
        process.waitFor()
        println("Keystore created, extracting certificate...")
        run("keytool", "-export", "-alias", "cloud", "-storepass", password, "-file", s"$secretsFolder/$CertificateName", "-keystore", s"$secretsFolder/$KeyStoreName")
                .waitFor()
        println("Certificate created.")
    }

    private def run(command: AnyRef*): Process = {
        val args: Array[String] = command.map(_.toString).toArray
        new ProcessBuilder()
                .command(args: _*)
                //.inheritIO()
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start()
    }

}

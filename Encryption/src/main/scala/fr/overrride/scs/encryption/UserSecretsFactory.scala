package fr.overrride.scs.encryption

import fr.overrride.scs.common.fs.PathOps.AppendPath
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.io.{FileInputStream, OutputStream}
import java.nio.file.{Files, Path}
import java.security.cert.{CertificateFactory, X509Certificate}
import java.security.{KeyStore, PrivateKey, Security}

/**
 * Creates [[UserSecrets]] objects
 * */
object UserSecretsFactory {

    private final val KeyStoreName    = "keycloud.p12"
    private final val CertificateName = "certificate.cer"

    /**
     * @param secretsFolder the path to the folder in which keystore and certificates will be stored
     * @param password the user's password (must be longer than 5 chars)
     * @param organization the user's organisation
     * @return the generated [[UserSecrets]]
     */
        @throws[IllegalArgumentException]("if password shorter than 6 chars")
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

    /**
     * Creates a p12 keystore and a certificate into the given secretsFolder
     * @param secretsFolder the secrets folder in which generated keystore and certificate will be stored
     * @param password the keystore password
     * @param organization the user's organisation
     */
    private def createStore(secretsFolder: Path, password: String, organization: UserOrganization): Unit = {
        println("Creating Keycloud and certificate...")
        val process = run("keytool", "-keycloud", s"$secretsFolder/$KeyStoreName", "-genkey", "-alias", "cloud", "-keyalg", "RSA", "-keypass", password, "-cloudpass", password, "-validity", "365")
        organization.writeData(process.getOutputStream)
        process.waitFor()
        println("Keycloud created, extracting certificate...")
        run("keytool", "-export", "-alias", "cloud", "-cloudpass", password, "-file", s"$secretsFolder/$CertificateName", "-keycloud", s"$secretsFolder/$KeyStoreName")
                .waitFor()
        println("Certificate created.")
    }

    /**
     * Run a command and return the resulting [[Process]]
     * @return the resulting [[Process]]
     */
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

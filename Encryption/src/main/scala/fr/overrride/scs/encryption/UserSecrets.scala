package fr.overrride.scs.encryption

import fr.overrride.scs.encryption.UserSecrets.BouncyCastleProvider
import org.bouncycastle.cms.jcajce.{JceCMSContentEncryptorBuilder, JceKeyTransEnvelopedRecipient, JceKeyTransRecipientInfoGenerator}
import org.bouncycastle.cms.{CMSAlgorithm, CMSEnvelopedData, CMSEnvelopedDataGenerator, CMSProcessableByteArray}

import java.io.IOException
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * Encrypt and decrypt data using the given certificate and private key
 * */
class UserSecrets private[encryption](certificate: X509Certificate, pk: PrivateKey) {

    private val generator = {
        val generator = new CMSEnvelopedDataGenerator()
        val jceKey    = new JceKeyTransRecipientInfoGenerator(certificate)
        generator.addRecipientInfoGenerator(jceKey)
        generator
    }

    private val encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
            .setProvider(BouncyCastleProvider)
            .build()

    private val recipient = new JceKeyTransEnvelopedRecipient(pk)

    /**
     * @param data the data do encrypt
     * @return the encrypted data
     */
    def encrypt(data: Array[Byte]): Array[Byte] = {
        val msg = new CMSProcessableByteArray(data)
        generator.generate(msg, encryptor).getEncoded
    }

    /**
     * @param data the data to decrypt
     * @return the decrypted data
     * @throws IOException If the given encrypted data is corrupted
     */
    @throws[IOException]("If the given encrypted data is corrupted")
    def decrypt(data: Array[Byte]): Array[Byte] = {
        val enveloped     = new CMSEnvelopedData(data)
        val recipientInfo = enveloped.getRecipientInfos.iterator().next()
        recipientInfo.getContent(recipient)
    }

}

object UserSecrets {

    private final val BouncyCastleProvider = "BC"
}
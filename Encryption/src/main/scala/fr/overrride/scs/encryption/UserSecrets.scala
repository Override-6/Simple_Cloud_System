package fr.overrride.scs.encryption

import fr.overrride.scs.encryption.UserSecrets.BouncyCastleProvider
import org.bouncycastle.cms.jcajce.{JceCMSContentEncryptorBuilder, JceKeyTransEnvelopedRecipient, JceKeyTransRecipientInfoGenerator}
import org.bouncycastle.cms.{CMSAlgorithm, CMSEnvelopedData, CMSEnvelopedDataGenerator, CMSProcessableByteArray}

import java.security.PrivateKey
import java.security.cert.X509Certificate

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

    def encrypt(data: Array[Byte]): Array[Byte] = {
        val msg = new CMSProcessableByteArray(data)
        generator.generate(msg, encryptor).getEncoded
    }

    def decrypt(bytes: Array[Byte]): Array[Byte] = {
        val enveloped     = new CMSEnvelopedData(bytes)
        val recipientInfo = enveloped.getRecipientInfos.iterator().next()
        recipientInfo.getContent(recipient)
    }

}

object UserSecrets {

    private final val BouncyCastleProvider = "BC"
}
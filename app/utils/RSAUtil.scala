package utils

import java.security._
import java.security.spec.{InvalidKeySpecException, PKCS8EncodedKeySpec, X509EncodedKeySpec}

import org.apache.commons.codec.binary.Base64

import scala.util.Random._

object RSAUtil {
  val RSA_MODULUS      = "Modulus"
  val RSA_EXPONENT     = "Exponent"
  val ALGORITHM        = "RSA"
  val CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding"

  def base64Encode(key: String): String =
    Base64.encodeBase64String(key.getBytes)

  def base64Decode(encodeKey: String) =
    new String(Base64.decodeBase64(encodeKey))

  def genKeyPair() = {
    val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
    val random           = new SecureRandom()
    keyPairGenerator.initialize(2048, random)
    val keyPair = keyPairGenerator.genKeyPair()

    val encodePrivateKey = keyPair.getPrivate().getEncoded()
    val encodePublicKey  = keyPair.getPublic().getEncoded()

    val x509EncodedKeySpec  = new X509EncodedKeySpec(encodePublicKey)
    val pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encodePrivateKey)

    val base64Public  = Base64.encodeBase64String(x509EncodedKeySpec.getEncoded)
    val base64Private = Base64.encodeBase64String(pkcs8EncodedKeySpec.getEncoded)

    (base64Public, base64Private)
  }

  def genRawKeyPair() = {
    val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
    val random           = new SecureRandom()
    keyPairGenerator.initialize(2048, random)
    val keyPair = keyPairGenerator.genKeyPair()

    val generatedPrivateKey = keyPair.getPrivate()
    val generatedPublicKey  = keyPair.getPublic()
    val encodePrivateKey    = generatedPrivateKey.getEncoded()
    val encodePublicKey     = generatedPublicKey.getEncoded()

    val x509EncodedKeySpec  = new X509EncodedKeySpec(encodePublicKey)
    val pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encodePrivateKey)

    val base64Public  = Base64.encodeBase64String(x509EncodedKeySpec.getEncoded)
    val base64Private = Base64.encodeBase64String(pkcs8EncodedKeySpec.getEncoded)

    (generatedPrivateKey, generatedPublicKey, base64Private, base64Public)
  }

  def decodePublicKey(encodedPublicKey: String): PublicKey =
    try {
      val decodedKey = Base64.decodeBase64(encodedPublicKey)
      val keyFactory = KeyFactory.getInstance(ALGORITHM)
      keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey))
    } catch {
      case e: NoSuchAlgorithmException =>
        throw new RuntimeException(e)
      case e: InvalidKeySpecException =>
        print("decodePublicKey: Invalid key specification.")
        throw new IllegalArgumentException(e)
    }

  def randomString(len: Int, chars: Seq[Char]): String = {
    val stringBuilder = new StringBuilder
    for (i <- 1 to len) {
      val randomNuber = nextInt(chars.length)
      stringBuilder.append(chars(randomNuber))
    }
    stringBuilder.toString
  }

  def randomAlphaNumeralGenerator(length: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    randomString(length, chars)
  }

  def sha256Hash(text: String): String =
    String.format(
      "%064x",
      new java.math.BigInteger(1, java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8")))
    )

  def shaMD5Hash(text: String): String =
    String.format(
      "%02x",
      new java.math.BigInteger(1, java.security.MessageDigest.getInstance("MD5").digest(text.getBytes("UTF-8")))
    )

  def maskForGDPR(input: String): String =
    input.replaceAll("(-[A-Za-z0-9]{4}-)", "-XXXX-") //Masked UserId: dfc7d4a3-XXXX-4330-XXXX-4e87655c9001

}

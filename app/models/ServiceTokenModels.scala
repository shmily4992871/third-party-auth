package models

//Key Related Case Classes
final case class ServiceKeyStore(unityId: String, publicKey: String, privateKey: String, ts: Long)

//Service Key Related Case Classes
final case class UnityId(unityId: String) extends AnyVal
final case class RequestKeyPair(unityId: String, secret: String)

//Service Token Related Case Classes
final case class ServiceTokenRaw(unityId: String, secretKey: String, issuedTime: Long, duration: Int)
final case class ServiceTokenResponse(serviceToken: String, expire: Long)
final case class ServiceTokenClaims(duration: Int, unityId: String)
final case class ServiceTokenDecodeReq(unityId: String, publicKey: String, serviceToken: String)
final case class ServiceTokenRequest(unityId: String, serviceToken: String)
final case class ServiceTokenValidateResult(validateResult: Boolean)

//Key Pair
final case class ServiceKeyPair(unityId: String, secret: String)

object JsonFormats {

  import play.api.libs.json.Json

  // Generates Writes and Reads, thanks to Json Macros
  implicit val serviceTokenResponseFormat = Json.format[ServiceTokenResponse]
  implicit val serviceKeyPairFormat       = Json.format[ServiceKeyPair]
  implicit val serviceKeyStoreFormat      = Json.format[ServiceKeyStore]
  implicit val serviceTokenValidateResult = Json.format[ServiceTokenValidateResult]
}

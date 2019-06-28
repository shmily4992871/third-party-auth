package services

import com.github.mehmetakiftutuncu.errors.{Errors, Maybe}
import com.twitter.inject.Logging
import com.twitter.util.Future
import models.{ServiceTokenClaims, ServiceTokenDecodeReq, ServiceTokenRaw, ServiceTokenResponse}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write
import pdi.jwt.exceptions.{JwtExpirationException, JwtValidationException}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import utils.RSAUtil
import utils.ServiceErrors.{ServiceTokenExpirationFail, ServiceTokenUnknownFail, ServiceTokenValidationFail}
import com.twitter.util.{Future => TwitterFuture}
import javax.inject.Singleton

import scala.util.{Failure, Success}
import scala.util.control.NonFatal

@Singleton
class ServiceTokenGenerator extends Logging {
  private val className = s"[${this.getClass.getSimpleName}]"
  implicit val formats  = DefaultFormats

  def apply(req: ServiceTokenRaw): TwitterFuture[Maybe[ServiceTokenResponse]] = {

    val ENCRYPT_ALGORITHM = JwtAlgorithm.RS256
    val ENCRYPT_SECRET    = req.secretKey //the private key
    val JWT_HEADER        = """{"typ":"JWT","alg":"RS256"}"""
    val tokenClaims       = ServiceTokenClaims(req.duration, req.unityId)
    val JWT_CLAIM         = JwtClaim(write(tokenClaims)).issuedAt(req.issuedTime).expiresIn(req.duration.toLong).toJson

    val token = Jwt.encode(
      JWT_HEADER,
      JWT_CLAIM,
      ENCRYPT_SECRET,
      ENCRYPT_ALGORITHM
    )
    val expiration: Long = req.issuedTime + req.duration

    info(className + "GenerateServiceToken token: " + token)
    info(className + "GenerateServiceToken expiration: " + expiration)

    Future(Maybe(ServiceTokenResponse(token, expiration)))
  }
}

//Validate the signed token using the public key and return the JWT claims
@Singleton
class DecodeServiceToken extends Logging {

  private val className = s"[${this.getClass.getSimpleName}]"
  info(s"className: $className")
  implicit val formats = DefaultFormats

  def apply(req: ServiceTokenDecodeReq): TwitterFuture[Maybe[Boolean]] = {

    info(s"DecodeServiceToken req: $req")
    val ENCRYPT_ALGORITHM = JwtAlgorithm.RS256
    val encodedPublicKey  = req.publicKey
    val TOKEN             = req.serviceToken
    val PUBLIC_KEY        = RSAUtil.decodePublicKey(encodedPublicKey)

    val isValidPublicKey = Jwt.isValid(TOKEN, PUBLIC_KEY, Seq(ENCRYPT_ALGORITHM))
    info(s"isValidPublicKey: $isValidPublicKey")

    val decodeResult =
      try {
        Jwt.decodeAll(TOKEN, PUBLIC_KEY, Seq(ENCRYPT_ALGORITHM)) match {
          case Success(tupleResult) => {
            // _1: header, _2: claims, _3: signature
            info("decoded token: " + write(TOKEN) + " result: " + write(tupleResult))
            val jsonValue = parse(tupleResult._2)
            val duration  = (jsonValue \ "duration").extract[Int]

            Maybe(true)
          }
          case Failure(error) => {
            info("decodeAuthToken exception: " + error.getMessage)
            error match {
              case x: JwtValidationException => {
                info("JwtValidationException = " + x)
                Maybe(Errors(new ServiceTokenValidationFail))
              }
              case x: JwtExpirationException => {
                info("JwtExpirationException = " + x)
                Maybe(Errors(new ServiceTokenExpirationFail))
              }
              case _: JwtExpirationException => {
                info("JwtExpirationException = SERVICE_TOKEN_UNKNOWN_ERROR")
                Maybe(Errors(new ServiceTokenUnknownFail))
              }
            }
          }
        }
      } catch {
        case NonFatal(e) =>
          error("exception =" + e)
          info("JwtExpirationException = TOKEN_UNKNOWN_ERROR")
          Maybe(Errors(new ServiceTokenUnknownFail))
      }
    Future(decodeResult)
  }
}

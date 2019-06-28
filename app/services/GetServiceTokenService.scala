package services

import com.twitter.inject.Logging
import db.QueryKeyPair
import io.github.hamsters.twitter.FutureEither
import javax.inject.{Inject, Singleton}
import io.github.hamsters.Validation.{KO, OK}
import models.{RequestKeyPair, ServiceTokenRaw, ServiceTokenResponse, UnityId}
import play.api.Configuration
import io.github.hamsters.twitter.Implicits._
import utils.RSAUtil._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class GetServiceTokenService @Inject()(serviceTokenGenerator: ServiceTokenGenerator,
                                       serviceKeysGenerator: ServiceKeysGenerator,
                                       queryKeyPair: QueryKeyPair,
                                       configuration: Configuration)
    extends Logging {

  private val className = s"[${this.getClass.getSimpleName}]"
  def apply(keypair: RequestKeyPair): Future[Option[ServiceTokenResponse]] = {
    val decodeResult = decodeKeyPair(keypair)
    val issuedTime   = System.currentTimeMillis() / 1000
    val duration     = configuration.underlying.getInt("SERVICE_TOKEN_EXPIRE_IN_SECONDS")
    val unityId      = decodeResult.unityId

    (for {
      keyPair  <- FutureEither(queryKeyPair(decodeResult))
      keyStore <- FutureEither(serviceKeysGenerator(UnityId(unityId)))
      serviceJWTToken <- FutureEither(
                          serviceTokenGenerator(
                            ServiceTokenRaw(unityId, keyStore.privateKey, issuedTime, duration)
                          )
                        )
    } yield serviceJWTToken).future.map {
      case OK(r) => Option(r)
      case KO(r) =>
        error(s"[$className] fail to generator serviceToken")
        None
    }
  }

  private def decodeKeyPair(keypair: RequestKeyPair): RequestKeyPair = {
    val unityId = base64Decode(keypair.unityId)
    val secret  = base64Decode(keypair.secret)
    RequestKeyPair(unityId, secret)
  }

}

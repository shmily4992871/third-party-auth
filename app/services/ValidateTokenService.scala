package services

import com.twitter.inject.Logging
import db.ReadServiceKey
import io.github.hamsters.Validation.{KO, OK}
import io.github.hamsters.twitter.FutureEither
import javax.inject.{Inject, Singleton}
import models.{ServiceTokenDecodeReq, ServiceTokenRequest, ServiceTokenValidateResult, UnityId}
import io.github.hamsters.twitter.Implicits._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ValidateTokenService @Inject()(readServiceKey: ReadServiceKey, decodeServiceToken: DecodeServiceToken)
    extends Logging {

  private val className = s"[${this.getClass.getSimpleName}]"
  def apply(data: ServiceTokenRequest): Future[ServiceTokenValidateResult] =
    (for {
      serviceKeyPair <- FutureEither(readServiceKey(UnityId(data.unityId)))
      serviceToken <- FutureEither(
                       decodeServiceToken(
                         ServiceTokenDecodeReq(data.unityId, serviceKeyPair.publicKey, data.serviceToken)
                       )
                     )
    } yield serviceToken).future.map {
      case OK(r) => ServiceTokenValidateResult(true)
      case KO(r) =>
        error(s"[$className] fail to generator serviceToken,Because token has expired, please regenerate")
        ServiceTokenValidateResult(false)
    }
}

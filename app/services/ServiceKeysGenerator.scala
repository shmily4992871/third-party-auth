package services

import com.github.mehmetakiftutuncu.errors.Maybe
import com.twitter.inject.Logging
import com.twitter.util.{Future => TwitterFuture}
import db.{QueryServiceKey, UpsertServiceKey}
import models.{ServiceKeyStore, UnityId}
import org.json4s.DefaultFormats
import play.api.Configuration
import utils.RSAUtil
import io.github.hamsters.twitter.FutureEither
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ServiceKeysGenerator @Inject()(queryServiceKey: QueryServiceKey,
                                     upsertServiceKey: UpsertServiceKey,
                                     configuration: Configuration)
    extends Logging {

  val expireTime       = configuration.underlying.getInt("SERVICE_TOKEN_EXPIRE_IN_SECONDS")
  implicit val formats = DefaultFormats

  private val className = s"[${this.getClass.getSimpleName}]"

  //Lookup key
  def apply(keyIndex: UnityId): TwitterFuture[Maybe[ServiceKeyStore]] = {

    val res = getOrGenKey(keyIndex.unityId)
    info(s"$className getOrGenKey: " + res)
    res
  }

  private def getOrGenKey(keyIndex: String): TwitterFuture[Maybe[ServiceKeyStore]] =
    queryServiceKey(UnityId(keyIndex)).flatMap {
      case Some(value) => {
        if ((System.currentTimeMillis - value.ts) / 1000 < expireTime) {
          TwitterFuture.value(Maybe(value))
        } else {
          genKey(keyIndex)
        }
      }
      case None => {
        genKey(keyIndex)
      }
    }

  private def genKey(keyIndex: String): TwitterFuture[Maybe[ServiceKeyStore]] = {
    val (rawPrivateKey, rawPublicKey, encodedPrivateKey, encodedPublicKey) = RSAUtil.genRawKeyPair
    val keyMapping                                                         = ServiceKeyStore(keyIndex, encodedPublicKey, encodedPrivateKey, System.currentTimeMillis)
    (for {
      serviceToken <- FutureEither(upsertServiceKey(keyMapping))
    } yield serviceToken).future
  }
}

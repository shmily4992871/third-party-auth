package db

import com.github.mehmetakiftutuncu.errors.{Errors, Maybe}
import com.twitter.inject.Logging
import javax.inject.{Inject, Singleton}
import models.{RequestKeyPair, ServiceKeyPair}
import io.github.hamsters.Validation.{KO, OK}
import reactivemongo.bson.BSONDocument
import utils.ServiceErrors.{MongoClientError, ReadServiceCredentialsFail, ReadServiceCredentialsNotFound}
import utils.PipeOperator._
import models.JsonFormats._
import com.twitter.util.{Future => TwitterFuture}
import io.github.hamsters.twitter.Implicits._
import modules.Application
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._

import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext

@Singleton
class QueryKeyPair @Inject()(implicit ec: ExecutionContext,
                             reactiveMongoApi: ReactiveMongoApi,
                             application: Application)
    extends Logging {
  private val className = s"[${this.getClass.getSimpleName}]"

  def apply(keyPair: RequestKeyPair): TwitterFuture[Maybe[ServiceKeyPair]] = {
    val unityId = keyPair.unityId
    val secret  = keyPair.secret
    info(s"searching for the service keypair of unity: $unityId")
    val query = BSONDocument("unityId" -> unityId)
    (for {
      res <- application.keyPairCollection.flatMap(
              _.find(selector = query, projection = Option.empty[BSONDocument])
                .one[ServiceKeyPair]
            )
    } yield res)
      .map {
        case Some(x) if (x.secret == secret) => OK(x)
        case Some(x) if (x.secret != secret) =>
          s"secret : $secret The given secret does not match the query result"
            .#!("[FetchServiceCredentials] fail")
            .|>(_ => KO(Errors(new ReadServiceCredentialsFail)))
        case None =>
          s"unityId : $unityId can't be found"
            .#!("[FetchServiceCredentials] fail")
            .|>(_ => KO(Errors(new ReadServiceCredentialsNotFound)))
      }
      .recover {
        case NonFatal(e) =>
          s"${e.getMessage}"
            .#!(s"[$className] unexpected fail")
            .|>(_ => KO(Errors(new MongoClientError)))
      }
  }
}

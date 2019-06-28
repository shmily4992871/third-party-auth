package db

import com.github.mehmetakiftutuncu.errors.{Errors, Maybe}
import com.twitter.inject.Logging
import io.github.hamsters.Validation.{KO, OK}
import javax.inject.Inject
import models.{ServiceKeyStore, UnityId}
import modules.Application
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONDocument
import utils.ServiceErrors.{MongoClientError, ReadServiceKeyFail}
import utils.PipeOperator._
import models.JsonFormats._
import reactivemongo.play.json._
import io.github.hamsters.twitter.Implicits._
import com.twitter.util.{Future => TwitterFuture}
import utils.RSAUtil.base64Decode

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class ReadServiceKey @Inject()(implicit ec: ExecutionContext,
                               reactiveMongoApi: ReactiveMongoApi,
                               application: Application)
    extends Logging {
  def apply(id: UnityId): TwitterFuture[Maybe[ServiceKeyStore]] = {
    info(s"[encodeUnityId]: ${id.unityId}")
    val unityId = base64Decode(id.unityId)
    val query   = BSONDocument("unityId" -> unityId)
    (for {
      res <- application.serviceSecretCol.flatMap(
              _.find(query, projection = Option.empty[BSONDocument])
                .one[ServiceKeyStore]
            )
    } yield res)
      .map {
        case Some(x) => OK(x)
        case None =>
          s"unityId : $unityId can't be found"
            .#!("[ReadServiceKey] fail")
            .|>(_ => KO(Errors(new ReadServiceKeyFail)))
      }
      .recover {
        case NonFatal(e) =>
          s"${e.getMessage}"
            .#!("[ReadServiceKey] unexpected fail")
            .|>(_ => KO(Errors(new MongoClientError)))
      }
  }

}

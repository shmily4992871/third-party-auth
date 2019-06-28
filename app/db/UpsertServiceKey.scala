package db

import com.github.mehmetakiftutuncu.errors.{Errors, Maybe}
import javax.inject._
import com.twitter.inject.Logging
import com.twitter.util.{Future => TwitterFuture}
import io.github.hamsters.Validation.{KO, OK}
import io.github.hamsters.twitter.Implicits._
import models.ServiceKeyStore
import modules.Application
import reactivemongo.play.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import utils.ServiceErrors.{MongoClientError, UpdateServiceKeyFail}
import utils.PipeOperator._
import models.JsonFormats._

import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext

@Singleton
class UpsertServiceKey @Inject()(implicit ec: ExecutionContext,
                                 reactiveMongoApi: ReactiveMongoApi,
                                 application: Application)
    extends Logging {
  private val className = s"[${this.getClass.getSimpleName}]"

  def apply(data: ServiceKeyStore): TwitterFuture[Maybe[ServiceKeyStore]] = {

    info(s"appId: ${data.unityId}")
    val selector = BSONDocument("unityId" -> data.unityId)
    val updateModifier = BSONDocument(
      f"$$set" -> BSONDocument(
        "unityId"    -> data.unityId,
        "privateKey" -> data.privateKey,
        "publicKey"  -> data.publicKey,
        "ts"         -> data.ts
      )
    )

    (for {
      res <- application.serviceSecretCol.flatMap(
              _.findAndUpdate(selector, updateModifier, fetchNewObject = true, upsert = true)
                .map(x => x.result[ServiceKeyStore].orElse(x.lastError))
            )
    } yield res)
      .map {
        case Some(x: ServiceKeyStore) =>
          s"${data.unityId}"
            .#|(s"[$className] success")
            .|>(_ => OK(x))
        case Some(x) =>
          s"${data.unityId} - ${x}"
            .#!(s"[$className] fail")
            .|>(_ => KO(Errors(new UpdateServiceKeyFail)))
        case None =>
          s"${data.unityId} can't be updated"
            .#!(s"[$className] fail")
            .|>(_ => KO(Errors(new UpdateServiceKeyFail)))
      }
      .recover {
        case WriteResult.Code(11000) =>
          "Mongo Error:"
            .#!(s"[$className] duplicate data")
            .|>(_ => KO(Errors(new UpdateServiceKeyFail)))
        case NonFatal(e) =>
          s"${e.getMessage}"
            .#!(s"[$className] unexpected fail")
            .|>(_ => KO(Errors(new MongoClientError)))
      }
  }

}

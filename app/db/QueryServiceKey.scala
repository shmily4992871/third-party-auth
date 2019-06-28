package db

import com.github.mehmetakiftutuncu.errors.Errors
import com.twitter.inject.Logging
import com.twitter.util.{Future => TwitterFuture}
import models.{ServiceKeyStore, UnityId}
import reactivemongo.bson.BSONDocument
import io.github.hamsters.twitter.Implicits._
import io.github.hamsters.Validation.KO
import utils.ServiceErrors.MongoClientError
import utils.PipeOperator._
import models.JsonFormats._
import javax.inject._
import modules.Application
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.util.control.NonFatal
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class QueryServiceKey @Inject()(implicit ec: ExecutionContext,
                                reactiveMongoApi: ReactiveMongoApi,
                                application: Application)
    extends Logging {
  private val className = s"[${this.getClass.getSimpleName}]"

  def apply(unityId: UnityId): TwitterFuture[Option[ServiceKeyStore]] = {
    info(s"searching for the service keystore of unity: ${unityId.unityId}")

    val query = BSONDocument("unityId" -> unityId.unityId)
    (for {
      res <- application.serviceSecretCol.flatMap(
              _.find(query, projection = Option.empty[BSONDocument])
                .one[ServiceKeyStore]
            )
    } yield res).recover {
      case NonFatal(e) =>
        s"${e.getMessage}"
          .#!(s"[$className] unexpected fail")
          .|>(_ => KO(Errors(new MongoClientError)))
        None
    }
  }
}

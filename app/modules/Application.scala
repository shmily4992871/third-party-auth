package modules

import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

class Application @Inject()(implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) {
  def serviceSecretCol: Future[JSONCollection]   = reactiveMongoApi.database.map(_.collection("ServiceSecret"))
  def keyStoreCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("KeyStore"))
  def keyPairCollection: Future[JSONCollection]  = reactiveMongoApi.database.map(_.collection("KeyPair"))

}

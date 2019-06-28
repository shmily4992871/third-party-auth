package utils

import models.{ServiceKeyPair, ServiceKeyStore, ServiceTokenResponse}
import reactivemongo.bson.{BSONDocument, Macros}

@SuppressWarnings(Array("all"))
object MongoUtils {
  implicit val serviceKeyStore      = Macros.handler[ServiceKeyStore]
  implicit val serviceTokenResponse = Macros.handler[ServiceTokenResponse]
  implicit val serviceKeyPair       = Macros.handler[ServiceKeyPair]
}

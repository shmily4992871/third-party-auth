import utils.RSAUtil._
object Test {
  def main(args: Array[String]): Unit = {
    val unityId = "unityId"
    val decodeUnityId = base64Encode(unityId)
    val secret = "secret"
    val decodeSecret = base64Encode(secret)
    println(s"[decodeUnityId]: $decodeUnityId  + [decodeSecret]: $decodeSecret")
  }
}

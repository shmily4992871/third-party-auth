package controllers

import com.twitter.inject.Logging
import io.swagger.annotations._
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{GetServiceTokenService, ValidateTokenService}
import models.JsonFormats._
import models.{RequestKeyPair, ServiceTokenRequest, ServiceTokenResponse, ServiceTokenValidateResult}

import scala.concurrent.ExecutionContext.Implicits.global

@Api(value = "/token")
class TokenController @Inject()(cc: ControllerComponents,
                                getServiceTokenService: GetServiceTokenService,
                                validateTokenService: ValidateTokenService)
    extends AbstractController(cc)
    with Logging {

  @ApiOperation(
    value = "Get a ServiceTokenResponse by unityId and secret",
    response = classOf[ServiceTokenResponse]
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 400, message = "Invalid unityId supplied"),
      new ApiResponse(code = 404, message = "UnityId not found"),
      new ApiResponse(code = 500, message = "Internal Server Error")
    )
  )
  def getServiceToken(@ApiParam(value = "unityId", required = true) unityId: String,
                      @ApiParam(value = "secret", required = true) secret: String) = Action.async {

    getServiceTokenService(RequestKeyPair(unityId, secret)) map { token =>
      token match {
        case Some(v) => Ok(Json.toJson(v))
        case None    => NotFound
      }
    }
  }

  @ApiOperation(
    value = "Validate serviceToken",
    response = classOf[ServiceTokenValidateResult]
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 400, message = "Invalid unityId supplied"),
      new ApiResponse(code = 404, message = "UnityId not found"),
      new ApiResponse(code = 500, message = "Internal Server Error")
    )
  )
  def validateServiceToken(@ApiParam(value = "unityId", required = true) unityId: String,
                           @ApiParam(value = "token", required = true) token: String) = Action.async {

    validateTokenService(ServiceTokenRequest(unityId, token)) map { result =>
      result.validateResult match {
        case true  => Ok(Json.toJson(result))
        case false => Ok(Json.toJson(result))
      }
    }
  }
}

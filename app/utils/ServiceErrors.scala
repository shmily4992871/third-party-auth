package utils

import com.github.mehmetakiftutuncu.errors.{Errors, SimpleError}

trait ServiceError

trait BadRequestError            extends ServiceError
trait NoContentError             extends ServiceError
trait FailedDependencyError      extends ServiceError
trait ForbiddenError             extends ServiceError
trait InternalError              extends ServiceError
trait InputError                 extends ServiceError
trait NotFoundError              extends ServiceError
trait NotAcceptableError         extends ServiceError
trait ServiceTokenForbiddenError extends ServiceError
trait ServiceTokenExpiredError   extends ServiceError
trait ProfileResponse            extends ServiceError

object ServiceErrors {

  // Mongo Fail
  class MongoClientError extends SimpleError(name = "ERROR-1000") with InternalError

  // Unexpected Fail
  class UnexpectedError       extends SimpleError(name = "ERROR-2000") with InternalError
  class InvalidParameterError extends SimpleError(name = "ERROR-2001") with InputError

  //Service Token Decode Fail
  class ServiceTokenContentFail
      extends SimpleError(name = "ERROR-3001: SERVICE_TOKEN_CONTENT_ERROR")
      with ServiceTokenForbiddenError
  class ServiceTokenValidationFail
      extends SimpleError(name = "ERROR-3002: SERVICE_TOKEN_VALIDATION_ERROR")
      with ServiceTokenForbiddenError
  class ServiceTokenExpirationFail
      extends SimpleError(name = "ERROR-3003: SERVICE_TOKEN_EXPIRATION_ERROR")
      with ServiceTokenExpiredError
  class ServiceTokenUnknownFail
      extends SimpleError(name = "ERROR-3004: SERVICE_TOKEN_UNKNOWN_ERROR")
      with ServiceTokenForbiddenError
  class DefaultServiceTokenError
      extends SimpleError(name = "ERROR-3010: DEFAULT_SERVICE_TOKEN_ERROR")
      with InternalError

  //Key Errors
  class UpdateServiceKeyFail extends SimpleError(name = "ERROR-5000") with InternalError
  class ReadServiceKeyFail   extends SimpleError(name = "ERROR-5001") with NotFoundError
  class UpdateUserKeyFail    extends SimpleError(name = "ERROR-6000") with InternalError
  val ReadUserKeyFail: Errors = new Errors(List(SimpleError("ERROR-6001: USER_TOKEN_UNKNOWN_ERROR"))) with NotFoundError

  //Credential Errors
  class CreateServiceCredentialsFail extends SimpleError(name = "ERROR-8000") with InternalError
  class CreateServiceCredentialsDuplicate
      extends SimpleError(name = "ERROR-8001: DUPLICATE_APPID_ERROR")
      with NotAcceptableError
  class ReadServiceCredentialsFail     extends SimpleError(name = "ERROR-8002") with InternalError
  class ReadServiceCredentialsNotFound extends SimpleError(name = "ERROR-8003: APPID_NOT_FOUND") with NotFoundError
  class ServiceCredentialsInvalid      extends SimpleError(name = "ERROR-8004") with ForbiddenError
  class DeleteServiceCredentialsFail
      extends SimpleError(name = "ERROR-8005: DELETE_CREDENTIALS_ERROR")
      with InternalError
  class DefaultServiceCredentialsError
      extends SimpleError(name = "ERROR-8010: DEFAULT_SERVICE_CREDENTIALS_ERROR")
      with InternalError

  class HttpClientError extends SimpleError(name = "ERROR-9001") with FailedDependencyError

  //RequestInvoice
  class RequestUserIdentityNoContentExtends extends SimpleError(name = "DEPENDENCY-ERROR-1000") with NoContentError
  class RequestUserIdentityBadRequest       extends SimpleError(name = "DEPENDENCY-ERROR-1001") with BadRequestError
  class RequestUserIdentityFail             extends SimpleError(name = "DEPENDENCY-ERROR-1002") with FailedDependencyError

  //Profile Errors
  class ProfileUnKnowFail extends SimpleError(name = "ERROR-9001") with InternalError

}

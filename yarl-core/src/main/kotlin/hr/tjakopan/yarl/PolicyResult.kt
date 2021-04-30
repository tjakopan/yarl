package hr.tjakopan.yarl

sealed class PolicyResult<out R>(val context: Context) {
  companion object {
    @JvmStatic
    fun <R> success(result: R, context: Context): PolicyResult<R> = Success(result, context)

    @JvmStatic
    fun failureWithException(
      exception: Throwable,
      exceptionType: ExceptionType,
      context: Context
    ): PolicyResult<Nothing> =
      Failure.FailureWithException(exception, exceptionType, context)

    @JvmStatic
    fun <R> failureWithResult(handledResult: R, context: Context): PolicyResult<R> =
      Failure.FailureWithResult(handledResult, context)
  }

  abstract val isSuccess: Boolean
  abstract val isFailure: Boolean
  abstract val isFailureWithException: Boolean
  abstract val isFailureWithResult: Boolean

  inline fun <T> fold(
    ifSuccess: (R, Context) -> T,
    ifFailureWithException: (finalException: Throwable, ExceptionType, FaultType, Context) -> T,
    ifFailureWithResult: (finalHandledResult: R, FaultType, Context) -> T
  ): T = when (this) {
    is Success -> ifSuccess(result, context)
    is Failure.FailureWithException -> ifFailureWithException(finalException, exceptionType, faultType, context)
    is Failure.FailureWithResult -> ifFailureWithResult(finalHandledResult, faultType, context)
  }

  inline fun onSuccess(action: (R, Context) -> Unit): PolicyResult<R> {
    if (isSuccess) action((this as Success).result, context)
    return this
  }

  inline fun onFailure(action: (FaultType, Context) -> Unit): PolicyResult<R> {
    if (isFailure) action((this as Failure).faultType, context)
    return this
  }

  inline fun onFailureWithException(action: (Throwable, ExceptionType, FaultType, Context) -> Unit): PolicyResult<R> {
    if (isFailureWithException) {
      this as Failure.FailureWithException
      action(finalException, exceptionType, faultType, context)
    }
    return this
  }

  inline fun onFailureWithResult(action: (R, FaultType, Context) -> Unit): PolicyResult<R> {
    if (isFailureWithResult) {
      this as Failure.FailureWithResult
      action(finalHandledResult, faultType, context)
    }
    return this
  }

  class Success<out R> internal constructor(val result: R, context: Context) : PolicyResult<R>(context) {
    override val isSuccess: Boolean = true
    override val isFailure: Boolean = false
    override val isFailureWithException: Boolean = false
    override val isFailureWithResult: Boolean = false
  }

  sealed class Failure<out R>(val faultType: FaultType, context: Context) : PolicyResult<R>(context) {
    override val isSuccess: Boolean = false
    override val isFailure: Boolean = true

    class FailureWithException internal constructor(
      val finalException: Throwable,
      val exceptionType: ExceptionType,
      context: Context
    ) :
      Failure<Nothing>(
        when (exceptionType) {
          ExceptionType.HANDLED_BY_THIS_POLICY -> FaultType.EXCEPTION_HANDLED_BY_THIS_POLICY
          else -> FaultType.UNHANDLED_EXCEPTION
        }, context
      ) {
      override val isFailureWithException: Boolean = true
      override val isFailureWithResult: Boolean = false
    }

    class FailureWithResult<out R> internal constructor(val finalHandledResult: R, context: Context) :
      Failure<R>(FaultType.RESULT_HANDLED_BY_THIS_POLICY, context) {
      override val isFailureWithException: Boolean = false
      override val isFailureWithResult: Boolean = true
    }
  }
}

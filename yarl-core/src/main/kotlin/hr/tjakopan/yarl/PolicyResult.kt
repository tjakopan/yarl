package hr.tjakopan.yarl

import arrow.core.Option

typealias PolicySuccess<R> = PolicyResult.Success<R>
typealias PolicyFailureWithException = PolicyResult.Failure.FailureWithException
typealias PolicyFailureWithResult<R> = PolicyResult.Failure.FailureWithResult<R>

sealed class PolicyResult<out R>(val context: Context) {
  fun <T> fold(
    ifSuccess: (Option<R>, Context) -> T,
    ifFailureWithException: (finalException: Throwable, ExceptionType, FaultType, Context) -> T,
    ifFailureWithResult: (finalHandledResult: Option<R>, FaultType, Context) -> T
  ): T = when (this) {
    is Success -> ifSuccess(result, context)
    is Failure.FailureWithException -> ifFailureWithException(finalException, exceptionType, faultType, context)
    is Failure.FailureWithResult -> ifFailureWithResult(finalHandledResult, faultType, context)
  }

  class Success<out R>(val result: Option<R>, context: Context) : PolicyResult<R>(context)

  sealed class Failure<out R>(val faultType: FaultType, context: Context) :
    PolicyResult<R>(context) {
    class FailureWithException(
      val finalException: Throwable,
      val exceptionType: ExceptionType,
      context: Context
    ) :
      Failure<Nothing>(
        when (exceptionType) {
          ExceptionType.HANDLED_BY_THIS_POLICY -> FaultType.EXCEPTION_HANDLED_BY_THIS_POLICY
          else -> FaultType.UNHANDLED_EXCEPTION
        }, context
      )

    class FailureWithResult<out R>(val finalHandledResult: Option<R>, context: Context) :
      Failure<R>(FaultType.RESULT_HANDLED_BY_THIS_POLICY, context)
  }
}

/**
 * Represents the type of exception resulting from a failed policy.
 */
enum class ExceptionType {
  /**
   * An exception type that has been defined to be handled by this policy.
   */
  HANDLED_BY_THIS_POLICY,
  /**
   * An exception type that has not been defined to be handled by this policy.
   */
  UNHANDLED
}

/**
 * Represents the type of outcome from a failed policy.
 */
enum class FaultType {
  /**
   * An exception type that has been defined to be handled by this policy.
   */
  EXCEPTION_HANDLED_BY_THIS_POLICY,
  /**
   * An exception type that has been not been defined to be handled by this policy
   */
  UNHANDLED_EXCEPTION,
  /**
   * A result value that has been defined to be handled by this policy
   */
  RESULT_HANDLED_BY_THIS_POLICY
}

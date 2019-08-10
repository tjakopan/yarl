package hr.tjakopan.yarl

typealias PolicySuccess = PolicyResult.Success
typealias PolicyFailure = PolicyResult.Failure
typealias PolicyGenericSuccess<TResult> = PolicyResultGeneric.Success<TResult>
typealias PolicyGenericFailureWithException<TResult> = PolicyResultGeneric.Failure.FailureWithException<TResult>
typealias PolicyGenericFailureWithResult<TResult> = PolicyResultGeneric.Failure.FailureWithResult<TResult>

/**
 * The captured result of executing a policy.
 *
 * @property context The context for this execution.
 */
sealed class PolicyResult(val context: Context) {
  fun <T> fold(ifSuccess: (Context) -> T, ifFailure: (finalException: Throwable, ExceptionType, Context) -> T): T =
    when (this) {
      is Success -> ifSuccess(context)
      is Failure -> ifFailure(finalException, exceptionType, context)
    }

  /**
   * A [PolicyResult] representing a successful execution through the policy.
   *
   * @constructor Builds a [PolicyResult] representing a successful execution through the policy.
   * @param context The policy execution context.
   */
  class Success(context: Context) : PolicyResult(context)

  /**
   * A [PolicyResult] representing a failed execution through the policy.
   *
   * @constructor Builds a [PolicyResult] representing a failed execution through the policy.
   * @param finalException The exception.
   * @param exceptionType The exception type.
   * @param context The policy execution context.
   *
   * @property finalException The final exception captured.
   * @property exceptionType The exception type of the final exception captured.
   */
  class Failure(val finalException: Throwable, val exceptionType: ExceptionType, context: Context) :
    PolicyResult(context)
}

/**
 * The captured result of executing a policy.
 */
sealed class PolicyResultGeneric<TResult>(val context: Context) {
  fun <T> fold(
    ifSuccess: (TResult?, Context) -> T,
    ifFailureWithException: (finalException: Throwable, ExceptionType, FaultType, Context) -> T,
    ifFailureWithResult: (finalHandledResult: TResult?, FaultType, Context) -> T
  ): T = when (this) {
    is Success -> ifSuccess(result, context)
    is Failure.FailureWithException -> ifFailureWithException(finalException, exceptionType, faultType, context)
    is Failure.FailureWithResult -> ifFailureWithResult(finalHandledResult, faultType, context)
  }

  /**
   * A [PolicyResultGeneric] representing a successful execution through the policy.
   *
   * @constructor Builds a [PolicyResultGeneric] representing a successful execution through the policy.
   * @param result The result returned by execution through the policy.
   * @param context The policy execution context.
   *
   * @property result The result of executing the policy.
   */
  class Success<TResult>(val result: TResult?, context: Context) : PolicyResultGeneric<TResult>(context)

  /**
   * @param faultType The fault type of the final fault captured.
   */
  sealed class Failure<TResult>(val faultType: FaultType, context: Context) : PolicyResultGeneric<TResult>(context) {
    /**
     * A [PolicyResultGeneric] representing a failed execution through the policy.
     *
     * @constructor Builds a [PolicyResultGeneric] representing a failed execution through the policy.
     * @param finalException The exception.
     * @param exceptionType The exception type.
     * @param context The policy execution context.
     *
     * @property finalException The final exception captured.
     * @property exceptionType The exception type of the final exception captured.
     */
    class FailureWithException<TResult>(
      val finalException: Throwable,
      val exceptionType: ExceptionType,
      context: Context
    ) :
      Failure<TResult>(
        when (exceptionType) {
          ExceptionType.HANDLED_BY_THIS_POLICY -> FaultType.EXCEPTION_HANDLED_BY_THIS_POLICY
          else -> FaultType.UNHANDLED_EXCEPTION
        }, context
      )

    /**
     * A [PolicyResultGeneric] representing a failed execution through the policy.
     *
     * @constructor Builds a [PolicyResultGeneric] representing a failed execution through the policy.
     * @param finalHandledResult The result returned by execution through the policy, which was treated as a handled
     * failure.
     * @param context The policy execution context.
     *
     * @property finalHandledResult The final handled result captured.
     */
    class FailureWithResult<TResult>(val finalHandledResult: TResult?, context: Context) :
      Failure<TResult>(FaultType.RESULT_HANDLED_BY_THIS_POLICY, context)
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

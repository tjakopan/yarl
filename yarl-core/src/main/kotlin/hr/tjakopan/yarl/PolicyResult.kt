package hr.tjakopan.yarl

/**
 * The captured result of executing a policy.
 *
 * @property context The context for this execution.
 */
sealed class PolicyResult<TResult>(val context: Context) {
  fun <T> fold(
    ifSuccessWithResult: (TResult, Context) -> T,
    ifSuccessWithoutResult: (Context) -> T,
    ifFailureWithException: (Throwable, ExceptionType, Context) -> T,
    ifFailureWithResult: (TResult, Context) -> T
  ): T = when (this) {
    is SuccessWithResult -> ifSuccessWithResult(result, context)
    is SuccessWithoutResult -> ifSuccessWithoutResult(context)
    is FailureWithException -> ifFailureWithException(finalException, exceptionType, context)
    is FailureWithResult -> ifFailureWithResult(finalHandledResult, context)
  }

  /**
   * A [PolicyResult] representing a successful execution through the policy.
   *
   * @constructor Builds a [PolicyResult] representing a successful execution through the policy.
   * @param result The result returned by execution through the policy.
   * @param context The policy execution context.
   *
   * @property result The result of executing the policy.
   */
  class SuccessWithResult<TResult>(val result: TResult, context: Context) :
    PolicyResult<TResult>(context)

  /**
   * A [PolicyResult] representing a successful execution through the policy.
   *
   * @constructor Builds a [PolicyResult] representing a successful execution through the policy.
   * @param context The policy execution context.
   */
  class SuccessWithoutResult(context: Context) : PolicyResult<Nothing>(context)

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
  class FailureWithException(
    val finalException: Throwable,
    val exceptionType: ExceptionType,
    context: Context
  ) : PolicyResult<Nothing>(context)

  /**
   * A [PolicyResult] representing a failed execution through the policy.
   *
   * @constructor Builds a [PolicyResult] representing a failed execution through the policy.
   * @param finalHandledResult The result returned by execution through the policy, which was treated as a handled
   * failure.
   * @param context The policy execution context.
   *
   * @property finalHandledResult The final handled result captured.
   */
  class FailureWithResult<TResult>(val finalHandledResult: TResult, context: Context) :
    PolicyResult<TResult>(context)
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

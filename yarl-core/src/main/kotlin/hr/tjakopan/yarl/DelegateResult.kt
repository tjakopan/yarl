package hr.tjakopan.yarl

/**
 * The captured outcome of executing an individual function.
 */
sealed class DelegateResult<out TResult> {
  fun <T> fold(ifResult: (TResult?) -> T, ifException: (Throwable) -> T): T = when (this) {
    is Result -> ifResult(result)
    is Exception -> ifException(exception)
  }

  /**
   * @property result The result of executing the delegate.
   *
   * @constructor Create an instance of [DelegateResult] representing an execution which returned [result].
   * @param result The result.
   */
  class Result<out TResult>(val result: TResult?) : DelegateResult<TResult>()

  /**
   * @property exception Any exception thrown while executing the delegate.
   *
   * @constructor Create an instance of [DelegateResult] representing an execution which threw [exception].
   * @param exception The exception.
   */
  class Exception(val exception: Throwable) : DelegateResult<Nothing>()
}

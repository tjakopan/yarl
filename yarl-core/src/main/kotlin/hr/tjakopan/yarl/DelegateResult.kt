package hr.tjakopan.yarl

import hr.tjakopan.yarl.annotations.Immutable

@Immutable
sealed class DelegateResult<out R> {
  companion object {
    @JvmStatic
    fun <R> success(value: R): DelegateResult<R> = Success(value)

    @JvmStatic
    fun failure(exception: Throwable): DelegateResult<Nothing> = Failure(exception)

    @JvmStatic
    inline fun <R> runCatching(exceptionPredicates: ExceptionPredicates, block: () -> R): DelegateResult<R> {
      return try {
        success(block())
      } catch (e: Throwable) {
        val exceptionToHandle = exceptionPredicates.firstMatchOrNull(e)
        if (exceptionToHandle != null) {
          failure(exceptionToHandle)
        } else {
          failure(e)
        }
      }
    }
  }

  abstract val isSuccess: Boolean
  abstract val isFailure: Boolean

  inline fun <T> fold(ifSuccess: (R) -> T, ifFailure: (Throwable) -> T): T = when (this) {
    is Success -> ifSuccess(value)
    is Failure -> ifFailure(exception)
  }

  inline fun onSuccess(action: (R) -> Unit): DelegateResult<R> {
    if (isSuccess) action((this as Success).value)
    return this
  }

  inline fun onFailure(action: (Throwable) -> Unit): DelegateResult<R> {
    if (isFailure) action((this as Failure).exception)
    return this
  }

  fun getOrThrow(): R {
    return when (this) {
      is Success -> this.value
      is Failure -> throw this.exception
    }
  }

  @Immutable
  class Success<out R> internal constructor(val value: R) : DelegateResult<R>() {
    override val isSuccess: Boolean = true
    override val isFailure: Boolean = false
  }

  @Immutable
  class Failure internal constructor(val exception: Throwable) : DelegateResult<Nothing>() {
    override val isSuccess = false
    override val isFailure = true
  }
}

package hr.tjakopan.yarl

import java.util.stream.Stream
import kotlin.streams.asSequence

typealias PolicyBuilderWithoutResult = PolicyBuilder.WithoutResult
typealias PolicyBuilderWithResult<TResult> = PolicyBuilder.WithResult<TResult>

/**
 * @property exceptionPredicates Predicates specifying exceptions that the policy is being configured to handle.
 */
sealed class PolicyBuilder<TResult>(internal val exceptionPredicates: ExceptionPredicates) {
  /**
   * Specifies the type of exception that this policy can handle.
   *
   * @param TException The type of the exception to handle.
   * @param exceptionClass The class of the exception to handle.
   * @return The PolicyBuilder instance.
   */
  fun <TException : Throwable> or(exceptionClass: Class<TException>): PolicyBuilder<TResult> {
    exceptionPredicates.add {
      when {
        exceptionClass.isInstance(it) -> it
        else -> null
      }
    }
    return this
  }

  /**
   * Specifies the type of exception that this policy can handle with additional filters on this exception type.
   *
   * @param TException The type of the exception to handle.
   * @param exceptionClass The class of the exception to handle.
   * @param exceptionPredicate The exception predicate to filter the type of exception this policy can handle.
   * @return The PolicyBuilder instance.
   */
  fun <TException : Throwable> or(
    exceptionClass: Class<TException>,
    exceptionPredicate: (TException) -> Boolean
  ): PolicyBuilder<TResult> {
    exceptionPredicates.add {
      @Suppress("UNCHECKED_CAST")
      when {
        exceptionClass.isInstance(it) && exceptionPredicate(it as TException) -> it
        else -> null
      }
    }
    return this
  }

  /**
   * Specifies the type of exception that this policy can handle if found as a cause of a regular [Throwable] or at any
   * level of nesting within [Throwable].
   *
   * @param TException The type of the exception to handle.
   * @param causeClass The class of cause exception to handle.
   * @return The PolicyBuilder instance.
   */
  fun <TException : Throwable> orCause(causeClass: Class<TException>): PolicyBuilder<TResult> {
    exceptionPredicates.add(handleCause { causeClass.isInstance(it) })
    return this
  }

  /**
   * Specifies the type of exception that this policy can handle, with additional filters on this exception type, if
   * found as a cause of a regular [Throwable] or at any level of nesting withing [Throwable].
   *
   * @param TException The type of the exception to handle.
   * @param causeClass The class of cause exception to handle.
   * @return The PolicyBuilder instance.
   */
  fun <TException : Throwable> orCause(
    causeClass: Class<TException>,
    exceptionPredicate: (TException) -> Boolean
  ): PolicyBuilder<TResult> {
    exceptionPredicates.add(handleCause {
      @Suppress("UNCHECKED_CAST")
      causeClass.isInstance(it) && exceptionPredicate(it as TException)
    })
    return this
  }

  companion object {
    @JvmSynthetic
    internal fun handleCause(predicate: (Throwable) -> Boolean): ExceptionPredicate {
      return fun(exception: Throwable): Throwable? {
        return Stream.iterate(exception, Throwable::cause)
          .asSequence()
          .filterNotNull()
          .find(predicate)
      }
    }
  }

  /**
   * Builder class that holds the list of current exception predicates.
   */
  class WithoutResult(exceptionPredicates: ExceptionPredicates) : PolicyBuilder<Nothing>(exceptionPredicates) {
    internal constructor(exceptionPredicate: ExceptionPredicate) : this(ExceptionPredicates()) {
      exceptionPredicates.add(exceptionPredicate)
    }
  }

  /**
   * Builder class that holds the list of current exception and result predicates.
   *
   * @property resultPredicates Predicates specifying results that the policy is being configured to handle.
   */
  class WithResult<TResult>(
    exceptionPredicates: ExceptionPredicates,
    internal val resultPredicates: ResultPredicates<TResult>
  ) : PolicyBuilder<TResult>(exceptionPredicates) {
    @JvmOverloads
    internal constructor(
      exceptionPredicate: ExceptionPredicate? = null,
      resultPredicate: ResultPredicate<TResult>? = null
    ) : this(ExceptionPredicates(), ResultPredicates<TResult>()) {
      if (exceptionPredicate != null) exceptionPredicates.add(exceptionPredicate)
      if (resultPredicate != null) resultPredicates.add(resultPredicate)
    }

    internal constructor(exceptionPredicates: ExceptionPredicates) : this(
      exceptionPredicates,
      ResultPredicates<TResult>()
    )

    /**
     * Specifies the type of result that this policy can handle with additional filters on the result.
     *
     * @param resultPredicate The predicate to filter the results this policy will handle.
     * @return The PolicyBuilder instance.
     */
    fun orResult(resultPredicate: ResultPredicate<TResult>): PolicyBuilder<TResult> {
      resultPredicates.add(resultPredicate)
      return this
    }

    /**
     * Specifies a result value which the policy will handle.
     *
     * @param result The TResult value this policy will handle.
     */
    fun orResult(result: TResult?): PolicyBuilder<TResult> = orResult { it == result }
  }
}

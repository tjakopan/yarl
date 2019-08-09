package hr.tjakopan.yarl

import java.util.stream.Stream

@JvmSynthetic
internal fun handleCause(predicate: (Throwable) -> Boolean): ExceptionPredicate {
  return fun(exception: Throwable): Throwable? {
    return Stream.iterate<Throwable>(exception, Throwable::cause)
      .filter { it != null }
      .filter(predicate)
      .findFirst()
      .orElse(null)
  }
}

/**
 * Builder class that holds the list of current exception predicates.
 */
class PolicyBuilder internal constructor(exceptionPredicate: ExceptionPredicate) {
  /**
   * Predicates specifying exceptions that the policy is being configured to handle.
   */
  @JvmSynthetic
  internal val exceptionPredicates: ExceptionPredicates = ExceptionPredicates().apply { add(exceptionPredicate) }
    @JvmSynthetic get

  /**
   * Specifies the type of exception that this policy can handle.
   *
   * @param TException The type of the exception to handle.
   * @param exceptionClass The class of the exception to handle.
   * @return The PolicyBuilder instance.
   */
  fun <TException : Throwable> or(exceptionClass: Class<TException>): PolicyBuilder {
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
  ): PolicyBuilder {
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
  fun <TException : Throwable> orCause(causeClass: Class<TException>): PolicyBuilder {
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
  ): PolicyBuilder {
    exceptionPredicates.add(handleCause {
      @Suppress("UNCHECKED_CAST")
      causeClass.isInstance(it) && exceptionPredicate(it as TException)
    })
    return this
  }

  /**
   * Specifies the type of result that this policy can handle with additional filters on the result.
   *
   * @param TResult The type of return values this policy will handle.
   * @param resultPredicate The predicate to filter the results this policy will handle.
   * @return The PolicyBuilderGeneric instance.
   */
  fun <TResult> orResult(resultPredicate: ResultPredicate<TResult>): PolicyBuilderGeneric<TResult> =
    PolicyBuilderGeneric<TResult>(exceptionPredicates).orResult(resultPredicate)

  /**
   * Specifies a result value which the policy will handle.
   *
   * @param TResult The type of return values this policy will handle.
   * @param result The TResult value this policy will handle.
   * @return The PolicyBuilderGeneric instance.
   */
  fun <TResult> orResult(result: TResult?): PolicyBuilderGeneric<TResult> = orResult { it == result }
}

/**
 * Builder class that holds the list of current execution predicates filtering TResult result values.
 */
class PolicyBuilderGeneric<TResult> private constructor(
  exceptionPredicates: ExceptionPredicates,
  resultPredicates: ResultPredicates<TResult>
) {
  /**
   * Predicates specifying exceptions that the policy is being configured to handle.
   */
  @JvmSynthetic
  internal val exceptionPredicates: ExceptionPredicates = exceptionPredicates
    @JvmSynthetic get

  /**
   * Predicates specifying results that the policy is being configured to handle.
   */
  @JvmSynthetic
  internal val resultPredicates: ResultPredicates<TResult> = resultPredicates
    @JvmSynthetic get

  private constructor() : this(ExceptionPredicates(), ResultPredicates())

  internal constructor(
    exceptionPredicate: ExceptionPredicate? = null,
    resultPredicate: ResultPredicate<TResult>? = null
  ) : this(ExceptionPredicates(), ResultPredicates()) {
    if (exceptionPredicate != null) exceptionPredicates.add(exceptionPredicate)
    if (resultPredicate != null) orResult(resultPredicate)
  }

  internal constructor(exceptionPredicates: ExceptionPredicates) : this(exceptionPredicates, ResultPredicates())

  /**
   * Specifies the type of result that this policy can handle with additional filters on the result.
   *
   * @param resultPredicate The predicate to filter the results this policy will handle.
   * @return The PolicyBuilderGeneric instance.
   */
  fun orResult(resultPredicate: ResultPredicate<TResult>): PolicyBuilderGeneric<TResult> {
    resultPredicates.add { resultPredicate(it) }
    return this
  }

  /**
   * Specifies a result value which the policy will handle.
   *
   * @param result The TResult value this policy will handle.
   * @return The PolicyBuilderGeneric instance.
   */
  fun orResult(result: TResult?): PolicyBuilderGeneric<TResult> = orResult { it == result }

  /**
   * Specifies the type of exception that this policy can handle.
   *
   * @param TException The type of the exception to handle.
   * @param exceptionClass The class of the exception to handle.
   * @return The PolicyBuilderGeneric instance.
   */
  fun <TException : Throwable> or(exceptionClass: Class<TException>): PolicyBuilderGeneric<TResult> {
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
   * @return The PolicyBuilderGeneric instance.
   */
  fun <TException : Throwable> or(
    exceptionClass: Class<TException>,
    exceptionPredicate: (TException) -> Boolean
  ): PolicyBuilderGeneric<TResult> {
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
   * @return The PolicyBuilderGeneric instance.
   */
  fun <TException : Throwable> orCause(causeClass: Class<TException>): PolicyBuilderGeneric<TResult> {
    exceptionPredicates.add(handleCause { causeClass.isInstance(it) })
    return this
  }

  /**
   * Specifies the type of exception that this policy can handle, with additional filters on this exception type, if
   * found as a cause of a regular [Throwable] or at any level of nesting withing [Throwable].
   *
   * @param TException The type of the exception to handle.
   * @param causeClass The class of cause exception to handle.
   * @return The PolicyBuilderGeneric instance.
   */
  fun <TException : Throwable> orCause(
    causeClass: Class<TException>,
    exceptionPredicate: (TException) -> Boolean
  ): PolicyBuilderGeneric<TResult> {
    exceptionPredicates.add(handleCause {
      @Suppress("UNCHECKED_CAST")
      causeClass.isInstance(it) && exceptionPredicate(it as TException)
    })
    return this
  }
}

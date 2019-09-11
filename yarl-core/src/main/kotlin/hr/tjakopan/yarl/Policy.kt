package hr.tjakopan.yarl

import hr.tjakopan.yarl.utilities.KeyHelper

/**
 * Transient fault handling policies that can be applied to delegates returning results of type [TResult].
 *
 * @constructor Constructs a new instance of a derived [PolicyGeneric] type with the passed [exceptionPredicates] and
 * [resultPredicates].
 * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
 * @param resultPredicates Predicates indicating which results the policy should handle.
 */
abstract class Policy<TResult, B : Policy.Builder<TResult, B>> protected constructor(policyBuilder: Builder<TResult, B>) :
  ISyncPolicy<TResult> {
  override val policyKey: String = policyBuilder.policyKey ?: "$javaClass-${KeyHelper.guidPart()}"

  protected val resultPredicates = policyBuilder.resultPredicates

  protected val exceptionPredicates = policyBuilder.exceptionPredicates

  /**
   * Updates the execution [Context] with context from the executing policy.
   *
   * @param executionContext The execution [Context].
   * @return [Pair.first] is [Context.policyWrapKey] prior to changes by this method. [Pair.second] is
   * [Context.policyKey] prior to changes by this method.
   */
  @JvmSynthetic
  internal open fun setPolicyContext(executionContext: Context): Pair<String?, String?> {
    val pair = Pair(executionContext.policyWrapKey, executionContext.policyKey)

    executionContext.policyKey = policyKey

    return pair
  }

  /**
   * Restores the supplied keys to the execution [Context].
   *
   * @param executionContext The execution [Context].
   * @param priorPolicyWrapKey The [Context.policyWrapKey] prior to execution through this policy.
   * @param priorPolicyKey The [Context.policyKey] prior to execution through this policy.
   */
  @JvmSynthetic
  internal fun restorePolicyContext(executionContext: Context, priorPolicyWrapKey: String?, priorPolicyKey: String?) {
    executionContext.policyWrapKey = priorPolicyWrapKey
    executionContext.policyKey = priorPolicyKey
  }

  override fun execute(context: Context, action: (Context) -> TResult?): TResult? {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    return try {
      implementation(context, action)
    } finally {
      restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
    }
  }

  override fun executeAndCapture(context: Context, action: (Context) -> TResult?): PolicyResult<TResult> {
    return try {
      val result = execute(context, action)
      if (resultPredicates.anyMatch(result)) {
        return PolicyFailureWithResult(result, context)
      }
      PolicySuccess(result, context)
    } catch (exception: Throwable) {
      PolicyFailureWithException(exception, getExceptionType(exceptionPredicates, exception), context)
    }
  }

  /**
   * Defines the implementation of a policy for synchronous executions returning [TResult].
   *
   * @param context The policy execution context.
   * @param action The action passed by calling code to execute through the policy
   * @return A [TResult] result of the execution.
   */
  protected abstract fun implementation(context: Context, action: (Context) -> TResult?): TResult?

  /**
   * Wraps the specified inner policy.
   *
   * @param innerPolicy The inner policy.
   */
//  fun wrap(innerPolicy: ISyncPolicy) = PolicyWrapGeneric(this, innerPolicy)

  /**
   * Wraps the specified inner policy.
   *
   * @param innerPolicy The inner policy.
   */
//  fun wrap(innerPolicy: ISyncPolicyGeneric<TResult>) = PolicyWrapGeneric(this, innerPolicy)

  /**
   * Builder class that holds the list of current execution predicates filtering TResult result values.
   */
  abstract class Builder<TResult, B : Builder<TResult, B>> protected constructor() {
    var policyKey: String? = null

    /**
     * Predicates specifying results that the policy is being configured to handle.
     */
    @JvmSynthetic
    internal var resultPredicates: ResultPredicates<TResult> = ResultPredicates.none()
      @JvmSynthetic get
      @JvmSynthetic set

    /**
     * Predicates specifying exceptions that the policy is being configured to handle.
     */
    @JvmSynthetic
    internal var exceptionPredicates: ExceptionPredicates = ExceptionPredicates.NONE
      @JvmSynthetic get
      @JvmSynthetic set

    /**
     * Specifies a filter on the return result values that this strongly-typed generic policy will handle.
     *
     * @param resultPredicate The predicate to filter the results this policy will handle.
     * @return The PolicyBuilder instance.
     */
    fun handleResult(resultPredicate: (TResult?) -> Boolean): B = orResult(resultPredicate)

    /**
     * Specifies the type of result that this policy can handle with additional filters on the result.
     *
     * @param resultPredicate The predicate to filter the results this policy will handle.
     * @return The PolicyBuilderGeneric instance.
     */
    fun orResult(resultPredicate: ResultPredicate<TResult>): B {
      resultPredicates.add { resultPredicate(it) }
      return `this$`()
    }

    /**
     * Specifies a return result value which the strongly-typed generic policy will handle.
     *
     * @param result The TResult value this policy will handle.
     * @return The PolicyBuilder instance.
     */
    fun handleResult(result: TResult?): B = orResult(result)

    /**
     * Specifies a result value which the policy will handle.
     *
     * @param result The TResult value this policy will handle.
     * @return The PolicyBuilderGeneric instance.
     */
    fun orResult(result: TResult?): B = orResult { it == result }

    /**
     * Specifies the type of exception that this policy can handle.
     *
     * @param TException The type of the exception to handle.
     * @param exceptionClass The class of the exception to handle.
     * @return The PolicyBuilder instance.
     */
    fun <TException : Throwable> handle(exceptionClass: Class<TException>): B = or(exceptionClass)

    /**
     * Specifies the type of exception that this policy can handle.
     *
     * @param TException The type of the exception to handle.
     * @param exceptionClass The class of the exception to handle.
     * @return The PolicyBuilderGeneric instance.
     */
    fun <TException : Throwable> or(exceptionClass: Class<TException>): B {
      exceptionPredicates.add {
        when {
          exceptionClass.isInstance(it) -> it
          else -> null
        }
      }
      return `this$`()
    }

    /**
     * Specifies the type of exception that this policy can handle with additional filters on this exception type.
     *
     * @param TException The type of the exception to handle.
     * @param exceptionClass The class of the exception to handle.
     * @param exceptionPredicate The exception predicate to filter the type of exception this policy can handle.
     * @return The PolicyBuilder instance.
     */
    fun <TException : Throwable> handle(
      exceptionClass: Class<TException>,
      exceptionPredicate: (TException) -> Boolean
    ): B = or(exceptionClass, exceptionPredicate)

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
    ): B {
      exceptionPredicates.add {
        @Suppress("UNCHECKED_CAST")
        when {
          exceptionClass.isInstance(it) && exceptionPredicate(it as TException) -> it
          else -> null
        }
      }
      return `this$`()
    }

    /**
     * Specifies the type of exception that this policy can handle if found as a cause exception of a regular
     * [Throwable], or at any level of nesting within a [Throwable].
     *
     * @param TException The type of the exception to handle.
     * @param causeClass The class of the cause exception to handle.
     * @return The PolicyBuilder instance, for fluent chaining.
     */
    fun <TException : Throwable> handleCause(causeClass: Class<TException>): B = orCause(causeClass)

    /**
     * Specifies the type of exception that this policy can handle if found as a cause of a regular [Throwable] or at any
     * level of nesting within [Throwable].
     *
     * @param TException The type of the exception to handle.
     * @param causeClass The class of cause exception to handle.
     * @return The PolicyBuilderGeneric instance.
     */
    fun <TException : Throwable> orCause(causeClass: Class<TException>): B {
      exceptionPredicates.add(handleCause { causeClass.isInstance(it) })
      return `this$`()
    }

    /**
     * Specifies the type of exception that this policy can handle if found as a cause exception of a regular
     * [Throwable], or at any level of nesting within a [Throwable].
     *
     * @param TException The type of the exception to handle.
     * @param causeClass The class of the cause exception to handle.
     * @return The PolicyBuilder instance, for fluent chaining.
     */
    fun <TException : Throwable> handleCause(
      causeClass: Class<TException>,
      exceptionPredicate: (TException) -> Boolean
    ): B = orCause(causeClass, exceptionPredicate)

    /**
     * Specifies the type of exception that this policy can handle, with additional filters on this exception type, if
     * found as a cause of a regular [Throwable] or at any level of nesting within a [Throwable].
     *
     * @param TException The type of the exception to handle.
     * @param causeClass The class of cause exception to handle.
     * @return The PolicyBuilderGeneric instance.
     */
    fun <TException : Throwable> orCause(
      causeClass: Class<TException>,
      exceptionPredicate: (TException) -> Boolean
    ): B {
      exceptionPredicates.add(handleCause {
        @Suppress("UNCHECKED_CAST")
        causeClass.isInstance(it) && exceptionPredicate(it as TException)
      })
      return `this$`()
    }

    protected abstract fun `this$`(): B
  }
}

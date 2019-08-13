package hr.tjakopan.yarl

import hr.tjakopan.yarl.noop.NoOpPolicyGeneric
import hr.tjakopan.yarl.wrap.PolicyWrapGeneric

/**
 * Transient fault handling policies that can be applied to delegates returning results of type [TResult].
 *
 * @constructor Constructs a new instance of a derived [PolicyGeneric] type with the passed [exceptionPredicates] and
 * [resultPredicates].
 * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
 * @param resultPredicates Predicates indicating which results the policy should handle.
 */
abstract class PolicyGeneric<TResult> internal constructor(
  exceptionPredicates: ExceptionPredicates?,
  resultPredicates: ResultPredicates<TResult>?
) : PolicyBaseGeneric<TResult>(exceptionPredicates, resultPredicates), ISyncPolicyGeneric<TResult> {
  /**
   * Constructs a new instance of a derived [PolicyGeneric] type with the passed [policyBuilderGeneric].
   *
   * @param policyBuilderGeneric A [PolicyBuilderGeneric] indicating which exceptions and results the policy should
   * handle.
   */
  protected constructor(policyBuilderGeneric: PolicyBuilderGeneric<TResult>? = null) : this(
    policyBuilderGeneric?.exceptionPredicates,
    policyBuilderGeneric?.resultPredicates
  )

  override fun withPolicyKey(policyKey: String): ISyncPolicyGeneric<TResult> {
    if (policyKeyInternal != null) throw policyKeyMustBeImmutableException

    policyKeyInternal = policyKey
    return this
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

  override fun executeAndCapture(context: Context, action: (Context) -> TResult?): PolicyResultGeneric<TResult> {
    return try {
      val result = execute(context, action)
      if (resultPredicates.anyMatch(result)) {
        return PolicyGenericFailureWithResult(result, context)
      }
      PolicyGenericSuccess(result, context)
    } catch (exception: Throwable) {
      PolicyGenericFailureWithException(exception, getExceptionType(exceptionPredicates, exception), context)
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
  fun wrap(innerPolicy: ISyncPolicy) = PolicyWrapGeneric(this, innerPolicy)

  /**
   * Wraps the specified inner policy.
   *
   * @param innerPolicy The inner policy.
   */
  fun wrap(innerPolicy: ISyncPolicyGeneric<TResult>) = PolicyWrapGeneric(this, innerPolicy)

  companion object {
    /**
     * Specifies the type of exception that this policy can handle.
     *
     * @param TException The type of the exception to handle.
     * @param exceptionClass The class of the exception to handle.
     * @return The PolicyBuilder instance.
     */
    @JvmStatic
    fun <TException : Throwable, TResult> handle(exceptionClass: Class<TException>): PolicyBuilderGeneric<TResult> =
      PolicyBuilderGeneric({
        when (exceptionClass.isInstance(it)) {
          true -> it
          else -> null
        }
      })

    /**
     * Specifies the type of exception that this policy can handle with additional filters on this exception type.
     *
     * @param TException The type of the exception to handle.
     * @param exceptionClass The class of the exception to handle.
     * @param exceptionPredicate The exception predicate to filter the type of exception this policy can handle.
     * @return The PolicyBuilder instance.
     */
    @JvmStatic
    fun <TException : Throwable, TResult> handle(
      exceptionClass: Class<TException>,
      exceptionPredicate: (TException) -> Boolean
    ): PolicyBuilderGeneric<TResult> =
      PolicyBuilderGeneric({
        @Suppress("UNCHECKED_CAST")
        when (exceptionClass.isInstance(it) && exceptionPredicate(it as TException)) {
          true -> it
          else -> null
        }
      })

    /**
     * Specifies the type of exception that this policy can handle if found as a cause exception of a regular
     * [Throwable], or at any level of nesting within a [Throwable].
     *
     * @param TException The type of the exception to handle.
     * @param causeClass The class of the cause exception to handle.
     * @return The PolicyBuilder instance, for fluent chaining.
     */
    @JvmStatic
    fun <TException : Throwable, TResult> handleCause(causeClass: Class<TException>): PolicyBuilderGeneric<TResult> =
      PolicyBuilderGeneric(handleCause { causeClass.isInstance(it) })

    /**
     * Specifies the type of exception that this policy can handle if found as a cause exception of a regular
     * [Throwable], or at any level of nesting within a [Throwable].
     *
     * @param TException The type of the exception to handle.
     * @param causeClass The class of the cause exception to handle.
     * @return The PolicyBuilder instance, for fluent chaining.
     */
    @JvmStatic
    fun <TException : Throwable, TResult> handleCause(
      causeClass: Class<TException>,
      exceptionPredicate: (TException) -> Boolean
    ): PolicyBuilderGeneric<TResult> =
      PolicyBuilderGeneric(handleCause {
        @Suppress("UNCHECKED_CAST")
        causeClass.isInstance(it) && exceptionPredicate(it as TException)
      })

    /**
     * Specifies a filter on the return result values that this strongly-typed generic policy will handle.
     *
     * @param resultPredicate The predicate to filter the results this policy will handle.
     * @return The PolicyBuilder instance.
     */
    @JvmStatic
    fun <TResult> handleResult(resultPredicate: (TResult?) -> Boolean): PolicyBuilderGeneric<TResult> =
      PolicyBuilderGeneric(resultPredicate = resultPredicate)

    /**
     * Specifies a return result value which the strongly-typed generic policy will handle.
     *
     * @param result The TResult value this policy will handle.
     * @return The PolicyBuilder instance.
     */
    @JvmStatic
    fun <TResult> handleResult(result: TResult?): PolicyBuilderGeneric<TResult> = handleResult { it == result }

    /**
     * Builds a NoOp [PolicyGeneric] that will execute without any custom behavior.
     *
     * @param TResult The type of return values this policy will handle.
     * @return The policy instance.
     */
    @JvmStatic
    fun <TResult> noOp() = NoOpPolicyGeneric<TResult>()
  }
}

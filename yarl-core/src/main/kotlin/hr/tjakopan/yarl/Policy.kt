package hr.tjakopan.yarl

import hr.tjakopan.yarl.wrap.PolicyWrap

/**
 * Transient fault handling policies that can be applied to delegates returning results of type [TResult].
 *
 * @constructor Constructs a new instance of a derived [Policy] type with the passed [exceptionPredicates] and
 * [resultPredicates].
 * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
 * @param resultPredicates Predicates indicating which results the policy should handle.
 */
abstract class Policy<TResult> internal constructor(
  exceptionPredicates: ExceptionPredicates?,
  resultPredicates: ResultPredicates<TResult>?
) : PolicyBase<TResult>(exceptionPredicates, resultPredicates), ISyncPolicy<TResult> {
  /**
   * Constructs a new instance of a derived [Policy] type with the passed [policyBuilder].
   *
   * @param policyBuilder A [Policy] indicating which exceptions and results the policy should
   * handle.
   */
  protected constructor(policyBuilder: PolicyBuilder<TResult>? = null) : this(
    policyBuilder?.exceptionPredicates,
    policyBuilder?.resultPredicates
  )

  override fun withPolicyKey(policyKey: String): ISyncPolicy<TResult> {
    if (policyKeyInternal != null) throw policyKeyMustBeImmutableException

    policyKeyInternal = policyKey
    return this
  }

  override fun execute(action: () -> TResult?): TResult? = execute(Context()) { action() }

  override fun execute(contextData: Map<String, Any>, action: (Context) -> TResult?): TResult? =
    execute(Context(contextData.toMutableMap())) { action(it) }

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

  override fun executeAndCapture(action: () -> TResult?): PolicyResult<TResult> =
    executeAndCapture(Context()) { action() }

  override fun executeAndCapture(
    contextData: Map<String, Any>,
    action: (Context) -> TResult?
  ): PolicyResult<TResult> = executeAndCapture(Context(contextData.toMutableMap())) { action(it) }

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
  fun wrap(innerPolicy: ISyncPolicy<TResult>) = PolicyWrap(this, innerPolicy)

  companion object {
    /**
     * Specifies the type of exception that this policy can handle.
     *
     * @param TException The type of the exception to handle.
     * @param exceptionClass The class of the exception to handle.
     * @return The PolicyBuilder instance.
     */
    @JvmStatic
    fun <TException : Throwable, TResult> handle(exceptionClass: Class<TException>): PolicyBuilder<TResult> =
      PolicyBuilder({
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
    ): PolicyBuilder<TResult> =
      PolicyBuilder({
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
    fun <TException : Throwable, TResult> handleCause(causeClass: Class<TException>): PolicyBuilder<TResult> =
      PolicyBuilder(handleCause { causeClass.isInstance(it) })

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
    ): PolicyBuilder<TResult> =
      PolicyBuilder(handleCause {
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
    fun <TResult> handleResult(resultPredicate: (TResult?) -> Boolean): PolicyBuilder<TResult> =
      PolicyBuilder(resultPredicate = resultPredicate)

    /**
     * Specifies a return result value which the strongly-typed generic policy will handle.
     *
     * @param result The TResult value this policy will handle.
     * @return The PolicyBuilder instance.
     */
    @JvmStatic
    fun <TResult> handleResult(result: TResult?): PolicyBuilder<TResult> = handleResult { it == result }

    /**
     * Creates a [PolicyWrap] of the given policies governing delegates returning values of type [TResult].
     *
     * @param TResult The return type of delegates which may be executed through the policy.
     * @param policies The policies to place in the wrap, outermost (at left) to innermost (at right).
     * @return The PolicyWrap.
     * @throws IllegalArgumentException The enumerable of policies to form the wrap must contain at least two policies.
     */
    @JvmStatic
    fun <TResult> wrap(vararg policies: ISyncPolicy<TResult>): PolicyWrap<TResult> {
      return when (policies.size) {
        0, 1 -> throw IllegalArgumentException("The array of policies to form the wrap must contain at least two policies.")
        2 -> PolicyWrap(policies[0] as Policy<TResult>, policies[1])
        else -> wrap(policies[0], wrap(*policies.drop(1).toTypedArray()))
      }
    }
  }
}

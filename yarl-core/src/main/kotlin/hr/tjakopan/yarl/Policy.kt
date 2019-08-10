package hr.tjakopan.yarl

/**
 * Transient exception handling policies that can be applied to synchronous delegates.
 *
 * @constructor Constructs a new instance of a derived [Policy] type with the passed [exceptionPredicates].
 * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
 */
abstract class Policy internal constructor(exceptionPredicates: ExceptionPredicates?) : PolicyBase(exceptionPredicates),
  ISyncPolicy {
  /**
   * Constructs a new instance of a derived [Policy] type with the passed [PolicyBuilder].
   *
   * @param policyBuilder A [PolicyBuilder] specifying which exceptions the policy should handle.
   */
  protected constructor(policyBuilder: PolicyBuilder? = null) : this(policyBuilder?.exceptionPredicates)

  override fun withPolicyKey(policyKey: String): ISyncPolicy {
    if (policyKeyInternal != null) throw policyKeyMustBeImmutableException

    policyKeyInternal = policyKey
    return this
  }

  override fun execute(action: () -> Unit) = execute(Context()) { action() }

  override fun execute(contextData: Map<String, Any>, action: (Context) -> Unit) =
    execute(Context(contextData.toMutableMap())) { action(it) }

  override fun execute(context: Context, action: (Context) -> Unit) {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    try {
      implementation(context, action)
    } finally {
      restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
    }
  }

  override fun <TResult> execute(action: () -> TResult?): TResult? = execute<TResult>(Context()) { action() }

  override fun <TResult> execute(contextData: Map<String, Any>, action: (Context) -> TResult?): TResult? =
    execute<TResult>(Context(contextData.toMutableMap())) { action(it) }

  override fun <TResult> execute(context: Context, action: (Context) -> TResult?): TResult? {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    return try {
      implementation(context, action)
    } finally {
      restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
    }
  }

  override fun executeAndCapture(action: () -> Unit): PolicyResult = executeAndCapture(Context()) { action() }

  override fun executeAndCapture(contextData: Map<String, Any>, action: (Context) -> Unit): PolicyResult =
    executeAndCapture(Context(contextData.toMutableMap())) { action(it) }

  override fun executeAndCapture(context: Context, action: (Context) -> Unit): PolicyResult {
    return try {
      execute(context, action)
      PolicySuccess(context)
    } catch (exception: Throwable) {
      PolicyFailure(exception, getExceptionType(exceptionPredicates, exception), context)
    }
  }

  override fun <TResult> executeAndCapture(action: () -> TResult?): PolicyResultGeneric<TResult> =
    executeAndCapture<TResult>(Context()) { action() }

  override fun <TResult> executeAndCapture(
    contextData: Map<String, Any>,
    action: (Context) -> TResult?
  ): PolicyResultGeneric<TResult> =
    executeAndCapture<TResult>(Context(contextData.toMutableMap())) { action(it) }

  override fun <TResult> executeAndCapture(
    context: Context,
    action: (Context) -> TResult?
  ): PolicyResultGeneric<TResult> {
    return try {
      PolicyGenericSuccess(execute(context, action), context)
    } catch (exception: Throwable) {
      PolicyGenericFailureWithException(exception, getExceptionType(exceptionPredicates, exception), context)
    }
  }

  /**
   * Defines the implementation of a policy for sync executions with no return value.
   *
   * @param context The policy execution context.
   * @param action The action passed by calling code to execute through the policy.
   */
  protected open fun implementation(context: Context, action: (Context) -> Unit) = implementation<Unit>(context, action)

  /**
   * Defines the implementation of a policy for synchronous executions returning [TResult].
   *
   * @param TResult The type returned by synchronous executions through the implementation.
   * @param context The policy execution context.
   * @param action The action passed by calling code to execute through the policy.
   * @return A [TResult] result of the execution.
   */
  protected abstract fun <TResult> implementation(context: Context, action: (Context) -> TResult?): TResult?

  companion object {
    /**
     * Specifies the type of exception that this policy can handle.
     *
     * @param TException The type of the exception to handle.
     * @param exceptionClass The class of the exception to handle.
     * @return The PolicyBuilder instance, for fluent chaining.
     */
    @JvmStatic
    fun <TException : Throwable> handle(exceptionClass: Class<TException>): PolicyBuilder =
      PolicyBuilder {
        when (exceptionClass.isInstance(it)) {
          true -> it
          else -> null
        }
      }

    /**
     * Specifies the type of exception that this policy can handle.
     *
     * @param TException The type of the exception to handle.
     * @param exceptionClass The class of the exception to handle.
     * @param exceptionPredicate The exception predicate to filter the type of exception this policy can handle.
     * @return The PolicyBuilder instance, for fluent chaining.
     */
    @JvmStatic
    fun <TException : Throwable> handle(
      exceptionClass: Class<TException>,
      exceptionPredicate: (TException) -> Boolean
    ): PolicyBuilder =
      PolicyBuilder {
        @Suppress("UNCHECKED_CAST")
        when (exceptionClass.isInstance(it) && exceptionPredicate(it as TException)) {
          true -> it
          else -> null
        }
      }

    /**
     * Specifies the type of exception that this policy can handle if found as a cause exception of a regular
     * [Throwable], or at any level of nesting within a [Throwable].
     *
     * @param [TException] The type of the exception to handle.
     * @param causeClass The class of the cause exception to handle.
     * @return The PolicyBuilder instance, for fluent chaining.
     */
    @JvmStatic
    fun <TException : Throwable> handleCause(causeClass: Class<TException>): PolicyBuilder =
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
    fun <TException : Throwable> handleCause(
      causeClass: Class<TException>,
      exceptionPredicate: (TException) -> Boolean
    ): PolicyBuilder =
      PolicyBuilder(handleCause {
        @Suppress("UNCHECKED_CAST")
        causeClass.isInstance(it) && exceptionPredicate(it as TException)
      })

    /**
     * Specifies the type of return result that this policy can handle with additional filters on the result.
     *
     * @param TResult The type of return values this policy will handle.
     * @param resultPredicate The predicate to filter the results this policy will handle.
     * @return The PolicyBuilder instance.
     */
    @JvmStatic
    fun <TResult> handleResult(resultPredicate: (TResult?) -> Boolean): PolicyBuilderGeneric<TResult> =
      PolicyBuilderGeneric(resultPredicate = resultPredicate)

    /**
     * Specifies the type of return result that this policy can handle, and a result value which the policy will handle.
     *
     * @param TResult The type of return values this policy will handle.
     * @param result The TResult value this policy will handle.
     * @return The PolicyBuilder instance.
     */
    @JvmStatic
    fun <TResult> handleResult(result: TResult?): PolicyBuilderGeneric<TResult> = handleResult { it == result }
  }
}

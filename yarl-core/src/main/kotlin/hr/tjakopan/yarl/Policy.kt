package hr.tjakopan.yarl

import hr.tjakopan.yarl.noop.NoOpPolicy
import hr.tjakopan.yarl.wrap.PolicyWrap
import hr.tjakopan.yarl.wrap.PolicyWrapGeneric

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

  override fun executeAndCapture(context: Context, action: (Context) -> Unit): PolicyResult {
    return try {
      execute(context, action)
      PolicySuccess(context)
    } catch (exception: Throwable) {
      PolicyFailure(exception, getExceptionType(exceptionPredicates, exception), context)
    }
  }

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
  protected open fun implementation(context: Context, action: (Context) -> Unit): Unit =
    implementation<Unit>(context, action)!!

  /**
   * Defines the implementation of a policy for synchronous executions returning [TResult].
   *
   * @param TResult The type returned by synchronous executions through the implementation.
   * @param context The policy execution context.
   * @param action The action passed by calling code to execute through the policy.
   * @return A [TResult] result of the execution.
   */
  protected abstract fun <TResult> implementation(context: Context, action: (Context) -> TResult?): TResult?

  /**
   * Wraps the specified inner policy.
   *
   * @param innerPolicy The inner policy.
   */
  fun wrap(innerPolicy: ISyncPolicy) = PolicyWrap(this, innerPolicy)

  /**
   * Wraps the specified inner policy.
   *
   * @param TResult The return type of delegates which may be executed through the policy.
   * @param innerPolicy The inner policy.
   */
  fun <TResult> wrap(innerPolicy: ISyncPolicyGeneric<TResult>) = PolicyWrapGeneric(this, innerPolicy)

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

    /**
     * Builds a NoOp [Policy] that will execute without any custom behavior.
     *
     * @return The policy instance.
     */
    @JvmStatic
    fun noOp() = NoOpPolicy()

    /**
     * Creates a [PolicyWrap] of the given policies.
     *
     * @param policies The policies to place in the wrap, outermost (at left) to innermost (at right).
     * @return The PolicyWrap.
     * @throws IllegalArgumentException The enumerable of policies to form the wrap must contain at least two policies.
     */
    @JvmStatic
    fun wrap(vararg policies: ISyncPolicy): PolicyWrap {
      return when (policies.size) {
        0, 1 -> throw IllegalArgumentException("The array of policies to form the wrap must contain at least two policies.")
        2 -> PolicyWrap(policies[0] as Policy, policies[1])
        else -> wrap(policies[0], wrap(*policies.drop(1).toTypedArray()))
      }
    }

    /**
     * Creates a [PolicyWrapGeneric] of the given policies governing delegates returning values of type [TResult].
     *
     * @param TResult The return type of delegates which may be executed through the policy.
     * @param policies The policies to place in the wrap, outermost (at left) to innermost (at right).
     * @return The PolicyWrapGeneric.
     * @throws IllegalArgumentException The enumerable of policies to form the wrap must contain at least two policies.
     */
    @JvmStatic
    fun <TResult> wrap(vararg policies: ISyncPolicyGeneric<TResult>): PolicyWrapGeneric<TResult> {
      return when (policies.size) {
        0, 1 -> throw IllegalArgumentException("The array of policies to form the wrap must contain at least two policies.")
        2 -> PolicyWrapGeneric(policies[0] as PolicyGeneric<TResult>, policies[1])
        else -> wrap(policies[0], wrap(*policies.drop(1).toTypedArray()))
      }
    }
  }
}

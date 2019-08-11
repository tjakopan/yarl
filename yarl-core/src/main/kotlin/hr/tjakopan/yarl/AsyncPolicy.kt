package hr.tjakopan.yarl

import hr.tjakopan.yarl.wrap.AsyncPolicyWrap
import hr.tjakopan.yarl.wrap.AsyncPolicyWrapGeneric
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.function.BiConsumer
import java.util.function.BiFunction

/**
 * Transient exception handling policies that can be applied to asynchronous delegates.
 *
 * @constructor Constructs a new instance of a derived [AsyncPolicy] type with the passed [exceptionPredicates].
 * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
 */
abstract class AsyncPolicy internal constructor(exceptionPredicates: ExceptionPredicates?) :
  PolicyBase(exceptionPredicates), IAsyncPolicy {
  /**
   * Constructs a new instance of a derived [AsyncPolicy] type with the passed [policyBuilder].
   *
   * @param policyBuilder A [PolicyBuilder] specifying which exceptions the policy should handle.
   */
  protected constructor(policyBuilder: PolicyBuilder? = null) : this(policyBuilder?.exceptionPredicates)

  override fun withPolicyKey(policyKey: String): IAsyncPolicy {
    if (policyKeyInternal != null) throw policyKeyMustBeImmutableException

    policyKeyInternal = policyKey
    return this
  }

  override fun executeAsync(action: () -> CompletionStage<Unit>): CompletionStage<Unit> =
    executeAsync(Context()) { action() }

  override fun executeAsync(executor: Executor, action: (Executor) -> CompletionStage<Unit>): CompletionStage<Unit> =
    executeAsync(Context(), executor) { _, ex -> action(ex) }

  override fun executeAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<Unit> =
    executeAsync(Context(contextData.toMutableMap())) { action(it) }

  override fun executeAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<Unit>
  ): CompletionStage<Unit> =
    executeAsync(Context(contextData.toMutableMap()), executor) { ctx, ex -> action(ctx, ex) }

  override fun executeAsync(context: Context, action: (Context) -> CompletionStage<Unit>): CompletionStage<Unit> {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    return implementationAsync(context, action)
      .whenCompleteAsync { _, _ ->
        restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
      }
  }

  override fun executeAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<Unit>
  ): CompletionStage<Unit> {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    return implementationAsync(context, executor, action)
      .whenCompleteAsync(BiConsumer { _, _ ->
        restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
      }, executor)
  }

  override fun <TResult> executeAsyncGeneric(action: () -> CompletionStage<TResult?>): CompletionStage<TResult?> =
    executeAsyncGeneric(Context()) { action() }

  override fun <TResult> executeAsyncGeneric(
    executor: Executor,
    action: (Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    executeAsyncGeneric(Context(), executor) { _, ex -> action(ex) }

  override fun <TResult> executeAsyncGeneric(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    executeAsyncGeneric(Context(contextData.toMutableMap())) { action(it) }

  override fun <TResult> executeAsyncGeneric(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    executeAsyncGeneric(Context(contextData.toMutableMap()), executor) { ctx, ex -> action(ctx, ex) }

  override fun <TResult> executeAsyncGeneric(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    return implementationAsyncGeneric(context, action)
      .whenCompleteAsync { _, _ ->
        restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
      }
  }

  override fun <TResult> executeAsyncGeneric(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    return implementationAsyncGeneric(context, executor, action)
      .whenCompleteAsync(BiConsumer { _, _ ->
        restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
      }, executor)
  }

  override fun executeAndCaptureAsync(action: () -> CompletionStage<Unit>): CompletionStage<PolicyResult> =
    executeAndCaptureAsync(Context()) { action() }

  override fun executeAndCaptureAsync(
    executor: Executor,
    action: (Executor) -> CompletionStage<Unit>
  ): CompletionStage<PolicyResult> =
    executeAndCaptureAsync(Context(), executor) { _, ex -> action(ex) }

  override fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<PolicyResult> =
    executeAndCaptureAsync(Context(contextData.toMutableMap())) { action(it) }

  override fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<Unit>
  ): CompletionStage<PolicyResult> =
    executeAndCaptureAsync(Context(contextData.toMutableMap()), executor) { ctx, ex -> action(ctx, ex) }

  override fun executeAndCaptureAsync(
    context: Context,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<PolicyResult> {
    return executeAsync(context, action)
      .handleAsync { _, exception ->
        when {
          exception != null -> return@handleAsync PolicyFailure(
            exception,
            getExceptionType(exceptionPredicates, exception),
            context
          )
          else -> return@handleAsync PolicySuccess(context)
        }
      }
  }

  override fun executeAndCaptureAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<Unit>
  ): CompletionStage<PolicyResult> {
    return executeAsync(context, executor, action)
      .handleAsync(BiFunction { _, exception ->
        when {
          exception != null -> return@BiFunction PolicyFailure(
            exception,
            getExceptionType(exceptionPredicates, exception),
            context
          )
          else -> return@BiFunction PolicySuccess(context)
        }
      }, executor)
  }

  override fun <TResult> executeAndCaptureAsyncGeneric(action: () -> CompletionStage<TResult?>): CompletionStage<PolicyResultGeneric<TResult?>> =
    executeAndCaptureAsyncGeneric(Context()) { action() }

  override fun <TResult> executeAndCaptureAsyncGeneric(
    executor: Executor,
    action: (Executor) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> =
    executeAndCaptureAsyncGeneric(Context(), executor) { _, ex -> action(ex) }

  override fun <TResult> executeAndCaptureAsyncGeneric(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> =
    executeAndCaptureAsyncGeneric(Context(contextData.toMutableMap())) { action(it) }

  override fun <TResult> executeAndCaptureAsyncGeneric(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> =
    executeAndCaptureAsyncGeneric(Context(contextData.toMutableMap()), executor) { ctx, ex -> action(ctx, ex) }

  override fun <TResult> executeAndCaptureAsyncGeneric(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> {
    return executeAsyncGeneric(context, action)
      .handleAsync { result, exception ->
        when {
          exception != null -> return@handleAsync PolicyGenericFailureWithException<TResult?>(
            exception,
            getExceptionType(exceptionPredicates, exception),
            context
          )
          else -> return@handleAsync PolicyGenericSuccess<TResult?>(result, context)
        }
      }
  }

  override fun <TResult> executeAndCaptureAsyncGeneric(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> {
    return executeAsyncGeneric(context, executor, action)
      .handleAsync(BiFunction { result, exception ->
        when {
          exception != null -> return@BiFunction PolicyGenericFailureWithException(
            exception,
            getExceptionType(exceptionPredicates, exception),
            context
          )
          else -> return@BiFunction PolicyGenericSuccess(result, context)
        }
      }, executor)
  }

  /**
   * Defines the implementation of a policy for async executions with no return value.
   *
   * @param context The policy execution context.
   * @param action The action passed by calling code to execute through the policy.
   * @return A [CompletionStage] representing the result of the execution.
   */
  protected abstract fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<Unit>

  /**
   * Defines the implementation of a policy for async executions with no return value.
   *
   * @param context The policy execution context.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action passed by calling code to execute through the policy.
   * @return A [CompletionStage] representing the result of the execution.
   */
  protected abstract fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<Unit>
  ): CompletionStage<Unit>

  /**
   * Defines the implementation of a policy for async executions returning [TResult].
   *
   * @param TResult The type returned by asynchronous executions through the implementation.
   * @param context The policy execution context.
   * @param action The action passed by calling code to execute through the policy.
   * @return A [CompletionStage] representing the result of the execution.
   */
  protected abstract fun <TResult> implementationAsyncGeneric(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>

  /**
   * Defines the implementation of a policy for async executions returning [TResult].
   *
   * @param TResult The type returned by asynchronous executions through the implementation.
   * @param context The policy execution context.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action passed by calling code to execute through the policy.
   * @return A [CompletionStage] representing the result of the execution.
   */
  protected abstract fun <TResult> implementationAsyncGeneric(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>

  /**
   * Wraps the specified inner policy.
   *
   * @param innerPolicy The inner policy.
   */
  fun wrapAsync(innerPolicy: IAsyncPolicy) = AsyncPolicyWrap(this, innerPolicy)

  /**
   * Wraps the specified inner policy.
   *
   * @param TResult The return type of delegates which may be executed through the policy.
   * @param innerPolicy The inner policy.
   */
  fun <TResult> wrapAsync(innerPolicy: IAsyncPolicyGeneric<TResult>) =
    AsyncPolicyWrapGeneric<TResult>(this, innerPolicy)

  companion object {
    /**
     * Creates a [AsyncPolicyWrap] of the given policies.
     *
     * @param policies The policies to place in the wrap, outermost (at left) to innermost (at right).
     * @return The PolicyWrap.
     * @throws IllegalArgumentException The enumerable of policies to form the wrap must contain at least two policies.
     */
    @JvmStatic
    fun wrapAsync(vararg policies: IAsyncPolicy): AsyncPolicyWrap {
      return when (policies.size) {
        0, 1 -> throw IllegalArgumentException("The array of policies to form the wrap must contain at least two policies.")
        2 -> AsyncPolicyWrap(policies[0] as AsyncPolicy, policies[1])
        else -> wrapAsync(policies[0], wrapAsync(*policies.drop(1).toTypedArray()))
      }
    }

    /**
     * Creates a [AsyncPolicyWrapGeneric] of the given policies.
     *
     * @param TResult The return type of delegates which may be executed through the policy.
     * @param policies The policies to place in the wrap, outermost (at left) to innermost (at right).
     * @return The PolicyWrap.
     * @throws IllegalArgumentException The enumerable of policies to form the wrap must contain at least two policies.
     */
    @JvmStatic
    fun <TResult> wrapAsync(vararg policies: IAsyncPolicyGeneric<TResult>): AsyncPolicyWrapGeneric<TResult> {
      return when (policies.size) {
        0, 1 -> throw IllegalArgumentException("The array of policies to form the wrap must contain at least two policies.")
        2 -> AsyncPolicyWrapGeneric(policies[0] as AsyncPolicyGeneric<TResult>, policies[1])
        else -> wrapAsync(policies[0], wrapAsync(*policies.drop(1).toTypedArray()))
      }
    }
  }
}

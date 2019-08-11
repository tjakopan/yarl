package hr.tjakopan.yarl

import hr.tjakopan.yarl.wrap.AsyncPolicyWrap
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.function.BiConsumer
import java.util.function.BiFunction

/**
 * Transient exception handling policies that can be applied to asynchronous delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 *
 * @constructor Constructs a new instance of a derived [AsyncPolicy] type with the passed [exceptionPredicates]
 * and [resultPredicates].
 * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
 * @param resultPredicates Predicates indicating which results the policy should handle.
 */
abstract class AsyncPolicy<TResult> internal constructor(
  exceptionPredicates: ExceptionPredicates?,
  resultPredicates: ResultPredicates<TResult>?
) : PolicyBase<TResult>(exceptionPredicates, resultPredicates), IAsyncPolicy<TResult> {
  /**
   * Constructs a new instance of a derived [AsyncPolicy] type with the passed [policyBuilder].
   *
   * @param policyBuilder A [PolicyBuilder] indicating which exceptions and results the policy should
   * handle.
   */
  protected constructor(policyBuilder: PolicyBuilder<TResult>? = null) : this(
    policyBuilder?.exceptionPredicates,
    policyBuilder?.resultPredicates
  )

  override fun withPolicyKey(policyKey: String): IAsyncPolicy<TResult> {
    if (policyKeyInternal != null) throw policyKeyMustBeImmutableException

    policyKeyInternal = policyKey
    return this
  }

  override fun executeAsync(action: () -> CompletionStage<TResult?>): CompletionStage<TResult?> =
    executeAsync(Context()) { action() }

  override fun executeAsync(
    executor: Executor,
    action: (Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    executeAsync(Context(), executor) { _, ex -> action(ex) }

  override fun executeAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    executeAsync(Context(contextData.toMutableMap())) { action(it) }

  override fun executeAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    executeAsync(Context(contextData.toMutableMap()), executor) { ctx, ex -> action(ctx, ex) }

  override fun executeAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> {
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
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    return implementationAsync(context, executor, action)
      .whenCompleteAsync(BiConsumer { _, _ ->
        restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
      }, executor)
  }

  override fun executeAndCaptureAsync(action: () -> CompletionStage<TResult?>): CompletionStage<PolicyResult<TResult?>> =
    executeAndCaptureAsync(Context()) { action() }

  override fun executeAndCaptureAsync(
    executor: Executor,
    action: (Executor) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResult<TResult?>> =
    executeAndCaptureAsync(Context(), executor) { _, ex -> action(ex) }

  override fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResult<TResult?>> =
    executeAndCaptureAsync(Context(contextData.toMutableMap())) { action(it) }

  override fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResult<TResult?>> =
    executeAndCaptureAsync(Context(contextData.toMutableMap()), executor) { ctx, ex -> action(ctx, ex) }

  override fun executeAndCaptureAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResult<TResult?>> {
    return executeAsync(context, action)
      .handleAsync { result, exception ->
        when {
          exception != null -> return@handleAsync PolicyFailureWithException<TResult?>(
            exception,
            getExceptionType(exceptionPredicates, exception),
            context
          )
          resultPredicates.anyMatch(result) -> return@handleAsync PolicyFailureWithResult<TResult?>(
            result,
            context
          )
          else -> return@handleAsync PolicySuccess<TResult?>(result, context)
        }
      }
  }

  override fun executeAndCaptureAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResult<TResult?>> {
    return executeAsync(context, executor, action)
      .handleAsync(BiFunction { result, exception ->
        when {
          exception != null -> return@BiFunction PolicyFailureWithException(
            exception,
            getExceptionType(exceptionPredicates, exception),
            context
          )
          resultPredicates.anyMatch(result) -> return@BiFunction PolicyFailureWithResult(result, context)
          else -> return@BiFunction PolicySuccess(result, context)
        }
      }, executor)
  }

  /**
   * Defines the implementation of a policy for async executions returning [TResult].
   *
   * @param context The policy execution context.
   * @param action The action passed by calling code to execute through the policy.
   * @return A [CompletionStage] representing the result of the execution.
   */
  protected abstract fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>

  /**
   * Defines the implementation of a policy for async executions returning [TResult].
   *
   * @param context The policy execution context.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action passed by calling code to execute through the policy.
   * @return A [CompletionStage] representing the result of the execution.
   */
  protected abstract fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>

  /**
   * Wraps the specified inner policy.
   *
   * @param innerPolicy The inner policy
   */
  fun wrapAsync(innerPolicy: IAsyncPolicy<TResult>) = AsyncPolicyWrap(this, innerPolicy)

  companion object {
    /**
     * Creates a [AsyncPolicyWrap] of the given policies.
     *
     * @param TResult The return type of delegates which may be executed through the policy.
     * @param policies The policies to place in the wrap, outermost (at left) to innermost (at right).
     * @return The PolicyWrap.
     * @throws IllegalArgumentException The enumerable of policies to form the wrap must contain at least two policies.
     */
    @JvmStatic
    fun <TResult> wrapAsync(vararg policies: IAsyncPolicy<TResult>): AsyncPolicyWrap<TResult> {
      return when (policies.size) {
        0, 1 -> throw IllegalArgumentException("The array of policies to form the wrap must contain at least two policies.")
        2 -> AsyncPolicyWrap(policies[0] as AsyncPolicy<TResult>, policies[1])
        else -> wrapAsync(policies[0], wrapAsync(*policies.drop(1).toTypedArray()))
      }
    }
  }
}

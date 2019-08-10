package hr.tjakopan.yarl

import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.function.BiConsumer
import java.util.function.BiFunction

/**
 * Transient exception handling policies that can be applied to asynchronous delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 *
 * @constructor Constructs a new instance of a derived [AsyncPolicyGeneric] type with the passed [exceptionPredicates]
 * and [resultPredicates].
 * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
 * @param resultPredicates Predicates indicating which results the policy should handle.
 */
abstract class AsyncPolicyGeneric<TResult> internal constructor(
  exceptionPredicates: ExceptionPredicates?,
  resultPredicates: ResultPredicates<TResult>?
) : PolicyBaseGeneric<TResult>(exceptionPredicates, resultPredicates), IAsyncPolicyGeneric<TResult> {
  /**
   * Constructs a new instance of a derived [AsyncPolicyGeneric] type with the passed [policyBuilderGeneric].
   *
   * @param policyBuilderGeneric A [PolicyBuilderGeneric] indicating which exceptions and results the policy should
   * handle.
   */
  protected constructor(policyBuilderGeneric: PolicyBuilderGeneric<TResult>? = null) : this(
    policyBuilderGeneric?.exceptionPredicates,
    policyBuilderGeneric?.resultPredicates
  )

  override fun withPolicyKey(policyKey: String): IAsyncPolicyGeneric<TResult> {
    if (policyKeyInternal != null) throw policyKeyMustBeImmutableException

    policyKeyInternal = policyKey
    return this
  }

  override fun executeAsync(action: () -> CompletionStage<TResult?>): CompletionStage<TResult?> =
    executeAsync(Context()) { action() }

  override fun executeAsync(executor: Executor, action: () -> CompletionStage<TResult?>): CompletionStage<TResult?> =
    executeAsync(Context(), executor) { action() }

  override fun executeAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    executeAsync(Context(contextData.toMutableMap())) { action(it) }

  override fun executeAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    executeAsync(Context(contextData.toMutableMap()), executor) { action(it) }

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
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    return implementationAsync(context, executor, action)
      .whenCompleteAsync(BiConsumer { _, _ ->
        restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
      }, executor)
  }

  override fun executeAndCaptureAsync(action: () -> CompletionStage<TResult?>): CompletionStage<PolicyResultGeneric<TResult?>> =
    executeAndCaptureAsync(Context()) { action() }

  override fun executeAndCaptureAsync(
    executor: Executor,
    action: () -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> =
    executeAndCaptureAsync(Context(), executor) { action() }

  override fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> =
    executeAndCaptureAsync(Context(contextData.toMutableMap())) { action(it) }

  override fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> =
    executeAndCaptureAsync(Context(contextData.toMutableMap()), executor) { action(it) }

  override fun executeAndCaptureAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> {
    return executeAsync(context, action)
      .handleAsync { result, exception ->
        when {
          exception != null -> return@handleAsync PolicyGenericFailureWithException(
            exception,
            getExceptionType(exceptionPredicates, exception),
            context
          )
          resultPredicates.anyMatch(result) -> return@handleAsync PolicyGenericFailureWithResult(result, context)
          else -> return@handleAsync PolicyGenericSuccess(result, context)
        }
      }
  }

  override fun executeAndCaptureAsync(
    context: Context,
    executor: Executor,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>> {
    return executeAsync(context, executor, action)
      .handleAsync(BiFunction { result, exception ->
        when {
          exception != null -> return@BiFunction PolicyGenericFailureWithException(
            exception,
            getExceptionType(exceptionPredicates, exception),
            context
          )
          resultPredicates.anyMatch(result) -> return@BiFunction PolicyGenericFailureWithResult(result, context)
          else -> return@BiFunction PolicyGenericSuccess(result, context)
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
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>
}

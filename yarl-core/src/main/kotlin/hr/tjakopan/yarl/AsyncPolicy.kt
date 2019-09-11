package hr.tjakopan.yarl

import hr.tjakopan.yarl.utilities.KeyHelper
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
abstract class AsyncPolicy<TResult, B : Policy.Builder<TResult, B>> protected constructor(
  policyBuilder: Policy.Builder<TResult, B>
) : IAsyncPolicy<TResult> {
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

  override fun executeAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> {
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
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> {
    val priorPolicyKeys = setPolicyContext(context)
    val priorPolicyWrapKey = priorPolicyKeys.first
    val priorPolicyKey = priorPolicyKeys.second
    return implementationAsync(context, executor, action)
      .whenCompleteAsync(BiConsumer { _, _ ->
        restorePolicyContext(context, priorPolicyWrapKey, priorPolicyKey)
      }, executor)
  }

  override fun executeAndCaptureAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<PolicyResult<TResult>> {
    return executeAsync(context, action)
      .handleAsync { result, exception ->
        when {
          exception != null -> return@handleAsync PolicyFailureWithException(
            exception,
            getExceptionType(exceptionPredicates, exception),
            context
          )
          resultPredicates.anyMatch(result) -> return@handleAsync PolicyFailureWithResult<TResult>(
            result,
            context
          )
          else -> return@handleAsync PolicySuccess<TResult>(result, context)
        }
      }
  }

  override fun executeAndCaptureAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<PolicyResult<TResult>> {
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
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult>

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
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult>

  /**
   * Wraps the specified inner policy.
   *
   * @param innerPolicy The inner policy
   */
//  fun wrapAsync(innerPolicy: IAsyncPolicy) = AsyncPolicyWrapGeneric(this, innerPolicy)

  /**
   * Wraps the specified inner policy.
   *
   * @param innerPolicy The inner policy
   */
//  fun wrapAsync(innerPolicy: IAsyncPolicyGeneric<TResult>) = AsyncPolicyWrapGeneric(this, innerPolicy)
}

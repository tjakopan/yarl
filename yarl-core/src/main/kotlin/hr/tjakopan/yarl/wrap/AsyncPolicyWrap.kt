package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.IAsyncPolicy
import hr.tjakopan.yarl.IsPolicy
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * A policy that allows two (and by recursion more) async Polly policies to wrap executions of async delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 */
class AsyncPolicyWrap<TResult> internal constructor(outer: AsyncPolicy<TResult>, inner: IAsyncPolicy<TResult>) :
  AsyncPolicy<TResult>(outer.exceptionPredicates, outer.resultPredicates), IPolicyWrap {
  private val _outer: IAsyncPolicy<TResult> = outer
  private val _inner: IAsyncPolicy<TResult> = inner

  override val outer: IsPolicy = _outer
  override val inner: IsPolicy = _inner

  override fun setPolicyContext(executionContext: Context): Pair<String?, String?> {
    val priorPolicyKeys = Pair(executionContext.policyWrapKey, executionContext.policyKey)
    if (executionContext.policyWrapKey == null) executionContext.policyWrapKey = policyKey
    super.setPolicyContext(executionContext)

    return priorPolicyKeys
  }

  override fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    AsyncPolicyWrapEngine.implementationAsyncGeneric(context, _outer, _inner, action)

  override fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    AsyncPolicyWrapEngine.implementationAsyncGeneric(context, executor, _outer, _inner, action)
}

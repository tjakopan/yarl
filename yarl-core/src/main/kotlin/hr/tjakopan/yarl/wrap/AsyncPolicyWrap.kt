package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.IAsyncPolicy
import hr.tjakopan.yarl.Policy
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * A policy that allows two (and by recursion more) async Polly policies to wrap executions of async delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 */
class AsyncPolicyWrap<TResult> private constructor(policyBuilder: Builder<TResult>) :
  AsyncPolicy<TResult, AsyncPolicyWrap.Builder<TResult>>(policyBuilder), IPolicyWrap {
  override val outer = policyBuilder.outer
  override val inner = policyBuilder.inner

  override fun setPolicyContext(executionContext: Context): Pair<String?, String?> {
    val priorPolicyKeys = Pair(executionContext.policyWrapKey, executionContext.policyKey)
    if (executionContext.policyWrapKey == null) executionContext.policyWrapKey = policyKey
    super.setPolicyContext(executionContext)

    return priorPolicyKeys
  }

  override fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    AsyncPolicyWrapEngine.implementationAsync(context, outer, inner, action)

  override fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    AsyncPolicyWrapEngine.implementationAsync(context, executor, outer, inner, action)

  class Builder<TResult>(
    @JvmSynthetic internal val outer: IAsyncPolicy<TResult>,
    @JvmSynthetic internal val inner: IAsyncPolicy<TResult>
  ) : Policy.Builder<TResult, Builder<TResult>>() {
    override fun self(): Builder<TResult> = this

    fun build() = AsyncPolicyWrap(this)
  }
}

package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.ISyncPolicy
import hr.tjakopan.yarl.Policy

/**
 * A policy that allows two (and by recursion more) Polly policies to wrap executions of delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 */
class PolicyWrap<TResult> private constructor(policyBuilder: Builder<TResult>) :
  Policy<TResult, PolicyWrap.Builder<TResult>>(policyBuilder), IPolicyWrap {
  override val outer = policyBuilder.outer
  override val inner = policyBuilder.inner

  override fun setPolicyContext(executionContext: Context): Pair<String?, String?> {
    val priorPolicyKeys = Pair(executionContext.policyWrapKey, executionContext.policyKey)
    if (executionContext.policyWrapKey == null) executionContext.policyWrapKey = policyKey
    super.setPolicyContext(executionContext)

    return priorPolicyKeys
  }

  override fun implementation(context: Context, action: (Context) -> TResult?): TResult? =
    PolicyWrapEngine.implementation1(context, outer, inner, action)

  class Builder<TResult>(
    @JvmSynthetic internal val outer: ISyncPolicy<TResult>,
    @JvmSynthetic internal val inner: ISyncPolicy<TResult>
  ) : Policy.Builder<TResult, Builder<TResult>>() {
    override fun self(): Builder<TResult> = this

    fun build() = PolicyWrap(this)
  }
}

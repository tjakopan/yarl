package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.ISyncPolicy
import hr.tjakopan.yarl.IsPolicy
import hr.tjakopan.yarl.Policy

/**
 * A policy that allows two (and by recursion more) Polly policies to wrap executions of delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 */
class PolicyWrap<TResult> internal constructor(outer: Policy<TResult>, inner: ISyncPolicy<TResult>) :
  Policy<TResult>(outer.exceptionPredicates, outer.resultPredicates), IPolicyWrap {
  private val _outer: ISyncPolicy<TResult> = outer
  private val _inner: ISyncPolicy<TResult> = inner

  override val outer: IsPolicy = _outer
  override val inner: IsPolicy = _inner

  override fun setPolicyContext(executionContext: Context): Pair<String?, String?> {
    val priorPolicyKeys = Pair(executionContext.policyWrapKey, executionContext.policyKey)
    if (executionContext.policyWrapKey == null) executionContext.policyWrapKey = policyKey
    super.setPolicyContext(executionContext)

    return priorPolicyKeys
  }

  override fun implementation(context: Context, action: (Context) -> TResult?): TResult? =
    PolicyWrapEngine.implementation(context, _outer, _inner, action)
}

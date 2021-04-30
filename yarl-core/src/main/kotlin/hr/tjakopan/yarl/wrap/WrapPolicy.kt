package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.ISyncPolicy
import hr.tjakopan.yarl.Policy

class WrapPolicy<R> internal constructor(policyBuilder: WrapPolicyBuilder<R>) :
  Policy<R, WrapPolicyBuilder<R>>(policyBuilder), IWrapPolicy {
  companion object WrapPolicy {
    @JvmStatic
    fun <R> builder(): WrapPolicyBuilder<R> = WrapPolicyBuilder()
  }

  override val outer: ISyncPolicy<R> = policyBuilder.outer
  override val inner: ISyncPolicy<R> = policyBuilder.inner

  override fun execute(context: Context, action: (Context) -> R): R {
    val executionContext = context.copy(policyWrapKey = policyKey)
    return super.execute(executionContext, action)
  }

  override fun implementation(context: Context, action: (Context) -> R): R =
    WrapEngine.implementation(action, context, outer, inner)
}

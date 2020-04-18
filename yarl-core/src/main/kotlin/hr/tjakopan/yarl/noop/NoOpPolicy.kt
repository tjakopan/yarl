package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy

class NoOpPolicy<R> internal constructor(policyBuilder: NoOpPolicyBuilder<R>) :
  Policy<R, NoOpPolicyBuilder<R>>(policyBuilder), INoOpPolicy {
  override fun implementation(context: Context, action: (Context) -> R): R = NoOpEngine.implementation(context, action)
}

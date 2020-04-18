package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context

class AsyncNoOpPolicy<R> internal constructor(policyBuilder: NoOpPolicyBuilder<R>) :
  AsyncPolicy<R, NoOpPolicyBuilder<R>>(policyBuilder), INoOpPolicy {
  override suspend fun implementation(context: Context, action: suspend (Context) -> R): R =
    NoOpEngine.implementation(context, action)
}

package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context

class AsyncNoOpPolicy<R> internal constructor() :
  AsyncPolicy<R, NoOpPolicyBuilder<R>>(NoOpPolicyBuilder()), INoOpPolicy {
  @JvmSynthetic
  override suspend fun implementation(context: Context, action: suspend (Context) -> R): R =
    NoOpEngine.implementation(context, action)
}

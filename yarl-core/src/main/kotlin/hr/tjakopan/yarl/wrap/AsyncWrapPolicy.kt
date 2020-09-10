package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.IAsyncPolicy

class AsyncWrapPolicy<R> internal constructor(policyBuilder: AsyncWrapPolicyBuilder<R>) :
  AsyncPolicy<R, AsyncWrapPolicyBuilder<R>>(policyBuilder), IWrapPolicy {
  companion object WrapPolicy {
    @JvmStatic
    fun <R> builder(): AsyncWrapPolicyBuilder<R> = AsyncWrapPolicyBuilder()
  }

  override val outer: IAsyncPolicy<R> = policyBuilder.outer
  override val inner: IAsyncPolicy<R> = policyBuilder.inner

  override suspend fun execute(context: Context, action: suspend (Context) -> R): R {
    val executionContext = context.copy(policyWrapKey = policyKey)
    return super.execute(executionContext, action)
  }

  override suspend fun implementation(context: Context, action: suspend (Context) -> R): R =
    WrapEngine.implementation(action, context, outer, inner)
}

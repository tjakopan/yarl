package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.IAsyncPolicy
import hr.tjakopan.yarl.PolicyBuilder

@JvmSuppressWildcards
class AsyncWrapPolicyBuilder<R> : PolicyBuilder<R, AsyncWrapPolicyBuilder<R>>() {
  @JvmSynthetic
  internal lateinit var outer: IAsyncPolicy<R>

  @JvmSynthetic
  internal lateinit var inner: IAsyncPolicy<R>

  fun wrap(outerPolicy: IAsyncPolicy<R>, innerPolicy: IAsyncPolicy<R>): AsyncWrapPolicy<R> {
    this.outer = outerPolicy
    this.inner = innerPolicy
    return AsyncWrapPolicy(this)
  }

  fun wrap(vararg policies: IAsyncPolicy<R>): AsyncWrapPolicy<R> = when (policies.size) {
    0, 1 -> throw IllegalArgumentException("The policies to form the wrap must contain at least two policies.")
    2 -> {
      this.outer = policies[0]
      this.inner = policies[1]
      AsyncWrapPolicy(this)
    }
    else -> wrap(policies[0], wrap(*policies.drop(1).toTypedArray()))
  }

  override fun self(): AsyncWrapPolicyBuilder<R> = this
}

package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.ISyncPolicy
import hr.tjakopan.yarl.PolicyBuilder

@JvmSuppressWildcards
class WrapPolicyBuilder<R> : PolicyBuilder<R, WrapPolicyBuilder<R>>() {
  @JvmSynthetic
  internal lateinit var outer: ISyncPolicy<R>

  @JvmSynthetic
  internal lateinit var inner: ISyncPolicy<R>

  fun wrap(outerPolicy: ISyncPolicy<R>, innerPolicy: ISyncPolicy<R>): WrapPolicy<R> {
    this.outer = outerPolicy
    this.inner = innerPolicy
    return WrapPolicy(this)
  }

  fun wrap(vararg policies: ISyncPolicy<R>): WrapPolicy<R> = when (policies.size) {
    0, 1 -> throw IllegalArgumentException("The policies to form the wrap must contain at least two policies.")
    2 -> {
      this.outer = policies[0]
      this.inner = policies[1]
      WrapPolicy(this)
    }
    else -> wrap(policies[0], wrap(*policies.drop(1).toTypedArray()))
  }

  override fun self(): WrapPolicyBuilder<R> = this
}

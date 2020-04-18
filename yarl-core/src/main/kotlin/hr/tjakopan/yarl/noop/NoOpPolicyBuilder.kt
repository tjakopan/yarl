package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.PolicyBuilder

class NoOpPolicyBuilder<R> : PolicyBuilder<R, NoOpPolicyBuilder<R>>() {
  fun noOp(): NoOpPolicy<R> = NoOpPolicy(this)

  fun noOpAsync(): AsyncNoOpPolicy<R> = AsyncNoOpPolicy(this)

  override fun self(): NoOpPolicyBuilder<R> = this
}

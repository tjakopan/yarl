package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.PolicyBuilder

class NoOpPolicyBuilder<R> : PolicyBuilder<R, NoOpPolicyBuilder<R>>() {
  override fun self(): NoOpPolicyBuilder<R> = this
}

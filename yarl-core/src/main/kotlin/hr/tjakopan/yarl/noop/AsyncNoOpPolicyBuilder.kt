package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.PolicyBuilder

class AsyncNoOpPolicyBuilder<R> : PolicyBuilder<R, AsyncNoOpPolicyBuilder<R>>() {
  fun noOp(): AsyncNoOpPolicy<R> = AsyncNoOpPolicy(this)

  override fun self(): AsyncNoOpPolicyBuilder<R> = this
}

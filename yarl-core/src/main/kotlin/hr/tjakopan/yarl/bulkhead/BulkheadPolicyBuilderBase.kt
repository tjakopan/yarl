package hr.tjakopan.yarl.bulkhead

import hr.tjakopan.yarl.PolicyBuilder
import kotlin.properties.Delegates

abstract class BulkheadPolicyBuilderBase<R, out B : BulkheadPolicyBuilderBase<R, B>> protected constructor() :
  PolicyBuilder<R, B>() {
  internal var maxParallelization by Delegates.notNull<Int>()

  @JvmSynthetic
  internal var maxQueueingActions: Int = 0
}

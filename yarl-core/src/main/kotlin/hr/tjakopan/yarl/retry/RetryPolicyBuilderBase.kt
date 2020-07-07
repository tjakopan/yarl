package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.PolicyBuilder
import java.time.Duration

abstract class RetryPolicyBuilderBase<R, B : RetryPolicyBuilderBase<R, B>> protected constructor() :
  PolicyBuilder<R, B>() {
  @JvmSynthetic
  internal var permittedRetryCount: Int = Int.MAX_VALUE

  @JvmSynthetic
  internal var sleepDurationsIterable: Iterable<Duration> = listOf()
}

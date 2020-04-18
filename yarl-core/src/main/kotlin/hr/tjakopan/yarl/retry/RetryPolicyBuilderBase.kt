package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.PolicyBuilder
import java.time.Duration

abstract class RetryPolicyBuilderBase<R, B : RetryPolicyBuilderBase<R, B>> protected constructor() :
  PolicyBuilder<R, B>() {
  internal var permittedRetryCount: Int = Int.MAX_VALUE
  internal var sleepDurationsIterable: Iterable<Duration> = listOf()
}

package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import hr.tjakopan.yarl.PolicyBuilder
import java.time.Duration

abstract class RetryPolicyBuilderBase<R, out B : RetryPolicyBuilderBase<R, B>> protected constructor() :
  PolicyBuilder<R, B>() {
  @JvmSynthetic
  internal var permittedRetryCount: Int = Int.MAX_VALUE

  @JvmSynthetic
  internal var sleepDurationsIterable: Iterable<Duration> = listOf()

  @JvmSynthetic
  internal var sleepDurationProvider: ((Int, DelegateResult<R>, Context) -> Duration)? = null
}

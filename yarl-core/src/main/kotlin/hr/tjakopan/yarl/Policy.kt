package hr.tjakopan.yarl

import hr.tjakopan.yarl.noop.AsyncNoOpPolicyBuilder
import hr.tjakopan.yarl.noop.NoOpPolicyBuilder
import hr.tjakopan.yarl.retry.AsyncRetryPolicyBuilder
import hr.tjakopan.yarl.retry.RetryPolicyBuilder

abstract class Policy<R, B : PolicyBuilder<R, B>> protected constructor(policyBuilder: PolicyBuilder<R, B>) :
  PolicyBase<R, B>(policyBuilder), ISyncPolicy<R> {
  override fun execute(context: Context, action: (Context) -> R): R {
    val executionContext: Context = context.copy(policyKey = policyKey)
    return implementation(executionContext, action)
  }

  override fun executeAndCapture(context: Context, action: (Context) -> R): PolicyResult<R> {
    return try {
      val result: R = execute(context, action)
      if (resultPredicates.anyMatch(result)) {
        return PolicyResult.failureWithResult(result, context)
      }
      return PolicyResult.success(result, context)
    } catch (exception: Throwable) {
      PolicyResult.failureWithException(exception, getExceptionType(exceptionPredicates, exception), context)
    }
  }

  protected abstract fun implementation(context: Context, action: (Context) -> R): R

  companion object Policy {
    @JvmStatic
    fun <R> noOp(): NoOpPolicyBuilder<R> = NoOpPolicyBuilder()

    @JvmStatic
    fun <R> asyncNoOp(): AsyncNoOpPolicyBuilder<R> = AsyncNoOpPolicyBuilder()

    @JvmStatic
    fun <R> retry(): RetryPolicyBuilder<R> = RetryPolicyBuilder()

    @JvmStatic
    fun <R> asyncRetry(): AsyncRetryPolicyBuilder<R> = AsyncRetryPolicyBuilder();
  }
}

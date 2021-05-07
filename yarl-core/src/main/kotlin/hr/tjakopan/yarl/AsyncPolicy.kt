package hr.tjakopan.yarl

abstract class AsyncPolicy<R, out B : PolicyBuilder<R, B>> protected constructor(policyBuilder: PolicyBuilder<R, B>) :
  PolicyBase<R, B>(policyBuilder), IAsyncPolicy<R> {
  @JvmSynthetic
  override suspend fun execute(context: Context, action: suspend (Context) -> R): R {
    val priorPolicyKey = context.policyKey
    context.policyKey = policyKey
    try {
      return implementation(context, action)
    } finally {
      context.policyKey = priorPolicyKey
    }
  }

  @JvmSynthetic
  override suspend fun executeAndCapture(context: Context, action: suspend (Context) -> R): PolicyResult<R> {
    try {
      val result = execute(context, action)
      if (resultPredicates.anyMatch(result)) {
        return PolicyResult.failureWithResult(result, context)
      }
      return PolicyResult.success(result, context)
    } catch (e: Throwable) {
      return PolicyResult.failureWithException(e, getExceptionType(exceptionPredicates, e), context)
    }
  }

  @JvmSynthetic
  protected abstract suspend fun implementation(context: Context, action: suspend (Context) -> R): R
}

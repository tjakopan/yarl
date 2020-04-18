package hr.tjakopan.yarl

abstract class AsyncPolicy<R, B : PolicyBuilder<R, B>> protected constructor(policyBuilder: PolicyBuilder<R, B>) :
  PolicyBase<R, B>(policyBuilder), IAsyncPolicy<R> {
  override suspend fun execute(context: Context, action: suspend (Context) -> R): R {
    val executionContext = context.copy(policyKey = policyKey)
    return implementation(executionContext, action)
  }

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

  protected abstract suspend fun implementation(context: Context, action: suspend (Context) -> R): R
}

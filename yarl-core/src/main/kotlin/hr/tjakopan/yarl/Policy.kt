package hr.tjakopan.yarl

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
}

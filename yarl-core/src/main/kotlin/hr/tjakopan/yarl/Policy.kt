package hr.tjakopan.yarl

abstract class Policy<R, out B : PolicyBuilder<R, B>> protected constructor(policyBuilder: PolicyBuilder<R, B>) :
  PolicyBase<R, B>(policyBuilder), ISyncPolicy<R> {
  companion object Policy

  override fun execute(context: Context, action: (Context) -> R): R {
    val priorPolicyKey = context.policyKey
    context.policyKey = policyKey
    try {
      return implementation(context, action)
    } finally {
      context.policyKey = priorPolicyKey
    }
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

package hr.tjakopan.yarl

abstract class PolicyBase<R, B : PolicyBuilder<R, B>> protected constructor(policyBuilder: PolicyBuilder<R, B>) {
  internal companion object {
    @JvmSynthetic
    internal fun getExceptionType(exceptionPredicates: ExceptionPredicates, exception: Throwable): ExceptionType =
      when {
        exceptionPredicates.firstMatchOrNull(exception) != null -> ExceptionType.HANDLED_BY_THIS_POLICY
        else -> ExceptionType.UNHANDLED
      }
  }

  val policyKey: String = when {
    policyBuilder.policyKey != null -> policyBuilder.policyKey
    else -> "${javaClass.simpleName}-${hr.tjakopan.yarl.utilities.KeyHelper.guidPart()}"
  } as String

  protected val resultPredicates = policyBuilder.resultPredicates

  protected val exceptionPredicates = policyBuilder.exceptionPredicates
}

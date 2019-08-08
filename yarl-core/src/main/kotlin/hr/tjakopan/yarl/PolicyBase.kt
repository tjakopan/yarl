package hr.tjakopan.yarl

import hr.tjakopan.yarl.utilities.KeyHelper

typealias PolicyBaseWithoutResult = PolicyBase.WithoutResult
typealias PolicyBaseWithResult<TResult> = PolicyBase.WithResult<TResult>

/**
 * Implements elements common to sync and async policies.
 */
sealed class PolicyBase<TResult>(
  protected val exceptionPredicates: ExceptionPredicates, policyKey: String? = null
) {
  internal companion object {
    @JvmSynthetic
    internal fun getExceptionType(exceptionPredicates: ExceptionPredicates, exception: Throwable): ExceptionType {
      return when (exceptionPredicates.firstMatchOrNull(exception) != null) {
        true -> ExceptionType.HANDLED_BY_THIS_POLICY
        false -> ExceptionType.UNHANDLED
      }
    }
  }

  val policyKey: String = policyKey ?: "$javaClass-${KeyHelper.guidPart()}"

  @JvmSynthetic
  internal open fun setPolicyContext(executionContext: Context): Pair<String?, String?> {
    val pair = Pair(executionContext.policyWrapKey, executionContext.policyKey)
    executionContext.policyKey = policyKey
    return pair
  }

  @JvmSynthetic
  internal fun restorePolicyContext(executionContext: Context, priorPolicyWrapKey: String?, priorPolicyKey: String?) {
    executionContext.policyWrapKey = priorPolicyWrapKey
    executionContext.policyKey = priorPolicyKey
  }

  abstract class WithoutResult internal constructor(
    exceptionPredicates: ExceptionPredicates,
    policyKey: String? = null
  ) : PolicyBase<Nothing>(exceptionPredicates, policyKey) {
    protected constructor(
      policyBuilder: PolicyBuilderWithoutResult,
      policyKey: String? = null
    ) : this(policyBuilder.exceptionPredicates, policyKey)
  }

  abstract class WithResult<TResult> internal constructor(
    exceptionPredicates: ExceptionPredicates,
    val resultPredicates: ResultPredicates<TResult> = ResultPredicates<TResult>(),
    policyKey: String? = null
  ) : PolicyBase<TResult>(exceptionPredicates, policyKey) {
    protected constructor(policyBuilder: PolicyBuilderWithResult<TResult>, policyKey: String? = null) : this(
      policyBuilder.exceptionPredicates,
      policyBuilder.resultPredicates,
      policyKey
    )
  }
}

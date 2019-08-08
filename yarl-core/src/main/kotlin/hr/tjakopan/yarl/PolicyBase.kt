package hr.tjakopan.yarl

import hr.tjakopan.yarl.utilities.KeyHelper

typealias PolicyBaseWithoutResult = PolicyBase.WithoutResult
typealias PolicyBaseWithResult<TResult> = PolicyBase.WithResult<TResult>

/**
 * Implements elements common to both non-generic and generic policies, and sync and async policies.
 *
 * @property exceptionPredicates Predicates indicating which exceptions the policy handles.
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

  /**
   * A key intended to be unique to each [IsPolicy] instance, which is passed with executions as the [Context.policyKey]
   * property.
   */
  val policyKey: String = policyKey ?: "$javaClass-${KeyHelper.guidPart()}"

  /**
   * Updates the execution [Context] with context from the executing policy.
   *
   * @param executionContext The execution [Context].
   * @return [Pair.first] is [Context.policyWrapKey] prior to changes by this method. [Pair.second] is
   * [Context.policyKey] prior to changes by this method.
   */
  @JvmSynthetic
  internal open fun setPolicyContext(executionContext: Context): Pair<String?, String?> {
    val pair = Pair(executionContext.policyWrapKey, executionContext.policyKey)
    executionContext.policyKey = policyKey
    return pair
  }

  /**
   * Restores the supplied keys to the execution [Context].
   *
   * @param executionContext The execution [Context].
   * @param priorPolicyWrapKey The [Context.policyWrapKey] prior to execution through this policy.
   * @param priorPolicyKey The [Context.policyKey] prior to execution through this policy.
   */
  @JvmSynthetic
  internal fun restorePolicyContext(executionContext: Context, priorPolicyWrapKey: String?, priorPolicyKey: String?) {
    executionContext.policyWrapKey = priorPolicyWrapKey
    executionContext.policyKey = priorPolicyKey
  }

  /**
   * Implements elements common to sync and async non-generic policies.
   *
   * @constructor Constructs a new instance of a derived type of [PolicyBase.WithoutResult] with the passed
   * [exceptionPredicates] and [policyKey].
   *
   * @param exceptionPredicates Predicates indicating which exception the policy should handle.
   * @param policyKey A key intended to be unique to each [IsPolicy] instance.
   */
  abstract class WithoutResult internal constructor(
    exceptionPredicates: ExceptionPredicates,
    policyKey: String? = null
  ) : PolicyBase<Nothing>(exceptionPredicates, policyKey) {
    /**
     * Constructs a new instance of a derived type of [PolicyBase.WithoutResult] with the passed [policyBuilder] and
     * [policyKey].
     *
     * @param policyBuilder A [PolicyBuilderWithoutResult] indicating which exceptions policy should handle.
     * @param policyKey A key intended to be unique to each [IsPolicy] instance.
     */
    protected constructor(
      policyBuilder: PolicyBuilderWithoutResult,
      policyKey: String? = null
    ) : this(policyBuilder.exceptionPredicates, policyKey)
  }

  /**
   * Implements elements common to sync and async generic policies.
   *
   * @constructor Constructs a new instance of a derived type of [PolicyBase.WithResult].
   * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
   * @param resultPredicates Predicates indicating which results the policy should handle.
   * @param policyKey A key intended to be unique to each [IsPolicy] instance.
   *
   * @property resultPredicates Predicates indicating which results the policy handles.
   */
  abstract class WithResult<TResult> internal constructor(
    exceptionPredicates: ExceptionPredicates,
    val resultPredicates: ResultPredicates<TResult> = ResultPredicates<TResult>(),
    policyKey: String? = null
  ) : PolicyBase<TResult>(exceptionPredicates, policyKey) {
    /**
     * Constructs a new instance of a derived type of [PolicyBase.WithResult] with the passed [PolicyBuilderWithResult]
     * and [policyKey].
     *
     * @param policyBuilder A [PolicyBuilderWithResult] indicating which exceptions and results the policy should handle.
     * @param policyKey A key intended to be unique to each [IsPolicy] instance.
     */
    protected constructor(policyBuilder: PolicyBuilderWithResult<TResult>, policyKey: String? = null) : this(
      policyBuilder.exceptionPredicates,
      policyBuilder.resultPredicates,
      policyKey
    )
  }
}

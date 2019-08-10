package hr.tjakopan.yarl

import hr.tjakopan.yarl.utilities.KeyHelper

@JvmSynthetic
internal val policyKeyMustBeImmutableException =
  IllegalArgumentException("policyKey cannot be changed once set; or (when using the default value after the policyKey property has been accessed.")
  @JvmSynthetic get

@JvmSynthetic
internal fun getExceptionType(exceptionPredicates: ExceptionPredicates, exception: Throwable): ExceptionType {
  return when (exceptionPredicates.firstMatchOrNull(exception) != null) {
    true -> ExceptionType.HANDLED_BY_THIS_POLICY
    false -> ExceptionType.UNHANDLED
  }
}

/**
 * Implements elements common to both non-generic and generic policies, and sync and async policies.
 *
 * @constructor Constructs a new instance of a derived type of [PolicyBase] with the passed [exceptionPredicates].
 * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
 */
abstract class PolicyBase internal constructor(exceptionPredicates: ExceptionPredicates? = null) {
  /**
   * A key intended to be unique to each [IsPolicy] instance.
   */
  protected var policyKeyInternal: String? = null

  /**
   * A key intended to be unique to each [IsPolicy] instance, which is passed with executions as the
   * [Context.policyKey] property.
   */
  val policyKey: String
    get() {
      return if (policyKeyInternal == null) {
        policyKeyInternal = "$javaClass-${KeyHelper.guidPart()}"
        policyKeyInternal as String
      } else {
        policyKeyInternal as String
      }
    }

  /**
   * Predicates indicating which exceptions the policy handles.
   */
  @JvmSynthetic
  internal val exceptionPredicates = exceptionPredicates ?: ExceptionPredicates.NONE
    @JvmSynthetic get

  /**
   * Constructs a new instance of a derived type of [PolicyBase] with the passed [policyBuilder].
   *
   * @param policyBuilder A [PolicyBuilder] indicating which exceptions the policy should handle.
   */
  protected constructor(policyBuilder: PolicyBuilder?) : this(policyBuilder?.exceptionPredicates)

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
}

/**
 * Implements elements common to sync and async generic policies.
 *
 * @constructor Constructs a new instance of a derived type of [PolicyBaseGeneric].
 * @param exceptionPredicates Predicates indicating which exceptions the policy should handle.
 * @param resultPredicates Predicates indicating which results the policy should handle.
 */
abstract class PolicyBaseGeneric<TResult> internal constructor(
  exceptionPredicates: ExceptionPredicates?,
  resultPredicates: ResultPredicates<TResult>?
) : PolicyBase(exceptionPredicates) {
  /**
   * Predicates indicating which results the policy handles.
   */
  @JvmSynthetic
  internal val resultPredicates: ResultPredicates<TResult> = resultPredicates ?: ResultPredicates.none()
    @JvmSynthetic get

  /**
   * Constructs a new instance of a derived type of [PolicyBuilderGeneric] with the passed [policyBuilder].
   *
   * @param policyBuilder A [PolicyBuilderGeneric] indicating which exceptions the policy should handle.
   */
  protected constructor(policyBuilder: PolicyBuilderGeneric<TResult>?) : this(
    policyBuilder?.exceptionPredicates,
    policyBuilder?.resultPredicates
  )
}

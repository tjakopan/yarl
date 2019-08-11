package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.*

/**
 * A policy that allows two (and by recursion more) Polly policies to wrap executions of delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 */
class PolicyWrapGeneric<TResult> private constructor(
  exceptionPredicates: ExceptionPredicates?,
  resultPredicates: ResultPredicates<TResult>?
) : PolicyGeneric<TResult>(exceptionPredicates, resultPredicates), IPolicyWrap {
  private var outerNonGeneric: ISyncPolicy? = null
  private var innerNonGeneric: ISyncPolicy? = null
  private var outerGeneric: ISyncPolicyGeneric<TResult>? = null
  private var innerGeneric: ISyncPolicyGeneric<TResult>? = null

  override val outer: IsPolicy
    get() = (outerGeneric ?: outerNonGeneric) as IsPolicy

  override val inner: IsPolicy
    get() = (innerGeneric ?: innerNonGeneric) as IsPolicy

  internal constructor(outer: Policy, inner: ISyncPolicyGeneric<TResult>) : this(
    outer.exceptionPredicates,
    ResultPredicates.none()
  ) {
    outerNonGeneric = outer
    innerGeneric = inner
  }

  internal constructor(outer: PolicyGeneric<TResult>, inner: ISyncPolicy) : this(
    outer.exceptionPredicates,
    outer.resultPredicates
  ) {
    outerGeneric = outer
    innerNonGeneric = inner
  }

  internal constructor(
    outer: PolicyGeneric<TResult>,
    inner: ISyncPolicyGeneric<TResult>
  ) : this(outer.exceptionPredicates, outer.resultPredicates) {
    outerGeneric = outer
    innerGeneric = inner
  }

  override fun setPolicyContext(executionContext: Context): Pair<String?, String?> {
    val priorPolicyKeys = Pair(executionContext.policyWrapKey, executionContext.policyKey)
    if (executionContext.policyWrapKey == null) executionContext.policyWrapKey = policyKey
    super.setPolicyContext(executionContext)

    return priorPolicyKeys
  }

  override fun implementation(context: Context, action: (Context) -> TResult?): TResult? {
    return when {
      outerNonGeneric != null -> when {
        innerNonGeneric != null -> PolicyWrapEngine.implementation(
          context,
          outerNonGeneric!!,
          innerNonGeneric!!,
          action
        )
        innerGeneric != null -> PolicyWrapEngine.implementation(context, outerNonGeneric!!, innerGeneric!!, action)
        else -> throw IllegalStateException("A ${javaClass.simpleName} must define an inner policy.")
      }
      outerGeneric != null -> when {
        innerNonGeneric != null -> PolicyWrapEngine.implementation(context, outerGeneric!!, innerNonGeneric!!, action)
        innerGeneric != null -> PolicyWrapEngine.implementation(context, outerGeneric!!, innerGeneric!!, action)
        else -> throw IllegalStateException("A ${javaClass.simpleName} must define an inner policy.")
      }
      else -> throw IllegalStateException("A ${javaClass.simpleName} must define an outer policy.")
    }
  }
}

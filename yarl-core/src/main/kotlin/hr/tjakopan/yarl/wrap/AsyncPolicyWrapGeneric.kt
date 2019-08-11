package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.*
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * A policy that allows two (and by recursion more) async Polly policies to wrap executions of async delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 */
class AsyncPolicyWrapGeneric<TResult> private constructor(
  exceptionPredicates: ExceptionPredicates?,
  resultPredicates: ResultPredicates<TResult>?
) : AsyncPolicyGeneric<TResult>(exceptionPredicates, resultPredicates), IPolicyWrap {
  private var outerNonGeneric: IAsyncPolicy? = null
  private var innerNonGeneric: IAsyncPolicy? = null
  private var outerGeneric: IAsyncPolicyGeneric<TResult>? = null
  private var innerGeneric: IAsyncPolicyGeneric<TResult>? = null

  override val outer: IsPolicy
    get() = (outerGeneric ?: outerNonGeneric) as IsPolicy
  override val inner: IsPolicy
    get() = (innerGeneric ?: innerNonGeneric) as IsPolicy

  internal constructor(outer: AsyncPolicy, inner: IAsyncPolicyGeneric<TResult>) : this(
    outer.exceptionPredicates,
    ResultPredicates.none()
  ) {
    outerNonGeneric = outer
    innerGeneric = inner
  }

  internal constructor(outer: AsyncPolicyGeneric<TResult>, inner: IAsyncPolicy) : this(
    outer.exceptionPredicates,
    outer.resultPredicates
  ) {
    outerGeneric = outer
    innerNonGeneric = inner
  }

  internal constructor(outer: AsyncPolicyGeneric<TResult>, inner: IAsyncPolicyGeneric<TResult>) : this(
    outer.exceptionPredicates,
    outer.resultPredicates
  ) {
    outerGeneric = outer
    innerGeneric = inner
  }

  override fun setPolicyContext(executionContext: Context): Pair<String?, String?> {
    val priorPolicyKeys = Pair(executionContext.policyWrapKey, executionContext.policyKey)
    if (executionContext.policyWrapKey == null) executionContext.policyWrapKey = policyKey
    super.setPolicyContext(executionContext)

    return priorPolicyKeys
  }

  override fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> {
    return when {
      outerNonGeneric != null -> when {
        innerNonGeneric != null -> AsyncPolicyWrapEngine.implementationAsyncGeneric(
          context,
          outerNonGeneric!!,
          innerNonGeneric!!,
          action
        )
        innerGeneric != null -> AsyncPolicyWrapEngine.implementationAsyncGeneric(
          context,
          outerNonGeneric!!,
          innerGeneric!!,
          action
        )
        else -> throw IllegalStateException("A ${javaClass.simpleName} must define an inner policy")
      }
      outerGeneric != null -> when {
        innerNonGeneric != null -> AsyncPolicyWrapEngine.implementationAsyncGeneric(
          context,
          outerGeneric!!,
          innerNonGeneric!!,
          action
        )
        innerGeneric != null -> AsyncPolicyWrapEngine.implementationAsyncGeneric(
          context,
          outerGeneric!!,
          innerGeneric!!,
          action
        )
        else -> throw IllegalStateException("A ${javaClass.simpleName} must define an inner policy")
      }
      else -> throw IllegalStateException("A ${javaClass.simpleName} must define an outer policy.")
    }
  }

  override fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> {
    return when {
      outerNonGeneric != null -> when {
        innerNonGeneric != null -> AsyncPolicyWrapEngine.implementationAsyncGeneric(
          context,
          executor,
          outerNonGeneric!!,
          innerNonGeneric!!,
          action
        )
        innerGeneric != null -> AsyncPolicyWrapEngine.implementationAsyncGeneric(
          context,
          executor,
          outerNonGeneric!!,
          innerGeneric!!,
          action
        )
        else -> throw IllegalStateException("A ${javaClass.simpleName} must define an inner policy")
      }
      outerGeneric != null -> when {
        innerNonGeneric != null -> AsyncPolicyWrapEngine.implementationAsyncGeneric(
          context,
          executor,
          outerGeneric!!,
          innerNonGeneric!!,
          action
        )
        innerGeneric != null -> AsyncPolicyWrapEngine.implementationAsyncGeneric(
          context,
          executor,
          outerGeneric!!,
          innerGeneric!!,
          action
        )
        else -> throw IllegalStateException("A ${javaClass.simpleName} must define an inner policy")
      }
      else -> throw IllegalStateException("A ${javaClass.simpleName} must define an outer policy.")
    }
  }
}

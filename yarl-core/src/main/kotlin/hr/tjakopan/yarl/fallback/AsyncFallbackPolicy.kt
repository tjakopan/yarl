package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import hr.tjakopan.yarl.Policy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * A fallback policy that can be applied to asynchronous delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 */
class AsyncFallbackPolicy<TResult> private constructor(policyBuilder: Builder<TResult>) :
  AsyncPolicy<TResult, AsyncFallbackPolicy.Builder<TResult>>(policyBuilder), IFallbackPolicy {
  private val onFallback = policyBuilder.onFallback
  private val fallbackAction = policyBuilder.fallbackAction
  private val executor = policyBuilder.executor

  override fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    AsyncFallbackEngine.implementationAsync(
      context,
      exceptionPredicates,
      resultPredicates,
      { dr, ctx -> onFallback(dr, ctx, null) },
      { dr, ctx -> fallbackAction(dr, ctx, null) },
      action
    )

  override fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    AsyncFallbackEngine.implementationAsync(
      context,
      executor,
      exceptionPredicates,
      resultPredicates,
      { dr, ctx, exe -> onFallbackAsync(dr, ctx, exe) },
      { dr, ctx, exe -> fallbackAction(dr, ctx, exe) },
      action
    )

  class Builder<TResult>() : Policy.Builder<TResult, Builder<TResult>>() {
    @JvmSynthetic
    internal var onFallback: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<Unit> =
      { _, _, _ -> CompletableFuture.completedStage(Unit) }
      private set

    @JvmSynthetic
    internal lateinit var fallbackAction: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<TResult>
      private set

    @JvmSynthetic
    internal var executor: Executor? = null
      private set

    constructor(fallbackAction: (DelegateResult<TResult>, Context) -> CompletionStage<TResult>) : this() {
      this.fallbackAction = { r, ctx, _ -> fallbackAction(r, ctx) }
    }

    constructor(
      executor: Executor,
      fallbackAction: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<TResult>
    ) : this() {
      this.executor = executor
      this.fallbackAction = fallbackAction
    }

    fun onFallback(onFallback: (DelegateResult<TResult>, Context) -> CompletionStage<Unit>) {
      this.onFallback = { r, ctx, _ -> onFallback(r, ctx) }
    }

    fun onFallback(onFallback: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<Unit>) {
      this.onFallback = onFallback
    }

    override fun self(): Builder<TResult> = this

    fun build() = AsyncFallbackPolicy(this)
  }
}

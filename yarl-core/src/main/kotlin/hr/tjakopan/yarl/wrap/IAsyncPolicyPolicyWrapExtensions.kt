package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.IAsyncPolicy

/**
 * Wraps this policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return An [AsyncPolicyWrap] instance representing the combined wrap.
 */
fun <TResult> IAsyncPolicy<TResult>.wrapAsync(innerPolicy: IAsyncPolicy<TResult>): AsyncPolicyWrap<TResult> =
  (this as AsyncPolicy).wrapAsync(innerPolicy)

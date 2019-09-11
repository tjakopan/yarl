@file:JvmName("PolicyWrapExtensions")
@file:JvmMultifileClass

package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.IAsyncPolicy

/**
 * Wraps this policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return An [AsyncPolicyWrap] instance representing the combined wrap.
 */
fun IAsyncPolicy.wrapAsync(innerPolicy: IAsyncPolicy): AsyncPolicyWrap = (this as AsyncPolicy<*>).wrapAsync(innerPolicy)

/**
 * Wraps this policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return An [AsyncPolicyWrap] instance representing the combined wrap.
 */
fun <TResult> IAsyncPolicy.wrapAsync(innerPolicy: IAsyncPolicy<TResult>): AsyncPolicyWrap<TResult> =
  (this as AsyncPolicy<*>).wrapAsync(innerPolicy)

/**
 * Wraps this policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return An [AsyncPolicyWrap] instance representing the combined wrap.
 */
fun <TResult> IAsyncPolicy<TResult>.wrapAsync(innerPolicy: IAsyncPolicy): AsyncPolicyWrap<TResult> =
  (this as AsyncPolicy<TResult, *>).wrapAsync(innerPolicy)

/**
 * Wraps this policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return An [AsyncPolicyWrap] instance representing the combined wrap.
 */
fun <TResult> IAsyncPolicy<TResult>.wrapAsync(innerPolicy: IAsyncPolicy<TResult>): AsyncPolicyWrap<TResult> =
  (this as AsyncPolicy<TResult, *>).wrapAsync(innerPolicy)

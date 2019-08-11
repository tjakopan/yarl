package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.AsyncPolicyGeneric
import hr.tjakopan.yarl.IAsyncPolicy
import hr.tjakopan.yarl.IAsyncPolicyGeneric

/**
 * Wraps this policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return An [AsyncPolicyWrap] instance representing the combined wrap.
 */
fun IAsyncPolicy.wrapAsync(innerPolicy: IAsyncPolicy): AsyncPolicyWrap = (this as AsyncPolicy).wrapAsync(innerPolicy)

/**
 * Wraps this policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return An [AsyncPolicyWrapGeneric] instance representing the combined wrap.
 */
fun <TResult> IAsyncPolicy.wrapAsync(innerPolicy: IAsyncPolicyGeneric<TResult>): AsyncPolicyWrapGeneric<TResult> =
  (this as AsyncPolicy).wrapAsync(innerPolicy)

/**
 * Wraps this policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return An [AsyncPolicyWrapGeneric] instance representing the combined wrap.
 */
fun <TResult> IAsyncPolicyGeneric<TResult>.wrapAsync(innerPolicy: IAsyncPolicy): AsyncPolicyWrapGeneric<TResult> =
  (this as AsyncPolicyGeneric).wrapAsync(innerPolicy)

/**
 * Wraps this policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return An [AsyncPolicyWrapGeneric] instance representing the combined wrap.
 */
fun <TResult> IAsyncPolicyGeneric<TResult>.wrapAsync(innerPolicy: IAsyncPolicyGeneric<TResult>): AsyncPolicyWrapGeneric<TResult> =
  (this as AsyncPolicyGeneric).wrapAsync(innerPolicy)

@file:JvmName("PolicyWrapExtensions")
@file:JvmMultifileClass

package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.ISyncPolicy
import hr.tjakopan.yarl.PolicyGeneric

/**
 * Wraps this policy around the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return A [PolicyWrap] instance representing the combined wrap.
 */
fun ISyncPolicy.wrap(innerPolicy: ISyncPolicy): PolicyWrap = (this as Policy<*>).wrap(innerPolicy)

/**
 * Wraps this policy around the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return A [PolicyWrapGeneric] instance representing the combined wrap.
 */
fun <TResult> ISyncPolicy.wrap(innerPolicy: ISyncPolicy<TResult>): PolicyWrapGeneric<TResult> =
  (this as Policy<*>).wrap(innerPolicy)

/**
 * Wraps this policy around the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return A [PolicyWrapGeneric] instance representing the combined wrap.
 */
fun <TResult> ISyncPolicy<TResult>.wrap(innerPolicy: ISyncPolicy): PolicyWrapGeneric<TResult> =
  (this as PolicyGeneric<TResult, *>).wrap(innerPolicy)

/**
 * Wraps this policy around the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return A [PolicyWrapGeneric] instance representing the combined wrap.
 */
fun <TResult> ISyncPolicy<TResult>.wrap(innerPolicy: ISyncPolicy<TResult>): PolicyWrapGeneric<TResult> =
  (this as PolicyGeneric<TResult, *>).wrap(innerPolicy)

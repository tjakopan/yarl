package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.ISyncPolicy
import hr.tjakopan.yarl.Policy

/**
 * Wraps this policy around the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return A [PolicyWrap] instance representing the combined wrap.
 */
fun <TResult> ISyncPolicy<TResult>.wrap(innerPolicy: ISyncPolicy<TResult>): PolicyWrap<TResult> =
  (this as Policy).wrap(innerPolicy)

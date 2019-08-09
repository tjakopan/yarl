package hr.tjakopan.yarl

import hr.tjakopan.yarl.wrap.PolicyWrapGeneric

/**
 * Converts a non-generic [ISyncPolicy] into a generic [ISyncPolicyGeneric] for handling only executions returning
 * [TResult].
 *
 * This method allows you to convert a non-generic [ISyncPolicy] into a generic [ISyncPolicyGeneric] for contexts
 * such as variables or parameters which may explicitly require a generic [ISyncPolicyGeneric].
 *
 * @return A generic [ISyncPolicyGeneric] version of the supplied non-generic [ISyncPolicy].
 */
fun <TResult> ISyncPolicy.asGenericPolicy() = this.wrap<TResult>(Policy.noOp())

/**
 * Wraps the specified outer policy round the inner policy.
 *
 * @param innerPolicy The inner policy.
 * @return A [PolicyWrapGeneric] instance representing the combined wrap.
 */
fun <TResult> ISyncPolicy.wrap(innerPolicy: ISyncPolicyGeneric<TResult>): PolicyWrapGeneric<TResult> =
  (this as Policy).wrap(innerPolicy)

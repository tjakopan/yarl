package hr.tjakopan.yarl

import hr.tjakopan.yarl.wrap.wrap

/**
 * Converts a non-generic [ISyncPolicy] into a generic [ISyncPolicyGeneric] for handling only executions returning
 * [TResult].
 *
 * This method allows you to convert a non-generic [ISyncPolicy] into a generic [ISyncPolicyGeneric] for contexts such
 * as variables or parameters which may explicitly require a generic [ISyncPolicyGeneric].
 *
 * @return A generic [ISyncPolicyGeneric] version of the supplied non-generic [ISyncPolicy].
 */
fun <TResult> ISyncPolicy.asGenericPolicy(): ISyncPolicyGeneric<TResult> =
  this.wrap(PolicyGeneric.noOp())

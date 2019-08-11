package hr.tjakopan.yarl

import hr.tjakopan.yarl.noop.AsyncNoOpPolicyGeneric
import hr.tjakopan.yarl.wrap.wrapAsync

/**
 * Converts a non-generic [IAsyncPolicy] into a generic [IAsyncPolicyGeneric] for handling only executions returning
 * [TResult].
 *
 * This method allows you to convert a non-generic [IAsyncPolicy] into a generic [IAsyncPolicyGeneric] for contexts such
 * as variables or parameters which may explicitly require a generic [IAsyncPolicyGeneric].
 *
 * @return A generic [IAsyncPolicyGeneric] version of the supplied non-generic [IAsyncPolicy].
 */
fun <TResult> IAsyncPolicy.asGenericAsyncPolicy(): IAsyncPolicyGeneric<TResult> =
  this.wrapAsync(AsyncNoOpPolicyGeneric.noOpAsync())

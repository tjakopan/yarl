package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.IsPolicy

/**
 * Defines properties and methods common to all NoOp policies.
 */
interface INoOpPolicy : IsPolicy

/**
 * Defines properties and methods common to all NoOp policies generic-typed for executions returning results of type
 * [TResult].
 */
interface INoOpPolicyGeneric<TResult> : INoOpPolicy

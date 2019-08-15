package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.IsPolicy

/**
 * Defines properties and methods common to all Fallback policies.
 */
interface IFallbackPolicy : IsPolicy {
}

/**
 * Defines properties and methods common to all Fallback policies generic-typed for executions returning results of type
 * [TResult].
 */
interface IFallbackPolicyGeneric<TResult> : IFallbackPolicy

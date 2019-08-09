package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.IsPolicy

interface INoOpPolicy : IsPolicy {

}

interface INoOpPolicyGeneric<TResult> : INoOpPolicy

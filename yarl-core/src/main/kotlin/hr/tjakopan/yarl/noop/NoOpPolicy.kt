package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.PolicyGeneric

class NoOpPolicy : Policy(), INoOpPolicy {
}

class NoOpPolicyGeneric<TResult>() : PolicyGeneric<TResult>(), INoOpPolicyGeneric<TResult> {

}

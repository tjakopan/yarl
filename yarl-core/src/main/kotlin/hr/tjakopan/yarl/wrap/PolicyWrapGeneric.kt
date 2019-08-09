package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.ISyncPolicyGeneric
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.PolicyGeneric

class PolicyWrapGeneric<TResult>(
  policy: Policy,
  innerPolicy: ISyncPolicyGeneric<TResult>
) : PolicyGeneric<TResult>() {
}

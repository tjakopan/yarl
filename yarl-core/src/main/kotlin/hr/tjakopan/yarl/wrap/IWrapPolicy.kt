package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.IPolicy
import kotlin.reflect.KClass

interface IWrapPolicy : IPolicy {
  val outer: IPolicy
  val inner: IPolicy

  fun getPolicies(): Iterable<IPolicy> {
    val policies = mutableListOf<IPolicy>()
    for (childPolicy in listOf(outer, inner)) {
      if (childPolicy is IWrapPolicy) {
        for (policy in childPolicy.getPolicies()) {
          policies.add(policy)
        }
      } else {
        policies.add(childPolicy)
      }
    }
    return policies
  }

  fun <P : IPolicy> getPolicies(policyClass: Class<P>): Iterable<P> =
    getPolicies().filterIsInstance(policyClass)

  @JvmSynthetic
  fun <P : IPolicy> getPolicies(policyClass: KClass<P>): Iterable<P> =
    getPolicies().filterIsInstance(policyClass.java)

  fun <P : IPolicy> getPolicies(policyClass: Class<P>, filter: (P) -> Boolean): Iterable<P> =
    getPolicies().filterIsInstance(policyClass)
      .filter(filter)

  @JvmSynthetic
  fun <P : IPolicy> getPolicies(policyClass: KClass<P>, filter: (P) -> Boolean): Iterable<P> =
    getPolicies().filterIsInstance(policyClass.java)
      .filter(filter)

  fun <P : IPolicy> getPolicy(policyClass: Class<P>): P? =
    getPolicies().filterIsInstance(policyClass)
      .firstOrNull()

  @JvmSynthetic
  fun <P : IPolicy> getPolicy(policyClass: KClass<P>): P? =
    getPolicies().filterIsInstance(policyClass.java)
      .firstOrNull()

  fun <P : IPolicy> getPolicy(policyClass: Class<P>, filter: (P) -> Boolean): P? =
    getPolicies().filterIsInstance(policyClass)
      .firstOrNull(filter)

  @JvmSynthetic
  fun <P : IPolicy> getPolicy(policyClass: KClass<P>, filter: (P) -> Boolean): P? =
    getPolicies().filterIsInstance(policyClass.java)
      .firstOrNull(filter)
}

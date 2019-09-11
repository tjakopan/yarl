package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.IsPolicy

/**
 * Defines properties and methods common to all PolicyWrap policies.
 */
@Suppress("UNCHECKED_CAST")
interface IPolicyWrap : IsPolicy {
  /**
   * Returns the outer [IsPolicy] in this [IPolicyWrap].
   */
  val outer: IsPolicy

  /**
   * Returns the next inner [IsPolicy] in this [IPolicyWrap].
   */
  val inner: IsPolicy

  /**
   * Returns all the policies in this [IPolicyWrap] in outer-to-inner order.
   *
   * @return An [Sequence] of all the policies in the wrap.
   */
  @JvmDefault
  fun getPolicies(): Sequence<IsPolicy> {
    return sequenceOf(this.outer, this.inner)
      .flatMap {
        when (it) {
          is IPolicyWrap -> it.getPolicies().asSequence()
          else -> sequenceOf(it)
        }
      }
  }

  /**
   * Returns all the policies in this [IPolicyWrap] of type [TPolicy], in outer-to-inner order.
   *
   * @param TPolicy The type of policies to return.
   * @param policyClass The class of policies to return.
   * @return An [Sequence] of all the policies of the given type.
   */
  @JvmDefault
  fun <TPolicy> getPolicies(policyClass: Class<TPolicy>): Sequence<TPolicy> =
    getPolicies()
      .filter { policyClass.isInstance(it) }
      .map { it as TPolicy }

  /**
   * Returns all the policies in this [IPolicyWrap] of type [TPolicy] matching the filter, in outer-to-inner order.
   *
   * @param TPolicy The type of policies to return.
   * @param policyClass The class of policies to return.
   * @param filter A filter to apply to any policies of type [TPolicy] found.
   * @return An [Sequence] of all the policies of the given type, matching the filter.
   */
  @JvmDefault
  fun <TPolicy> getPolicies(policyClass: Class<TPolicy>, filter: (TPolicy) -> Boolean): Sequence<TPolicy> =
    getPolicies()
      .filter { policyClass.isInstance(it) }
      .map { it as TPolicy }
      .filter(filter)

  /**
   * Returns the single policy in this [IPolicyWrap] of type [TPolicy].
   *
   * @param TPolicy The type of policies to return.
   * @param policyClass The class of policies to return.
   * @return A [TPolicy] if one is found; else null.
   * @throws IllegalArgumentException If more than one policy of the type is found in the wrap.
   */
  @JvmDefault
  fun <TPolicy> getPolicy(policyClass: Class<TPolicy>): TPolicy? {
    val policies = getPolicies()
      .filter { policyClass.isInstance(it) }
      .map { it as TPolicy }
    return when {
      policies.count() == 0 -> null
      else -> policies.single()
    }
  }

  /**
   * Returns the single policy in this [IPolicyWrap] of type [TPolicy] matching the filter.
   *
   * @param TPolicy The type of policies to return.
   * @param policyClass The class of policies to return.
   * @param filter A filter to apply to any policies of type [TPolicy] found.
   * @return A matching [TPolicy] if one is found; else null.
   * @throws IllegalArgumentException If more than one policy of the type is found in the wrap.
   */
  @JvmDefault
  fun <TPolicy> getPolicy(policyClass: Class<TPolicy>, filter: (TPolicy) -> Boolean): TPolicy? {
    val policies = getPolicies()
      .filter { policyClass.isInstance(it) }
      .map { it as TPolicy }
      .filter(filter)
    return when {
      policies.count() == 0 -> null
      else -> policies.single()
    }
  }
}

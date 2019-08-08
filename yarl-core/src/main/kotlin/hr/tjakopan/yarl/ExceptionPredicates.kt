package hr.tjakopan.yarl

/**
 * A collection of predicates used to define whether a policy handles a given [Throwable].
 */
class ExceptionPredicates(private val predicates: MutableList<ExceptionPredicate> = mutableListOf()) {
  companion object {
    /**
     * Specifies that no exception handling filters are applied or are required.
     */
    @JvmStatic
    val NONE = ExceptionPredicates()
  }

  @JvmSynthetic
  internal fun add(predicate: ExceptionPredicate) {
    predicates += predicate
  }

  /**
   * Assess whether the passed [Throwable], [ex], matches any of the predicates.
   *
   * It the .handleCause() method was used when configuring the policy, predicates may test whether any causes of [ex]
   * match and may return a matching cause.
   *
   * @param ex The exception to assess against the predicates.
   * @return The first exception to match a predicate; or null, if no match is found.
   */
  fun firstMatchOrNull(ex: Throwable): Throwable? {
    return predicates.map { it(ex) }
      .firstOrNull { it != null }
  }
}

package hr.tjakopan.yarl

/**
 * A collection of predicated used to define whether a policy hndles a given [Throwable].
 */
class ExceptionPredicates(private val predicates: MutableList<ExceptionPredicate>) {
  companion object {
    /**
     * Specifies that no exception handling filters are applied or are required.
     */
    @JvmStatic
    val NONE = ExceptionPredicates()
  }

  constructor() : this(mutableListOf())

  internal fun add(predicate: ExceptionPredicate) {
    predicates += predicate
  }

  fun firstMatchOrNull(ex: Throwable): Throwable? {
    return predicates.map { it.test(ex) }
      .firstOrNull { it != null }
  }
}

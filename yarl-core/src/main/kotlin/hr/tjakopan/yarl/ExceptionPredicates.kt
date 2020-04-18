package hr.tjakopan.yarl

typealias ExceptionPredicate = (e: Throwable) -> Throwable?

class ExceptionPredicates private constructor(private val predicates: List<ExceptionPredicate>) {
  companion object {
    @JvmField
    val NONE = ExceptionPredicates()
  }

  constructor() : this(listOf())

  internal operator fun plus(predicate: ExceptionPredicate): ExceptionPredicates {
    val predicates = this.predicates + predicate
    return ExceptionPredicates(predicates)
  }

  fun firstMatchOrNull(e: Throwable): Throwable? {
    return predicates.mapNotNull { it(e) }
      .firstOrNull()
  }
}

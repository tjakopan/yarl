package hr.tjakopan.yarl

import hr.tjakopan.yarl.annotations.Immutable

typealias ExceptionPredicate = (e: Throwable) -> Throwable?

@Immutable
class ExceptionPredicates private constructor(private val predicates: List<ExceptionPredicate>) {
  constructor() : this(listOf())

  @JvmSynthetic
  internal operator fun plus(predicate: ExceptionPredicate): ExceptionPredicates {
    val predicates = this.predicates + predicate
    return ExceptionPredicates(predicates)
  }

  fun firstMatchOrNull(e: Throwable): Throwable? {
    return predicates.mapNotNull { it(e) }
      .firstOrNull()
  }

  companion object {
    @JvmField
    val NONE = ExceptionPredicates()
  }
}

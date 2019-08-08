package hr.tjakopan.yarl

/**
 * A predicate that can be run against a passed result value of type TResult. Predicates are used to define whether
 * policies handle the given result.
 *
 * @param result The passed result, against which to evaluate the predicate.
 * @param TResult The type of results which this predicate can evaluate.
 * @return True if the passed result matched the predicate; otherwise, false.
 *
 */
typealias ResultPredicate<TResult> = (result: TResult?) -> Boolean

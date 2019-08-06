package hr.tjakopan.yarl

/**
 * A predicate that can be run against a passed result value of type TResult. Predicates are used to define whether
 * policies handle the given result.
 *
 * TResult - The type of results which this predicate can evaluate.
 *
 * @param result The passed result, against which to evaluate the predicate.
 * @return True if the passed result matched the predicate; otherwise, false.
 *
 */
typealias ResultPredicate<TResult> = (result: TResult) -> Boolean

package hr.tjakopan.yarl.test.helpers

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.PolicyResult

internal fun <R> Policy<R, *>.raiseResults(vararg resultsToRaise: R): R {
  val iterator = resultsToRaise.iterator()
  return this.execute {
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    return@execute iterator.next();
  }
}

internal fun <R> Policy<R, *>.raiseResults(context: Context, vararg resultsToRaise: R): R {
  val iterator = resultsToRaise.iterator()
  return this.execute(context) {
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    return@execute iterator.next();
  }
}

internal fun <R> Policy<R, *>.raiseResultsOnExecuteAndCapture(
  context: Context,
  vararg resultsToRaise: R
): PolicyResult<R> {
  val iterator = resultsToRaise.iterator()
  return this.executeAndCapture(context) {
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    return@executeAndCapture iterator.next();
  }
}

internal fun <E : Throwable> Policy<Unit, *>.raiseExceptions(
  numberOfTimesToRaiseException: Int,
  exceptionSupplier: (Int) -> E
) {
  var counter = 0
  this.execute {
    counter++
    if (counter <= numberOfTimesToRaiseException) {
      throw exceptionSupplier(counter)
    }
  }
}

internal fun <E : Throwable> Policy<Unit, *>.raiseExceptions(
  context: Context,
  numberOfTimesToRaiseException: Int,
  exceptionSupplier: (Int) -> E
) {
  var counter = 0
  this.execute(context) {
    counter++
    if (counter <= numberOfTimesToRaiseException) {
      throw exceptionSupplier(counter)
    }
  }
}

internal fun <E : Throwable> Policy<Unit, *>.raiseExceptionsOnExecuteAndCapture(
  context: Context,
  numberOfTimesToRaiseException: Int,
  exceptionSupplier: (Int) -> E
) {
  var counter = 0
  this.executeAndCapture(context) {
    counter++
    if (counter <= numberOfTimesToRaiseException) {
      throw exceptionSupplier(counter)
    }
  }
}

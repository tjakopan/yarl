package hr.tjakopan.yarl.test.helpers

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.PolicyResult
import kotlinx.coroutines.CancellationException

internal suspend fun <R> AsyncPolicy<R, *>.raiseResults(vararg resultsToRaise: R): R {
  val iterator = resultsToRaise.iterator()
  return this.execute {
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    return@execute iterator.next()
  }
}

internal suspend fun <R> AsyncPolicy<R, *>.raiseResults(context: Context, vararg resultsToRaise: R): R {
  val iterator = resultsToRaise.iterator()
  return this.execute(context) {
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    return@execute iterator.next()
  }
}

internal suspend fun <R> AsyncPolicy<R, *>.raiseResults(onExecute: () -> Unit, vararg resultsToRaise: R): R {
  val iterator = resultsToRaise.iterator()
  return this.execute {
    onExecute()
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    return@execute iterator.next()
  }
}

internal suspend fun <R> AsyncPolicy<R, *>.raiseResultsAndOrCancellation(
  attemptDuringWhichToCancel: Int,
  onExecute: () -> Unit,
  vararg resultsToRaise: R
): R {
  var counter = 0
  val iterator = resultsToRaise.iterator()
  return this.execute {
    onExecute()
    counter++
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    if (counter >= attemptDuringWhichToCancel) {
      throw CancellationException()
    }
    return@execute iterator.next()
  }
}

internal suspend fun <R> AsyncPolicy<R, *>.raiseResultsOnExecuteAndCapture(vararg resultsToRaise: R): PolicyResult<R> {
  val iterator = resultsToRaise.iterator()
  return this.executeAndCapture {
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    return@executeAndCapture iterator.next()
  }
}

internal suspend fun <R> AsyncPolicy<R, *>.raiseResultsOnExecuteAndCapture(
  context: Context,
  vararg resultsToRaise: R
): PolicyResult<R> {
  val iterator = resultsToRaise.iterator()
  return this.executeAndCapture(context) {
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    return@executeAndCapture iterator.next()
  }
}

internal suspend fun <E : Throwable> AsyncPolicy<Unit, *>.raiseExceptions(
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

internal suspend fun <E : Throwable> AsyncPolicy<Unit, *>.raiseExceptions(
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

internal suspend fun <E : Throwable> AsyncPolicy<Unit, *>.raiseExceptionsOnExecuteAndCapture(
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

internal suspend fun <E : Throwable> AsyncPolicy<Unit, *>.raiseExceptions(
  numberOfTimesToRaiseException: Int,
  onExecute: () -> Unit,
  exceptionSupplier: (Int) -> E
) {
  var counter = 0
  return this.execute {
    onExecute()
    counter++
    if (counter <= numberOfTimesToRaiseException) {
      throw exceptionSupplier(counter)
    }
  }
}

internal suspend fun <E : Throwable> AsyncPolicy<Unit, *>.raiseExceptionsAndOrCancellation(
  numberOfTimesToRaiseException: Int,
  attemptDuringWhichToCancel: Int,
  onExecute: () -> Unit,
  exceptionSupplier: (Int) -> E
) {
  var counter = 0
  return this.execute {
    onExecute()
    counter++
    if (counter >= attemptDuringWhichToCancel) {
      throw CancellationException()
    }
    if (counter <= numberOfTimesToRaiseException) {
      throw exceptionSupplier(counter)
    }
  }
}

package hr.tjakopan.yarl.test.helpers;

import hr.tjakopan.yarl.AsyncPolicy;
import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.PolicyResult;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class AsyncPolicyUtils {
  private AsyncPolicyUtils() {
  }

  @SafeVarargs
  public static <R> CompletableFuture<R> raiseResults(final AsyncPolicy<R, ?> policy, final R... resultsToRaise) {
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.executeAsync(() -> {
      if (!iterator.hasNext()) {
        throw new IllegalArgumentException("Not enough values in resultsToRaise.");
      }
      return CompletableFuture.completedFuture(iterator.next());
    });
  }

  @SafeVarargs
  public static <R> CompletableFuture<R> raiseResults(final AsyncPolicy<R, ?> policy, final Context context,
                                                      final R... resultsToRaise) {
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.executeAsync(context, ctx -> {
      if (!iterator.hasNext()) {
        throw new IllegalArgumentException("Not enough values in resultsToRaise.");
      }
      return CompletableFuture.completedFuture(iterator.next());
    });
  }

  @SafeVarargs
  public static <R> CompletableFuture<R> raiseResults(final AsyncPolicy<R, ?> policy,
                                                      final Function1<R, CompletableFuture<R>> action,
                                                      final R... resultsToRaise) {
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.executeAsync(() -> {
      if (!iterator.hasNext()) {
        throw new IllegalArgumentException("Not enough values in resultsToRaise.");
      }
      return action.invoke(iterator.next());
    });
  }

  @SafeVarargs
  public static <R> CompletableFuture<PolicyResult<R>> raiseResultsOnExecuteAndCapture(final AsyncPolicy<R, ?> policy,
                                                                                       final Context context,
                                                                                       final R... resultsToRaise) {
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.executeAndCaptureAsync(context, ctx -> {
      if (!iterator.hasNext()) {
        throw new IllegalArgumentException("Not enough values in resultsToRaise.");
      }
      return CompletableFuture.completedFuture(iterator.next());
    });
  }

  @SafeVarargs
  public static <R> CompletableFuture<R> raiseResultsAndOrCancellation(final AsyncPolicy<R, ?> policy,
                                                                       final int attemptDuringWhichToCancel,
                                                                       final Function1<R, CompletableFuture<R>> action,
                                                                       final R... resultsToRaise) {
    final var counter = new AtomicInteger(0);
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.executeAsync(() -> {
      if (!iterator.hasNext()) {
        throw new IllegalArgumentException("Not enough values in resultsToRaise.");
      }
      return action.invoke(iterator.next())
        .whenCompleteAsync((r, e) -> {
          counter.incrementAndGet();
          if (counter.get() >= attemptDuringWhichToCancel) {
            throw new CancellationException();
          }
        });
    });
  }

  public static <E extends RuntimeException> CompletableFuture<Void> raiseExceptions(
    final AsyncPolicy<Void, ?> policy,
    final int numberOfTimesToRaiseException,
    final Function<Integer, E> exceptionSupplier) {
    final var counter = new AtomicInteger(0);
    return policy.executeAsync(() -> {
      counter.incrementAndGet();
      if (counter.get() <= numberOfTimesToRaiseException) {
        throw exceptionSupplier.apply(counter.get());
      }
      return CompletableFuture.completedFuture(null);
    });
  }

  public static <E extends RuntimeException> CompletableFuture<Void> raiseExceptions(
    final AsyncPolicy<Void, ?> policy,
    final Context context,
    final int numberOfTimesToRaiseException,
    final Function<Integer, E> exceptionSupplier) {
    final var counter = new AtomicInteger(0);
    return policy.executeAsync(context, ctx -> {
      counter.incrementAndGet();
      if (counter.get() <= numberOfTimesToRaiseException) {
        throw exceptionSupplier.apply(counter.get());
      }
      return CompletableFuture.completedFuture(null);
    });
  }

  public static <E extends RuntimeException> CompletableFuture<Void> raiseExceptions(
    final AsyncPolicy<Void, ?> policy,
    final int numberOfTimesToRaiseException,
    final Function0<CompletableFuture<Void>> action,
    final Function<Integer, E> exceptionSupplier
  ) {
    final var counter = new AtomicInteger(0);
    return policy.executeAsync(() ->
      action.invoke()
        .whenCompleteAsync((v, e) -> {
          counter.incrementAndGet();
          if (counter.get() <= numberOfTimesToRaiseException) {
            throw exceptionSupplier.apply(counter.get());
          }
        }));
  }

  public static <E extends RuntimeException, R> CompletableFuture<R> raiseExceptions(
    final AsyncPolicy<R, ?> policy,
    final int numberOfTimesToRaiseException,
    final Function0<CompletableFuture<Void>> action,
    final R successResult,
    final Function<Integer, E> exceptionSupplier
  ) {
    final var counter = new AtomicInteger(0);
    return policy.executeAsync(() ->
      action.invoke()
        .thenApplyAsync(v -> {
          counter.incrementAndGet();
          if (counter.get() <= numberOfTimesToRaiseException) {
            throw exceptionSupplier.apply(counter.get());
          }
          return successResult;
        }));
  }

  public static <E extends RuntimeException> CompletableFuture<PolicyResult<Void>> raiseExceptionsOnExecuteAndCapture(
    final AsyncPolicy<Void, ?> policy,
    final Context context,
    final int numberOfTimesToRaiseException,
    final Function<Integer, E> exceptionSupplier) {
    final var counter = new AtomicInteger(0);
    return policy.executeAndCaptureAsync(context, ctx -> {
      counter.incrementAndGet();
      if (counter.get() <= numberOfTimesToRaiseException) {
        throw exceptionSupplier.apply(counter.get());
      }
      return CompletableFuture.completedFuture(null);
    });
  }

  public static <E extends RuntimeException> CompletableFuture<Void> raiseExceptionsAndOrCancellation(
    final AsyncPolicy<Void, ?> policy,
    final int numberOfTimesToRaiseException,
    final int attemptDuringWhichToCancel,
    final Function0<CompletableFuture<Void>> action,
    final Function<Integer, E> exceptionSupplier
  ) {
    final var counter = new AtomicInteger(0);
    return policy.executeAsync(() ->
      action.invoke()
        .thenRunAsync(() -> {
          counter.incrementAndGet();
          if (counter.get() >= attemptDuringWhichToCancel) {
            throw new CancellationException();
          }
          if (counter.get() <= numberOfTimesToRaiseException) {
            throw exceptionSupplier.apply(counter.get());
          }
        }));
  }

  public static <E extends RuntimeException, R> CompletableFuture<R> raiseExceptionsAndOrCancellation(
    final AsyncPolicy<R, ?> policy,
    final int numberOfTimesToRaiseException,
    final int attemptDuringWhichToCancel,
    final Function0<CompletableFuture<Void>> action,
    final R successResult,
    final Function<Integer, E> exceptionSupplier
  ) {
    final var counter = new AtomicInteger(0);
    return policy.executeAsync(() ->
      action.invoke()
        .thenApplyAsync(v -> {
          counter.incrementAndGet();
          if (counter.get() >= attemptDuringWhichToCancel) {
            throw new CancellationException();
          }
          if (counter.get() <= numberOfTimesToRaiseException) {
            throw exceptionSupplier.apply(counter.get());
          }
          return successResult;
        }));
  }
}

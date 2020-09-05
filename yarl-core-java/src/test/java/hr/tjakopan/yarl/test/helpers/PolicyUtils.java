package hr.tjakopan.yarl.test.helpers;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.Policy;
import hr.tjakopan.yarl.PolicyResult;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class PolicyUtils {
  private PolicyUtils() {
  }

  @SafeVarargs
  public static <R> R raiseResults(final Policy<R, ?> policy, final R... resultsToRaise) {
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.execute(() -> {
      if (!iterator.hasNext()) {
        throw new ArrayIndexOutOfBoundsException("Not enough values in resultsToRaise.");
      }
      return iterator.next();
    });
  }

  @SafeVarargs
  public static <R> R raiseResults(final Policy<R, ?> policy, final Context context, final R... resultsToRaise) {
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.execute(context, ctx -> {
      if (!iterator.hasNext()) {
        throw new ArrayIndexOutOfBoundsException("Not enough values in resultsToRaise.");
      }
      return iterator.next();
    });
  }

  public static <R> R raiseResultsAndOrExceptions(final Policy<R, ?> policy, Class<R> resultClass,
                                                  final Object... resultsOrExceptionsToRaise) {
    final var iterator = Stream.of(resultsOrExceptionsToRaise).iterator();
    return policy.execute(() -> {
      if (!iterator.hasNext()) {
        throw new ArrayIndexOutOfBoundsException("Not enough values in resultsOrExceptionsToRaise.");
      }
      final var current = iterator.next();
      if (current instanceof RuntimeException) {
        throw (RuntimeException) current;
      } else if (resultClass.isInstance(current)) {
        //noinspection unchecked
        return (R) current;
      } else {
        throw new ArrayIndexOutOfBoundsException("Value is not either a runtime exception or result.");
      }
    });
  }

  @SafeVarargs
  public static <R> PolicyResult<R> raiseResultsOnExecuteAndCapture(final Policy<R, ?> policy, final Context context,
                                                                    final R... resultsToRaise) {
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.executeAndCapture(context, ctx -> {
      if (!iterator.hasNext()) {
        throw new ArrayIndexOutOfBoundsException("Not enough values in resultsToRaise.");
      }
      return iterator.next();
    });
  }

  public static <E extends RuntimeException> void raiseExceptions(final Policy<Void, ?> policy,
                                                                  final int numberOfTimesToRaiseException,
                                                                  final Function<Integer, E> exceptionSupplier) {
    final var counter = new AtomicInteger(0);
    policy.execute(() -> {
      counter.incrementAndGet();
      if (counter.get() <= numberOfTimesToRaiseException) {
        throw exceptionSupplier.apply(counter.get());
      }
      return null;
    });
  }

  public static <E extends RuntimeException> void raiseExceptions(final Policy<Void, ?> policy,
                                                                  final Context context,
                                                                  final int numberOfTimesToRaiseException,
                                                                  final Function<Integer, E> exceptionSupplier) {
    final var counter = new AtomicInteger(0);
    policy.execute(context, ctx -> {
      counter.incrementAndGet();
      if (counter.get() <= numberOfTimesToRaiseException) {
        throw exceptionSupplier.apply(counter.get());
      }
      return null;
    });
  }

  public static <E extends RuntimeException> void raiseExceptionsOnExecuteAndCapture(final Policy<Void, ?> policy,
                                                                                     final Context context,
                                                                                     final int numberOfTimesToRaiseException,
                                                                                     final Function<Integer, E> exceptionSupplier) {
    final var counter = new AtomicInteger(0);
    policy.execute(context, ctx -> {
      counter.incrementAndGet();
      if (counter.get() <= numberOfTimesToRaiseException) {
        throw exceptionSupplier.apply(counter.get());
      }
      return null;
    });
  }
}

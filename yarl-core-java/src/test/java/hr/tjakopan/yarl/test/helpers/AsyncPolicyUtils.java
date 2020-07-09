package hr.tjakopan.yarl.test.helpers;

import hr.tjakopan.yarl.AsyncPolicy;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AsyncPolicyUtils {
  private AsyncPolicyUtils() {
  }

  @SafeVarargs
  public static <R> R raiseResults(final AsyncPolicy<R, ?> policy, R... resultsToRaise) {
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.executeAsync(() -> {
      if (!iterator.hasNext()) {
        throw new IllegalArgumentException("Not enough values in resultsToRaise.");
      }
      return CompletableFuture.completedFuture(iterator.next());
    })
      .join();
  }
}

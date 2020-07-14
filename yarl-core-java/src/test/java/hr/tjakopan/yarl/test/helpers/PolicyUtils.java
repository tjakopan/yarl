package hr.tjakopan.yarl.test.helpers;

import hr.tjakopan.yarl.Policy;

import java.util.stream.Stream;

public class PolicyUtils {
  private PolicyUtils() {
  }

  @SafeVarargs
  public static <R> R raiseResults(final Policy<R, ?> policy, R... resultsToRaise) {
    final var iterator = Stream.of(resultsToRaise).iterator();
    return policy.execute(() -> {
      if (!iterator.hasNext()) {
        throw new IllegalArgumentException("Not enough values in resultsToRaise.");
      }
      return iterator.next();
    });
  }
}

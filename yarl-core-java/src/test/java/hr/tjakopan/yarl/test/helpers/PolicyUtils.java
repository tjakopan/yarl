package hr.tjakopan.yarl.test.helpers;

import hr.tjakopan.yarl.Policy;

public class PolicyUtils {
  private PolicyUtils() {
  }

  public static <R> void raiseResults(final Policy<R, ?> policy, R... resultsToRaise) {
    for (final var result : resultsToRaise) {
      policy.execute(() -> result);
    }
  }
}

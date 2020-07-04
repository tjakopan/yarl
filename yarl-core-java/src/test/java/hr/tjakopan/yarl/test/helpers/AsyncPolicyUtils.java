package hr.tjakopan.yarl.test.helpers;

import hr.tjakopan.yarl.AsyncPolicy;

public class AsyncPolicyUtils {
  private AsyncPolicyUtils() {
  }

  public static  <R> void raiseResultsAsync(final AsyncPolicy<R, ?> policy, R... resultsToRaise) {
    for (final var result : resultsToRaise) {
      policy.executeAsync(() -> result).join();
    }
  }
}

package hr.tjakopan.yarl.fallback;

import hr.tjakopan.yarl.Policy;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;

public class FallbackTestJava {
  @Test
  public void shouldNotExecuteFallbackWhenExecutedDelegateDoesNotThrow() {
    final var fallbackActionExecuted = new AtomicBoolean(false);
    final Function0<Unit> fallbackAction = () -> {
      fallbackActionExecuted.set(true);
      return null;
    };
    final var fallbackPolicy = FallbackBuilder.fallback(
      Policy.handle(ArithmeticException.class),
      fallbackAction
    );

    fallbackPolicy.execute(() -> {
      return null;
    });

    assertFalse(fallbackActionExecuted.get());
  }
}

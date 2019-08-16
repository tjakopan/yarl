package hr.tjakopan.yarl.fallback;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.Policy;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;

public class FallbackTest {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  //region Configuration guard condition tests
  @Test
  public void shouldThrowWhenFallbackActionIsNull() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("parameter fallbackAction");

    final Function0<Unit> fallbackAction = null;
    FallbackBuilder.fallback(
      Policy.handle(ArithmeticException.class),
      fallbackAction
    );
  }

  @Test
  public void shouldThrowWhenFallbackActionIsNullWithOnFallback() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("parameter fallbackAction");

    final Function0<Unit> fallbackAction = null;
    final Function1<Throwable, Unit> onFallback = e -> Unit.INSTANCE;
    FallbackBuilder.fallback(
      Policy.handle(ArithmeticException.class),
      onFallback,
      fallbackAction
    );
  }

  @Test
  public void shouldThrowWhenFallbackActionIsNullWithOnFallbackWithContext() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("parameter fallbackAction");

    final Function1<Context, Unit> fallbackAction = null;
    final Function2<Throwable, Context, Unit> onFallback = (e, ctx) -> Unit.INSTANCE;
    FallbackBuilder.fallback(
      Policy.handle(ArithmeticException.class),
      onFallback,
      fallbackAction
    );
  }

  @Test
  public void shouldThrowWhenOnFallbackDelegateIsNull() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("parameter onFallback");

    final Function0<Unit> fallbackAction = () -> Unit.INSTANCE;
    final Function1<Throwable, Unit> onFallback = null;
    FallbackBuilder.fallback(
      Policy.handle(ArithmeticException.class),
      onFallback,
      fallbackAction
    );
  }

  @Test
  public void shouldThrowWhenOnFallbackDelegateIsNullWithContext() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("parameter onFallback");

    final Function1<Context, Unit> fallbackAction = ctx -> Unit.INSTANCE;
    final Function2<Throwable, Context, Unit> onFallback = null;
    FallbackBuilder.fallback(
      Policy.handle(ArithmeticException.class),
      onFallback,
      fallbackAction
    );
  }
  //endregion

  //region Policy operation tests
  @Test
  public void shouldNotExecuteFallbackWhenExecutedDelegateDoesNotThrow() {
    final var fallbackActionExecuted = new AtomicBoolean(false);
    final Function0<Unit> fallbackAction = () -> {
      fallbackActionExecuted.set(true);
      return Unit.INSTANCE;
    };
    final var fallbackPolicy = FallbackBuilder.fallback(
      Policy.handle(ArithmeticException.class),
      fallbackAction
    );

    fallbackPolicy.execute(() -> {
      return Unit.INSTANCE;
    });

    assertFalse(fallbackActionExecuted.get());
  }
  //endregion
}

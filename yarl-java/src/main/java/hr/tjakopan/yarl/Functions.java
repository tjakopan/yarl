package hr.tjakopan.yarl;

import kotlin.Unit;
import kotlin.jvm.functions.*;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Functions {
  private Functions() {
  }

  public static Function0<Unit> fromRunnable(final Runnable runnable) {
    return () -> {
      runnable.run();
      return Unit.INSTANCE;
    };
  }

  public static Function0<CompletableFuture<Unit>> fromRunnableAsync(final Runnable runnable) {
    return () -> {
      runnable.run();
      return CompletableFuture.completedFuture(Unit.INSTANCE);
    };
  }

  public static <T> Function1<T, Unit> fromConsumer(final Consumer<T> consumer) {
    return t -> {
      consumer.accept(t);
      return Unit.INSTANCE;
    };
  }

  public static <T> Function1<T, CompletableFuture<Unit>> fromConsumerAsync(final Consumer<T> consumer) {
    return t -> {
      consumer.accept(t);
      return CompletableFuture.completedFuture(Unit.INSTANCE);
    };
  }

  public static <T1, T2> Function2<T1, T2, Unit> fromConsumer2(final BiConsumer<T1, T2> consumer) {
    return (t1, t2) -> {
      consumer.accept(t1, t2);
      return Unit.INSTANCE;
    };
  }

  public static <T1, T2> Function2<T1, T2, CompletableFuture<Unit>> fromConsumer2Async(final BiConsumer<T1, T2> consumer) {
    return (t1, t2) -> {
      consumer.accept(t1, t2);
      return CompletableFuture.completedFuture(Unit.INSTANCE);
    };
  }

  public static <T1, T2, T3> Function3<T1, T2, T3, Unit> fromConsumer3(final Function<T1, BiConsumer<T2, T3>> consumer) {
    return (t1, t2, t3) -> {
      consumer.apply(t1).accept(t2, t3);
      return Unit.INSTANCE;
    };
  }

  public static <T1, T2, T3> Function3<T1, T2, T3, CompletableFuture<Unit>> fromConsumer3Async(
    final Function<T1, BiConsumer<T2, T3>> consumer) {
    return (t1, t2, t3) -> {
      consumer.apply(t1).accept(t2, t3);
      return CompletableFuture.completedFuture(Unit.INSTANCE);
    };
  }

  public static <T1, T2, T3, T4> Function4<T1, T2, T3, T4, Unit> fromConsumer4(
    final Function<T1, Function<T2, BiConsumer<T3, T4>>> consumer) {
    return (t1, t2, t3, t4) -> {
      consumer.apply(t1).apply(t2).accept(t3, t4);
      return Unit.INSTANCE;
    };
  }

  public static <T1, T2, T3, T4> Function4<T1, T2, T3, T4, CompletableFuture<Unit>> fromConsumer4Async(
    final Function<T1, Function<T2, BiConsumer<T3, T4>>> consumer) {
    return (t1, t2, t3, t4) -> {
      consumer.apply(t1).apply(t2).accept(t3, t4);
      return CompletableFuture.completedFuture(Unit.INSTANCE);
    };
  }
}

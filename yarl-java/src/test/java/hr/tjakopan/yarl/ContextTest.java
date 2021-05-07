package hr.tjakopan.yarl;

import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextTest {
  @Test
  public void shouldAssignOperationKeyFromConstructor() {
    final var context = Context.of("SomeKey");

    assertThat(context.getOperationKey()).isEqualTo("SomeKey");
    assertThat(context.keySet().size()).isEqualTo(0);
  }

  @Test
  public void shouldAssignOperationKeyAndContextDataFromConstructor() {
    final var context = Context.of("SomeKey", new HashMap<>() {{
      put("key1", "value1");
      put("key2", "value2");
    }});

    assertThat(context.getOperationKey()).isEqualTo("SomeKey");
    assertThat(context.get("key1")).isEqualTo("value1");
    assertThat(context.get("key2")).isEqualTo("value2");
  }

  @Test
  public void noArgsConstructorShouldAssignNoOperationKey() {
    final var context = Context.of();

    assertThat(context.getOperationKey()).isNull();
  }

  @Test
  public void shouldAssignCorrelationIdWhenAccessed() {
    final var context = Context.of("SomeKey");

    assertThat(context.getCorrelationId()).isNotNull();
  }

  @Test
  public void shouldReturnConsistentCorrelationId() {
    final var context = Context.of("SomeKey");

    final var uuid1 = context.getCorrelationId();
    final var uuid2 = context.getCorrelationId();

    assertThat(uuid1).isSameAs(uuid2);
  }
}

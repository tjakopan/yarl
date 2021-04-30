package hr.tjakopan.yarl;

import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextTest {
  @Test
  public void shouldAssignOperationKeyFromBuilder() {
    final var context = Context.builder()
      .operationKey("SomeKey")
      .build();

    assertThat(context.getOperationKey()).isEqualTo("SomeKey");
    assertThat(context.getContextData().keySet().size()).isEqualTo(0);
  }

  @Test
  public void shouldAssignOperationKeyAndContextDataFromBuilder() {
    final var context = Context.builder()
      .operationKey("SomeKey")
      .contextData(new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
      }})
      .build();

    assertThat(context.getOperationKey()).isEqualTo("SomeKey");
    assertThat(context.getContextData().get("key1")).isEqualTo("value1");
    assertThat(context.getContextData().get("key2")).isEqualTo("value2");
  }

  @Test
  public void noArgsBuilderShouldAssignNoOperationKey() {
    final var context = Context.builder()
      .build();

    assertThat(context.getOperationKey()).isNull();
  }

  @Test
  public void noArgsConstructorShouldAssignNoOperationKey() {
    final var context = new Context();

    assertThat(context.getOperationKey()).isNull();
  }

  @Test
  public void shouldAssignCorrelationIdWhenAccessed() {
    final var context = Context.builder()
      .operationKey("SomeKey")
      .build();

    assertThat(context.getCorrelationId()).isNotNull();
  }

  @Test
  public void shouldReturnConsistentCorrelationId() {
    final var context = Context.builder()
      .operationKey("SomeKey")
      .build();

    final var uuid1 = context.getCorrelationId();
    final var uuid2 = context.getCorrelationId();

    assertThat(uuid1).isSameAs(uuid2);
  }
}

package hr.tjakopan.yarl;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class ContextTestJava {
  @Test
  public void shouldAssignOperationKeyFromConstructor() {
    final var context = new Context("SomeKey");

    assertEquals("SomeKey", context.getOperationKey());
    assertEquals(0, context.getKeys().size());
  }

  @Test
  public void shouldAssignOperationKeyAndContextDataFromConstructor() {
    final var contextData = new HashMap<String, Object>() {{
      put("key1", "value1");
      put("key2", "value2");
    }};
    final var context = new Context("SomeKey", contextData);

    assertEquals("SomeKey", context.getOperationKey());
    assertEquals("value1", context.get("key1"));
    assertEquals("value2", context.get("key2"));
  }

  @Test
  public void noArgsCtorShouldAssignNoOperationKey() {
    final var context = new Context();

    assertNull(context.getOperationKey());
  }

  @Test
  public void shouldAssignCorrelationIdWhenAccessed() {
    final var context = new Context("SomeKey");

    assertNotNull(context.getCorrelationId());
  }

  @Test
  public void shouldReturnConsistentCorrelationId() {
    final var context = new Context("SomeKey");

    final var retrieved1 = context.getCorrelationId();
    final var retrieved2 = context.getCorrelationId();

    assertEquals(retrieved1, retrieved2);
  }
}

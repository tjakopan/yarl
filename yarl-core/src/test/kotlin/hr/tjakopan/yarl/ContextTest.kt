package hr.tjakopan.yarl

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class ContextTest {
  @Test
  fun shouldAssignOperationKeyFromConstructor() {
    val context = Context(operationKey = "SomeKey")

    assertThat(context.operationKey).isEqualTo("SomeKey")
    assertThat(context.contextData.keys.size).isEqualTo(0)
  }

  @Test
  fun shouldAssignOperationKeyAndContextDataFromConstructor() {
    val context = Context(operationKey = "SomeKey", contextData = mapOf("key1" to "value1", "key2" to "value2"))

    assertThat(context.operationKey).isEqualTo("SomeKey")
    assertThat(context.contextData["key1"]).isEqualTo("value1")
    assertThat(context.contextData["key2"]).isEqualTo("value2")
  }

  @Test
  fun noArgsBuilderShouldAssignNoOperationKey() {
    val context = Context.builder()
      .build()

    assertThat(context.operationKey).isNull()
  }

  @Test
  fun noArgsConstructorShouldAssignNoOperationKey() {
    val context = Context()

    assertThat(context.operationKey).isNull()
  }

  @Test
  fun shouldAssignCorrelationIdWhenAccessed() {
    val context = Context(operationKey = "SomeKey")

    @Suppress("UsePropertyAccessSyntax")
    assertThat(context.correlationId).isNotNull()
  }

  @Test
  fun shouldReturnConsistentCorrelationId() {
    val context = Context(operationKey = "SomeKey")

    val uuid1 = context.correlationId
    val uuid2 = context.correlationId

    assertThat(uuid1).isSameAs(uuid2)
  }
}

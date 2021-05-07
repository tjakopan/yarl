package hr.tjakopan.yarl

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class ContextTest {
  @Test
  fun `should assign operation key from constructor`() {
    val context = Context(operationKey = "SomeKey")

    assertThat(context.operationKey).isEqualTo("SomeKey")
    assertThat(context.keys.size).isEqualTo(0)
  }

  @Test
  fun `should assign operation key and context data from constructor`() {
    val context = Context(operationKey = "SomeKey", contextData = mutableMapOf("key1" to "value1", "key2" to "value2"))

    assertThat(context.operationKey).isEqualTo("SomeKey")
    assertThat(context["key1"]).isEqualTo("value1")
    assertThat(context["key2"]).isEqualTo("value2")
  }

  @Test
  fun `no-args constructor should assign no operation key`() {
    val context = Context()

    assertThat(context.operationKey).isNull()
  }

  @Test
  fun `should assign correlation id when accessed`() {
    val context = Context(operationKey = "SomeKey")

    @Suppress("UsePropertyAccessSyntax")
    assertThat(context.correlationId).isNotNull()
  }

  @Test
  fun `should return consistent correlation id`() {
    val context = Context(operationKey = "SomeKey")

    val uuid1 = context.correlationId
    val uuid2 = context.correlationId

    assertThat(uuid1).isSameAs(uuid2)
  }
}

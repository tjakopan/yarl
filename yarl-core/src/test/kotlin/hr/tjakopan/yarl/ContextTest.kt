package hr.tjakopan.yarl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContextTest {
  @Test
  fun shouldAssignOperationKeyFromConstructor() {
    val context = Context("SomeKey")

    assertEquals("SomeKey", context.operationKey)
    assertEquals(0, context.keys.count())
  }

  @Test
  fun shouldAssignOperationKeyAndContextDataFromConstructor() {
    val context = Context("SomeKey", mutableMapOf("key1" to "value1", "key2" to "value2"))

    assertEquals("SomeKey", context.operationKey)
    assertEquals("value1", context["key1"])
    assertEquals("value2", context["key2"])
  }

  @Test
  fun noArgsCtorShouldAssignNoOperationKey() {
    val context = Context()

    assertNull(context.operationKey)
  }

  @Test
  fun shouldAssignCorrelationIdWhenAccessed() {
    val context = Context("SomeKey")

    assertTrue { context.correlationId != null }
  }

  @Test
  fun shouldReturnConsistentCorrelationId() {
    val context = Context("SomeKey")

    val retrieved1 = context.correlationId
    val retrieved2 = context.correlationId

    assertTrue { retrieved1 == retrieved2 }
  }
}

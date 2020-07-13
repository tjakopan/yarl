package hr.tjakopan.yarl

import kotlin.reflect.KClass

abstract class PolicyBuilder<R, out B : PolicyBuilder<R, B>> protected constructor() {
  var policyKey: String? = null

  @JvmSynthetic
  internal var resultPredicates: ResultPredicates<R> = ResultPredicates()
    @JvmSynthetic get
    @JvmSynthetic set

  @JvmSynthetic
  internal var exceptionPredicates: ExceptionPredicates = ExceptionPredicates()
    @JvmSynthetic get
    @JvmSynthetic set

  fun policyKey(policyKey: String): B {
    this.policyKey = policyKey
    return self()
  }

  @JvmSynthetic
  fun <E : Throwable> handle(exceptionClass: KClass<E>): B {
    exceptionPredicates += {
      when {
        exceptionClass.isInstance(it) -> it
        else -> null
      }
    }
    return self()
  }

  fun <E : Throwable> handle(exceptionClass: Class<E>): B = handle(exceptionClass.kotlin)

  @JvmSynthetic
  fun <E : Throwable> handle(exceptionClass: KClass<E>, exceptionPredicate: (E) -> Boolean): B {
    exceptionPredicates += {
      @Suppress("UNCHECKED_CAST")
      when {
        exceptionClass.isInstance(it) && exceptionPredicate(it as E) -> it
        else -> null
      }
    }
    return self()
  }

  fun <E : Throwable> handle(exceptionClass: Class<E>, exceptionPredicate: (E) -> Boolean): B =
    handle(exceptionClass.kotlin, exceptionPredicate)

  @JvmSynthetic
  fun <E : Throwable> handleCause(causeClass: KClass<E>): B {
    exceptionPredicates += causePredicate {
      causeClass.isInstance(it)
    }
    return self()
  }

  fun <E : Throwable> handleCause(causeClass: Class<E>): B = handleCause(causeClass.kotlin)

  @JvmSynthetic
  fun <E : Throwable> handleCause(causeClass: KClass<E>, exceptionPredicate: (E) -> Boolean): B {
    exceptionPredicates += causePredicate {
      @Suppress("UNCHECKED_CAST")
      causeClass.isInstance(it) && exceptionPredicate(it as E)
    }
    return self()
  }

  fun <E : Throwable> handleCause(causeClass: Class<E>, exceptionPredicate: (E) -> Boolean): B =
    handleCause(causeClass.kotlin, exceptionPredicate)

  @JvmSynthetic
  internal fun causePredicate(predicate: (Throwable) -> Boolean): ExceptionPredicate {
    return fun(exception: Throwable): Throwable? {
      return generateSequence(exception, Throwable::cause)
        .firstOrNull(predicate)
    }
  }

  fun handleResult(resultPredicate: ResultPredicate<R>): B {
    resultPredicates += resultPredicate
    return self()
  }

  fun handleResult(result: R): B {
    resultPredicates += {
      it != null && it == result || it == null && result == null
    }
    return self()
  }

  protected abstract fun self(): B
}

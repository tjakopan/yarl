package hr.tjakopan.yarl

abstract class PolicyBuilder<R, B : PolicyBuilder<R, B>> protected constructor() {
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

  fun <E : Throwable> handle(exceptionClass: Class<E>): B {
    exceptionPredicates += {
      when {
        exceptionClass.isInstance(it) -> it
        else -> null
      }
    }
    return self()
  }

  fun <E : Throwable> handle(exceptionClass: Class<E>, exceptionPredicate: (E) -> Boolean): B {
    exceptionPredicates += {
      @Suppress("UNCHECKED_CAST")
      when {
        exceptionClass.isInstance(it) && exceptionPredicate(it as E) -> it
        else -> null
      }
    }
    return self()
  }

  fun <E : Throwable> handleCause(causeClass: Class<E>): B {
    exceptionPredicates += causePredicate {
      causeClass.isInstance(it)
    }
    return self()
  }

  fun <E : Throwable> handleCause(causeClass: Class<E>, exceptionPredicate: (E) -> Boolean): B {
    exceptionPredicates += causePredicate {
      @Suppress("UNCHECKED_CAST")
      causeClass.isInstance(it) && exceptionPredicate(it as E)
    }
    return self()
  }

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

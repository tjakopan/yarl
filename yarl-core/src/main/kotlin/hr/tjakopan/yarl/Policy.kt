package hr.tjakopan.yarl

import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrElse
import hr.tjakopan.yarl.utilities.KeyHelper

abstract class Policy<R, B : Policy.Builder<R, B>> protected constructor(policyBuilder: Builder<R, B>) :
  ISyncPolicy<R> {
  protected val policyKey: String = policyBuilder.policyKey.getOrElse { "$javaClass-${KeyHelper.guidPart()}" }

  protected val resultPredicates = policyBuilder.resultPredicates

  protected val exceptionPredicates = policyBuilder.exceptionPredicates

  override fun execute(context: Context, action: (Context) -> Option<R>): Option<R> = implementation(context, action)

  override fun executeAndCapture(context: Context, action: (Context) -> Option<R>): PolicyResult<R> {
    return Either<Throwable, R>.
//    return try {
//      val result = execute(context, action)
//      if (resultPredicates.anyMatch(result)) {
//        return PolicyFailureWithResult(result, context)
//      }
//      PolicySuccess(result, context)
//    } catch (exception: Throwable) {
//      PolicyFailureWithException(exception, getExceptionType(exceptionPredicates, exception), context)
//    }
  }

  protected abstract fun implementation(context: Context, action: (Context) -> Option<R>): Option<R>

  abstract class Builder<R, B : Builder<R, B>> protected constructor() {
    var policyKey: Option<String> = Option.empty()

    @JvmSynthetic
    internal var resultPredicates: ResultPredicates<R> = noResultPredicates()
      @JvmSynthetic get
      @JvmSynthetic set

    @JvmSynthetic
    internal var exceptionPredicates: ExceptionPredicates = NO_EXCEPTION_PREDICATES
      @JvmSynthetic get
      @JvmSynthetic set

    fun <E : Throwable> handle(exceptionClass: Class<E>): B = or(exceptionClass)

    fun <E : Throwable> or(exceptionClass: Class<E>): B {
      exceptionPredicates = exceptionPredicates.plus {
        when {
          exceptionClass.isInstance(it) -> Option.just(it)
          else -> Option.empty()
        }
      }
      return self()
    }

    fun <E : Throwable> handle(exceptionClass: Class<E>, exceptionPredicate: (E) -> Boolean): B =
      or(exceptionClass, exceptionPredicate)

    fun <E : Throwable> or(exceptionClass: Class<E>, exceptionPredicate: (E) -> Boolean): B {
      exceptionPredicates = exceptionPredicates.plus {
        @Suppress("UNCHECKED_CAST")
        when {
          exceptionClass.isInstance(it) && exceptionPredicate(it as E) -> Option.just(it)
          else -> Option.empty()
        }
      }
      return self()
    }

    fun <E : Throwable> handleCause(causeClass: Class<E>): B = orCause(causeClass)

    fun <E : Throwable> orCause(causeClass: Class<E>): B {
      exceptionPredicates = exceptionPredicates.plus(handleCause { causeClass.isInstance(it) })
      return self()
    }

    fun <E : Throwable> handleCause(causeClass: Class<E>, exceptionPredicate: (E) -> Boolean): B =
      orCause(causeClass, exceptionPredicate)

    fun <E : Throwable> orCause(causeClass: Class<E>, exceptionPredicate: (E) -> Boolean): B {
      exceptionPredicates = exceptionPredicates.plus(handleCause {
        @Suppress("UNCHECKED_CAST")
        causeClass.isInstance(it) && exceptionPredicate(it as E)
      })
      return self()
    }

    fun handleResult(resultPredicate: (Option<R>) -> Boolean): B = orResult(resultPredicate)

    fun orResult(resultPredicate: ResultPredicate<R>): B {
      resultPredicates = resultPredicates.plus(resultPredicate)
      return self()
    }

    fun handleResult(result: Option<R>): B = orResult(result)

    fun orResult(result: Option<R>): B = orResult { it == result }

    protected abstract fun self(): B

    protected abstract fun build(): Policy<R, B>
  }
}

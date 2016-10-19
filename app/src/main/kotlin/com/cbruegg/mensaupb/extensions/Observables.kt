package com.cbruegg.mensaupb.extensions

import org.funktionale.either.Either
import rx.Observable
import rx.Subscriber
import rx.lang.kotlin.observable
import java.io.IOException

fun <T: Any> ioObservable(completeOnError: Boolean = true, body: (s: Subscriber<in T>) -> Unit): Observable<Either<IOException, T>> {
    return observable {
        try {
            body(it.mapToSingleNonEitherSubscriber())
        } catch (e: IOException) {
            it.onNext(Either.Left(e))
            if (completeOnError) {
                it.onCompleted()
            }
        }
    }
}

/**
 * Only keep left items, mapping them to their left value.
 */
fun <L: Any, R: Any> Observable<Either<L, R>>.filterLeft(): Observable<L> = filter { it.isLeft() }.map { it.left().get() }

/**
 * Only keep right items, mapping them to their right value.
 */
fun <L: Any, R: Any> Observable<Either<L, R>>.filterRight(): Observable<R> = filter { it.isRight() }.map { it.right().get() }


private fun <T: Any, T1: Any> Subscriber<in Either<T, T1>>.mapToSingleNonEitherSubscriber(): Subscriber<in T1> = object : Subscriber<T1>() {
    override fun onNext(t: T1) {
        this@mapToSingleNonEitherSubscriber.onNext(Either.Right(t))
    }

    override fun onError(e: Throwable?) {
        this@mapToSingleNonEitherSubscriber.onError(e)
    }

    override fun onCompleted() {
        this@mapToSingleNonEitherSubscriber.onCompleted()
    }
}

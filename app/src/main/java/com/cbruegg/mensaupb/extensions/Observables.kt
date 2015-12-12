package com.cbruegg.mensaupb.extensions

import org.funktionale.either.Either
import rx.Observable
import rx.Subscriber
import rx.lang.kotlin.observable
import java.io.IOException

public fun <T> ioObservable(completeOnError: Boolean = true, body: (s: Subscriber<in T>) -> Unit): Observable<Either<IOException, T>> {
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

private fun <T, T1> Subscriber<in Either<T, T1>>.mapToSingleNonEitherSubscriber(): Subscriber<in T1> = object : Subscriber<T1>() {
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

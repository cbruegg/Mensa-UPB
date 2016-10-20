package com.cbruegg.mensaupb.extensions

import org.funktionale.either.Either
import rx.Observable
import java.io.IOException

fun <T: Any> ioObservable(body: () -> T): Observable<Either<IOException, T>> {
    return Observable.fromCallable {
        try {
            Either.Right<IOException, T>(body())
        } catch (e: IOException) {
            Either.Left<IOException, T>(e)
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
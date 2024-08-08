package com.inspeco.extensions

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


operator fun AutoClearedDisposable.plusAssign(disposable: Disposable) =  this.add(disposable)

fun runOnIoScheduler(func: () -> Unit):Disposable = Completable.fromCallable(func).subscribeOn(Schedulers.io()).subscribe()
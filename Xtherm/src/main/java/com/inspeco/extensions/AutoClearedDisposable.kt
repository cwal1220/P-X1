package com.inspeco.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable


/**
 * AutoClearedDisposable
 */
class AutoClearedDisposable(
        private val lifecycleOwner: AppCompatActivity,
        /**
   * onStop 콜백 함수가 호출되었을 때 관리하고 있는 디스포저블 객체를 해지할지 여부를 지정. 기본값은 true
   */
  private val alwaysClearOnStop: Boolean = true,
        private val compositeDisposable: CompositeDisposable = CompositeDisposable()
) : LifecycleObserver {

  /**
   * 디스포저블 추가
   */
  fun add(disposable: Disposable) {
    // lifecycleOwner.lifecycle을 사용 참조하고 있는 컴포넌트의 lifecycle 객체에 접근
    // lifecycle.currentState 를 사용하여 상태 정보인 Lifecycle.state에 접근
    // 현재 상태가 특정 상태의 이후 상태인지 여부를 반환
    // 코틀린 표준 라이브러리에서 제공하는 check() 함수로 Lifecycle.State.isAtLeast() 함수의 반환값이 참인지 확인
    // 만약 참이 아닌 경우 IllegalStateException예외를 발생
    check(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))

    // 앞선 검증 절차를 통과한 경우에만 디스포저블 추가
    compositeDisposable.add(disposable)
  }

  /**
   * onStop() 콜백 함수가 호출되면 cleanUp()함수를 호출
   */
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun cleanUp() {
    // onStop() 콜백 함수가 호출 되었을 때 무조건 디스포저블 해제하지 않는 경우
    // 액티비티의 isFinishing 메서드를 사용하여 액티비티가 종료되지 않는 시점(예: 다른 액티비티 호출)에만 디스포저블을 해제하지 않도록 한다.
    if (!alwaysClearOnStop && !lifecycleOwner.isFinishing) {
      return
    }

    compositeDisposable.clear()
  }

  /**
   * onDestory() 콜백 함수가 호출되면 detachSelf() 함수를 호출
   */
  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  fun detachSelf() {
    // 관리하는 디스포저블 해제
    compositeDisposable.clear()

    // 더 이상 액티비티의 생명주기 이벤트를 받지 않도록 액티비티 생명주기 옵서버에서 제거
    lifecycleOwner.lifecycle.removeObserver(this)
  }
}
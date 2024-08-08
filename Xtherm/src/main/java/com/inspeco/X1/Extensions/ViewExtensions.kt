package com.inspeco.X1.Extensions

import android.animation.Animator
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.Animation

/**
 * Noise 라이브러리에서 사용한다. 여러가지 값 정의
 */
val Float.dp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)

val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

val textPaint: () -> Paint = {
    Paint().apply {
        color = Color.parseColor("#AAFFFFFF")
        style = Paint.Style.FILL
        textSize = 12f.px
        typeface = Typeface.MONOSPACE
    }
}

val graphTextPaint: () -> Paint = {
    Paint().apply {
        color = Color.parseColor("#777777")
        style = Paint.Style.FILL
        textSize = 9f.px
        typeface = Typeface.MONOSPACE
    }
}

val graphTextPaint2: () -> Paint = {
    Paint().apply {
        color = Color.parseColor("#888888")
        style = Paint.Style.FILL
        textSize = 12f.px
        typeface = Typeface.MONOSPACE
    }
}

val errTextPaint: () -> Paint = {
    Paint().apply {
        color = Color.parseColor("#BBFF0000")
        style = Paint.Style.FILL
        textSize = 12f.px
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
}

fun ViewPropertyAnimator.onEnd(then: () -> Unit): ViewPropertyAnimator {
    this.setListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            then()
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
        }
    })

    return this
}

fun ViewPropertyAnimator.onTerminate(then: () -> Unit): ViewPropertyAnimator {
    this.setListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            then()
        }

        override fun onAnimationCancel(animation: Animator?) {
            then()
        }

        override fun onAnimationStart(animation: Animator?) {
        }
    })

    return this
}

fun Animation.onTerminate(then: () -> Unit): Animation {
    this.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            then()
        }

        override fun onAnimationStart(animation: Animation?) {
        }
    })

    return this
}

fun View.padding(i: Int) {
    setPadding(i, i, i, i)
}


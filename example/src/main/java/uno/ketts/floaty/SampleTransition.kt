package uno.ketts.floaty

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionSet
import android.util.AttributeSet

/**
 * Created by kettsun0123 on 2018/02/20.
 */
class CustomTransition : TransitionSet {
    constructor() {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        setOrdering(ORDERING_SEQUENTIAL)
        addTransition(Fade(Fade.OUT)).addTransition(initChangeBounds()).addTransition(Fade(Fade.IN))
    }

    private fun initChangeBounds(): Transition {
        val changeBounds = ChangeBounds()
        changeBounds.setDuration(250)
        return changeBounds
    }
}
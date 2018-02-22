package uno.ketts.floaty;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.transition.AutoTransition;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by Yuji Koketsu on 2017/09/27.
 */

public final class Floaty {

    static final int ANIMATION_DURATION = 250;
    static final int ANIMATION_FADE_DURATION = 180;

    public static final int LENGTH_INDEFINITE = -2;
    public static final int LENGTH_SHORT = -1;
    public static final int LENGTH_LONG = 0;

    private final ViewGroup targetParent;
    private final Context context;
    final Stage stage;
    final FloatyContentLayout view;
    private Scene scene;
    private Scene beforeScene;

    static final Handler fHandler;
    static final int MSG_SHOW = 0;
    static final int MSG_DISMISS = 1;
    static final int MSG_REPLACE = 2;

    private final AccessibilityManager accessibilityManager;

    private int duration;
    private Transition transition = new AutoTransition();

    static {
        fHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW:
                        ((Floaty) message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((Floaty) message.obj).hideView();
                        return true;
                    case MSG_REPLACE:
                        ((Floaty) message.obj).replaceView();
                }
                return false;
            }
        });
    }

    public static class Callback {
        public static final int DISMISS_EVENT_SWIPE = 0;
        public static final int DISMISS_EVENT_ACTION = 1;
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;
        public static final int DISMISS_EVENT_REPLACE = 5;

        @RestrictTo(LIBRARY_GROUP)
        @IntDef({
                DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT, DISMISS_EVENT_MANUAL,
                DISMISS_EVENT_CONSECUTIVE, DISMISS_EVENT_REPLACE
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent {
        }

        public void onShown(Floaty sb) {
            // Stub implementation to make API check happy.
        }

        public void onDismissed(Floaty floaty, @DismissEvent int event) {
            // Stub implementation to make API check happy.
        }
    }

    interface OnLayoutChangeListener {
        void onLayoutChange(View view, int left, int top, int right, int bottom);
    }

    @RestrictTo(LIBRARY_GROUP)
    interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View v);

        void onViewDetachedFromWindow(View v);
    }

    private Floaty(ViewGroup parent, View content) {
        if (parent == null) {
            throw new IllegalArgumentException("Transient bottom bar must have non-null parent");
        }
        if (content == null) {
            throw new IllegalArgumentException("Transient bottom bar must have non-null content");
        }

        targetParent = parent;
        context = parent.getContext();
        view = (FloatyContentLayout) content;

        stage = Stage.of(targetParent);

        ViewCompat.setAccessibilityLiveRegion(stage, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
        ViewCompat.setImportantForAccessibility(stage, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

        // Make sure that we fit system windows and have a listener to apply any insets
        stage.setFitsSystemWindows(true);
        ViewCompat.setOnApplyWindowInsetsListener(stage,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        // Copy over the bottom inset as padding so that we're displayed
                        // above the navigation bar
                        v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                                insets.getSystemWindowInsetBottom());
                        return insets;
                    }
                });

        accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        scene = new Scene(stage, view);
    }

    @NonNull
    public static Floaty make(@NonNull View view, @NonNull @LayoutRes int layoutId) {
        final ViewGroup parent = findSuitableParent(view);
        if (parent == null) {
            throw new IllegalArgumentException(
                    "No suitable parent found from the given view. " + "Please provide a valid view.");
        }

        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final FloatyContentLayout content =
                (FloatyContentLayout) inflater.inflate(layoutId, parent, false);
        final Floaty floaty = new Floaty(parent, content);

        floaty.setDuration(LENGTH_SHORT);

        return floaty;
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                } else {
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        return fallback;
    }

    public void show() {
        FloatyManager.getInstance().show(scene, duration, managerCallback);
    }

    public void dismiss() {
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL);
    }

    public void replace() {
        FloatyManager.getInstance().replace(scene, duration, managerCallback);
    }

    public boolean isShown() {
        return FloatyManager.getInstance().isCurrent(managerCallback);
    }

    private void dispatchDismiss(@Callback.DismissEvent int event) {
        FloatyManager.getInstance().dismiss(managerCallback, event);
    }

    private void onViewShown() {
        FloatyManager.getInstance().onShown(managerCallback);
    }


    private void onViewHidden() {
        FloatyManager.getInstance().onDismissed(managerCallback);
        final ViewParent parent = stage.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(stage);
        }
    }

    @NonNull
    public Floaty setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    @NonNull
    public Floaty setTransition(Transition transition) {
        this.transition = transition;
        return this;
    }

    private void showView() {
        stage.addView(view);

        if (stage.getParent() == null) {
            final ViewGroup.LayoutParams lp = stage.getLayoutParams();

            targetParent.addView(stage);
        }

        stage.setOnAttachStateChangeListener(new Floaty.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });

        if (ViewCompat.isLaidOut(stage)) {
            if (shouldAnimate()) {
                animateStageIn();
                animateViewIn();
            }
        } else {
            stage.setOnLayoutChangeListener(new Floaty.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    Floaty.this.stage.setOnLayoutChangeListener(null);

                    if (shouldAnimate()) {
                        animateStageIn();
                        animateViewIn();
                    }
                }
            });
        }
    }

    private void hideView() {
        if (shouldAnimate() && view.getVisibility() == View.VISIBLE) {
            animateViewOut();
            animateStageOut();
        }
    }


    private void replaceView() {
        if (scene != null && beforeScene != null) {
            if (transition == null) {
                transition = new AutoTransition();
            }
            transition.addListener(transitionListener);
            TransitionManager.go(scene, transition);
        }
    }

    private void animateStageIn() {
        final ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(0F, 1F);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float currentAlpha = (float) valueAnimator.getAnimatedValue();
                stage.setAlpha(currentAlpha);
            }
        });
        animator.start();
    }

    private void animateViewIn() {
        final int viewHeight = view.getHeight();
        view.setTranslationY(viewHeight);
        final ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(viewHeight, 0);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                onViewShown();
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private int previousAnimatedIntValue = viewHeight;

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int currentAnimatedIntValue = (int) animator.getAnimatedValue();
                view.setTranslationY(currentAnimatedIntValue);

                previousAnimatedIntValue = currentAnimatedIntValue;
            }
        });
        animator.start();
    }

    private void animateStageOut() {
        final ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(1F, 0F);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                targetParent.removeView(stage);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float currentAlpha = (float) valueAnimator.getAnimatedValue();
                stage.setAlpha(currentAlpha);
            }
        });
        animator.start();
    }

    private void animateViewOut() {
        final ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(0, view.getHeight());
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                stage.removeView(view);
                onViewHidden();
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private int previousAnimatedIntValue = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int currentAnimatedIntValue = (int) animator.getAnimatedValue();
                view.setTranslationY(currentAnimatedIntValue);

                previousAnimatedIntValue = currentAnimatedIntValue;
            }
        });
        animator.start();
    }

    private boolean shouldAnimate() {
        return !accessibilityManager.isEnabled();
    }

    public static final class FloatyLayout extends FloatyBaseLayout {
        public FloatyLayout(Context context) {
            super(context);
        }

        public FloatyLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int childCount = getChildCount();
            int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY));
                }
            }
        }
    }

    static class FloatyBaseLayout extends FrameLayout {
        private Floaty.OnLayoutChangeListener mOnLayoutChangeListener;
        private Floaty.OnAttachStateChangeListener mOnAttachStateChangeListener;

        FloatyBaseLayout(Context context) {
            this(context, null);
        }

        FloatyBaseLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatyLayout);
            if (a.hasValue(R.styleable.FloatyLayout_elevation)) {
                ViewCompat.setElevation(this,
                        a.getDimensionPixelSize(R.styleable.FloatyLayout_elevation, 0));
            }
            a.recycle();

            setClickable(true);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewAttachedToWindow(this);
            }

            ViewCompat.requestApplyInsets(this);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }

        void setOnLayoutChangeListener(Floaty.OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }

        void setOnAttachStateChangeListener(Floaty.OnAttachStateChangeListener listener) {
            mOnAttachStateChangeListener = listener;
        }
    }

    final FloatyManager.Callback managerCallback = new FloatyManager.Callback() {
        @Override
        public void show() {
            fHandler.sendMessage(fHandler.obtainMessage(MSG_SHOW, Floaty.this));
        }

        @Override
        public void replace(Scene before) {
            Floaty.this.beforeScene = before;
            fHandler.sendMessage(fHandler.obtainMessage(MSG_REPLACE, Floaty.this));
        }

        @Override
        public void dismiss(int event) {
            fHandler.sendMessage(fHandler.obtainMessage(MSG_DISMISS, event, 0, Floaty.this));
        }
    };

    final Transition.TransitionListener transitionListener = new Transition.TransitionListener() {
        @Override
        public void onTransitionStart(@NonNull Transition transition) {

        }


        @Override
        public void onTransitionEnd(@NonNull Transition transition) {
            FloatyManager.getInstance().onReplaced(managerCallback);
        }

        @Override
        public void onTransitionCancel(@NonNull Transition transition) {

        }

        @Override
        public void onTransitionPause(@NonNull Transition transition) {

        }

        @Override
        public void onTransitionResume(@NonNull Transition transition) {

        }
    };
}

package uno.ketts.floaty;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by kettsun0123 on 2018/02/15.
 */
public class Stage extends FrameLayout {
    private Floaty.OnLayoutChangeListener onLayoutChangeListener;
    private Floaty.OnAttachStateChangeListener onAttachStateChangeListener;

    private static Stage stage;

    public Stage(@NonNull Context context) {
        super(context);
    }

    public Stage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatyLayout);
        if (a.hasValue(R.styleable.FloatyLayout_elevation)) {
            ViewCompat.setElevation(this,
                    a.getDimensionPixelSize(R.styleable.FloatyLayout_elevation, 0));
        }
        a.recycle();

        setClickable(true);
    }

    public static Stage of(ViewGroup root) {
        if (stage == null) {
            final LayoutInflater inflater = LayoutInflater.from(root.getContext());
            stage = (Stage) inflater.inflate(R.layout.layout_floaty_stage, root, false);
        }
        return stage;
    }

    public static void dismiss() {
        stage.removeAllViews();
    }

    public static void clear() {
        stage = null;
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (onLayoutChangeListener != null) {
            onLayoutChangeListener.onLayoutChange(this, l, t, r, b);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (onAttachStateChangeListener != null) {
            onAttachStateChangeListener.onViewAttachedToWindow(this);
        }

        ViewCompat.requestApplyInsets(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (onAttachStateChangeListener != null) {
            onAttachStateChangeListener.onViewDetachedFromWindow(this);
        }
    }

    void setOnLayoutChangeListener(Floaty.OnLayoutChangeListener listener) {
        onLayoutChangeListener = listener;
    }

    void setOnAttachStateChangeListener(Floaty.OnAttachStateChangeListener listener) {
        onAttachStateChangeListener = listener;
    }
}

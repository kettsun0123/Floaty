package uno.ketts.floaty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Yuji Koketsu on 2017/11/29.
 */
public class FloatyContentLayout extends FrameLayout {

    private int maxWidth;

    public FloatyContentLayout(Context context) {
        this(context, null);
    }

    public FloatyContentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatyContentLayout);
        maxWidth = a.getDimensionPixelSize(R.styleable.FloatyContentLayout_maxWidth, -1);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (maxWidth > 0 && getMeasuredWidth() > maxWidth) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
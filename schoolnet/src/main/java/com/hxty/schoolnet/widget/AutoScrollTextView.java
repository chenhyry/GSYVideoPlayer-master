package com.hxty.schoolnet.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class AutoScrollTextView extends TextView implements OnClickListener {
    public final static String TAG = AutoScrollTextView.class.getSimpleName();

    private float textLength = 0f;//文本长度
    private float viewWidth = 0f;
    private float step = 0f;//文字的横坐标
    private float y = 0f;//文字的纵坐标
    private float temp_view_plus_text_length = 0.0f;//用于计算的临时变量
    private float temp_view_plus_two_text_length = 0.0f;//用于计算的临时变量
    public boolean isStarting = false;//是否开始滚动
    private Paint paint = null;//绘图样式
    private String text = "";//文本内容


    public AutoScrollTextView(Context context) {
        super(context);
        initView();
    }

    public AutoScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AutoScrollTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }


    private void initView() {
        setOnClickListener(this);
    }


    public void init(WindowManager windowManager) {
        paint = getPaint();
        text = getText().toString();
        textLength = paint.measureText(text);
        viewWidth = getWidth();
        if (viewWidth == 0) {
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                viewWidth = display.getWidth();
            }
        }
        step = textLength;
        temp_view_plus_text_length = viewWidth + textLength;
        temp_view_plus_two_text_length = viewWidth + textLength * 2;
//        y = getTextSize() + getPaddingTop();
        y = getHeight() / 2;
    }

//    @Override
//    public Parcelable onSaveInstanceState() {
//        Parcelable superState = super.onSaveInstanceState();
//        SavedState ss = new SavedState(superState);
//
//        ss.step = step;
//        ss.isStarting = isStarting;
//
//        return ss;
//    }
//
//    @Override
//    public void onRestoreInstanceState(Parcelable state) {
//        if (!(state instanceof SavedState)) {
//            super.onRestoreInstanceState(state);
//            return;
//        }
//        SavedState ss = (SavedState) state;
//        super.onRestoreInstanceState(ss.getSuperState());
//
//        step = ss.step;
//        isStarting = ss.isStarting;
//    }
//
//    public static class SavedState extends BaseSavedState {
//        public boolean isStarting = false;
//        public float step = 0.0f;
//
//        SavedState(Parcelable superState) {
//            super(superState);
//        }
//
//        @Override
//        public void writeToParcel(Parcel out, int flags) {
//            super.writeToParcel(out, flags);
//            out.writeBooleanArray(new boolean[]{isStarting});
//            out.writeFloat(step);
//        }
//
//
//        public static final Parcelable.Creator<SavedState> CREATOR
//                = new Parcelable.Creator<SavedState>() {
//
//            public SavedState[] newArray(int size) {
//                return new SavedState[size];
//            }
//
//            @Override
//            public SavedState createFromParcel(Parcel in) {
//                return new SavedState(in);
//            }
//        };
//
//        private SavedState(Parcel in) {
//            super(in);
//            boolean[] b = null;
//            in.readBooleanArray(b);
//            if (b != null && b.length > 0)
//                isStarting = b[0];
//            step = in.readFloat();
//        }
//    }


    public void startScroll() {
        isStarting = true;
        invalidate();
    }


    public void stopScroll() {
        isStarting = false;
        invalidate();
    }

    private onShowNextMsg onShowNextMsg;

    public interface onShowNextMsg {
        void turn();
    }

    public void setOnShowNextMsg(onShowNextMsg onShowNextMsg) {
        this.onShowNextMsg = onShowNextMsg;
    }

    private OnClickTextListener onClickTextListener;

    public interface OnClickTextListener {
        void onClickText();
    }

    public void setOnClickTextListener(OnClickTextListener onClickTextListener) {
        this.onClickTextListener = onClickTextListener;
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(0xffffffff);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (-fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(text, temp_view_plus_text_length - step, y + baseline, paint);
        if (!isStarting) {
            return;
        }
//        step += 0.5;
        step += 1.0;
//        if (step > temp_view_plus_two_text_length)
//            step = textLength;
        if (step >= temp_view_plus_two_text_length) {
            if (onShowNextMsg != null) {
                onShowNextMsg.turn();
            }
        }

        invalidate();
    }

    @Override
    public void onClick(View v) {
        if (isStarting) {
//            stopScroll();
            if (onClickTextListener != null) {
                onClickTextListener.onClickText();
            }
        } else {
//            startScroll();
        }
    }

}
package com.hxty.schoolnet.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.hxty.schoolnet.R;


public class WaitProgressDialog extends Dialog {
    private ProgressBar progressBar;
    private AnimationDrawable animation;
    private int reseonImageId = R.drawable.progress_anim;

    public WaitProgressDialog(Context context, int reseonImageId) {
        super(context, R.style.trandialog);
        if (reseonImageId != 0) this.reseonImageId = reseonImageId;
        this.setCancelable(false);
    }

    public WaitProgressDialog(Context context) {
        this(context, 0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wait_dialog);
//        RelativeLayout relative = (RelativeLayout) findViewById(R.id.);
//        relative.getLayoutParams().width = getScreenWidth(App.getInstance());
//        relative.getLayoutParams().height = getScreenHeight(App.getInstance());
        progressBar = (ProgressBar) findViewById(R.id.wait_progressBar);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        /*if (image.getBackground().getClass() == AnimationDrawable.class) {
            animation = (AnimationDrawable) image.getBackground();
            animation.start();
        }*/
    }

    public void show() {
        super.show();
    }

    @Override
    public void cancel() {
        super.cancel();
        if (animation != null) {
            animation.stop();
        }
    }

    public void setImageBg(int imageResourceId) {
        this.reseonImageId = imageResourceId;
    }

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getHeight();
    }

}

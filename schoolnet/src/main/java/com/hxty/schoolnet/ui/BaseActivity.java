package com.hxty.schoolnet.ui;

import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.hxty.schoolnet.widget.WaitProgressDialog;

public class BaseActivity extends AppCompatActivity {

    public static final int SHOW_SCREENSAVER = 100;

    private WaitProgressDialog waitProgressDialog;

    public void showDialog() {
        if (waitProgressDialog == null || !waitProgressDialog.isShowing()) {
            waitProgressDialog = new WaitProgressDialog(this);
            waitProgressDialog.show();
        }
    }

    public void dismissDialog() {
        if (waitProgressDialog != null && waitProgressDialog.isShowing() && !isFinishing()) {
            waitProgressDialog.dismiss();
            waitProgressDialog = null;
        }
    }


    public void hideNavigationBar() {
        final Window window = getWindow();
        setHideVirtualKey(window);
        window.getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                setHideVirtualKey(window);
            }
        });
    }

    public void setHideVirtualKey(Window window) {
        //保持布局状态
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                //布局位于状态栏下方
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                //全屏
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                //隐藏导航栏
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions |= 0x00001000;
        } else {
            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        window.getDecorView().setSystemUiVisibility(uiOptions);
    }

}

package com.hxty.schoolnet.ui;


import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hxty.schoolnet.App;
import com.hxty.schoolnet.R;
import com.hxty.schoolnet.widget.CustomControlVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;


public class ScreenVideoActivity extends BaseActivity {

    private CustomControlVideo player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_video);

        ImageView click_me_iv = findViewById(R.id.click_me_iv);
        click_me_iv.setOnClickListener(v -> finish());

        //视频
        player = findViewById(R.id.player);

        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption
                .setIsTouchWiget(false)//是否可以滑动调整
                .setRotateViewAuto(false)
//                .setLockLand(true)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
//                .setNeedLockFull(true)
                .setUrl(getUrl())
                .setCacheWithPlay(true)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                    }

                    @Override
                    public void onPlayError(String url, Object... objects) {
                        Toast.makeText(ScreenVideoActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
                        App.currentPlayIndex++;
                        ScreenVideoActivity.this.finish();
                    }

                    @Override
                    public void onAutoComplete(String url, Object... objects) {
                        App.currentPlayIndex++;
                        player.setUp(getUrl(), true, "");
//                        player.setPlayTag(getUrl());
                        //播放
                        player.startPlayLogic();
                    }
                }).build(player);

        player.setNeedHideTopBottomAuto(false);
        player.setNeedtouchDoubleUp(false);
        player.getClTop().setVisibility(View.GONE);
        player.getRlBottom().setVisibility(View.GONE);

//        player.setPlayTag(videoUrl);
        //播放
        player.startPlayLogic();
    }

    private String getUrl() {
        App.currentPlayIndex = App.currentPlayIndex % App.screenVideoUrls.size();
        return App.screenVideoUrls.get(App.currentPlayIndex);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }

    @Override
    public void onBackPressed() {
        //释放所有
        player.setVideoAllCallBack(null);
        super.onBackPressed();
    }


    /**
     * 当前后左右键被按下的时候，被触发(这里可是有前提的哦，那就是当前的activity中必须没有view正在监听按键
     * ，例如：当前如果有一个EditText正在等待输入，当我们按下dpad时，不会触发事件哦)
     * Activity.onKeyDown();
     * 当某个键被按下时会触发，但不会被任何的该Activity内的任何view处理。
     * 默认按下KEYCODE_BACK键后会回到上一个Activity。
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN://按向下键
            case KeyEvent.KEYCODE_DPAD_UP:// 按向上键
            case KeyEvent.KEYCODE_DPAD_LEFT://按向左键
            case KeyEvent.KEYCODE_DPAD_RIGHT://按向右键
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                finish();
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}

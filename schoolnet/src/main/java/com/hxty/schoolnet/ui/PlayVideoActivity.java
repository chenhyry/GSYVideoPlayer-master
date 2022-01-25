package com.hxty.schoolnet.ui;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.hxty.schoolnet.App;
import com.hxty.schoolnet.R;
import com.hxty.schoolnet.entity.BaseResponse;
import com.hxty.schoolnet.net.JsonCallback;
import com.hxty.schoolnet.net.RequestManager;
import com.hxty.schoolnet.utils.sphelper.CommonValue;
import com.hxty.schoolnet.utils.sphelper.ConfigHelper;
import com.hxty.schoolnet.widget.CustomControlVideo;
import com.lzy.okgo.model.Response;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;


public class PlayVideoActivity extends BaseActivity {

    private int KeyId;
    private String videoTitle;
    private String videoUrl;
    private String videoImgUrl;
    private String videoThumb;
    private ImageView ivLike;
    private Animation animation;
    private ConfigHelper helper;

    private CustomControlVideo player;
    private boolean playStaus = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        KeyId = getIntent().getIntExtra("KeyId", -1);
        videoTitle = getIntent().getStringExtra("videoTitle");
        videoUrl = getIntent().getStringExtra("videoUrl");
        videoImgUrl = getIntent().getStringExtra("videoImgUrl");
        videoThumb = getIntent().getStringExtra("videoThumb");

        ivLike = findViewById(R.id.ivLike);
        helper = ConfigHelper.getDefaultConfigHelper(PlayVideoActivity.this);
        ivLike.setOnClickListener(v -> doLike());

        //视频
        player = findViewById(R.id.player);

        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
        ImageView imageView = new ImageView(this);
        if (!TextUtils.isEmpty(videoImgUrl)) {
            Glide.with(this).load(videoImgUrl).into(imageView);
        } else if (!TextUtils.isEmpty(videoThumb)) {
            Glide.with(this).load(videoThumb).into(imageView);
        }
        gsyVideoOption
                .setThumbImageView(imageView)
                .setIsTouchWiget(true)//是否可以滑动调整
                .setRotateViewAuto(false)
//                .setLockLand(true)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
//                .setNeedLockFull(true)
                .setUrl(videoUrl)
                .setCacheWithPlay(true)
                .setVideoTitle(videoTitle)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                        hasPressLike();
                    }

                    @Override
                    public void onPlayError(String url, Object... objects) {
                        PlayVideoActivity.this.finish();
                        Toast.makeText(PlayVideoActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAutoComplete(String url, Object... objects) {
                        PlayVideoActivity.this.finish();
                        if ("1".equals(App.SCHOOLTYPE)) {//登录状态到学校首页
                            PlayVideoActivity.this.startActivity(new Intent(PlayVideoActivity.this, SchoolMainActivity.class));
                        } else if ("2".equals(App.SCHOOLTYPE)) {//登录状态到机构首页
                            PlayVideoActivity.this.startActivity(new Intent(PlayVideoActivity.this, JGDWMainActivity.class));
                        } else {//普通用户 未登录到平台
                            PlayVideoActivity.this.startActivity(new Intent(PlayVideoActivity.this, MainActivity.class));
                        }
                    }
                }).build(player);

        player.getTvTitle().setText(videoTitle);

        player.setPlayTag(videoUrl);
        //播放
        player.startPlayLogic();

        //增加节目点击次数
        RequestManager.getInstance().AddProgramClickNum(KeyId + "", new JsonCallback<BaseResponse<String>>() {
            @Override
            public void onSuccess(Response<BaseResponse<String>> response) {

            }
        });
        initListener();
        hideNavigationBar();
    }

    private void doLike() {
        if (!ivLike.isSelected()) {
            if (animation == null) {
                animation = AnimationUtils.loadAnimation(PlayVideoActivity.this, R.anim.unlove_love);
            }
            ivLike.startAnimation(animation);
            ivLike.setSelected(true);

            RequestManager.getInstance().AddProgramDianzan(KeyId + "", helper.getString(CommonValue.TOKEN, CommonValue.USER_NULL_DEFAULT), new JsonCallback<BaseResponse<String>>() {
                @Override
                public void onSuccess(Response<BaseResponse<String>> response) {

                }

                @Override
                public void onError(Response<BaseResponse<String>> response) {
                    super.onError(response);
                    ivLike.setSelected(false);
                }
            });
        } else {
            Toast.makeText(PlayVideoActivity.this, "已点赞", Toast.LENGTH_SHORT).show();
        }
    }


    public void initListener() {
        player.getBack().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//        player.getVolumeSeekBar().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser) {
//                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
//                    int currentVolume1 = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                    seekBar.setProgress(currentVolume1);
//                    int percentVolume = (int) ((float) currentVolume1 / (float) maxVolume * 100);
//                    player.getVolumePercent().setText(String.valueOf(percentVolume) + "%");
//                }
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
////                textView.setText("开始了");
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
////                textView.setText("停止了，当前值为："+seekBarProgress);
////                if(seekBarProgress == seekBar.getMax()){
////                    textView.setText("达到最值");
////                }
//            }
//        });
        player.getPlayImg().setOnClickListener(view -> {
            if (playStaus) {
                player.onVideoPause();
                player.getPlayImg().setBackgroundResource(R.drawable.ic_play);
                playStaus = false;
            } else {
                player.onVideoResume();
                player.getPlayImg().setBackgroundResource(R.drawable.ic_video_pause);
                playStaus = true;
            }
        });
        player.getIvShare().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UMImage image = new UMImage(PlayVideoActivity.this, videoThumb);
                UMVideo video = new UMVideo(videoUrl);
                video.setTitle(videoTitle);//视频的标题
                video.setThumb(image);//视频的缩略图
                video.setDescription(videoTitle);//视频的描述
                new ShareAction(PlayVideoActivity.this).withMedia(video)
                        .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
                        .setCallback(umShareListener).open();
            }
        });
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

    private void hasPressLike() {
        //是否点过赞
        if (!TextUtils.isEmpty(App.SCHOOLTYPE)) {
            ivLike.setVisibility(View.VISIBLE);
            RequestManager.getInstance().CheckProgramDianzan(KeyId + "", helper.getString(CommonValue.TOKEN, CommonValue.USER_NULL_DEFAULT), new JsonCallback<BaseResponse<String>>() {
                @Override
                public void onSuccess(Response<BaseResponse<String>> response) {
                    if ("1".equals(response.body().handleResult)) {
                        ivLike.setSelected(true);
                    }
                }
            });
        } else {
            ivLike.setVisibility(View.GONE);
        }
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
                break;
            case KeyEvent.KEYCODE_DPAD_UP:// 按向上键
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://按向左键
                int positionBack = (int) (player.getCurrentPositionWhenPlaying() - player.getDuration() * 0.08f);
                if (positionBack < 0) {
                    positionBack = 0;
                }
                player.seekTo(positionBack);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT://按向右键
                int positionFor = (int) (player.getCurrentPositionWhenPlaying() - player.getDuration() * 0.08f);
                if (positionFor > player.getDuration()) {
                    positionFor = player.getDuration();
                }
                player.seekTo(positionFor);
                break;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                player.getPlayImg().performClick();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA platform) {
            //分享开始的回调
        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            Toast.makeText(PlayVideoActivity.this, platform + " 分享成功啦", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            Toast.makeText(PlayVideoActivity.this, platform + " 分享失败啦", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            Toast.makeText(PlayVideoActivity.this, platform + " 分享取消了", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

}

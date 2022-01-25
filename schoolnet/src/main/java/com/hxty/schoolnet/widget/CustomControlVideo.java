package com.hxty.schoolnet.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.hxty.schoolnet.R;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;


/**
 * Created by shuyu on 2016/12/7.
 * 注意
 * 这个播放器的demo配置切换到全屏播放器
 * 这只是单纯的作为全屏播放显示，如果需要做大小屏幕切换，请记得在这里耶设置上视频全屏的需要的自定义配置
 */

public class CustomControlVideo extends StandardGSYVideoPlayer {

    private ConstraintLayout clTop;
    private RelativeLayout surface_container;
    private RelativeLayout rlBottom;
    private TextView mMoreScale;
    private TextView tvTitle;
    private ImageView ivBack;
    private ImageView ivShare;

    private Handler handler;
    private Runnable runnable;
    private static final int HIDE_TOP_BOTTOM = 100;
    private boolean needtouchDoubleUp = true;
    private boolean needHideTopBottomAuto = true;

    //记住切换数据源类型
    private int mType = 0;

    private int mTransformSize = 0;

    //数据源
    private int mSourcePosition = 0;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public CustomControlVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public CustomControlVideo(Context context) {
        super(context);
    }

    public CustomControlVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private TextView tv_volume;
    private SeekBar volume_progress;
    private ImageView iv_show_list;

    @Override
    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {
        super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
        //不给触摸快进，如果需要，屏蔽下方代码即可
//        mChangePosition = false;

        //不给触摸音量，如果需要，屏蔽下方代码即可
        mChangeVolume = false;

        //不给触摸亮度，如果需要，屏蔽下方代码即可
//        mBrightness = false;
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        initView();
    }

    private void initView() {
        mMoreScale = findViewById(R.id.moreScale);
        mMoreScale.setVisibility(GONE);
        volume_progress = findViewById(R.id.volume_progress);
        tv_volume = findViewById(R.id.tv_volume);
        ic_paly_pause = findViewById(R.id.ic_paly_pause);
        iv_show_list = findViewById(R.id.iv_show_list);

        tvTitle = findViewById(R.id.tvTitle);
        ivBack = findViewById(R.id.ivBack);
        ivShare = findViewById(R.id.ivShare);
        clTop = findViewById(R.id.clTop);
        surface_container = findViewById(R.id.surface_container);
        rlBottom = findViewById(R.id.rlBottom);
        surface_container.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (needHideTopBottomAuto) {
                    if (handler == null) {
                        handler = new Handler();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                handler.removeCallbacks(runnable);
                                rlBottom.setVisibility(View.GONE);
                                clTop.setVisibility(View.GONE);
                            }
                        };
                    }
                    handler.removeCallbacks(runnable);

                    if (rlBottom.getVisibility() == View.VISIBLE) {
                        rlBottom.setVisibility(View.GONE);
                        clTop.setVisibility(View.GONE);
                    } else {
                        rlBottom.setVisibility(View.VISIBLE);
                        clTop.setVisibility(View.VISIBLE);
                        handler.postDelayed(runnable, 3000);
                    }
                }
            }
        });

        //切换清晰度
        mMoreScale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHadPlay) {
                    return;
                }
                if (mType == 0) {
                    mType = 1;
                } else if (mType == 1) {
                    mType = 2;
                } else if (mType == 2) {
                    mType = 3;
                } else if (mType == 3) {
                    mType = 4;
                } else if (mType == 4) {
                    mType = 0;
                }
                resolveTypeUI();
            }
        });

    }


    /**
     * 双击暂停/播放
     * 如果不需要，重载为空方法即可
     */
    protected void touchDoubleUp(MotionEvent e) {
        if (!mHadPlay) {
            return;
        }
        if (needtouchDoubleUp) {
            clickStartIcon();
        }
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {
        super.onSurfaceSizeChanged(surface, width, height);
        resolveTransform();
    }

    /**
     * 处理显示逻辑
     */
    @Override
    public void onSurfaceAvailable(Surface surface) {
        super.onSurfaceAvailable(surface);
        resolveRotateUI();
        resolveTransform();
    }

    /**
     * 处理镜像旋转
     * 注意，暂停时
     */
    protected void resolveTransform() {
        switch (mTransformSize) {
            case 1: {
                Matrix transform = new Matrix();
                transform.setScale(-1, 1, mTextureView.getWidth() / 2, 0);
                mTextureView.setTransform(transform);
                mTextureView.invalidate();
            }
            break;
            case 2: {
                Matrix transform = new Matrix();
                transform.setScale(1, -1, 0, mTextureView.getHeight() / 2);
                mTextureView.setTransform(transform);
                mTextureView.invalidate();
            }
            break;
            case 0: {
                Matrix transform = new Matrix();
                transform.setScale(1, 1, mTextureView.getWidth() / 2, 0);
                mTextureView.setTransform(transform);
                mTextureView.invalidate();
            }
            break;
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.custom_control_video;
    }


    public void setNeedtouchDoubleUp(boolean needtouchDoubleUp) {
        this.needtouchDoubleUp = needtouchDoubleUp;
    }

    public void setNeedHideTopBottomAuto(boolean needHideTopBottomAuto) {
        this.needHideTopBottomAuto = needHideTopBottomAuto;
    }

    /**
     * 获取返回按键
     */
    public ImageView getBack() {
        return ivBack;
    }

    /**
     * 获取titile
     */
    public TextView getTvTitle() {
        return tvTitle;
    }

    public ImageView getIvShare() {
        return ivShare;
    }

    public RelativeLayout getRlBottom() {
        return rlBottom;
    }

    public ConstraintLayout getClTop() {
        return clTop;
    }

    //播放按钮
    public ImageView getPlayImg() {
        return ic_paly_pause;
    }

    //pop list
    public ImageView getShowList() {
        return iv_show_list;
    }


    //播放暂停
    public ImageView ic_paly_pause;

    //音量控制进度条
    public SeekBar getVolumeSeekBar() {
        return volume_progress;
    }

    //音量进度半分比
    public TextView getVolumePercent() {
        return tv_volume;
    }

    /**
     * 推出全屏时将对应处理参数逻辑返回给非播放器
     *
     * @param oldF
     * @param vp
     * @param gsyVideoPlayer
     */
    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        if (gsyVideoPlayer != null) {
            CustomControlVideo sampleVideo = (CustomControlVideo) gsyVideoPlayer;
            mSourcePosition = sampleVideo.mSourcePosition;
            mType = sampleVideo.mType;
            mTransformSize = sampleVideo.mTransformSize;
            resolveTypeUI();
        }
    }

    /**
     * 旋转逻辑
     */
    private void resolveRotateUI() {
        if (!mHadPlay) {
            return;
        }
        mTextureView.setRotation(mRotate);
        mTextureView.requestLayout();
    }

    /**
     * 显示比例
     * 注意，GSYVideoType.setShowType是全局静态生效，除非重启APP。
     */
    private void resolveTypeUI() {
        if (!mHadPlay) {
            return;
        }
        if (mType == 1) {
            mMoreScale.setText("16:9");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);
        } else if (mType == 2) {
            mMoreScale.setText("4:3");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
        } else if (mType == 3) {
            mMoreScale.setText("全屏");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
        } else if (mType == 4) {
            mMoreScale.setText("拉伸全屏");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
        } else if (mType == 0) {
            mMoreScale.setText("默认比例");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
        }
        changeTextureViewShowType();
        if (mTextureView != null)
            mTextureView.requestLayout();
    }


}

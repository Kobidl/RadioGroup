package com.komi.radiogroup.audio_recorder;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.komi.radiogroup.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class AudioRecordButton extends RelativeLayout {

    private static final int TIME_OUT = 60 * 1000;
    private final int DEFAULT_ICON_SIZE = Math.round(getResources().getDimension(R.dimen.default_icon_size));
    private final int DEFAULT_REMOVE_ICON_SIZE =  Math.round(getResources().getDimension(R.dimen.default_icon_remove_size));

    private Context mContext;
    private RelativeLayout mLayoutTimer;
    private RelativeLayout mLayoutVoice;

    private Chronometer mChronometer;
    private ImageView mImageView;
    private ImageButton mImageButton;
    private AudioListener mAudioListener;
    private AudioRecording mAudioRecording;

    private float initialX = 0;
    private float initialXImageButton;
    private float initialTouchX;

    private int recorderImageWidth = 0;
    private int recorderImageHeight = 0;
    private int removeImageWidth = 0;
    private int removeImageHeight = 0;
    private Drawable drawableMicVoice;
    private Drawable drawableRemoveButton;
    private boolean isPlaying = false;
    private boolean isPausing = false;

    private boolean enabled = true;
    Timer t;
    TimerTask tt;
    Handler handler = new Handler();

    private WindowManager.LayoutParams params;

    public AudioRecordButton(Context context) {
        super(context);
        setupLayout(context, null, -1, -1);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupLayout(context, attrs, -1, -1);
    }

    public AudioRecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupLayout(context, attrs, defStyleAttr, -1);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AudioRecordButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setupLayout(context, attrs, defStyleAttr, defStyleRes);
    }

    WindowManager.LayoutParams getViewParams() {
        return this.params;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!enabled) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isPlaying && !isPausing) {
                    isPlaying = true;
                    initialTouchX = event.getRawX();
                    changeImageView();

                    if (this.initialX == 0) {
                        this.initialX = this.mImageView.getX();
                    }

                    mLayoutTimer.setVisibility(VISIBLE);
                    mImageButton.setVisibility(VISIBLE);
                    startRecord();
                    vibrate(80);
                    t = new Timer();
                    tt = new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    MotionEvent myEvent = MotionEvent.obtain(1000, 1000, MotionEvent.ACTION_UP, 0, 0, 0);
                                    onTouchEvent(myEvent);
                                    vibrate(200);
                                }
                            });
                        };
                    };
                    t.schedule(tt,TIME_OUT+1000);

                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (isPlaying && !isPausing) {

                    this.mImageView.setX(event.getX() - this.mImageView.getWidth() / 2);

                    if (this.mImageView.getX() < DEFAULT_REMOVE_ICON_SIZE - 20) {
                        this.mImageView.setX(0);
                        this.changeSizeToRemove();
                    } else if (this.mImageView.getX() > DEFAULT_REMOVE_ICON_SIZE + DEFAULT_REMOVE_ICON_SIZE / 2) {
                        this.unRevealSizeToRemove();
                    }

                    if (this.mImageView.getX() <= 0) {
                        this.mImageButton.setX(0);
                    }

                    if (this.mImageView.getX() > this.initialX) {
                        this.mImageView.setX(this.initialX);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                if(tt!=null){
                    tt.cancel();
                }
                if(t!=null){
                    t.cancel();
                }
                if (isPlaying && !isPausing) {
                    isPausing = true;
                    moveImageToBack();

                    mLayoutTimer.setVisibility(INVISIBLE);
                    mImageButton.setVisibility(INVISIBLE);

                    if (this.mImageView.getX() < DEFAULT_REMOVE_ICON_SIZE - 10) {
                        stopRecord(true);
                    } else {
                        stopRecord(false);
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private void vibrate(int vibration) {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(vibration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(vibration);
        }

    }

    private void moveImageToBack() {
        this.mImageButton.setAlpha(0.5f);
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(this.mImageView.getX(), this.initialX);

        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) animation.getAnimatedValue();
                mImageView.setX(x);
                if (mImageView.getX() > DEFAULT_REMOVE_ICON_SIZE){
                    unRevealSizeToRemove();
                }
            }
        });

        positionAnimator.setDuration(200);
        positionAnimator.start();
    }

    private void startRecord() {
        if (mAudioListener != null) {
            AudioListener audioListener = new AudioListener() {
                @Override
                public void onStop(RecordingItem recordingItem) {
                    mAudioListener.onStop(recordingItem);
                }

                @Override
                public void onCancel() {
                    mAudioListener.onCancel();
                }

                @Override
                public void onError(Exception e) {
                    mAudioListener.onError(e);
                }
            };

            this.mAudioRecording =
                    new AudioRecording(this.mContext)
                            .setNameFile("/" + UUID.randomUUID() + "-audio.ogg")
                            .start(audioListener);
        }
    }

    private void stopRecord(final Boolean cancel) {
        if (mAudioListener != null) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAudioRecording.stop(cancel);
                    unRevealImageView();
                    isPlaying = false;
                    isPausing = false;
                }
            }, 300);
        }
    }

    public void setOnAudioListener(AudioListener audioListener) {
        this.mAudioListener = audioListener;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setupLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        /**
         *  Component Attributes
         */
        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AudioButton,
                    defStyleAttr, defStyleRes);

            recorderImageWidth = (int) typedArray.getDimension(R.styleable.AudioButton_recorder_image_size,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            recorderImageHeight = (int) typedArray.getDimension(R.styleable.AudioButton_recorder_image_size,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            removeImageWidth = (int) typedArray.getDimension(R.styleable.AudioButton_remove_image_size,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            removeImageHeight = (int) typedArray.getDimension(R.styleable.AudioButton_remove_image_size,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            drawableMicVoice = typedArray.getDrawable(R.styleable.AudioButton_recorder_image);
            drawableRemoveButton = typedArray.getDrawable(R.styleable.AudioButton_remove_image);
        }

        /**
         * layout to chronometer
         */
        mLayoutTimer = new RelativeLayout(context);
        mLayoutTimer.setId(9 + 1);
        mLayoutTimer.setVisibility(INVISIBLE);
        mLayoutTimer.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_event));

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        Integer margin = Math.round(getResources().getDimension(R.dimen.chronometer_margin));
        layoutParams.setMargins(margin, margin, margin, margin);
        addView(mLayoutTimer, layoutParams);

        /**
         * chronometer
         */
        this.mChronometer = new Chronometer(context);
        this.mChronometer.setTextColor(Color.WHITE);

        LayoutParams layoutParamsChronometer = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParamsChronometer.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mLayoutTimer.addView(this.mChronometer, layoutParamsChronometer);

        /**
         * Layout to voice and cancel audio
         */
        mLayoutVoice = new RelativeLayout(context);
        LayoutParams layoutVoiceParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutVoiceParams.addRule(RelativeLayout.BELOW, 9 + 1);
        addView(this.mLayoutVoice, layoutVoiceParams);

        /**
         * Image voice
         */
        this.mImageView = new ImageView(context);
        this.mImageView.setBackground(drawableMicVoice != null ? drawableMicVoice : ContextCompat.getDrawable(context, R.drawable.mic_shape));
        LayoutParams layoutParamImage = new LayoutParams(
                recorderImageWidth > 0 ? recorderImageWidth : DEFAULT_ICON_SIZE,
                recorderImageHeight > 0 ? recorderImageHeight : DEFAULT_ICON_SIZE);
        layoutParamImage.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        this.mLayoutVoice.addView(this.mImageView, layoutParamImage);

        /**
         * Image Button
         */
        this.mImageButton = new ImageButton(context);
        this.mImageButton.setVisibility(INVISIBLE);
        this.mImageButton.setAlpha(0.5f);
        this.mImageButton.setImageDrawable(drawableRemoveButton != null ? drawableRemoveButton : ContextCompat.getDrawable(context, R.drawable.ic_close));
        this.mImageButton.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_circle));
        this.mImageButton.setColorFilter(Color.WHITE);

        LayoutParams layoutParamImageButton = new LayoutParams(
                ((removeImageWidth > 0) && (removeImageWidth < DEFAULT_REMOVE_ICON_SIZE)) ? removeImageWidth : DEFAULT_REMOVE_ICON_SIZE,
                ((removeImageHeight > 0) && (removeImageHeight < DEFAULT_REMOVE_ICON_SIZE)) ? removeImageHeight : DEFAULT_REMOVE_ICON_SIZE
        );
        layoutParamImageButton.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        this.mLayoutVoice.addView(this.mImageButton, layoutParamImageButton);

        this.initialXImageButton = this.mImageButton.getX();

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
        if(enabled)
            this.mImageView.setBackground(drawableMicVoice != null ? drawableMicVoice : ContextCompat.getDrawable(mContext, R.drawable.mic_shape));
        else
            this.mImageView.setBackground(drawableMicVoice != null ? drawableMicVoice : ContextCompat.getDrawable(mContext, R.drawable.mic_shape_disabled));
    }

    public void changeImageView() {
        LayoutTransition transition = new LayoutTransition();
        transition.setDuration(600);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            transition.enableTransitionType(LayoutTransition.CHANGING);
        }
        this.mLayoutTimer.setLayoutTransition(transition);
        this.setLayoutTransition(transition);

        this.mChronometer.setBase(SystemClock.elapsedRealtime());
        this.mChronometer.start();
        
        this.mImageView.setScaleX(0.8f);
        this.mImageView.setScaleY(0.8f);
        this.requestLayout();
    }

    public void changeSizeToRemove() {
        if (this.mImageButton.getLayoutParams().width != this.mImageView.getWidth()) {
            this.mImageButton.getLayoutParams().width = this.mImageView.getWidth();
            this.mImageButton.getLayoutParams().height = this.mImageView.getHeight();
            this.mImageButton.requestLayout();
            this.mImageButton.setX(0);
        }
    }

    public void unRevealSizeToRemove() {
        this.mImageButton.getLayoutParams().width = ((removeImageWidth > 0) && (removeImageWidth < DEFAULT_REMOVE_ICON_SIZE)) ? removeImageWidth : DEFAULT_REMOVE_ICON_SIZE;
        this.mImageButton.getLayoutParams().height = ((removeImageHeight > 0) && (removeImageHeight < DEFAULT_REMOVE_ICON_SIZE)) ? removeImageHeight : DEFAULT_REMOVE_ICON_SIZE;
        this.mImageButton.requestLayout();
    }

    public void unRevealImageView() {
        this.mChronometer.stop();

        this.mImageView.setScaleX(1f);
        this.mImageView.setScaleY(1f);
        this.requestLayout();
    }

}
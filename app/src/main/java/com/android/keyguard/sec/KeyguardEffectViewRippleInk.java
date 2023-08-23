package com.android.keyguard.sec;

import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.aj.effect.MainActivity;
import com.aj.effect.R;
import com.samsung.android.visualeffect.EffectDataObj;
import com.samsung.android.visualeffect.EffectView;
import com.samsung.android.visualeffect.IEffectListener;

import java.io.InputStream;
import java.util.HashMap;

/* loaded from: classes.dex */
public class KeyguardEffectViewRippleInk extends EffectView implements KeyguardEffectViewBase {
    private static final int DOWN_SOUND_PATH = R.raw.ve_ripple_down; //"/system/media/audio/ui/ve_ripple_down.ogg";
    public static final int UPDATE_TYPE_CHANGE_BACKGROUND = 1;
    public static final int UPDATE_TYPE_USER_SWITCHING = 2;
    private static final int UP_SOUND_PATH = R.raw.ve_ripple_up; //"/system/media/audio/ui/ve_ripple_up.ogg";
    final int SOUND_ID_DOWN = 0;
    final int SOUND_ID_DRAG = 1;
    final int SOUND_ID_LOCK = 2;
    final int SOUND_ID_UNLOCK = 3;
    private final String TAG;
    private final long UNLOCK_SOUND_PLAY_TIME;
    private boolean isSystemSoundChecked;
    private boolean isUnlocked;
    KeyguardManager keyguardManager;
    private float leftVolumeMax;
    private Context mContext;
    private SoundHandler mHandler;
    private IEffectListener mListener;
    private SoundPool mSoundPool;
    private Runnable releaseSoundRunnable;
    private float rightVolumeMax;
    Message soundMsg;
    private int[] sounds;
    private int windowHeight;
    private int windowWidth;

    public KeyguardEffectViewRippleInk(Context context) {
        super(context);
        this.TAG = "RippleInk_KeyguardEffect";
        this.mSoundPool = null;
        this.leftVolumeMax = 1.0f;
        this.rightVolumeMax = 1.0f;
        this.UNLOCK_SOUND_PLAY_TIME = 2000L;
        this.releaseSoundRunnable = null;
        this.isSystemSoundChecked = true;
        this.sounds = null;
        this.isUnlocked = false;
        this.windowWidth = 0;
        this.windowHeight = 0;
        init(context);
    }

    private void init(Context context) {
        Log.d("RippleInk_KeyguardEffect", "KeyguardEffectViewRippleInk Constructor");
        this.mContext = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        this.windowWidth = displayMetrics.widthPixels;
        this.windowHeight = displayMetrics.heightPixels;
        Log.d("RippleInk_KeyguardEffect", "KeyguardEffectViewRippleInk windowWidth = " + this.windowWidth + ", windowHeight = " + this.windowHeight);
        setEffect(8);
        EffectDataObj data = new EffectDataObj();
        data.setEffect(8);
        data.rippleInkData.windowWidth = this.windowWidth;
        data.rippleInkData.windowHeight = this.windowHeight;
        data.rippleInkData.reflectionBitmap = makeResBitmap(R.drawable.reflectionmap);
        init(data);
        this.sounds = new int[4];
        HashMap<String, Bitmap> map2 = new HashMap<>();
        map2.put("Bitmap", setBackground());
        handleCustomEvent(0, map2);
        this.mListener = new IEffectListener() { // from class: com.android.keyguard.sec.effect.KeyguardEffectViewRippleInk.1
            public void onReceive(int status, HashMap<?, ?> params) {
                if (status == 1) {
                    if (((String) params.get("sound")).contentEquals("down")) {
                        KeyguardEffectViewRippleInk.this.playSound(0);
                    } else if (((String) params.get("sound")).contentEquals("drag")) {
                        KeyguardEffectViewRippleInk.this.playSound(1);
                    }
                }
            }
        };
        setListener(this.mListener);
    }

    private Bitmap setBackground() {
        Log.d("RippleInk_KeyguardEffect", "setBackground");
        Bitmap pBitmap = MainActivity.bitm; /* todo = null;
        BitmapDrawable newBitmapDrawable = KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext);
        if (newBitmapDrawable != null) {
            pBitmap = newBitmapDrawable.getBitmap();
            if (pBitmap != null) {
                Log.d("RippleInk_KeyguardEffect", "pBitmap.width = " + pBitmap.getWidth() + ", pBitmap.height = " + pBitmap.getHeight());
            } else {
                Log.d("RippleInk_KeyguardEffect", "pBitmap is null");
            }
        } else {
            Log.d("RippleInk_KeyguardEffect", "newBitmapDrawable is null");
        }*/
        return pBitmap;
    }

    private Bitmap makeResBitmap(int res) {
        Bitmap result = null;
        try {
            InputStream is = this.mContext.getResources().openRawResource(res);
            result = BitmapFactory.decodeStream(is);
            is.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    private void makeSound() {
        stopReleaseSound();
        if (this.mSoundPool == null) { // todo (KeyguardProperties.isEffectProcessSeparated() || KeyguardUpdateMonitor.getInstance(this.mContext).hasBootCompleted()) &&
            Log.d("RippleInk_KeyguardEffect", "WaterColor sound : new SoundPool");
            AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            this.mSoundPool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(attr).build();
            this.sounds[0] = this.mSoundPool.load(mContext, DOWN_SOUND_PATH, 1);
            this.sounds[1] = this.mSoundPool.load(mContext, UP_SOUND_PATH, 1);
            sounds[SOUND_ID_LOCK] = mSoundPool.load(mContext, R.raw.lock_ripple, 1);
            sounds[SOUND_ID_UNLOCK] = mSoundPool.load(mContext, R.raw.unlock_ripple, 1);
        }
        if (this.mHandler == null) {
            Log.d("RippleInk_KeyguardEffect", "new SoundHandler()");
            this.mHandler = new SoundHandler();
        }
    }

    private void releaseSound() {
        this.releaseSoundRunnable = new Runnable() { // from class: com.android.keyguard.sec.effect.KeyguardEffectViewRippleInk.2
            @Override // java.lang.Runnable
            public void run() {
                if (KeyguardEffectViewRippleInk.this.mSoundPool != null) {
                    Log.d("RippleInk_KeyguardEffect", "WaterColor sound : release SoundPool");
                    KeyguardEffectViewRippleInk.this.mSoundPool.release();
                    KeyguardEffectViewRippleInk.this.mSoundPool = null;
                }
                KeyguardEffectViewRippleInk.this.releaseSoundRunnable = null;
            }
        };
        postDelayed(this.releaseSoundRunnable, 2000L);
    }

    private void stopReleaseSound() {
        if (this.releaseSoundRunnable != null) {
            removeCallbacks(this.releaseSoundRunnable);
            this.releaseSoundRunnable = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void playSound(int soundId) {
        checkSound();
        stopReleaseSound();
        if (this.mSoundPool == null) {
            Log.d("RippleInk_KeyguardEffect", "ACTION_DOWN, mSoundPool == null");
            makeSound();
        }
        if (this.isSystemSoundChecked && this.mSoundPool != null) {
            int streanID = this.mSoundPool.play(this.sounds[soundId], this.leftVolumeMax, this.rightVolumeMax, 0, 0, 1.0f);
            if (soundId == 1 && this.mHandler != null) {
                this.soundMsg = this.mHandler.obtainMessage();
                this.soundMsg.arg1 = streanID - 1;
                this.soundMsg.arg2 = 4;
                this.mHandler.sendMessage(this.soundMsg);
            }
        }
    }

    private void checkSound() {
        ContentResolver cr = this.mContext.getContentResolver();
        int result = Settings.System.getInt(cr, "lockscreen_sounds_enabled", -2);
        this.isSystemSoundChecked = result == 1;
    }

    /* loaded from: classes.dex */
    public class SoundHandler extends Handler {
        public SoundHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (KeyguardEffectViewRippleInk.this.mSoundPool != null) {
                float volume = 0.2f * KeyguardEffectViewRippleInk.this.soundMsg.arg2;
                KeyguardEffectViewRippleInk.this.mSoundPool.setVolume(KeyguardEffectViewRippleInk.this.soundMsg.arg1, volume, volume);
                if (msg.arg2 != 0) {
                    KeyguardEffectViewRippleInk.this.soundMsg = KeyguardEffectViewRippleInk.this.mHandler.obtainMessage();
                    KeyguardEffectViewRippleInk.this.soundMsg.arg1 = msg.arg1;
                    KeyguardEffectViewRippleInk.this.soundMsg.arg2 = msg.arg2 - 1;
                    sendMessageDelayed(KeyguardEffectViewRippleInk.this.soundMsg, 10L);
                }
            }
        }
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void show() {
        Log.d("RippleInk_KeyguardEffect", "show");
        makeSound();
        reInit(null);
        clearScreen();
        this.isUnlocked = false;
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void reset() {
        Log.d("RippleInk_KeyguardEffect", "reset");
        clearScreen();
        this.isUnlocked = false;
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void cleanUp() {
        Log.d("RippleInk_KeyguardEffect", "cleanUp");
        stopReleaseSound();
        releaseSound();
        postDelayed(new Runnable() { // from class: com.android.keyguard.sec.effect.KeyguardEffectViewRippleInk.3
            @Override // java.lang.Runnable
            public void run() {
                KeyguardEffectViewRippleInk.this.clearScreen();
                KeyguardEffectViewRippleInk.this.isUnlocked = false;
            }
        }, 400L);
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void update() {
        Log.d("RippleInk_KeyguardEffect", "update");
        HashMap<String, Bitmap> map = new HashMap<>();
        map.put("Bitmap", setBackground());
        handleCustomEvent(0, map);
    }

    public void update(int updateType) {
        Log.d("RippleInk_KeyguardEffect", "changeBackground()");
        HashMap<String, Bitmap> map = new HashMap<>();
        map.put("Bitmap", setBackground());
        handleCustomEvent(0, map);
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void screenTurnedOn() {
        Log.d("RippleInk_KeyguardEffect", "screenTurnedOn");
        this.isUnlocked = false;
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void screenTurnedOff() {
        Log.d("RippleInk_KeyguardEffect", "screenTurnedOff");
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void showUnlockAffordance(long startDelay, Rect rect) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("StartDelay", Long.valueOf(startDelay));
        map.put("Rect", rect);
        handleCustomEvent(1, map);
        this.isUnlocked = false;
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public long getUnlockDelay() {
        return 0L;
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void handleUnlock(View view, MotionEvent event) {
        Log.d("RippleInk_KeyguardEffect", "handleUnlock");
        this.isUnlocked = true;
        playSound(SOUND_ID_UNLOCK);
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void playLockSound() {
        playSound(SOUND_ID_LOCK);
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (!this.isUnlocked) {
            handleTouchEvent(event, view);
        }
        return true;
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public boolean handleHoverEvent(View view, MotionEvent event) {
        return true;
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("RippleInk_KeyguardEffect", "onDetachedFromWindow");
        if (this.mHandler != null) {
            this.mHandler.removeMessages(0);
            this.mHandler = null;
        }
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void setHidden(boolean isHidden) {
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.d("RippleInk_KeyguardEffect", "onWindowFocusChanged hasWindowFocus = " + hasWindowFocus);
        if (hasWindowFocus || !this.isUnlocked) {
        }
    }

    @Override // com.android.keyguard.sec.effect.KeyguardEffectViewBase
    public void setContextualWallpaper(Bitmap bmp) {
        Log.d("RippleInk_KeyguardEffect", "setContextualWallpaper");
        if (bmp == null) {
            Log.d("RippleInk_KeyguardEffect", "bmp is null" + bmp);
            return;
        }
        Bitmap bmp2 = KeyguardEffectViewUtil.getPreferredConfigBitmap(bmp, Bitmap.Config.ARGB_8888);
        HashMap<String, Bitmap> map = new HashMap<>();
        map.put("Bitmap", bmp2);
        handleCustomEvent(0, map);
    }

    public static boolean isBackgroundEffect() {
        return true;
    }

    public static String getCounterEffectName() {
        return null;
    }
}
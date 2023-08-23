package com.aj.effect;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.keyguard.sec.KeyguardEffectViewController;
import com.android.keyguard.sec.KeyguardUnlockView;

public class MainActivity extends Activity {

    public static Bitmap bitm;
    public static Canvas canv;
    public static Button multiactionButton;
    public static boolean unlockBool;

    public static int effect = KeyguardEffectViewController.EFFECT_MONTBLANC;

    ImageView imgView;

    KeyguardEffectViewController controller;
    KeyguardUnlockView mUnlockView;
    FrameLayout mBackgroundRootLayout;
    FrameLayout mForegroundRootLayout;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        w.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        /*EffectView mEffectView = new EffectView(this);
        KeyguardEffectViewBase mEffectView = new KeyguardEffectViewBrilliantRing(this);
        KeyguardUnlockView mUnlockView = new KeyguardUnlockView(this);
        mEffectView.setEffect(7);
        if (mEffectView != null) {
            mEffectView.reset();
            mEffectView.show();
            mEffectView.update();
        }
        mUnlockView.setUnlockView(mEffectView); */

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        bitm = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
        canv = new Canvas(bitm);

        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imageView);

        controller = KeyguardEffectViewController.getInstance(this);
        mUnlockView = findViewById(R.id.keyguard_unlock_view);
        mBackgroundRootLayout = findViewById(R.id.keyguard_effect_behind);
        mForegroundRootLayout = findViewById(R.id.keyguard_effect_front);

        /*KeyguardUnlockView mUnlockView = findViewById(R.id.keyguard_unlock_view);
        Handler handler = new Handler();*/

        multiactionButton = findViewById(R.id.multiact);
        multiactionButton.setOnClickListener(v -> {
            mUnlockView.reset();
            controller.reset();
            multiactionButton.setAlpha(0.5F);
            multiactionButton.setText(R.string.wall);
        });
        //WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable realWall = getDrawable(R.drawable.bluesky); //wallpaperManager.getFastDrawable();
        Drawable wall = getDrawable(R.drawable.wall);
        Drawable wall1 = getDrawable(R.drawable.wall1);
        Drawable prev = getDrawable(R.drawable.setting_preview_unlock);
        multiactionButton.setOnLongClickListener(v -> {
            if (imgView.getDrawable() == prev)
                imgView.setImageDrawable(wall1);
            else if (imgView.getDrawable() == wall1)
                imgView.setImageDrawable(realWall);
            else
                imgView.setImageDrawable(prev);

            imgView.draw(canv);
            controller.handleWallpaperImageChanged();
            return true;
        });

        Button affordance = findViewById(R.id.affordance);
        affordance.setOnClickListener(v -> {
            mUnlockView.showUnlockAffordance();
        });

        Switch unlock = findViewById(R.id.unlock);
        unlockBool = unlock.isChecked();
        unlock.setOnCheckedChangeListener((compoundButton, b) -> unlockBool = b);

        Button effectSwitch = findViewById(R.id.effectsw);
        effectSwitch.setText(R.string.unlock_effect);
        effectSwitch.setOnClickListener(view -> {
            //findViewById(R.id.buttons).setAlpha(0.0f);
            controller.playLockSound();
            switchActivity(this);
            /*if (effect == KeyguardEffectViewController.EFFECT_ABSTRACTTILE) {
                effectSwitch.setText(R.string.unlock_effect_brilliant_ring);
                effect = KeyguardEffectViewController.EFFECT_BRILLIANTRING;
            } else if (effect == KeyguardEffectViewController.EFFECT_BRILLIANTRING) {
                effectSwitch.setText(R.string.light_effect);
                effect = KeyguardEffectViewController.EFFECT_LIGHT;
            } else if (effect == KeyguardEffectViewController.EFFECT_LIGHT) {
                effectSwitch.setText(R.string.blind_effect);
                effect = KeyguardEffectViewController.EFFECT_BLIND;
            } else {
                effectSwitch.setText(R.string.unlock_effect_abstract);
                effect = KeyguardEffectViewController.EFFECT_ABSTRACTTILE;
            }*/

        });
        TextView clock = findViewById(R.id.clock);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) clock.getLayoutParams();
        int fontSize = this.getResources().getDimensionPixelSize(R.dimen.kg_singleclock_time_text_size_normal);
        lp.leftMargin = -((int) (fontSize / 15.0f));
        clock.setLayoutParams(lp);
        controller.setEffectLayout(mBackgroundRootLayout, mForegroundRootLayout, null);
        /*mBackgroundRootLayout.addView(controller, -1, -1);
        controller.update();
        /*getWindow().setBackgroundDrawableResource(R.drawable.wall);
        FrameLayout view = findViewById(R.id.lay);
        view.addView(mUnlockView); */
    }

    public static void switchActivity(Context context) {
        Intent intent = new Intent(context, UnlockEffect.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        controller.screenTurnedOff();
    }

    @Override
    public void onResume() {
        super.onResume();
        controller.screenTurnedOn();
        controller.show();
        mUnlockView.showUnlockAffordance();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        imgView.draw(canv);
        controller.update();
    }

}
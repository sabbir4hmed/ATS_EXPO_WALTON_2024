package com.sabbir.atsexpo.waltonmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private int[] imageResources = {
            R.drawable.a,
            R.drawable.b,
            R.drawable.c,
            R.drawable.d,
            //R.drawable.e,
            // Add your image resources here
    };
    private Techniques[] animations = {
            // Current animations
            Techniques.SlideInLeft,
            Techniques.SlideInRight,
            Techniques.SlideInUp,
            Techniques.SlideInDown
    };

    private int currentImageIndex = 0;
    private long lastVolumeUpPress = 0;
    private static final long DOUBLE_PRESS_INTERVAL = 500;
    private Handler handler;
    private Runnable animationRunnable;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        // Request focus for the window
        getWindow().getDecorView().setFocusable(true);
        getWindow().getDecorView().setFocusableInTouchMode(true);
        getWindow().getDecorView().requestFocus();

        setupWindow();
        setupUI();
        startImageTransition();
    }

private void setupWindow() {
    // Set all window flags
    getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    );

    // Block navigation buttons
    View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LOW_PROFILE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE
    );

    // Additional navigation blocking
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        getWindow().setDecorFitsSystemWindows(false);
        WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.hide(
                    WindowInsets.Type.statusBars() |
                            WindowInsets.Type.navigationBars() |
                            WindowInsets.Type.systemGestures() |
                            WindowInsets.Type.systemBars()
            );
            insetsController.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
    }

    // Set maximum brightness
    try {
        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 204);
    } catch (Exception e) {
        e.printStackTrace();
    }

    WindowManager.LayoutParams params = getWindow().getAttributes();
    params.screenBrightness = 1.0f;
    params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
    getWindow().setAttributes(params);

    // Block all gestures for Android 11+
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        decorView.post(() -> {
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars()
                        | WindowInsets.Type.navigationBars()
                        | WindowInsets.Type.systemGestures());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        });
    }
}

    private void setupUI() {
        imageView = findViewById(R.id.imageView);
        handler = new Handler();
        hideSystemUI();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    /*private void startImageTransition() {
        // Initialize first image
        imageView.setImageResource(imageResources[0]);

        animationRunnable = new Runnable() {
            @Override
            public void run() {
                int nextImageIndex = (currentImageIndex + 1) % imageResources.length;

                // Create next image view with proper configuration
                ImageView nextImageView = new ImageView(MainActivity.this);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                nextImageView.setLayoutParams(params);
                nextImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                nextImageView.setImageResource(imageResources[nextImageIndex]);

                // Position the next image off-screen to the right
                nextImageView.setTranslationX(imageView.getWidth());

                // Add to layout
                RelativeLayout rootLayout = (RelativeLayout) imageView.getParent();
                rootLayout.addView(nextImageView);

                // Animate both views simultaneously
                nextImageView.animate()
                        .translationX(0)
                        .setDuration(1000)
                        .setInterpolator(new AccelerateDecelerateInterpolator());

                imageView.animate()
                        .translationX(-imageView.getWidth())
                        .setDuration(1000)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(() -> {
                            // Update current image and reset position
                            imageView.setTranslationX(0);
                            imageView.setImageResource(imageResources[nextImageIndex]);
                            rootLayout.removeView(nextImageView);
                            currentImageIndex = nextImageIndex;
                        });

                handler.postDelayed(this, 4000);
            }
        };
        handler.post(animationRunnable);
    }
*/

    private void startImageTransition() {
        // Initialize the first image
        imageView.setImageResource(imageResources[0]);

        animationRunnable = new Runnable() {
            @Override
            public void run() {
                int nextImageIndex = (currentImageIndex + 1) % imageResources.length;

                // Create the next image view
                ImageView nextImageView = new ImageView(MainActivity.this);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                nextImageView.setLayoutParams(params);
                nextImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                nextImageView.setImageResource(imageResources[nextImageIndex]);

                // Add to layout
                RelativeLayout rootLayout = (RelativeLayout) imageView.getParent();
                rootLayout.addView(nextImageView);

                // Determine slide direction
                int direction = random.nextInt(4); // 0 = up, 1 = down, 2 = left, 3 = right
                float nextImageStartX = 0, nextImageStartY = 0, currentImageEndX = 0, currentImageEndY = 0;

                switch (direction) {
                    case 0: // Slide Up
                        nextImageStartY = rootLayout.getHeight(); // Start below
                        currentImageEndY = -rootLayout.getHeight(); // Slide current up
                        break;
                    case 1: // Slide Down
                        nextImageStartY = -rootLayout.getHeight(); // Start above
                        currentImageEndY = rootLayout.getHeight(); // Slide current down
                        break;
                    case 2: // Slide Left
                        nextImageStartX = rootLayout.getWidth(); // Start on the right
                        currentImageEndX = -rootLayout.getWidth(); // Slide current left
                        break;
                    case 3: // Slide Right
                        nextImageStartX = -rootLayout.getWidth(); // Start on the left
                        currentImageEndX = rootLayout.getWidth(); // Slide current right
                        break;
                }

                // Position the next image initially
                nextImageView.setTranslationX(nextImageStartX);
                nextImageView.setTranslationY(nextImageStartY);

                // Animators for next and current images
                ObjectAnimator nextImageSlideX = ObjectAnimator.ofFloat(nextImageView, "translationX", 0);
                ObjectAnimator nextImageSlideY = ObjectAnimator.ofFloat(nextImageView, "translationY", 0);
                ObjectAnimator currentImageSlideX = ObjectAnimator.ofFloat(imageView, "translationX", currentImageEndX);
                ObjectAnimator currentImageSlideY = ObjectAnimator.ofFloat(imageView, "translationY", currentImageEndY);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(nextImageSlideX, nextImageSlideY, currentImageSlideX, currentImageSlideY);
                animatorSet.setDuration(1000); // Animation duration
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Update current image and reset position
                        imageView.setImageResource(imageResources[nextImageIndex]);
                        imageView.setTranslationX(0);
                        imageView.setTranslationY(0);
                        rootLayout.removeView(nextImageView);
                        currentImageIndex = nextImageIndex;
                    }
                });
                animatorSet.start();

                // Schedule the next transition
                handler.postDelayed(this, 4000);
            }
        };
        handler.post(animationRunnable);
    }





    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        );
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideSystemUI();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastVolumeUpPress <= DOUBLE_PRESS_INTERVAL) {
                finish();
                return true;
            }
            lastVolumeUpPress = currentTime;
            return true;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && animationRunnable != null) {
            handler.removeCallbacks(animationRunnable);
        }
    }
}


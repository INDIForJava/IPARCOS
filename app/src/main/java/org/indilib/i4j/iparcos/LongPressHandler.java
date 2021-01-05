package org.indilib.i4j.iparcos;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author marcocipriani01
 */
public abstract class LongPressHandler {

    private final Handler handler = new Handler();
    protected final View incrementalView;
    protected final View decrementalView;
    protected boolean autoIncrement = false;
    protected boolean autoDecrement = false;
    protected final long delay;

    private final Runnable counterRunnable = new Runnable() {
        @Override
        public void run() {
            if (autoIncrement) {
                onIncrement();
                handler.postDelayed(this, delay);
            } else if (autoDecrement) {
                onDecrement();
                handler.postDelayed(this, delay);
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    public LongPressHandler(View incrementView, View decrementView, long delay) {
        this.delay = delay;
        this.incrementalView = incrementView;
        this.decrementalView = decrementView;

        this.decrementalView.setOnClickListener(v -> onDecrement());
        this.decrementalView.setOnLongClickListener(v -> {
            autoDecrement = true;
            handler.postDelayed(counterRunnable, LongPressHandler.this.delay);
            return false;
        });
        this.decrementalView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && autoDecrement) {
                autoDecrement = false;
            }
            return false;
        });

        this.incrementalView.setOnClickListener(v -> onIncrement());
        this.incrementalView.setOnLongClickListener(v -> {
            autoIncrement = true;
            handler.postDelayed(counterRunnable, LongPressHandler.this.delay);
            return false;
        });
        this.incrementalView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && autoIncrement) {
                autoIncrement = false;
            }
            return false;
        });
    }

    protected abstract void onIncrement();

    protected abstract void onDecrement();
}
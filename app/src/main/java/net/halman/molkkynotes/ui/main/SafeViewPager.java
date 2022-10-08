package net.halman.molkkynotes.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class SafeViewPager extends ViewPager {
    public SafeViewPager(@NonNull Context context) { super(context); }
    public SafeViewPager(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (Exception ignore) {}
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return super.onInterceptTouchEvent(event);
        } catch (Exception ignore) {}
        return false;
    }
}

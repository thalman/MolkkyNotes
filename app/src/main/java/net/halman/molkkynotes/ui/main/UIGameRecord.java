package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.MolkkySheet;
import net.halman.molkkynotes.R;

public class UIGameRecord extends View implements MolkkySheet.SheetDrawable {
    private MolkkyGame _game = null;
    private ShapeDrawable _line = new ShapeDrawable(new RectShape());
    private ScaleGestureDetector _scale_detector;
    private GestureDetector _gesture_detector;
    private Canvas _canvas = null;
    private float _scale_factor = 1.0f;
    private float _scale_point_x = 0.0f;
    private float _scale_point_y = 0.0f;
    private int _pan_x = 0;
    private int _pan_y = 0;
    private float _last_touch_x = 0.0f;
    private float _last_touch_y = 0.0f;
    private int _what_to_draw = -1;

    public static final int DRAW_CURRENT_ROUND = 1;
    public static final int DRAW_GAME = 2;

    // https://developer.android.com/training/gestures/scale
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            _scale_factor *= detector.getScaleFactor();
            _scale_point_x =  detector.getFocusX();
            _scale_point_y = detector.getFocusY();
            // Don't let the object get too small or too large.
            _scale_factor = Math.max(0.1f, Math.min(_scale_factor, 5.0f));

            setX(10.0f);
            invalidate();
            return true;
        }
    }

    public UIGameRecord(Context context) {
        super(context);
        init(context, null, 0);
    }

    public UIGameRecord(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public UIGameRecord(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        _scale_detector = new ScaleGestureDetector(context, new ScaleListener());
        _gesture_detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener());
//        setPivotX(0.0f);
//        setPivotY(0.0f);
//        setScaleX(1.0f);
//        setScaleY(1.0f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        _scale_detector.onTouchEvent(ev);
        final int action = ev.getAction();

        switch(action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = (ev.getX() - _scale_point_x) / _scale_factor;
                final float y = (ev.getY() - _scale_point_y) / _scale_factor;
                _last_touch_x = x;
                _last_touch_y = y;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float x = (ev.getX() - _scale_point_x)/_scale_factor;
                final float y = (ev.getY() - _scale_point_y)/_scale_factor;
                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!_scale_detector.isInProgress()) {
                    final float dx = x - _last_touch_x; // change in X
                    final float dy = y - _last_touch_y; // change in Y
                    _pan_x += dx;
                    _pan_y += dy;
                    invalidate();
                }

                _last_touch_x = x;
                _last_touch_y = y;
                break;
            }

            case MotionEvent.ACTION_UP: {
                final float x = (ev.getX() - _scale_point_x)/_scale_factor;
                final float y = (ev.getY() - _scale_point_y)/_scale_factor;
                _last_touch_x = 0;
                _last_touch_y = 0;
                invalidate();
            }
        }

        return true;
    }

    public void game(MolkkyGame game)
    {
        _game = game;
        invalidate();
    }

    public void whatToDraw(int what)
    {
        _what_to_draw = what;
    }

    public void drawText(int x1, int y1, int x2, int y2, String text, int fontSize, int typeface, boolean centered)
    {
        TextPaint tp = new TextPaint();
        tp.setColor(getResources().getColor(R.color.colorWhite));
        tp.setTextSize(fontSize);
        tp.setAntiAlias(true);
        tp.setTypeface(Typeface.create(Typeface.DEFAULT, typeface));

        int padding = 10;
        x1 += padding;
        y1 += padding;
        int width = x2 - x1 - 2*padding;
        int height = y2 - y1 - 2*padding;

        StaticLayout sl = new StaticLayout(text, tp, width,
                centered ? Layout.Alignment.ALIGN_CENTER : Layout.Alignment.ALIGN_NORMAL,
                1.0f, 0.0f, false);
        int h = sl.getHeight();

        _canvas.save();
        _canvas.translate(x1, y1 + (height - h) / 2);
        sl.draw(_canvas);
        _canvas.restore();
    }

    public void drawCircle(int x, int y, int r, boolean thick)
    {
        Paint tp = new Paint();
        tp.setColor(getResources().getColor(R.color.colorWhite));
        tp.setStrokeWidth(thick ? 6 : 3);
        tp.setStyle(Paint.Style.STROKE);
        tp.setAntiAlias(true);
        _canvas.drawCircle(x, y, r, tp);
    }

    public void drawLine(int x1, int y1, int x2, int y2, boolean thick)
    {
        Paint tp = new Paint();
        tp.setColor(getResources().getColor(R.color.colorWhite));
        tp.setAntiAlias(true);
        tp.setStrokeWidth(thick ? 6 : 3);
        tp.setStyle(Paint.Style.STROKE);
        _canvas.drawLine(x1, y1, x2, y2, tp);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        _canvas = canvas;
        _canvas.save();
        _canvas.scale(_scale_factor, _scale_factor, _scale_point_x, _scale_point_y);
        _canvas.translate(_pan_x, _pan_y);
        MolkkySheet sheet = new MolkkySheet();
        switch (_what_to_draw) {
            case DRAW_CURRENT_ROUND:
                sheet.setOffset(10,10);
                Rect R = sheet.currentRound(_game, this, getContext());
                sheet.setOffset(10, R.bottom);
                sheet.currentGame(_game, this, getContext());
                break;
        }
        _canvas.restore();
    }
}

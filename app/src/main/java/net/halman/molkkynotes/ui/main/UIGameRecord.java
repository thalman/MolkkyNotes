package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
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
//    private GestureDetector _gesture_detector;
    private Canvas _canvas = null;
    private float _scale_factor = 1.0f;
    private float _scale_point_x = 0.0f;
    private float _scale_point_y = 0.0f;
    private int _pan_x = 0;
    private int _pan_y = 0;
    private float _last_touch_x = 0.0f;
    private float _last_touch_y = 0.0f;
    private int _what_to_draw = -1;
    private boolean _need_fit = true;

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
//        _gesture_detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener());
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
                try {
                    final float x = (ev.getX() - _scale_point_x) / _scale_factor;
                    final float y = (ev.getY() - _scale_point_y) / _scale_factor;
                    _last_touch_x = x;
                    _last_touch_y = y;
                } catch (Exception ignored) {}
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                try {
                    final float x = (ev.getX() - _scale_point_x) / _scale_factor;
                    final float y = (ev.getY() - _scale_point_y) / _scale_factor;
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
                } catch (Exception ignored) {}
                break;
            }

            case MotionEvent.ACTION_UP: {
                try {
                    final float x = (ev.getX() - _scale_point_x) / _scale_factor;
                    final float y = (ev.getY() - _scale_point_y) / _scale_factor;
                    _last_touch_x = 0;
                    _last_touch_y = 0;
                    invalidate();
                } catch (Exception ignored) {};
                break;
            }
        }

        return true;
    }

    public void game(MolkkyGame game)
    {
        if (_game != game) {
            _game = game;
            fit();
        } else {
            invalidate();
        }
    }

    public void whatToDraw(int what)
    {
        _what_to_draw = what;
    }

    public void drawDrawable(int x1, int y1, int x2, int y2, Drawable drawable)
    {
        // not needed
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
        x2 -= padding;
        y2 -= padding;
        int width = x2 - x1;
        int height = y2 - y1;

        StaticLayout sl = new StaticLayout(text, tp, width,
                centered ? Layout.Alignment.ALIGN_CENTER : Layout.Alignment.ALIGN_NORMAL,
                1.0f, 0.0f, false);
        int h = sl.getHeight();
        while (h > y2 - y1 && fontSize > 1) {
            fontSize--;
            tp.setTextSize(fontSize);
            sl = new StaticLayout(text, tp, width,
                    centered ? Layout.Alignment.ALIGN_CENTER : Layout.Alignment.ALIGN_NORMAL,
                    1.0f, 0.0f, false);
            h = sl.getHeight();
        }

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

    public void fit()
    {
        _pan_y = 0;
        _pan_x = 0;
        _scale_factor = 1.0f;
        _scale_point_x = 0;
        _scale_point_y = 0;
        _need_fit = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        _canvas = canvas;
        boolean finished = false;
        while(! finished) {
            _canvas.save();
            _canvas.scale(_scale_factor, _scale_factor, _scale_point_x, _scale_point_y);
            _canvas.translate(_pan_x, _pan_y);

            MolkkySheet sheet = new MolkkySheet();
            sheet.dryRun(_need_fit);
            Rect R = null;
            switch (_what_to_draw) {
                case DRAW_CURRENT_ROUND: {
                    sheet.setOffset(10, 10);
                    R = sheet.currentRound(_game, true, this, getContext());
                    sheet.setOffset(10, R.bottom);
                    Rect R2 = sheet.currentGame(_game, this, getContext());
                    R.bottom += R2.bottom;
                    R.right = Math.max(R.right, R2.right);
                    break;
                }
                case DRAW_GAME: {
                    sheet.setOffset(10, 10);
                    R = sheet.currentGame(_game, this, getContext());
                    int y = R.bottom;
                    for (int idx = 0; idx < _game.rounds().size(); idx++) {
                        sheet.setOffset(10, y);
                        Rect R2 = sheet.round(_game, idx, false, true, this, getContext());
                        y += R2.bottom;
                        R.right = Math.max(R.right, R2.right);
                    }

                    R.bottom = y;
                    break;
                }
            }

            _canvas.restore();
            finished = ! _need_fit;
            if (_need_fit) {
                if (R != null) {
                    float w = getWidth();
                    float h = getHeight();
                    _scale_factor = Math.min(w / (R.right + 20), h / (R.bottom + 20));
                }
                _need_fit = false;
            }
        }
    }
}

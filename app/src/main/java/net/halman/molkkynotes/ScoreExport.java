package net.halman.molkkynotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.File;
import java.io.FileOutputStream;

public class ScoreExport implements MolkkySheet.SheetDrawable {
    private Canvas _canvas;
    private Bitmap _bitmap;
    private Context _context;

    public ScoreExport(Context context)
    {
        _context = context;
    }

    public void drawText(int x1, int y1, int x2, int y2, String text, int fontSize, int typeface, boolean centered)
    {
        TextPaint tp = new TextPaint();
        tp.setColor(_context.getResources().getColor(R.color.colorBlack));
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
        tp.setColor(_context.getResources().getColor(R.color.colorBlack));
        tp.setStrokeWidth(thick ? 6 : 3);
        tp.setStyle(Paint.Style.STROKE);
        tp.setAntiAlias(true);
        _canvas.drawCircle(x, y, r, tp);
    }

    public void drawLine(int x1, int y1, int x2, int y2, boolean thick)
    {
        Paint tp = new Paint();
        tp.setColor(_context.getResources().getColor(R.color.colorBlack));
        tp.setAntiAlias(true);
        tp.setStrokeWidth(thick ? 6 : 3);
        tp.setStyle(Paint.Style.STROKE);
        _canvas.drawLine(x1, y1, x2, y2, tp);
    }

    public void drawDrawable(int x1, int y1, int x2, int y2, Drawable drawable)
    {
        drawable.setBounds(x1, y1, x2, y2);
        drawable.draw(_canvas);
    }

    public void jpeg (MolkkyGame game, String file_name)
    {
        Rect size = new Rect(0, 0, 100, 100);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        for (int i = 1; i <= 2; ++i) {
            _bitmap = Bitmap.createBitmap(size.right, size.bottom, conf);
            _canvas = new Canvas(_bitmap);
            _canvas.drawColor(Color.WHITE);


            MolkkySheet sheet = new MolkkySheet();
            sheet.setOffset(100, 100);
            Rect R = sheet.title(game, this, _context);
            sheet.setOffset(100, 100 + R.bottom);
            Rect R2 = sheet.currentGame(game, this, _context);
            R.bottom += R2.bottom;
            R.right = Math.max(R.right, R2.right);

            int y = R.bottom + 100;
            for (int idx = 0; idx < game.rounds().size(); idx++) {
                sheet.setOffset(100, y);
                R2 = sheet.round(game, idx, false, true, this, _context);
                y += R2.bottom;
                R.right = Math.max(R.right, R2.right);
            }

            size.right = R.right + 200;
            size.bottom = y + 100;
        }

        File file = new File(file_name);
        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            _bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

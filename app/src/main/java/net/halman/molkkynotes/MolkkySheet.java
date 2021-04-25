package net.halman.molkkynotes;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class MolkkySheet {
    private int _box_height = 120;
    private int _box_width = 90;
    private int _text_size = 50;
    private int _offset_x = 0;
    private int _offset_y = 0;
    private boolean _dry_run = false;

    public void dryRun(boolean dry_run)
    {
        _dry_run = dry_run;
    }

    private void drawLine(SheetDrawable d, int x1, int y1, int x2, int y2, boolean thick)
    {
        if (! _dry_run) {
            d.drawLine(_offset_x + x1, _offset_y + y1, _offset_x + x2, _offset_y + y2, thick);
        }
    }

    private void drawBoxedLine(SheetDrawable d, float x1, float y1, float x2, float y2, boolean thick)
    {
        drawLine(d,
                (int)(x1 * _box_width), (int)(y1 * _box_height),
                (int)(x2 * _box_width), (int)(y2 * _box_height), thick);
    }

    void drawText(SheetDrawable d, int x1, int y1, int x2, int y2, String text, int textSize, int typeface, boolean centered)
    {
        if (! _dry_run) {
            d.drawText(x1 + _offset_x, y1 + _offset_y, x2 + _offset_x, y2 + _offset_y, text, textSize, typeface, centered);
        }
    }

    void drawBoxedText(SheetDrawable d, float x1, float y1, float x2, float y2, String text, int textSize, int typeface, boolean centered)
    {
        drawText(d,
                (int)(x1 * _box_width), (int)(y1 * _box_height),
                (int)(x2 * _box_width), (int)(y2 * _box_height),
                text, textSize, typeface, centered);
    }

    void drawBoxedTextCN(SheetDrawable d, float x1, float y1, float x2, float y2, String text, int textSize)
    {
        drawBoxedText(d, x1, y1, x2, y2, text, textSize, Typeface.NORMAL, true);
    }

    void drawBoxedCircle(SheetDrawable d, float x1, float y1, float x2, float y2, boolean thick)
    {
        if (_dry_run) {
            return;
        }

        x1 = x1 * _box_width + _offset_x;
        x2 = x2 * _box_width + _offset_x;
        y1 = y1 * _box_height + _offset_y;
        y2 = y2 * _box_height + _offset_y;

        int R =  (int)(Math.min((x2 - x1), (y2 - y1)) * 0.4f);
        d.drawCircle((int)((x1 + x2)/ 2), (int)((y1 + y2) / 2), R, thick);
    }

    void drawBoxedTextPenalty(SheetDrawable d, float x1, float y1, float x2, float y2, String text, int textSize)
    {
        drawText(d,
                (int)(x1 * _box_width), (int)(y1 * _box_height),
                (int)(x2 * _box_width), (int)(y2 * _box_height),
                text, textSize, Typeface.NORMAL, true);
        float dx = (x2 - x1) * 0.2f;
        float dy = (y2 - y1) * 0.4f;
        drawBoxedLine(d, x1 + dx, y2 - dy, x2 - dx, y1 + dy, textSize > 35);
    }


    void drawDrawable(SheetDrawable d, float x1, float y1, float x2, float y2, Drawable drawable)
    {
        if (! _dry_run) {
            d.drawDrawable((int)(_offset_x + x1), (int)(_offset_y + y1), (int)(_offset_x + x2), (int)(_offset_y + y2), drawable);
        }
    }

    public Rect round(MolkkyGame game, int roundidx, boolean sortTeams, boolean alignColumns, SheetDrawable sheet, Context context)
    {
        if (game == null || sheet == null || context == null) {
            return new Rect(0, 0, 0, 0);
        }

        MolkkyRound round = null;
        try {
            round = game.rounds().get(roundidx);
        } catch (Exception e) {
            return new Rect(0,0,0,0);
        }

        ArrayList<MolkkyTeam> teams = round.teams();
        if (sortTeams) {
            teams = round.teamOrder();
        }

        Resources res = context.getResources();

        int nhits = round.numberOfHits();
        if (alignColumns) {
            for (MolkkyRound r: game.rounds()) {
                nhits = Math.max(nhits, r.numberOfHits());
            }
        }

        int columns = nhits / teams.size();
        if (nhits % teams.size() != 0) {
            ++columns;
        }

        if (columns < 5) {
            columns = 5;
        }

        int width = 4 + columns;
        int lines = teams.size();

        drawBoxedText(sheet, 0, 0, width, 1, context.getResources().getString(R.string.sheetRound, roundidx + 1), _text_size, Typeface.NORMAL, false);
        // table header
        drawBoxedLine(sheet, 0, 1, width, 1, true);
        drawBoxedLine(sheet, 0, 2, width, 2, true);
        drawBoxedLine(sheet, 0, 1.5f, 3, 1.5f, false);
        drawBoxedLine(sheet, 1, 1.5f, 1, 2, false);
        drawBoxedLine(sheet, 2, 1.5f, 2, 2, false);
        drawBoxedLine(sheet, 3, 1, 3, 2, true);

        drawBoxedText(sheet,0f, 1f, 3f, 1.5f, res.getString(R.string.sheetScore), _text_size / 2, Typeface.NORMAL, true);
        drawBoxedText(sheet,0f, 1.5f, 1f, 2f, res.getString(R.string.sheetPoints), _text_size / 2, Typeface.NORMAL, true);
        drawBoxedText(sheet,1f, 1.5f, 2f, 2f, "0", _text_size / 2, Typeface.NORMAL, true);
        drawBoxedTextPenalty(sheet,2f, 1.5f, 3f, 2f, Integer.toString(game.penaltyOverGoal()), _text_size / 2);

        drawBoxedLine(sheet, 4, 1, 4, 2,true);
        for (int i = 0; i < columns; i++) {
            if (i < columns - 1) {
                drawBoxedLine(sheet, 5 + i, 1, 5 + i, 2, (i == columns));
            }
            drawBoxedTextCN(sheet, 4 + i, 1, 5 + i, 2, Integer.toString(i + 1), _text_size);
        }

        float line = 2;
        for(MolkkyTeam team: teams) {
            drawBoxedText(sheet,0f, line, width, line + 0.7f, team.name(), _text_size, Typeface.BOLD, false);
            drawBoxedLine(sheet, 0f, line + 0.7f, width, line + 0.7f, false);

            drawBoxedText(sheet,0f, line + 0.7f, 1f, line + 1.5f, Integer.toString(round.teamScore(team)), _text_size, Typeface.BOLD, true);
            drawBoxedTextCN(sheet,1f, line + 0.7f, 2f, line + 1.5f, Integer.toString(round.numberOfZeros(team)), _text_size);
            drawBoxedTextCN(sheet,2f, line + 0.7f, 3f, line + 1.5f, Integer.toString(round.numberOfPenalties(team)), _text_size);

            for (int i = 1; i < width; ++i) {
                drawBoxedLine(sheet, i, line + 0.7f, i, line + 1.5f, (i == 3 || i == 4));
            }

            ArrayList<MolkkyHit> hits = round.teamHits(team);
            int sum = 0;
            int col = 0;
            for (MolkkyHit h: hits) {
                switch (h.hit()) {
                    case MolkkyHit.NOTPLAYED:
                        break;
                    case MolkkyHit.LINECROSS:
                        if (sum >= game.goal() - 12 - 1) {
                            sum = game.penaltyOverGoal();
                        }
                        drawBoxedTextCN(sheet, 4 + col, line + 0.7f, 5 + col, line + 1.5f, Integer.toString(sum), _text_size);
                        drawBoxedCircle(sheet, 4 + col, line + 0.7f, 5 + col, line + 1.5f, false);
                        break;
                    default:
                        sum += h.hit();
                        if (sum > game.goal()) {
                            sum = game.penaltyOverGoal();
                            drawBoxedTextPenalty(sheet,4 + col, line + 0.7f, 5 + col, line + 1.5f, Integer.toString(sum), _text_size);
                        } else {
                            drawBoxedTextCN(sheet, 4 + col, line + 0.7f, 5 + col, line + 1.5f, Integer.toString(sum), _text_size);
                            if (h.hit() == 0) {
                                drawBoxedCircle(sheet, 4 + col, line + 0.7f, 5 + col, line + 1.5f, false);
                            }
                        }

                        break;
                }
                ++col;
            }

            drawBoxedLine(sheet, 0f, line + 1.5f, width, line + 1.5f, true);
            line += 1.5f;
        }

        drawBoxedLine(sheet, 0, 1f, 0, line, true);
        drawBoxedLine(sheet, width, 1f, width, line, true);

        return new Rect(0,0, width * _box_width,(int)(line * _box_height));
    }

    public Rect currentRound(MolkkyGame game, boolean sortTeams, SheetDrawable sheet, Context context) {
        if (game == null || sheet == null || context == null) {
            return new Rect(0, 0, 0, 0);
        }

        return round(game, game.round(), sortTeams, false, sheet, context);
    }

    public Rect title(MolkkyGame game, SheetDrawable sheet, Context context)
    {
        Drawable d = context.getResources().getDrawable(R.drawable.ic_mollky_icon);
        if (! _dry_run) {
            sheet.drawDrawable(_offset_x, _offset_y, _offset_x + 100, _offset_y + 100, d);
        }
        if (game.date() != null) {
            game.dateAsString();
            String date = android.text.format.DateFormat.getLongDateFormat(context).format(game.date());
            String time = android.text.format.DateFormat.getTimeFormat(context).format(game.date());
            drawBoxedText(sheet, 1.5f, 0, 12, 1,
                    context.getResources().getString(R.string.sheetTitle, date, time),
                    _text_size, Typeface.NORMAL, false);
        }

        return new Rect(0, 0, 12 * _box_width, 1 * _box_height);
    }

    public Rect currentGame(MolkkyGame game, SheetDrawable sheet, Context context)
    {
        if (game == null || sheet == null || context == null) {
            return new Rect(0, 0, 0, 0);
        }

        ArrayList<MolkkyTeam> teams = game.gameTeamOrder();
        ArrayList<MolkkyRound> rounds = game.rounds();
        int width = 4 + 3*rounds.size();
        Resources res = context.getResources();

        drawBoxedText(sheet, 0, 0, width, 1, res.getString(R.string.sheetGame), _text_size, Typeface.NORMAL, false);
        // table header
        drawBoxedLine(sheet, 0, 1, width, 1, true);
        drawBoxedLine(sheet, 0, 1.5f, width, 1.5f, false);
        drawBoxedLine(sheet, 0, 2, width, 2, true);

        drawBoxedTextCN(sheet, 0, 1f, 4, 1.5f, res.getString(R.string.sheetTotal), _text_size / 2);
        drawBoxedTextCN(sheet,0, 1.5f, 2, 2f, res.getString(R.string.sheetPoints), _text_size / 2);
        drawBoxedTextCN(sheet,2, 1.5f, 3, 2f, "0", _text_size / 2);
        drawBoxedTextPenalty(sheet,3, 1.5f, 4, 2f, Integer.toString(game.penaltyOverGoal()), _text_size / 2);
        drawBoxedLine(sheet, 2, 1.5f, 2, 2f, false);
        drawBoxedLine(sheet, 3, 1.5f, 3, 2f, false);
        for (int i = 1; i <= rounds.size(); i++) {
            drawBoxedTextCN(sheet, i * 3 + 1, 1f, i * 3 + 4, 1.5f, res.getString(R.string.sheetRound, i), _text_size / 2);

            drawBoxedTextCN(sheet,i * 3 + 1, 1.5f, i*3 + 2, 2f, res.getString(R.string.sheetPoints), _text_size / 2);
            drawBoxedTextCN(sheet,i * 3 + 2, 1.5f, i * 3 + 3, 2f, "0", _text_size / 2);
            drawBoxedTextPenalty(sheet,i * 3 + 3, 1.5f, i * 3 + 4, 2f, Integer.toString(game.penaltyOverGoal()), _text_size / 2);

            drawBoxedLine(sheet, i * 3 + 1, 1.0f, i * 3 + 1, 2f, true);
            drawBoxedLine(sheet, i * 3 + 2, 1.5f, i * 3 + 2, 2f, false);
            drawBoxedLine(sheet, i * 3 + 3, 1.5f, i * 3 + 3, 2f, false);
        }

        float line = 2;
        for (MolkkyTeam team: teams) {
            drawBoxedText(sheet,0f, line, width, line + 0.7f, game.teamLongName(team), _text_size, Typeface.BOLD, false);
            drawBoxedLine(sheet, 0f, line + 0.7f, width, line + 0.7f, false);

            drawBoxedText(sheet,0f, line + 0.7f, 2f, line + 1.5f, Integer.toString(game.gameTeamScore(team)), _text_size, Typeface.BOLD, true);
            drawBoxedLine(sheet, 2, line + 0.7f, 2, line + 1.5f, false);
            drawBoxedTextCN(sheet,2f, line + 0.7f, 3f, line + 1.5f, Integer.toString(game.gameNumberOfZeros(team)), _text_size);
            drawBoxedLine(sheet, 3, line + 0.7f, 3, line + 1.5f, false);
            drawBoxedTextCN(sheet,3f, line + 0.7f, 4f, line + 1.5f, Integer.toString(game.gameNumberOfPenalties(team)), _text_size);

            int col = 4;
            for (MolkkyRound round: game.rounds()) {
                drawBoxedLine(sheet, col, line + 0.7f, col, line + 1.5f, true);
                drawBoxedTextCN(sheet, col, line + 0.7f, col + 1, line + 1.5f, Integer.toString(round.teamScore(team)), _text_size);
                drawBoxedLine(sheet, col + 1, line + 0.7f, col + 1, line + 1.5f, false);
                drawBoxedTextCN(sheet,col + 1, line + 0.7f, col + 2, line + 1.5f, Integer.toString(round.numberOfZeros(team)), _text_size);
                drawBoxedLine(sheet, col + 2, line + 0.7f, col + 2, line + 1.5f, false);
                drawBoxedTextCN(sheet,col + 2, line + 0.7f, col + 3, line + 1.5f, Integer.toString(round.numberOfPenalties(team)), _text_size);
                col += 3;
            }

            drawBoxedLine(sheet, 0f, line + 1.5f, width, line + 1.5f, true);
            line += 1.5f;
        }

        // finish the frame
        drawBoxedLine(sheet, 0, 1, 0, line, true);
        drawBoxedLine(sheet, width, 1, width, line, true);

        return new Rect(0, 0, width * _box_width, (int)(line * _box_height));
    }

    public Rect emptySheet(SheetDrawable sheet, Context context)
    {
        if (sheet == null || context == null) {
            return new Rect(0, 0, 0, 0);
        }


        /* frame */
        int top = 3;
        int line = top;
        int width = 7 + 20  + 2;

        drawBoxedLine(sheet, 0, 0, width, 0, true);
        drawBoxedLine(sheet, 0, 2.5f, width, 2.5f, true);
        drawBoxedLine(sheet, 0, 0, 0, 2.5f, true);
        drawBoxedLine(sheet, width, 0, width, 2.5f, true);
        drawBoxedText(sheet, 1, 0.2f, width, 1.0f, context.getResources().getString(R.string.sheetRecord), _text_size * 12 / 10, Typeface.BOLD, false);
        drawBoxedText(sheet, 1, 1.3f, 8, 2.3f, context.getResources().getString(R.string.sheetDate), _text_size, Typeface.BOLD, false);
        drawBoxedText(sheet, 10, 1.3f, width, 2.3f, context.getResources().getString(R.string.sheetPlace), _text_size, Typeface.BOLD, false);
        Drawable d = context.getResources().getDrawable(R.drawable.ic_mollky_icon);
        if (d != null) {
            float y = (2.5f * _box_height - 2 * _box_width)/2.0f;
            drawDrawable(sheet,
                    (width - 2.5f) * _box_width,
                    y,
                    (width - 0.5f) * _box_width,
                    y + 2 * _box_width,
                    d);
        }

        /* header texts */
        drawBoxedText(sheet, 0, line, 7, line + 1, context.getResources().getString(R.string.sheetTeams), _text_size, Typeface.BOLD, true);
        drawBoxedTextCN(sheet, 7, line, width - 2, line + 0.5f, context.getResources().getString(R.string.sheetHits), _text_size * 2 / 3);
        drawBoxedTextCN(sheet, width - 2, line, width, line + 0.5f, context.getResources().getString(R.string.sheetCount), _text_size * 2 / 3);

        /* horizontal lines */
        drawBoxedLine(sheet, 0, line,  width, line, true);
        drawBoxedLine(sheet, 7, line + 0.5f,  width, line + 0.5f, false);
        line++;
        drawBoxedLine(sheet, 0, line,  width, line, true);
        line++;

        for(int i = 0; i < 11; i++) {
            drawBoxedLine(sheet, 1, line - 0.5f,  7, line - 0.5f, false);
            drawBoxedLine(sheet, 0, line,  7, line, true);
            drawBoxedLine(sheet, 7, line,  width, line, false);
            line++;
        }
        drawBoxedLine(sheet, 1, line - 0.5f,  7, line - 0.5f, false);
        drawBoxedLine(sheet, 0, line,  width, line, true);

        /* vertical lines */
        drawBoxedLine(sheet, 0, top,  0, line, true);
        drawBoxedLine(sheet, 1, top + 1,  1, line, true);
        drawBoxedLine(sheet, 7, top,  7, line, true);
        drawBoxedLine(sheet, width - 2, top,  width - 2, line, true);
        drawBoxedLine(sheet, width - 1, top + 0.5f,  width - 1, line, false);
        drawBoxedLine(sheet, width, top,  width, line, true);
        for (int i = 0; i < 20; i++) {
            drawBoxedLine(sheet, 8 + i, top + 0.5f,  8 + i, line, false);
        }

        /* vertical numbers */
        for(int i = 0; i < 12; i++) {
            drawBoxedText(sheet, 0, i + 1 + top, 1, i + 2 + top, Integer.toString(i + 1), _text_size, Typeface.BOLD, true);
        }

        /* horizontal numbers */
        for(int i = 0; i < 20; i++) {
            drawBoxedText(sheet, 7 + i,  top + 0.5f, 8 + i, top + 1, Integer.toString(i + 1), _text_size * 2 / 3, Typeface.BOLD, true);
        }
        drawBoxedText(sheet, width - 2,  top + 0.5f, width - 1, top + 1, Integer.toString(0), _text_size * 2 / 3, Typeface.BOLD, true);
        drawBoxedText(sheet, width - 1,  top + 0.5f, width, top + 1, Integer.toString(25), _text_size * 2 / 3, Typeface.BOLD, true);
        drawBoxedLine(sheet, width - 0.8f,  top + 0.9f, width - 0.2f, top + 0.6f, false);

        return new Rect(0, 0, width * _box_width, (int)(line * _box_height));
    }

    public void setOffset(int x, int y)
    {
        _offset_x = x;
        _offset_y = y;
    }

    public interface SheetDrawable {
        void drawLine(int x1, int y1, int x2, int y2, boolean thick);
        void drawText(int x1, int y1, int x2, int y2, String text, int textSize, int typeface, boolean centered);
        void drawCircle(int x, int y, int r, boolean thick);
        void drawDrawable(int x1, int y1, int x2, int y2, Drawable drawable);
    }
}

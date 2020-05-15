package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.halman.molkkynotes.R;

public class UIPlayer extends LinearLayout {
    private TextView _team = null;
    private TextView _score = null;

    public UIPlayer(Context context) {
        super(context);
        init(context, null, 0);
    }

    public UIPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public UIPlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.ui_player, this);

        _team = (TextView) this.findViewById(R.id.team);
        _score = (TextView) this.findViewById(R.id.score);
    }

    public void name(String team)
    {
        _team.setText(team);
    }

    public String name()
    {
        return _team.getText().toString();
    }

    public void score(String score)
    {
        _score.setText(score);
        if (score.isEmpty()) {
            _score.setVisibility(GONE);
        } else {
            _score.setVisibility(VISIBLE);
        }
    }
}

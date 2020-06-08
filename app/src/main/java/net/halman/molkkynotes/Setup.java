package net.halman.molkkynotes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Setup {
    final static int ELIMINATION = 0;
    final static int ZEROPOINTS = 1;

    private boolean _playInTeams = false;
    private int _goal = 50;
    private int _penaltyWhenOverGoal = 25;
    private int _penaltyFor3Zeros = ELIMINATION;
    private boolean _keep_screen_on = false;
    private boolean _auto_forward = false;

    public int penaltyOverGoal()
    {
        return _penaltyWhenOverGoal;
    }

    public void penaltyOverGoal(int penalty)
    {
        _penaltyWhenOverGoal = penalty;
    }

    public int penaltyFor3Zeros()
    {
        return _penaltyFor3Zeros;
    }

    public void penaltyFor3Zeros(int penalty)
    {
        if (penalty >= ELIMINATION && penalty <= ZEROPOINTS) {
            _penaltyFor3Zeros = penalty;
        } else {
            _penaltyFor3Zeros = ELIMINATION;
        }
    }

    public boolean playInTeams()
    {
        return _playInTeams;
    }

    public void playInTeams(boolean playInTeams)
    {
        _playInTeams = playInTeams;
    }

    public boolean autoForward()
    {
        return _auto_forward;
    }

    public void autoForward(boolean autoforward)
    {
        _auto_forward = autoforward;
    }

    public int goal()
    {
        return _goal;
    }

    public void goal(int goal)
    {
        _goal = goal;
    }

    public boolean keepScreenOn()
    {
        return _keep_screen_on;
    }

    public void keepScreenOn(boolean keep_screen_on)
    {
        _keep_screen_on = keep_screen_on;
    }

    public void save(Activity activity)
    {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("play-in-teams", _playInTeams);
        editor.putInt("goal", _goal);
        editor.putInt("penalty-when-over", _penaltyWhenOverGoal);
        editor.putInt("penalty-for-3-zeros", _penaltyFor3Zeros);
        editor.putBoolean("keep-screen-on", _keep_screen_on);
        editor.putBoolean("auto-forward", _auto_forward);
        editor.apply();
    }

    public void load(Activity activity)
    {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        _playInTeams =  sharedPref.getBoolean("play-in-teams", false);
        _goal = sharedPref.getInt("goal", 50);
        _penaltyWhenOverGoal = sharedPref.getInt("penalty-when-over", 25);
        penaltyFor3Zeros(sharedPref.getInt("penalty-for-3-zeros", ELIMINATION));
        _keep_screen_on =  sharedPref.getBoolean("keep-screen-on", false);
        _auto_forward =  sharedPref.getBoolean("auto-forward", false);
    }
}

package net.halman.molkkynotes;

import android.content.Context;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class MolkkyGame implements Serializable {
    private ArrayList<MolkkyTeam> _teams = new ArrayList<MolkkyTeam>();
    private ArrayList<MolkkyRound> _rounds = new ArrayList<>();
    private int _current_round = -1;
    private int _starting_team = 0;
    private int _goal = 50;
    private int _penalty_goal_over = 25;

    private class MolkkyGameScoreComparator implements Comparator<MolkkyTeam> {
        public int compare(MolkkyTeam t1, MolkkyTeam t2) {
            // usually toString should not be used,
            // instead one of the attributes or more in a comparator chain
            int score1 = gameTeamScore(t1);
            int score2 = gameTeamScore(t2);
            if (score1 > score2) {
                return -1;
            } else if (score1 < score2) {
                return +1;
            }

            int z1 = gameNumberOfZeros(t1);
            int z2 = gameNumberOfZeros(t2);
            if (z1 > z2) {
                return +1;
            } else if (z1 < z2) {
                return -1;
            }
            return 0;
        }
    }

    void addTeam(MolkkyTeam team)
    {
        if (team == null || _teams.size() >= 10 || gameStarted()) {
            return;
        }

        for (MolkkyPlayer p: team.members()) {
            if (hasPlayer(p)) {
                return;
            }
        }
        _teams.add(team);
    }

    public void addPlayer(MolkkyPlayer p)
    {
        if (p == null) {
            return;
        }

        MolkkyTeam t = new MolkkyTeam();
        t.addMember(p);
        addTeam(t);
    }

    void clearTeams() {
        _teams.clear();
        _starting_team = 0;
    }

    void clearRounds() {
        _rounds.clear();
        _current_round = 0;
        addRound();
    }

    public void start()
    {
        _rounds.clear();
        addRound();
        _current_round = 0;
    }

    public int currentRoundIndex()
    {
        return _current_round;
    }

    private MolkkyRound currentRound()
    {
        if (_current_round < 0) {
            return null;
        }

        return _rounds.get(_current_round);
    }

    public void addRound()
    {
        int idx = _rounds.size();
        MolkkyRound r = new MolkkyRound(_goal, _penalty_goal_over);

        for (int t = 0; t < _teams.size(); t++) {
            r.addTeam(_teams.get((idx + t) % _teams.size()));
        }
        _rounds.add(r);
    }

    public void startNextRound()
    {
        if (currentRound() != null && !currentRound().over()) {
            return;
        }

        addRound();
        _current_round = _rounds.size() - 1;
    }

    int numberOfRounds()
    {
        return _rounds.size();
    }

    public boolean gameStarted()
    {
        if (_rounds.size() == 0) {
            return false;
        }

        if (_current_round > 0) {
            return true;
        }

        return currentRound().started();
    }

    public boolean roundStarted() {
        return (_rounds.size() > 0) && (currentRound().started());
    }

    public int roundCursor()
    {
        if (_rounds.size() == 0) {
            return -1;
        }

        return currentRound().cursor();
    }

    public MolkkyTeam currentTeam()
    {
        if (_teams.size() == 0) {
            return null;
        }

        if (_rounds.size() == 0) {
            return null;
        }

        return currentRound().currentTeam();
    }

    public MolkkyTeam nextTeam()
    {
        if (_teams.size() == 0) {
            return null;
        }

        if (_rounds.size() == 0) {
            return null;
        }

        return currentRound().nextTeam();
    }

    public void hit(MolkkyHit hit)
    {
        if (hit == null) {
            return;
        }

        MolkkyRound r = currentRound();
        if (r != null) {
            r.currentHit(hit);
        }
    }

    public void hit(int value)
    {
        MolkkyRound r = currentRound();
        if (r != null) {
            r.currentHit(value);
        }
    }

    public MolkkyHit hit()
    {
        MolkkyRound r = currentRound();
        if (r != null) {
            MolkkyHit result = r.currentHit();
            if (result != null) {
                return result;
            }
        }

        return new MolkkyHit();
    }

    public void nextHit()
    {
        MolkkyRound r = currentRound();
        if (r != null) {
            r.nextHit();
        }
    }

    public void prevHit()
    {
        MolkkyRound r = currentRound();
        if (r != null) {
            r.prevHit();
        }
    }

    public boolean roundCursorAtTheStart() {
        MolkkyRound r = currentRound();
        if (r != null) {
            return r.cursorAtTheStart();
        }

        return true;
    }

    public boolean roundCursorAtTheEnd() {
        MolkkyRound r = currentRound();
        if (r != null) {
            return r.cursorAtTheEnd();
        }

        return true;
    }

    public boolean roundOver()
    {
        MolkkyRound r = currentRound();
        if (r != null) {
            return r.over();
        }

        return true;
    }

    public ArrayList<MolkkyTeam> roundTeamOrder()
    {
        MolkkyRound r = currentRound();
        if (r != null) {
            return r.teamOrder();
        }

        return null;
    }

    public ArrayList<MolkkyTeam> gameTeamOrder()
    {
        ArrayList<MolkkyTeam> result = new ArrayList<MolkkyTeam>();
        for (MolkkyTeam t: _teams) {
            result.add(t);
        }

        Collections.sort(result, new MolkkyGameScoreComparator());
        return result;
    }

    public String roundTeamScoreAsString(MolkkyTeam t)
    {
        MolkkyRound r = currentRound();
        if (r != null) {
            return r.teamScoreAsString(t);
        }

        return "";
    }

    public int roundTeamScore(MolkkyTeam t) {
        MolkkyRound r = currentRound();
        if (r != null) {
            return r.teamScore(t);
        }

        return 0;
    }

    public int gameTeamScore(MolkkyTeam t) {
        int score = 0;
        for (MolkkyRound r: _rounds) {
            score += r.teamScore(t);
        }

        return score;
    }

    public int gameNumberOfZeros(MolkkyTeam t)
    {
        int zeros = 0;
        for (MolkkyRound r: _rounds) {
            zeros += r.numberOfZeros(t);
        }

        return zeros;
    }

    public int roundNumberOfZeros(MolkkyTeam t)
    {
        MolkkyRound r = currentRound();
        if (r == null) {
            return 0;
        }

        return r.numberOfZeros(t);
    }

    public String gameTeamScoreAsString(MolkkyTeam t)
    {
        String prefix = "";
        String score = "";
        for (MolkkyRound r: _rounds) {
            score += prefix + r.teamScore(t);
            prefix = "/";
        }

        return score;
    }

    public void setup(Setup s)
    {
        if (s == null) {
            return;
        }

        _goal = s.goal();
        _penalty_goal_over = s.penaltyOverGoal();
        MolkkyRound r = currentRound();
        if (r != null) {
            r.setup(_goal, _penalty_goal_over);
        }
    }

    public int goal()
    {
        return _goal;
    }

    public int penaltyOverGoal()
    {
        return _penalty_goal_over;
    }

    public int round()
    {
        return _current_round;
    }

    public int roundProgress()
    {
        MolkkyRound r = currentRound();
        if (r != null) {
            return r.roundProgress();
        }

        return 0;
    }

    public ArrayList<MolkkyTeam> teams()
    {
        return _teams;
    }

    public boolean hasPlayer(MolkkyPlayer player)
    {
        for (MolkkyTeam team: _teams) {
            if (team.hasPlayer(player)) {
                return true;
            }
        }

        return false;
    }

    public void shuffleTeams()
    {
        if (_teams.size() <= 1 || gameStarted()) {
            return;
        }

        ArrayList<MolkkyTeam> shuffle = new ArrayList<MolkkyTeam>();
        Random rand = new Random();

        while (_teams.size() > 0) {
            int idx = rand.nextInt(_teams.size());
            MolkkyTeam t = _teams.get(idx);
            shuffle.add(t);
            _teams.remove(idx);
        }

        _teams = shuffle;
    }

    public MolkkyTeam teamByTeamsName(String name)
    {
        for (MolkkyTeam t: _teams) {
            if (t.name().equals(name)) {
                return t;
            }
        }

        return null;
    }

    public MolkkyTeam teamByPlayersName(String name)
    {
        for (MolkkyTeam t: _teams) {
            if (t.hasPlayer(name)) {
                return t;
            }
        }

        return null;
    }

    public void removePlayerByName(String name)
    {
        MolkkyTeam t = teamByPlayersName(name);
        if (t == null) {
            return;
        }

        t.removePlayer(name);
        if (t.size() == 0) {
            _teams.remove(t);
        }
    }

    public ArrayList<MolkkyRound> rounds()
    {
        return _rounds;
    }

    public int teamHealth(MolkkyTeam team)
    {
        return currentRound().teamHealth(team);
    }

    public void save(Context context, String file_name)
    {
        try {
            String[] title = {"Player/Team", "Points", "Zeros", "", "Hits"};
            String[] empty = {};
            FileOutputStream fos = context.openFileOutput(file_name, context.MODE_PRIVATE);
            OutputStreamWriter fow = new OutputStreamWriter(fos);
            CSVWriter writer = new CSVWriter(fow);

            writer.writeNext(title);

            ArrayList<String> columns = new ArrayList<>();
            for (MolkkyRound round : _rounds) {
                for (MolkkyTeam team: round.teams()) {
                    columns.clear();
                    columns.add(team.name());
                    columns.add(Integer.toString(round.teamScore(team)));
                    columns.add(Integer.toString(round.numberOfZeros(team)));
                    columns.add("");
                    for(MolkkyHit hit: round.teamHits(team)) {
                        columns.add(Integer.toString(hit.hit()));
                    }
                    String[] columns_array = new String[columns.size()];
                    columns.toArray(columns_array);
                    writer.writeNext(columns_array);
                }
                writer.writeNext(empty);
            }
        } catch (Exception e) {
            Log.d("ex", e.toString());
        };
    }
}

package net.halman.molkkynotes;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class MolkkyRound implements Serializable {
    public static final int GOOD = 0;
    public static final int ZERO = 1;
    public static final int TWOZEROS = 2;
    public static final int OUT = 3;

    private int _startingTeam = 0;
    private int _current = -1;
    private int _goal = 50;
    private int _penalty_over_goal = 25;
    private ArrayList<MolkkyTeam> _teams = new ArrayList<MolkkyTeam>();
    private ArrayList<MolkkyHit> _hits = new ArrayList<MolkkyHit>();


    public MolkkyRound(int goal, int penalty_over_goal)
    {
        setup(goal, penalty_over_goal);
    }

    public void setup(int goal, int penalty_over_goal)
    {
        _goal = goal;
        _penalty_over_goal = penalty_over_goal;
    }

    private class MolkkyScoreComparator implements Comparator<MolkkyTeam> {
        public int compare(MolkkyTeam t1, MolkkyTeam t2) {
            int score1 = teamScore(t1);
            int score2 = teamScore(t2);
            if (score1 > score2) {
                return -1;
            } else if (score1 < score2) {
                return +1;
            }

            int z1 = numberOfZeros(t1);
            int z2 = numberOfZeros(t2);
            if (z1 > z2) {
                return +1;
            } else if (z1 < z2) {
                return -1;
            }
            return 0;
        }
    }

    public ArrayList<MolkkyTeam> teams()
    {
        return _teams;
    }

    void addTeam(MolkkyTeam team) {
        if (!_teams.contains(team)) {
            _teams.add(team);
        }
    }

    void startingTeam(int index) {
        if (index >= 0 && index < _teams.size()) {
            _startingTeam = index;
        }
    }

    void startingTeam(MolkkyTeam t) {
        _startingTeam = _teams.indexOf(t);
        if (_startingTeam < 0) _startingTeam = 0;
    }

    int startingTeam() {
        return _startingTeam;
    }

    MolkkyTeam currentTeam() {
        if (_teams.size() == 0 || _current < 0) return null;
        return _teams.get((_current + _startingTeam) % _teams.size());
    }

    MolkkyTeam nextTeam() {
        if (_teams.size() == 0) return null;
        return _teams.get((_current + _startingTeam + 1) % _teams.size());
    }

    MolkkyHit currentHit() {
        if (_current == -1) {
            return null;
        }

        while (_hits.size() <= _current) {
            _hits.add(new MolkkyHit());
        }

        return _hits.get(_current);
    }

    void currentHit(int score) {
        if (currentHit() == null) {
            return;
        }

        currentHit().hit(score);
    }

    void currentHit(MolkkyHit hit) {
        if (hit == null || currentHit() == null) {
            return;
        }

        currentHit().hit(hit);
    }

    MolkkyHit nextHit() {
        if (_current < _hits.size() - 1) {
            _current++;
        } else {
            if (!over()) _current++;
        }

        MolkkyHit hit = currentHit();
        if ((hit.hit() == MolkkyHit.NOTPLAYED) && (teamHealth(currentTeam()) == OUT)) {
            return nextHit();
        }

        return hit;
    }

    MolkkyHit prevHit() {
        if (_current < 0) {
            return null;
        }

        if (_current == 0) {
            _current--;
            return null;
        }

        if (_current > 0) _current--;

        MolkkyHit hit = currentHit();
        if ((hit.hit() == MolkkyHit.NOTPLAYED) && (teamHealth(currentTeam()) == OUT)) {
            return prevHit();
        }

        return hit;
    }

    private ArrayList<MolkkyHit> teamHits(int teamidx) {
        ArrayList<MolkkyHit> hits = new ArrayList<MolkkyHit>();
        int first = teamidx - _startingTeam;
        if (first < 0) first += _teams.size();
        while (first < _hits.size()) {
            hits.add(_hits.get(first));
            first += _teams.size();
        }
        return hits;
    }

    ArrayList<MolkkyHit> teamHits(MolkkyTeam team) {
        int idx = _teams.indexOf(team);
        if (idx >= 0) return teamHits(idx);
        return new ArrayList<MolkkyHit>();
    }

    public int teamScore(MolkkyTeam team) {
        // check whether we are still in game
        if (teamHealth(team) == OUT) return 0;

        // check whether all other teams are out
        int otherTeamsOut = 0;
        for (MolkkyTeam t : _teams) {
            if (t != team) {
                if (teamHealth(t) == OUT) otherTeamsOut++;
            }
        }
        if (otherTeamsOut == _teams.size() - 1) return _goal;

        // more players in the round
        ArrayList<MolkkyHit> hits = teamHits(team);
        int score = 0;
        for (MolkkyHit hit : hits) {
            if (hit.hit() > 0) {
                score += hit.hit();
                if (score > _goal) score = _penalty_over_goal;
                if (score == _goal) return score;
            }
            if (hit.hit() == MolkkyHit.LINECROSS && score >= (_goal - 13)) {
                score = _penalty_over_goal;
            }
        }
        return score;
    }

    public int numberOfZeros(MolkkyTeam team) {
        int zeros = 0;
        ArrayList<MolkkyHit> hits = teamHits(team);

        for (MolkkyHit hit : hits) {
            if (hit.hit() == 0) {
                zeros++;
            }
        }

        return zeros;
    }

    public int teamHealth(MolkkyTeam team) {
        ArrayList<MolkkyHit> hits = teamHits(team);
        int zeros = 0;
        int health = GOOD;
        for (int i = 0; i < hits.size(); i++) {
            switch (hits.get(i).hit()) {
                case MolkkyHit.NOTPLAYED:
                    break;
                case 0:
                case MolkkyHit.LINECROSS:
                    zeros++;
                    break;
                default:
                    zeros = 0;
                    break;
            }
            if (zeros == 3) return OUT;
        }
        if (zeros == 1) return ZERO;
        if (zeros == 2) return TWOZEROS;
        return GOOD;
    }

    public boolean over() {
        // someone has 50?
        for (MolkkyTeam t : _teams) {
            if (teamScore(t) == _goal) return true;
        }

        // last team in the game?
        int teams_in = 0;
        for (MolkkyTeam t : _teams) {
            if (teamHealth(t) != OUT) {
                teams_in++;
            }
        }
        if (teams_in == 1) {
            return true;
        }

        return false;
    }

    public boolean started() {
        if (_hits.size() == 0) {
            return false;
        }

        if (_hits.size() == 1 && _hits.get(0).hit() == MolkkyHit.NOTPLAYED) {
            return false;
        }

        return true;
    }

    public boolean cursorAtTheStart()
    {
        return _current == 0;
    }

    public boolean cursorAtTheEnd()
    {
        return _current == _hits.size() - 1;
    }

    public int cursor()
    {
        return _current;
    }

    public ArrayList<MolkkyTeam> teamOrder()
    {
        ArrayList<MolkkyTeam> result = new ArrayList<MolkkyTeam>();
        for (MolkkyTeam t: _teams) {
            result.add(t);
        }
        Collections.sort(result, new MolkkyScoreComparator());
        return result;
    }

    public String teamScoreAsString(MolkkyTeam t)
    {
        StringBuilder s = new StringBuilder();
        ArrayList<MolkkyHit> hits = teamHits(t);
        String prefix = "";
        for (MolkkyHit h: hits) {
            switch (h.hit()) {
                case MolkkyHit.LINECROSS:
                    s.append(prefix);
                    s.append("0");
                    prefix = "/";
                    break;
                case MolkkyHit.NOTPLAYED:
                    break;
                default:
                    s.append(prefix);
                    s.append(h.hit());
                    prefix = "/";
                    break;
            }
        }

        return s.toString();
    }

    public int roundProgress()
    {
        if (_teams.size() == 0 || _current < 0) {
            return 0;
        }

        return _current / _teams.size();
    }
}

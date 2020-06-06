package net.halman.molkkynotes;

import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class MolkkyGame implements Serializable {
    private ArrayList<MolkkyTeam> _teams = new ArrayList<MolkkyTeam>();
    private ArrayList<MolkkyRound> _rounds = new ArrayList<>();
    private int _current_round = -1;
    private int _goal = 50;
    private int _penalty_goal_over = 25;
    private Date _date = null;

    private class MolkkyGameScoreComparator implements Comparator<MolkkyTeam> {
        public int compare(MolkkyTeam t1, MolkkyTeam t2) {
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

    public MolkkyRound currentRound()
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

        if (_date == null) {
            _date = new Date();
        }

        MolkkyRound r = currentRound();
        if (r != null) {
            r.currentHit(hit);
        }
    }

    public void hit(int value)
    {
        hit(new MolkkyHit(value));
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

    public int gameNumberOfPenalties(MolkkyTeam t)
    {
        int penalties = 0;
        for (MolkkyRound r: _rounds) {
            penalties += r.numberOfPenalties(t);
        }

        return penalties;
    }

    public int roundNumberOfPenalties(MolkkyTeam t)
    {
        MolkkyRound r = currentRound();
        if (r == null) {
            return 0;
        }

        return r.numberOfPenalties(t);
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

    private int teamIndex(MolkkyTeam team)
    {
        for (int i = 0; i < _teams.size(); ++i) {
            if (_teams.get(i) == team || _teams.get(i).name().equals(team.name())) {
                return i;
            }
        }

        return -1;
    }
    /*
        Player/Team, Points, Zeros, , Hits
        team1, 50, 2, , 3,0,0,7,6,8,9
        team2, 40, 0, , 3,1,1,7,6,8,9

        team2, 50, 2, , 3,0,0,7,6,8,9
        team1, 40, 0, , 3,1,1,7,6,8,9
        [EOF]
     */
    public void CSVExport(String full_file_name)
    {
        try {
            String[] empty = {};

            FileOutputStream fos = new FileOutputStream(full_file_name);
            OutputStreamWriter fow = new OutputStreamWriter(fos);
            CSVWriter writer = new CSVWriter(fow);

            // Write header
            // game name
            {
                ArrayList<MolkkyTeam> teams = gameTeamOrder();
                String value = dateAsString();
                for (MolkkyTeam t: teams) {
                    value = value + ", " + t.name();
                }

                writer.writeNext(new String[]{"title:", value});
            }

            // goal/penalty
            writer.writeNext(new String[]{"goal:", Integer.toString(_goal)});
            writer.writeNext(new String[]{"penalty:", Integer.toString(_penalty_goal_over)});

            // teams
            {
                for (MolkkyTeam t: _teams) {
                    ArrayList<MolkkyPlayer> members = t.members();
                    String [] line = new String[members.size() + 1];
                    line[0] = "team" + (teamIndex(t) + 1) + ":";
                    int c = 1;
                    for(MolkkyPlayer p: members) {
                        line[c] = p.name();
                        ++c;
                    }
                    writer.writeNext(line);
                }
            }

            // end of header
            writer.writeNext(empty);

            // Write result
            writer.writeNext(new String[] {"Team", "Players", "Points", "Zeros", "Penalties", "", "Hits"});

            ArrayList<String> columns = new ArrayList<>();
            for (MolkkyRound round : _rounds) {
                for (MolkkyTeam team: round.teams()) {
                    columns.clear();
                    columns.add("team" + (teamIndex(team) + 1));
                    columns.add(team.name());
                    columns.add(Integer.toString(round.teamScore(team)));
                    columns.add(Integer.toString(round.numberOfZeros(team)));
                    columns.add(Integer.toString(round.numberOfPenalties(team)));
                    columns.add("");
                    for(MolkkyHit hit: round.teamHits(team)) {
                        if (hit.hit() == MolkkyHit.LINECROSS) {
                            columns.add("X");
                        } else {
                            columns.add(Integer.toString(hit.hit()));
                        }
                    }
                    String[] columns_array = new String[columns.size()];
                    columns.toArray(columns_array);
                    writer.writeNext(columns_array);
                }
                writer.writeNext(empty);
            }

            writer.close();
        } catch (Exception e) {
            Log.d("ex", e.toString());
        };
    }

    public void CSVImport(String full_file_name)
    {
        _teams.clear();
        _rounds.clear();
        try {
            FileInputStream fis = new FileInputStream(full_file_name);
            InputStreamReader isr = new InputStreamReader(fis);
            CSVReader reader = new CSVReader(isr);
            String[] line;
            ArrayList<String[]> lines = new ArrayList<>();
            HashMap<String, MolkkyTeam> team_map = new HashMap<>();

            // header
            line = reader.readNext();
            while (line != null && line.length > 0 && !line[0].isEmpty()) {
                if (line[0].equalsIgnoreCase("goal:")) {
                    _goal = Integer.parseInt(line[1]);
                }

                if (line[0].equalsIgnoreCase("penalty:")) {
                    _penalty_goal_over = Integer.parseInt(line[1]);
                }

                if (line[0].toLowerCase().startsWith("team")) {
                    MolkkyTeam t = new MolkkyTeam();
                    for (int i = 1; i < line.length; i++) {
                        if (!line[i].isEmpty()) {
                            t.addMember(new MolkkyPlayer(line[i]));
                        }
                    }
                    if (t.size() > 0) {
                        team_map.put(line[0], t);
                        _teams.add(t);
                    }
                }

                line = reader.readNext();
            }

            while(line != null && (line.length == 0 || (line.length > 0 && line[0].isEmpty()))) {
                line = reader.readNext();
            }

            // on title
            if (line != null) {
                line = reader.readNext();
            }

            // read rounds
            while (line != null) {
                String [] nextLine = reader.readNext();
                if (line.length > 0 && !line[0].isEmpty()) {
                    // line is not empty, just add it for future evaluation
                    lines.add(line);
                }

                if (line.length == 0 || line[0].isEmpty() || nextLine == null) {
                    if (lines.size() > 0) {
                        // we have some lines to be evaluated
                        // find teams and add them to the game
                        MolkkyRound round = new MolkkyRound(_goal, _penalty_goal_over);
                        for(String [] l: lines) {
                            MolkkyTeam t = team_map.get(l[0] + ":");
                            if (t == null) {
                                throw new IOException();
                            }

                            round.addTeam(t);
                        }

                        // fill hits
                        round.nextHit();

                        // find empty column
                        String [] tmp = lines.get(0);
                        int idx = 2;
                        for (int i = 2; i < tmp.length; i++) {
                            if (tmp[i].isEmpty()) {
                                idx = i + 1;
                                break;
                            }
                        }

                        boolean cont = true;
                        while (cont) {
                            cont = false;
                            for (String l[] : lines) {
                                if (l.length > idx) {
                                    cont = true;
                                    if (l[idx].equalsIgnoreCase("X")) {
                                        round.currentHit(MolkkyHit.LINECROSS);
                                    } else {
                                        round.currentHit(Integer.parseInt(l[idx]));
                                    }
                                    round.nextHit();
                                } else {
                                    if (!round.over()) {
                                        round.currentHit(MolkkyHit.NOTPLAYED);
                                        round.nextHit();
                                    }
                                }
                            }
                            ++idx;
                        }

                        _rounds.add(round);
                        lines.clear();
                    }
                }

                line = nextLine;
            }
        } catch (IOException e) {
            _teams.clear();
            _rounds.clear();
            _current_round = -1;
        }
    }

    public Date date()
    {
        return _date;
    }

    public String dateAsString()
    {
        if (_date == null) {
            _date = new Date();
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd--HH-mm");
        return format.format(_date);
    }

    private static String CSVHeaderItem(String full_file_name, String item)
    {
        try {
            FileInputStream fis = new FileInputStream(full_file_name);
            InputStreamReader isr = new InputStreamReader(fis);
            CSVReader reader = new CSVReader(isr);

            String[] line = reader.readNext();
            while (line != null && line.length > 0 && !line[0].isEmpty()) {
                if (line[0].equalsIgnoreCase(item)) {
                    return line[1];
                }

                line = reader.readNext();
            }
        } catch (Exception e) { }

        return "";
    }

    public static String CVSTitle(String full_file_name)
    {
        return CSVHeaderItem(full_file_name, "title:");
    }

    public static Date CVSDate(String full_file_name)
    {
        String sdate = CSVHeaderItem(full_file_name, "date:");
        if (sdate.isEmpty()) {
            File F = new File(full_file_name);
            String name = F.getName();
            sdate = name.substring(0, 17);
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd--HH-mm");
        try {
            return format.parse(sdate);
        } catch (Exception e) {
            return null;
        }
    }
}

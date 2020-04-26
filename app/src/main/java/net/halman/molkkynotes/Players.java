package net.halman.molkkynotes;

import android.util.Log;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Players {
    private static final String PLAYERSDB = "players.csv";
    private ArrayList<MolkkyPlayer> _players = new ArrayList<>();

    public Players ()
    {
    }

    private class SortPlayers implements Comparator<MolkkyPlayer> {
        public int compare(MolkkyPlayer a, MolkkyPlayer b)
        {
            return a.name().compareTo(b.name());
        }
    }

    public int size()
    {
        return _players.size();
    }

    public void sort()
    {
        Collections.sort(_players, new SortPlayers());
    }

    public boolean add(MolkkyPlayer player)
    {
        for (MolkkyPlayer p: _players) {
            if (player.name().equals(p.name())) {
                return false;
            }
        }

        _players.add(player);
        save();
        return true;
    }

    public MolkkyPlayer get(int idx)
    {
        return _players.get(idx);
    }

    public MolkkyPlayer get(String name)
    {
        for (MolkkyPlayer p: _players) {
            if (name.equals(p.name())) {
                return p;
            }
        }
        return null;
    }

    public void save()
    {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(PLAYERSDB));
            for (MolkkyPlayer p: _players) {
                ArrayList<String> where = new ArrayList<String>();
                where.add(p.name());
                for (String score: p.history().split(",")){
                    where.add(score);
                }

                String[] list = new String[where.size()];
                where.toArray(list);
                writer.writeNext(list);
            }
            writer.close();
        } catch (Exception e) {};
    }

    public void load()
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(PLAYERSDB));
            String[] line;
            int id = 1;
            while ((line = reader.readNext()) != null) {
                if (line.length > 0 && ! line[0].isEmpty()) {
                    Log.d("Players", "new player " +  line[0]);
                    MolkkyPlayer p = new MolkkyPlayer(line[0]);
                    for (int i = 1; i < line.length; i++) {
                        try {
                            int points = Integer.parseInt(line[i]);
                            p.addRoundScore(points);
                        } catch (Exception e) {};
                    }
                    p.id(id++);
                    add(p);
                }
            }
        } catch (IOException e) {
            add(new MolkkyPlayer("Ringo"));
            add(new MolkkyPlayer("George"));
            add(new MolkkyPlayer("Paul"));
            add(new MolkkyPlayer("John"));
        }

        sort();
    }
}

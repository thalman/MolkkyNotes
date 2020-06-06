package net.halman.molkkynotes;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
;


public class Players {
    public static final int PLAYERS_UPDATED = 2;

    private static final String PLAYERSDB = "players.csv";
    private ArrayList<MolkkyPlayer> _players = new ArrayList<>();
    private Handler _handler;
    private Context _context;
    private LoadingTask _loading_task;
    private Thread _loading_thread;

    public Players ()
    {
    }

    private class SortPlayers implements Comparator<MolkkyPlayer> {
        public int compare(MolkkyPlayer a, MolkkyPlayer b)
        {
            return a.name().compareTo(b.name());
        }
    }

    private class LoadingTask implements Runnable {
        public void run() {
            _players.clear();
            try {
                FileInputStream fis = _context.openFileInput(PLAYERSDB);
                InputStreamReader isr = new InputStreamReader(fis);
                CSVReader reader = new CSVReader(isr);
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

            notifyUpdate();
        }
    }

    public void notifyUpdate()
    {
        if (_handler != null) {
            _handler.sendMessage(_handler.obtainMessage(PLAYERS_UPDATED));
        }
    }

    public synchronized int size()
    {
        return _players.size();
    }

    public void sort()
    {
        Collections.sort(_players, new SortPlayers());
    }

    public synchronized boolean add(MolkkyPlayer player)
    {
        for (MolkkyPlayer p: _players) {
            if (player.name().equals(p.name())) {
                return false;
            }
        }

        _players.add(player);
        return true;
    }

    public synchronized MolkkyPlayer get(int idx)
    {
        try {
            return _players.get(idx);
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized MolkkyPlayer get(String name)
    {
        for (MolkkyPlayer p: _players) {
            if (name.equals(p.name())) {
                return p;
            }
        }
        return null;
    }

    public synchronized void save(Context context)
    {
        try {
            FileOutputStream fos = context.openFileOutput(PLAYERSDB, context.MODE_PRIVATE);
            OutputStreamWriter fow = new OutputStreamWriter(fos);
            CSVWriter writer = new CSVWriter(fow);
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
        } catch (Exception e) {
            Log.d("ex", e.toString());
        };
    }

    public void load(Context context, Handler handler)
    {
        _context = context;
        _handler = handler;
        _loading_task = new LoadingTask();
        _loading_thread = new Thread(_loading_task);
        _loading_thread.start();
    }

    public void reload() {
        if (_loading_thread.isAlive()) {
            return;
        }

        _loading_task = new LoadingTask();
        _loading_thread = new Thread(_loading_task);
        _loading_thread.start();
    }

    public synchronized void remove(String name) {
        MolkkyPlayer p = get(name);
        if (p != null) {
            _players.remove(p);
        }
    }

    public synchronized void remove(MolkkyPlayer p) {
        remove(p.name());
    }
}

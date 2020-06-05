package net.halman.molkkynotes;

import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class History {
    public static final int HISTORY_UPDATED = 1;

    private Thread _load_thread;
    private LoadingTask _loading_task;
    private File _history_dir;
    private Handler _handler;

    private class HistoryItem {
        String path = "";
        String name = "";
    }

    class LoadingTask implements Runnable {
        public void run() {
            _history_items.clear();
            File[] files = _history_dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches(".*\\.csv");
                }
            });

            for(File F: files) {
                HistoryItem i = new HistoryItem();
                i.path = F.getAbsolutePath();
                i.name = F.getName();
                _history_items.add(i);
            }

            Collections.sort(_history_items, new Comparator<HistoryItem>() {
                @Override
                public int compare(HistoryItem t0, HistoryItem t1) {
                    return t1.name.compareTo(t0.name);
                }
            });

            if (_handler != null) {
                _handler.sendMessage(_handler.obtainMessage(HISTORY_UPDATED));
            }
        }
    }

    private ArrayList<HistoryItem> _history_items = new ArrayList<>();

    private File historyDir(Context context)
    {
        return context.getExternalFilesDir("history");
    }

    public void reload()
    {
        if (_load_thread.isAlive()) {
            return;
        }

        _load_thread.start();
    }

    public void load(Context context, Handler handler)
    {
        _history_dir = context.getExternalFilesDir("history");
        _handler = handler;
        _loading_task = new LoadingTask();
        _load_thread = new Thread(_loading_task);
        _load_thread.start();
    }

    public synchronized int size()
    {
        return _history_items.size();
    }

    public synchronized String getName(int index)
    {
        return _history_items.get(index).name;
    }

    public synchronized void add(HistoryItem item)
    {
        _history_items.add(item);
    }

    public synchronized void clear()
    {
        _history_items.clear();
    }

    public synchronized String getPath(int index)
    {
        return _history_items.get(index).path;
    }
}

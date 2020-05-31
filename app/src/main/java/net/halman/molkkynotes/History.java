package net.halman.molkkynotes;

import android.content.Context;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class History {

    private class HistoryItem {
        String path = "";
        String name = "";
    }

    private ArrayList<HistoryItem> _history_items = new ArrayList<>();

    private File historyDir(Context context)
    {
        return context.getExternalFilesDir("history");
    }

    public void load(Context context)
    {
        _history_items.clear();
        File dir = historyDir(context);
        File[] files = dir.listFiles(new FilenameFilter() {
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
    }

    public int size()
    {
        return _history_items.size();
    }

    public String get(int index)
    {
        return _history_items.get(index).name;
    }
    public String getPath(int index)
    {
        return _history_items.get(index).path;
    }
}

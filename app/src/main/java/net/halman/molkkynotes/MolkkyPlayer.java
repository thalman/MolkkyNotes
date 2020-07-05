package net.halman.molkkynotes;


import java.io.Serializable;

public class MolkkyPlayer extends Object implements Serializable {
    static final public int HISTORY_LIMIT = 30;

    private int _id = 0;

    private String _name = "";

    private  String _history = "";

    public MolkkyPlayer()
    {
        _name = "";
    }

    public MolkkyPlayer(String name)
    {
        _name = name;
    }

    public MolkkyPlayer(MolkkyPlayer other)
    {
        _name = other._name;
        _history = other._history;
        _id = other._id;
    }

    public int id()
    {
        return _id;
    }

    public void id(int id)
    {
        _id = id;
    }

    public String name()
    {
        return _name;
    }

    public void name (String name)
    {
        _name = name;
    }

    public MolkkyPlayer setName (String name)
    {
        _name = name;
        return this;
    }

    public double averageScore()
    {
        int sum = 0;
        int count = 0;

        for (String n: _history.split(",")) {
            try {
                sum += Integer.parseInt(n);
                count++;
            } catch (Exception e) {}
        }

        if (count > 0) {
            return (sum + 0.0) / count;
        }
        return 0.0;
    }

    public String averageScoreString() {
        return String.format("%.0f", averageScore());
    }

    private void limitHistory()
    {
        String items[] = _history.split(",");
        if (items.length <= HISTORY_LIMIT) {
            return;
        }

        StringBuilder s = new StringBuilder("");
        String prefix = "";
        for (int i = items.length - HISTORY_LIMIT; i < items.length; i++) {
            s.append(prefix);
            s.append(items[i]);
            prefix = ",";
        }

        _history = s.toString();
    }

    public void addRoundScore (int points) {
        if (_history.equals("")) {
            _history = "" + points;
        } else {
            _history += "," + points;
        }

        limitHistory();
    }

    public String history()
    {
        return _history;
    }

    public void history(String history)
    {
        _history = history;
    }

    public boolean equals(MolkkyPlayer p)
    {
        return _name.equals(p._name);
    }
}

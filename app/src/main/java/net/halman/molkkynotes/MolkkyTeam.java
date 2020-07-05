package net.halman.molkkynotes;

import java.io.Serializable;
import java.util.ArrayList;

public class MolkkyTeam implements Serializable {
    private int _id = 0;
    private ArrayList<MolkkyPlayer> _players = new ArrayList<MolkkyPlayer>();
    private int _current = 0;

    public MolkkyTeam () { }

    public MolkkyTeam (MolkkyTeam other)
    {
        _id = other._id;
        _current = other._current;
        for (MolkkyPlayer player: other.players()) {
            addPlayer(player);
        }
    }

    public int id()
    {
        return _id;
    }

    public void id(int id)
    {
        _id = id;
    }

    public void addPlayer (MolkkyPlayer P) {
        if (P == null || hasPlayer(P)) {
            return;
        }

        _players.add(new MolkkyPlayer(P));
    }

    public void addPlayer (int index, MolkkyPlayer P) {
        if (P == null || hasPlayer(P)) {
            return;
        }

        _players.add(index, new MolkkyPlayer(P));
    }

    ArrayList <String> playerNames () {
        ArrayList <String> result = new ArrayList <String> ();
        for (int i = _current; i < _players.size(); i++) {
            result.add(_players.get (i).name ());
        }
        for (int i = 0; i < _current; i++) {
            result.add(_players.get (i).name ());
        }
        return result;
    }

    MolkkyPlayer currentPlayer () {
        if (_players.size() == 0) {
            return null;
        }
        if (_current >= _players.size() || _current < 0) {
            _current = 0;
        }
        return _players.get(_current);
    }

    MolkkyPlayer nextPlayer () {
        _current++;
        if (_current > _players.size()) _current = 0;
        return currentPlayer();
    }

    MolkkyPlayer prevPlayer () {
        _current--;
        if (_current < 0) _current = _players.size() - 1;
        return currentPlayer();
    }

    public String name()
    {
        StringBuilder result = new StringBuilder("");
        for(int a = 0; a < _players.size(); a++) {
            result.append(_players.get(a).name());
            if (a < _players.size() - 1) {
                result.append( ", ");
            }
        }

        return result.toString();
    }

    public int getPlayersIndex(String playersName)
    {
        for (MolkkyPlayer player: _players) {
            if (player.name().equals(playersName)) {
                return _players.indexOf(player);
            }
        }

        return -1;
    }

    public int getPlayersIndex(MolkkyPlayer player)
    {
        if (player == null) {
            return -1;
        }

        return getPlayersIndex(player.name());
    }

    public boolean hasPlayer(MolkkyPlayer player)
    {
        return (getPlayersIndex(player) >= 0);
    }

    public boolean hasPlayer(String player)
    {
        return (getPlayersIndex(player) >= 0);
    }

    public void removePlayer(String name)
    {
        for (MolkkyPlayer player: _players) {
            if (player.name().equals(name)) {
                _players.remove(player);
                return;
            }
        }
    }

    public void removePlayer(MolkkyPlayer p)
    {
        removePlayer(p.name());
    }

    public void removePlayer(int index)
    {
        if (index >= 0 && index < _players.size()) {
            _players.remove(index);
        }
    }

    public ArrayList<MolkkyPlayer> players()
    {
        return _players;
    }

    public int size()
    {
        return _players.size();
    }
}

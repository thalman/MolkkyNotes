package net.halman.molkkynotes;

import java.io.Serializable;
import java.util.ArrayList;

public class MolkkyTeam implements Serializable {
    private ArrayList<MolkkyPlayer> _members = new ArrayList<MolkkyPlayer>();
    private int _current = 0;
    private String _name = "";

    public MolkkyTeam (String name) {
        _name = name;
    }

    public MolkkyTeam () { }

    public void addMember (MolkkyPlayer P) {
        if (P == null || hasPlayer(P)) {
            return;
        }

        _members.add(new MolkkyPlayer(P));
    }

    ArrayList <String> memberNames () {
        ArrayList <String> result = new ArrayList <String> ();
        for (int i = _current; i < _members.size(); i++) {
            result.add(_members.get (i).name ());
        }
        for (int i = 0; i < _current; i++) {
            result.add(_members.get (i).name ());
        }
        return result;
    }

    MolkkyPlayer currentPlayer () {
        if (_members.size() == 0) {
            return null;
        }
        if (_current >= _members.size() || _current < 0) {
            _current = 0;
        }
        return _members.get(_current);
    }

    MolkkyPlayer nextPlayer () {
        _current++;
        if (_current > _members.size()) _current = 0;
        return currentPlayer();
    }

    MolkkyPlayer prevPlayer () {
        _current--;
        if (_current < 0) _current = _members.size() - 1;
        return currentPlayer();
    }

    public String name()
    {
        StringBuilder result = new StringBuilder("");
        for(int a = 0; a < _members.size(); a++) {
            result.append(_members.get(a).name());
            if (a < _members.size() - 1) {
                result.append( ", ");
            }
        }
        return result.toString();
    }

    public boolean hasPlayer(MolkkyPlayer player)
    {
        for (MolkkyPlayer member: _members) {
            if (member.equals(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPlayer(String player)
    {
        for (MolkkyPlayer member: _members) {
            if (member.name().equals(player)) {
                return true;
            }
        }
        return false;
    }

    public void removePlayer(String name)
    {
        for (MolkkyPlayer member: _members) {
            if (member.name().equals(name)) {
                _members.remove(member);
                return;
            }
        }
    }

    public void removePlayer(MolkkyPlayer p)
    {
        removePlayer(p.name());
    }

    public ArrayList<MolkkyPlayer> members()
    {
        return _members;
    }

    public int size()
    {
        return _members.size();
    }
}

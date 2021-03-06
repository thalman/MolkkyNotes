package net.halman.molkkynotes;

import java.io.Serializable;

public class MolkkyHit implements Serializable {
    public static final int NOTPLAYED = -2;
    public static final int LINECROSS = -1;

    private int _hit = NOTPLAYED;

    public MolkkyHit() {
        _hit = NOTPLAYED;
    }

    public MolkkyHit(int hit) {
        _hit = hit;
    }

    public int hit()
    {
        return _hit;
    }

    public void hit(int hit)
    {
        this._hit = hit;
    }

    public void hit(MolkkyHit hit)
    {
        if (hit == null) {
            return;
        }

        this._hit = hit._hit;
    }
}

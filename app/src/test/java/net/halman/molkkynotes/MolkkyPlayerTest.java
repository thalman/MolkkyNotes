package net.halman.molkkynotes;

import org.junit.Test;
import static org.junit.Assert.*;

public class MolkkyPlayerTest {
    @Test
    public void playerAverage() {
        MolkkyPlayer p = new MolkkyPlayer("Tom");
        assertEquals(p.name(),"Tom");
        p.name ("Mike");
        assertEquals(p.name(),"Mike");
        assertEquals("0", p.averageScoreString());
        p.addRoundScore(10);
        assertEquals("10", p.averageScoreString());
        p.addRoundScore(20);
        assertEquals("15", p.averageScoreString());
        p.addRoundScore(25);
        assertEquals("18", p.averageScoreString());
    }
    @Test
    public void playerAverageLast10() {
        MolkkyPlayer p = new MolkkyPlayer();
        for (int i = 0; i < 10; i++) p.addRoundScore(10);
        for (int i = 0; i < 5; i++) p.addRoundScore(20);
        assertEquals("13", p.averageScoreString());
    }
}

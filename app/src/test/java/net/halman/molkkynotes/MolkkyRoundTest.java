package net.halman.molkkynotes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MolkkyRoundTest {
    @Test
    public void MolkkyRound() {
        MolkkyPlayer tom = new MolkkyPlayer("Tom");
        MolkkyPlayer mike = new MolkkyPlayer("Mike");
        MolkkyTeam toms = new MolkkyTeam();
        MolkkyTeam mikes = new MolkkyTeam();
        toms.addPlayer(tom);
        mikes.addPlayer(mike);

        MolkkyRound round = new MolkkyRound();
        round.addTeam(toms);
        round.addTeam(mikes);


        round.currentHit(10);
        round.nextHit();
        round.currentHit(5);
        round.nextHit();
        round.currentHit(10);
        round.nextHit();
        round.currentHit(5);
        round.nextHit();

        assertEquals(20, round.teamScore(toms));
        assertEquals(10, round.teamScore(mikes));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(toms));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(mikes));

        round.currentHit(0);
        round.nextHit();
        round.currentHit(MolkkyHit.LINECROSS);
        round.nextHit();

        assertEquals(20, round.teamScore(toms));
        assertEquals(10, round.teamScore(mikes));
        assertEquals(MolkkyRound.ZERO, round.teamHealth(toms));
        assertEquals(MolkkyRound.ZERO, round.teamHealth(mikes));

        round.currentHit(0);
        round.nextHit();
        round.currentHit(0);
        round.nextHit();

        assertEquals(20, round.teamScore(toms));
        assertEquals(10, round.teamScore(mikes));
        assertEquals(MolkkyRound.TWOZEROS, round.teamHealth(toms));
        assertEquals(MolkkyRound.TWOZEROS, round.teamHealth(mikes));

        round.currentHit(1);
        round.nextHit();
        round.currentHit(1);
        round.nextHit();

        assertEquals(21, round.teamScore(toms));
        assertEquals(11, round.teamScore(mikes));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(toms));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(mikes));

        round.currentHit(9);
        round.nextHit();
        round.currentHit(9);
        round.nextHit();
        round.currentHit(12);
        round.nextHit();
        round.currentHit(12);
        round.nextHit();

        assertEquals(42, round.teamScore(toms));
        assertEquals(32, round.teamScore(mikes));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(toms));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(mikes));

        round.currentHit(MolkkyHit.LINECROSS);
        round.nextHit();
        round.currentHit(12);
        round.nextHit();

        assertEquals(25, round.teamScore(toms));
        assertEquals(44, round.teamScore(mikes));
        assertEquals(MolkkyRound.ZERO, round.teamHealth(toms));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(mikes));

        round.currentHit(12);
        round.nextHit();
        round.currentHit(12);
        round.nextHit();

        assertEquals(37, round.teamScore(toms));
        assertEquals(25, round.teamScore(mikes));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(toms));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(mikes));
        assertEquals (false, round.over());

        round.currentHit(12);
        round.nextHit();
        round.currentHit(12);
        round.nextHit();

        assertEquals(49, round.teamScore(toms));
        assertEquals(37, round.teamScore(mikes));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(toms));
        assertEquals(MolkkyRound.GOOD, round.teamHealth(mikes));
        assertEquals (false, round.over());

        round.currentHit(1);
        assertEquals(50, round.teamScore(toms));
        assertEquals(37, round.teamScore(mikes));
        assertEquals (true, round.over());
    }
}

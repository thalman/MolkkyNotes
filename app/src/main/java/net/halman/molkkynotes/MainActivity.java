package net.halman.molkkynotes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.halman.molkkynotes.ui.main.GameFragment;
import net.halman.molkkynotes.ui.main.ResultsFragment;
import net.halman.molkkynotes.ui.main.SectionsPagerAdapter;
import net.halman.molkkynotes.ui.main.TeamsFragment;
import net.halman.molkkynotes.ui.main.UIButton;

public class MainActivity extends AppCompatActivity
        implements TeamsFragment.OnFragmentInteractionListener,
        GameFragment.OnGameFragmentInteractionListener,
        ResultsFragment.OnResultsFragmentInteractionListener
{

    private MolkkyGame _game = new MolkkyGame();
    private Players _players = new Players();
    private Setup _setup = new Setup();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onTabChange(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        FloatingActionButton fab = findViewById(R.id.fabMail);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _players.load(this);
        _setup.load(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.navigation, menu);
        MenuItem i = menu.findItem(R.id.menuPlayInTeams);
        i.setChecked(_setup.playInTeams());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menuNewGame:
                onNewGameMenuClick();
                break;
            case R.id.menuPlayInTeams:
                item.setChecked(!item.isChecked());
                _setup.playInTeams(item.isChecked());
                break;
            case R.id.menuPreferences:
                break;
        }

        return true;
    }

    @Override
    public void onStop() {
        _setup.save(this);
        super.onStop();
    }

    @Override
    public void onStart() {
        _setup.load(this);
        super.onStart();
    }

    public void switchTab(int idx)
    {
        if (idx < 0 || idx > 2) {
            return;
        }

        TabLayout tabs = findViewById(R.id.tabs);
        TabLayout.Tab tab = tabs.getTabAt(idx);
        if (tab != null) {
            tab.select();
        }
    }

    public void onResultsFragmentInteraction(Uri uri) {
        Log.d("UI", uri.toString());
    }

    public MolkkyGame game()
    {
        return _game;
    }

    public Players players()
    {
        return _players;
    }

    private Fragment getFragment(int page)
    {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + page);
    }

    private TeamsFragment teamsFragment()
    {
        return (TeamsFragment) getFragment(0);
    }

    private GameFragment gameFragment()
    {
        return (GameFragment) getFragment(1);
    }

    private ResultsFragment resultsFragment()
    {
        return (ResultsFragment) getFragment(2);
    }

    public void onTabChange(int position)
    {
        Log.d("UI", "Selected tab " + position);
    }

    public void onNewRoundStart()
    {
        switchTab(1);
        GameFragment g = gameFragment();
        if (g != null) {
            g.updateScreen();
        }
    }

    public void onNewGameStart()
    {
        _game.clearTeams();
        _game.clearRounds();
        switchTab(0);
        onTabChange(0);
    }

    public void onNewGameMenuClick()
    {
        if (_game.roundOver() || !_game.roundStarted()) {
            onNewGameStart();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialogStartNewGame);
            builder.setMessage(R.string.dialogRoundInProgress);
            builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onNewGameStart();
                }
            });

            builder.setNegativeButton(R.string.dCancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }
}
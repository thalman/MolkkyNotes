package net.halman.molkkynotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.print.PrintHelper;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import net.halman.molkkynotes.ui.main.GameFragment;
import net.halman.molkkynotes.ui.main.HistoryFragment;
import net.halman.molkkynotes.ui.main.ResultsFragment;
import net.halman.molkkynotes.ui.main.SectionsPagerAdapter;
import net.halman.molkkynotes.ui.main.TeamListDialog;
import net.halman.molkkynotes.ui.main.TeamsFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements TeamsFragment.OnFragmentInteractionListener,
        GameFragment.OnGameFragmentInteractionListener,
        ResultsFragment.OnResultsFragmentInteractionListener,
        HistoryFragment.OnHistoryFragmentInteractionListener
{
    static final int MOVE_FORWARD = 3;

    private MolkkyGame _game = new MolkkyGame();
    private Players _players = new Players();
    private Setup _setup = new Setup();
    private History _history = new History();
    private final static String _game_state_file = "game.bin";
    private String _history_open_file = "";
    private Handler _handler = null;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case Players.PLAYERS_UPDATED:
                        TeamsFragment t = teamsFragment();
                        if (t != null) {
                            t.notifyPlayersChanged();
                        }
                        break;
                    case History.HISTORY_UPDATED:
                        HistoryFragment f = historyFragment();
                        if (f != null) {
                            f.notifyGameSaved();
                        }
                        break;
                    case MOVE_FORWARD:
                        GameFragment g = gameFragment();
                        if (g != null) {
                            g.gameStepForward();
                        }
                    default:
                        super.handleMessage(inputMessage);
                }
            }
        };

        _players.load(this, _handler);
        _setup.load(this);
        _history.load(this, _handler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.navigation, menu);
        MenuItem i = menu.findItem(R.id.menuPlayInTeams);
        i.setChecked(_setup.playInTeams());
        i = menu.findItem(R.id.menuKeepScreenOn);
        i.setChecked(_setup.keepScreenOn());
        i = menu.findItem(R.id.menuAutoForward);
        i.setChecked(_setup.autoForward());
        i = menu.findItem(R.id.menuMaxBrightness);
        i.setChecked(_setup.setBrightness());
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
            case R.id.menuAutoForward:
                item.setChecked(!item.isChecked());
                _setup.autoForward(item.isChecked());
                break;
            case R.id.menuChangeStartingTeam:
                onChangeStartingTeam();
                break;
            case R.id.menuRules:
                onRules();
                break;
            case R.id.menuKeepScreenOn:
                item.setChecked(!item.isChecked());
                _setup.keepScreenOn(item.isChecked());
                onKeepScreenOn();
                break;
            case R.id.menuMaxBrightness:
                item.setChecked(!item.isChecked());
                _setup.setBrightness(item.isChecked());
                onChangeBrightness();
                break;
            case R.id.menuPreferences:
                onSetupMenuClick();
                break;
            case R.id.menuPrint:
                onPrintMenuClick();
                break;
        }

        if (item.isCheckable()) {
            _setup.save(this);
        }

        return true;
    }

    @Override
    public void onStop() {
        cancelForwardMove();
        _setup.save(this);
        saveGame();
        super.onStop();
    }

    @Override
    public void onStart() {
        _setup.load(this);
        onKeepScreenOn();
        onChangeBrightness();
        loadGame();
        super.onStart();
    }

    public void saveGame() {
        try {
            FileOutputStream file = openFileOutput(_game_state_file, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(file);
            oos.writeObject(_game);
            oos.flush();
            oos.close();
            file.close();
        } catch (Exception e) {
        }
    }

    public void loadGame() {
        try {
            FileInputStream file = openFileInput(_game_state_file);
            ObjectInputStream ois = new ObjectInputStream(file);
            _game = (MolkkyGame) ois.readObject();
            ois.close();
            file.close();
        } catch (Exception e) {
            _game = new MolkkyGame();
        }
    }

    public void switchTab(int idx)
    {
        if (idx < 0 || idx > 3) {
            return;
        }

        TabLayout tabs = findViewById(R.id.tabs);
        TabLayout.Tab tab = tabs.getTabAt(idx);
        if (tab != null) {
            tab.select();
        }
    }

    public MolkkyGame game()
    {
        return _game;
    }

    public Players players()
    {
        return _players;
    }

    public Setup setup()
    {
        return _setup;
    }

    public History history()
    {
        return _history;
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

    private HistoryFragment historyFragment()
    {
        return (HistoryFragment) getFragment(3);
    }

    public void onTabChange(int position)
    {
        Log.d("UI", "Selected tab " + position);
    }

    private void updatePlayersStatistics()
    {
        for (MolkkyRound r: _game.rounds()) {
            if (r.over()) {
                for (MolkkyTeam t: _game.teams()) {
                    int score = r.teamScore(t);
                    for(MolkkyPlayer p: t.players()) {
                        MolkkyPlayer player = _players.get(p.name());
                        if (player != null)
                            player.addRoundScore(score);
                    }
                }
            }
        }

        _players.save(this);
        _players.notifyUpdate();
    }

    public void onNewGameStart()
    {
        // this is called from menu -> new game
        updatePlayersStatistics();
        _game.stripUnfinishedRound();
        if (_game.roundOver()) {
            File dir = getExternalFilesDir("history");
            File path = new File(dir, _game.dateAsString() + ".csv");
            _game.CSVExport(path.toString());
            _history.reload();
        }

        _game = new MolkkyGame();
        _game.setup(_setup);

        TeamsFragment t = teamsFragment();
        if (t != null) {
            t.updateScreen();
        }

        switchTab(0);
        onTabChange(0);
    }

    public void onGameOver()
    {
        // this is called from result fragment
        updatePlayersStatistics();
        _game = new MolkkyGame();
        _game.setup(_setup);
        _history.reload();
        if (getString(R.string.sponsoredVersion).equalsIgnoreCase("no")) {
            new Promotion(this).promote();
        }
    }


    public void onNewGameMenuClick()
    {
        if (_game.roundOver() || !_game.roundStarted()) {
            onNewGameStart();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MolkkyAlertDialogStyle);
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

    public void onSetupMenuClick()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dialogSetup);

        LayoutInflater inflater = getLayoutInflater();
        final View dialog_layout = inflater.inflate(R.layout.setup_dialog, null);
        final MainActivity activity = this;
        EditText goal = dialog_layout.findViewById(R.id.goalValue);
        goal.setText(Integer.toString(_setup.goal()));
        EditText penalty = dialog_layout.findViewById(R.id.penaltyOverValue);
        penalty.setText(Integer.toString(_setup.penaltyOverGoal()));
        Spinner spinner = dialog_layout.findViewById(R.id.setupNextSetTeamValue);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
               R.array.dialogNextSetTeamValues, R.layout.white_spinner);
        adapter.setDropDownViewResource(R.layout.white_spinner_dropdown);
        spinner.setAdapter(adapter);

        final int[] next_set_start_team_candidate = new int[1];
        next_set_start_team_candidate[0] = _setup.nextSetStartingTeam();
        spinner.setSelection(next_set_start_team_candidate[0]);

        Spinner.OnItemSelectedListener spinnerListener = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                next_set_start_team_candidate[0] = pos;
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Empty interface callback
            }
        };

        spinner.setOnItemSelectedListener(spinnerListener);

        builder.setView(dialog_layout);
        builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    EditText goal = dialog_layout.findViewById(R.id.goalValue);
                    EditText penalty = dialog_layout.findViewById(R.id.penaltyOverValue);
                    int g = Integer.parseInt(goal.getText().toString());
                    int p = Integer.parseInt(penalty.getText().toString());
                    if (p > g) {
                        p = g;
                    }

                    _setup.goal(g);
                    _setup.penaltyOverGoal(p);
                    _setup.save(activity);
                    _setup.nextSetStartingTeam(next_set_start_team_candidate[0]);
                    _game.setup(_setup);
                } catch (Exception e) {
                    Log.d("MA", e.toString());
                };
                dialog.dismiss();
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

    private void onPrintMenuClick()
    {
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dialogPrintOrSend);

        LayoutInflater inflater = getLayoutInflater();
        final View dialog_layout = inflater.inflate(R.layout.items_dialog, null);
        final MainActivity activity = this;
        final ListView listView = dialog_layout.findViewById(R.id.dialogListView);

        String [] lines = getResources().getStringArray(R.array.dialogPrintOrSendValues);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.items_dialog_item, lines);
        listView.setAdapter(adapter);

        builder.setView(dialog_layout);
        final AlertDialog ad = builder.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>adapter, View v, int position, long id) {
                switch (position) {
                    case 0: {
                        // print
                        PrintHelper printer = new PrintHelper(context);
                        printer.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                        ScoreExport export = new ScoreExport(context);
                        Bitmap sheet = export.emptySheet();
                        printer.printBitmap("Empty Sheet Molkky", sheet);
                        break;
                    }
                    case 1: {
                        // share
                        try {
                            ScoreExport export = new ScoreExport(context);
                            File dir = context.getExternalFilesDir("tmp");
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }

                            File file = new File(dir, "empty-sheet.jpg");
                            export.emptySheetJpeg(file.getAbsolutePath());
                            String provider = getApplicationContext().getPackageName() + ".provider";
                            Uri uri = FileProvider.getUriForFile(context, provider, file);

                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("image/*");
                            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(sharingIntent, getString(R.string.historyShare)));
                        } catch (Exception ignored) { }
                        break;
                    }
                }
                ad.dismiss();
            }
        });
    }

    private void onKeepScreenOn()
    {
        if (_setup.keepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onRules()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dialogRules);
        builder.setMessage(R.string.dialogRulesDetail);
        builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void onChangeStartingTeam()
    {
        if (_game.roundStarted()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MolkkyAlertDialogStyle);
            builder.setTitle(R.string.dialogRoundInProgress);
            builder.setTitle(R.string.dialogRoundInProgressChangeTeam);
            builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
            return;
        }

        AlertDialog.Builder builder = TeamListDialog.getBuilder(this, _game, false, new TeamListDialog.OnTeamSelectedListener() {
            @Override
            public void onTeamSelected(int which) {
                _game.changeCurrentRoundTeams(which);
                GameFragment gf = gameFragment();
                if (gf != null) {
                    gf.updateScreen();
                }
            }
        });

        if (builder != null) {
            builder.setTitle(R.string.dialogNextSetTeam);
            builder.show();
        }
    }

    private void onChangeBrightness()
    {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        if (_setup.setBrightness()) {
            // 1.0f => set brightness to maximum
            layout.screenBrightness = 1.0f;
        } else {
            // negative number => set to user's default
            layout.screenBrightness = -1.0f;
        }
        getWindow().setAttributes(layout);
    }

    public String historyOpenFile()
    {
        return _history_open_file;
    }

    public void historySaveStatus(String file)
    {
        _history_open_file = file;
    }

    public void scheduleForwardMove()
    {
        if (_setup.autoForward()) {
            cancelForwardMove();
            _handler.sendEmptyMessageDelayed(3, 2500);
        }
    }

    public void cancelForwardMove()
    {
        if (_setup.autoForward()) {
            _handler.removeMessages(MOVE_FORWARD);
        }
    }
}

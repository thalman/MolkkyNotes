package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.MolkkyPlayer;
import net.halman.molkkynotes.MolkkyTeam;
import net.halman.molkkynotes.Players;
import net.halman.molkkynotes.PlayersAdapter;
import net.halman.molkkynotes.R;
import net.halman.molkkynotes.Setup;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TeamsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TeamsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeamsFragment extends Fragment {
    private OnFragmentInteractionListener _listener;

    private RecyclerView _all_players = null;
    private LinearLayout _selected_teams = null;
    private PlayersAdapter _players_adapter = null;
    private LinearLayoutManager _layout_manager = null;
    private int[] _team_view_ids = {R.id.tTeam1, R.id.tTeam2, R.id.tTeam3, R.id.tTeam4, R.id.tTeam5, R.id.tTeam6, R.id.tTeam7, R.id.tTeam8, R.id.tTeam9, R.id.tTeam10 };
    private UIButton _mix = null;

    public TeamsFragment() {
        // Required empty public constructor
    }

    public static TeamsFragment newInstance() {
        return new TeamsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_teams, container, false);

        _all_players = result.findViewById(R.id.allPlayers);
        _selected_teams = result.findViewById(R.id.frgSelectedTeams);

        _mix = result.findViewById(R.id.tMix);
        _mix.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (gameInProgress()) {
                        return;
                    }

                    MolkkyGame game = _listener.game();
                    if (game.teams().size() > 1) {
                        game.shuffleTeams();
                        updateScreen();
                        _listener.switchTab(1);
                    }
                }
            }
        );


        View addUser = result.findViewById(R.id.teamsAddUser);
        if (addUser != null) {
            addUser.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userAddDialog();
                    }
                }
            );
        }

        if (_all_players != null && _listener != null) {
            MolkkyGame game = _listener.game();
            Players players = _listener.players();
            _players_adapter = new PlayersAdapter(players, getResources(), new PlayersAdapter.OnPlayerListener() {
                @Override
                public void onPlayerClick(String name) {
                    onRecyclerViewClick(name);
                }

                @Override
                public void onPlayerLongClick(String name) {
                    onRecyclerViewLongClick(name);
                }
            });

            _all_players.setAdapter(_players_adapter);
            _layout_manager = new LinearLayoutManager(getContext());
            _all_players.setLayoutManager(_layout_manager);
        } else {
            _all_players = null;
        }

        for(int a = 0; a < _team_view_ids.length; a++) {
            UIPlayer btn = _selected_teams.findViewById(_team_view_ids[a]);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onRemovePlayer((UIPlayer)view);
                }
            });
        }

        updateScreen();
        return result;
    }

    private void onRemovePlayer(UIPlayer btn)
    {
        if (gameInProgress()) {
            return;
        }

        String text = btn.name();
        MolkkyGame game = _listener.game();
        if (game.gameStarted()) {
            return;
        }

        MolkkyTeam team = game.teamByTeamsName(text);
        if (team == null) {
            return;
        }

        if (team.size() == 1) {
            game.removePlayerByName(text);
        } else {
            removePlayerFromTeamDialog(team);
            return;
        }

        updateScreen();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            _listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _listener = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            updateScreen();
        }
    }

    public void onRecyclerViewClick(String name)
    {
        if (_listener == null) {
            return;
        }

        if (gameInProgress()) {
            return;
        }

        MolkkyGame game = _listener.game();
        Setup setup = _listener.setup();
        Players players = _listener.players();
        Log.d("A", "User add " + name);

        MolkkyPlayer p = players.get(name);
        if (playerAlreadyInTheGame(p)) {
            return;
        }

        if (setup.playInTeams() && game.teams().size() > 0) {
            addPlayerToTeamDialog(p);
        } else {
            game.addPlayer(p);
            updateScreen();
        }
    }

    public void onRecyclerViewLongClick(String name) {
        if (_listener == null) {
            return;
        }

        userEditDialog(name);
    }


    public void updateScreen()
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame game = _listener.game();
        for(int a = 0; a < _team_view_ids.length; a++) {
            UIPlayer btn = _selected_teams.findViewById(_team_view_ids[a]);
            if (btn != null) {
                try {
                    MolkkyTeam t = game.teams().get(a);
                    btn.name(t.name());
                    btn.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    btn.name("");
                    btn.setVisibility(View.GONE);
                }
            }
        }

        _mix.active(game.teams().size() > 1 && !_listener.game().gameStarted());
    }

    public void userAddDialog () {
        if (_listener == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dAddPlayer);
        LayoutInflater inflater = getLayoutInflater();
        final View dialog_layout = inflater.inflate(R.layout.players_dialog, null);
        builder.setView(dialog_layout);
        builder.setPositiveButton(R.string.dAdd, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (_listener == null) {
                    return;
                }
                Players players = _listener.players();
                EditText t = dialog_layout.findViewById(R.id.dPlayersName);
                if (t != null) {
                    String name = t.getText().toString();
                    if (!name.isEmpty()) {
                        if (players.add(new MolkkyPlayer(name))) {
                            players.sort();
                            players.save(getContext());
                            _players_adapter.notifyDataSetChanged();
                        }
                    }
                }
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

    public void userEditDialog (String name) {
        if (_listener == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dEditPlayer);
        LayoutInflater inflater = getLayoutInflater();
        final View dialog_layout = inflater.inflate(R.layout.players_dialog, null);
        final EditText t = dialog_layout.findViewById(R.id.dPlayersName);
        t.setText(name);
        final String old_name = name;

        builder.setView(dialog_layout);
        builder.setPositiveButton(R.string.dSave, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (_listener == null) {
                    return;
                }

                Players players = _listener.players();
                String name = t.getText().toString();
                if (!name.isEmpty()) {
                    MolkkyPlayer p = _listener.players().get(old_name);
                    if (p != null) {
                        p.setName(name);
                        players.sort();
                        players.save(getContext());
                        _players_adapter.notifyDataSetChanged();
                    }
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.dDelete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (_listener == null) {
                    return;
                }

                Players players = _listener.players();
                MolkkyPlayer p = _listener.players().get(old_name);
                if (p != null) {
                    players.remove(old_name);
                    players.save(getContext());
                    _players_adapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(R.string.dCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addPlayerToTeamDialog(MolkkyPlayer p)
    {
        if (_listener == null) {
            return;
        }

        final MolkkyGame game = _listener.game();
        CharSequence [] items = new CharSequence [game.teams().size() + 1];
        items[0] = getString(R.string.dialogNewTeam);
        final MolkkyPlayer player = new MolkkyPlayer(p);

        for (int i = 0; i < game.teams().size(); ++i) {
             items[i+1] = game.teams().get(i).name();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(getString(R.string.dialogAddPlayerToTeam, p.name()));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    game.addPlayer(player);
                } else {
                    MolkkyTeam t = game.teams().get(which - 1);
                    t.addMember(player);
                }
                updateScreen();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void removePlayerFromTeamDialog(final MolkkyTeam team)
    {

        if (team.size() <= 1) {
            _listener.game().removePlayerByName(team.name());
            updateScreen();
            return;
        }

        CharSequence [] items = new CharSequence [team.size()];
        for (int i = 0; i < team.size(); ++i) {
            items[i] = team.members().get(i).name();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dialogRemovePlayerFromTeam);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                team.members().remove(which);
                updateScreen();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public boolean gameInProgress () {
        if (_listener == null) {
            return true;
        }

        if (! _listener.game().gameStarted()) {
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.teamsGameInProgress);
        builder.setMessage(R.string.teamsGameInProgressDeail);
        builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        return true;
    }

    private boolean playerAlreadyInTheGame (MolkkyPlayer p) {
        if (_listener == null) {
            return true;
        }

        if (! _listener.game().hasPlayer(p)) {
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dialogPlayerInTheGame);
        builder.setMessage(R.string.dialogPlayerInTheGameDetail);
        builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
        return true;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        MolkkyGame game();
        Players players();
        void switchTab(int i);
        Setup setup();
    }
}

package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import net.halman.molkkynotes.TeamMembersAdapter;
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
public class TeamsFragment extends Fragment implements TeamMembersAdapter.TeamMembersListener {
    private OnFragmentInteractionListener _listener;

    private RecyclerView _all_players = null;
    private PlayersAdapter _players_adapter = null;
    private LinearLayoutManager _layout_manager = null;
    private RecyclerView _teams_view = null;
    private TeamMembersAdapter _teams_adapter = null;
    private ItemTouchHelper _teams_touch_helper = null;
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
    public synchronized View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_teams, container, false);

        _all_players = result.findViewById(R.id.allPlayers);
        _teams_view = result.findViewById(R.id.frgTeamsTeams);

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

            _teams_adapter = new TeamMembersAdapter(getContext(), _listener.game(), this);
            _teams_view.setAdapter(_teams_adapter);
            _teams_view.setLayoutManager(new LinearLayoutManager(getContext()));
            _teams_touch_helper = new ItemTouchHelper(createItemTouchHelper(_teams_adapter));
            _teams_touch_helper.attachToRecyclerView(_teams_view);
        } else {
            _all_players = null;
        }


        updateScreen();
        return result;
    }

    @Override
    public void onTeamsStartDrag(RecyclerView.ViewHolder viewHolder)
    {
        if (_teams_touch_helper != null) {
            _teams_touch_helper.startDrag(viewHolder);
        }
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
            // editTeamDialog(team);
            // removePlayerFromTeamDialog(team);
            return;
        }

        updateScreen();
    }

    @Override
    public synchronized void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            _listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public synchronized void onDetach() {
        super.onDetach();
        _listener = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (_teams_adapter != null) {
                _teams_adapter.cleanupEmptyTeams();
            }
            updateScreen();
        } else {
            if (_teams_adapter != null) {
                _teams_adapter.removeTrailingEmptyTeams();
            }
        }
    }

    public synchronized void notifyPlayersChanged()
    {
        if (_listener == null) {
            return;
        }

        if (_players_adapter != null) {
            _players_adapter.notifyDataSetChanged();
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
            if (game.teams().size() < 10) {
                game.addPlayer(p);
                updateScreen();
            } else {
                limitedTeamNumber(false);
            }
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
        _teams_adapter.setGame(game);
        _teams_adapter.cleanupEmptyTeams();
        _teams_adapter.notifyDataSetChanged();
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
        final MolkkyPlayer player = new MolkkyPlayer(p);
        AlertDialog.Builder builder = TeamListDialog.getBuilder(getContext(), game.teams(), true, new TeamListDialog.OnTeamSelectedListener() {
            @Override
            public void onTeamSelected(int which) {
                if (which == game.teams().size()) {
                    game.addPlayer(player);
                } else {
                    MolkkyTeam t = game.teams().get(which);
                    t.addPlayer(player);
                }
                updateScreen();
            }
        });

        builder.setTitle(getString(R.string.dialogAddPlayerToTeam, p.name()));
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
            items[i] = team.players().get(i).name();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dialogRemovePlayerFromTeam);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                team.players().remove(which);
                updateScreen();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private ItemTouchHelper.Callback createItemTouchHelper(final TeamMembersAdapter adapter) {
        ItemTouchHelper.Callback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                adapter.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.onRemoveItem(viewHolder.getAdapterPosition());
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder) {

                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                          int actionState) {   // We only want the active item
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder instanceof TeamMembersAdapter.TeamMembersViewHolder) {
                        TeamMembersAdapter.TeamMembersViewHolder vh =
                                (TeamMembersAdapter.TeamMembersViewHolder) viewHolder;
                        if (vh.drag_handle != null) {
                            // this is player
                            vh.root.setBackgroundColor(getResources().getColor(R.color.colorGray));
                        }
                    }
                }

                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                if (viewHolder instanceof TeamMembersAdapter.TeamMembersViewHolder) {
                    TeamMembersAdapter.TeamMembersViewHolder vh =
                            (TeamMembersAdapter.TeamMembersViewHolder) viewHolder;
                    if (vh.drag_handle != null) {
                        // this is player
                        vh.root.setBackgroundColor(Color.TRANSPARENT);
                    }
                }

                adapter.cleanupEmptyTeams();
            }
        };
        return simpleCallback;
    }

//    private void editTeamDialog(final MolkkyTeam team)
//    {
//        if (team == null) {
//            return;
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
//        LayoutInflater inflater = getLayoutInflater();
//        final View dialog_layout = inflater.inflate(R.layout.items_dialog, null);
//        final RecyclerView items_view = dialog_layout.findViewById(R.id.dialogRecyclerView);
//
//        // use a linear layout manager
//        RecyclerView.LayoutManager layout_manager = new LinearLayoutManager(getContext());
//        items_view.setLayoutManager(layout_manager);
//
//        // specify an adapter (see also next example)
//        TeamMembersAdapter mAdapter = new TeamMembersAdapter(team.members());
//        items_view.setAdapter(mAdapter);
//
//        ItemTouchHelper ta = new ItemTouchHelper(createItemTouchHelper(mAdapter));
//        ta.attachToRecyclerView(items_view);
//
//        builder.setView(dialog_layout);
//        builder.setTitle(R.string.dialogRemovePlayerFromTeam);
//
//        builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//
//        builder.show();
//    }


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

    private void limitedTeamNumber (boolean playInTeams) {
        if (_listener == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        if (playInTeams) {
            builder.setTitle(R.string.dialogNumberOfTeams);
            builder.setMessage(R.string.dialogNumberOfTeamsDetail);
        } else {
            builder.setTitle(R.string.dialogNumberOfPlayers);
            builder.setMessage(R.string.dialogNumberOfPlayersDetail);
        }
        builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
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

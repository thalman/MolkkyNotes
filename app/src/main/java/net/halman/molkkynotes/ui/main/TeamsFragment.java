package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

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
public class TeamsFragment extends Fragment implements TeamMembersAdapter.TeamMembersListener, PlayersAdapter.OnPlayerListener {
    private OnFragmentInteractionListener _listener;

    private PlayersAdapter _players_adapter = null;
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

        RecyclerView _all_players = result.findViewById(R.id.allPlayers);
        RecyclerView _teams_view = result.findViewById(R.id.frgTeamsTeams);

        _mix = result.findViewById(R.id.tMix);
        _mix.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (gameInProgress()) {
                        _listener.switchTab(1);
                        return;
                    }

                    MolkkyGame game = _listener.game();
                    if (game.teams().size() > 1) {
                        _listener.startGame();
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
            Players players = _listener.players();
            _players_adapter = new PlayersAdapter(players, getResources(), _listener.game(), this);

            _all_players.setAdapter(_players_adapter);
            LinearLayoutManager _layout_manager = new LinearLayoutManager(getContext());
            _all_players.setLayoutManager(_layout_manager);
            ItemTouchHelper _players_touch_helper = new ItemTouchHelper(createPlayersItemTouchHelper(_players_adapter));
            _players_touch_helper.attachToRecyclerView(_all_players);

            _teams_adapter = new TeamMembersAdapter(getContext(), _listener.game(), this);
            _teams_view.setAdapter(_teams_adapter);
            _teams_view.setLayoutManager(new LinearLayoutManager(getContext()));
            _teams_touch_helper = new ItemTouchHelper(createTeamsItemTouchHelper(_teams_adapter));
            _teams_touch_helper.attachToRecyclerView(_teams_view);
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

    @Override
    public void onTeamsDataSetChanged()
    {
        if (_listener != null) {
            _players_adapter.notifyDataSetFilterChanged();
            _mix.active(mixButtonActive(_listener.game()));
        }
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
    public synchronized void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (_teams_adapter != null) {
                _teams_adapter.cleanupEmptyTeams();
                updateScreen();
            }
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


    public void onPlayerClick(MolkkyPlayer player)
    {
        if (_listener == null || player == null) {
            return;
        }

        _listener.cancelGameStart();

        if (_listener.game().gameStarted()) {
            // game already started only teams modification
            addPlayerToTeamDialog(player, false);
            return;
        }

        MolkkyGame game = _listener.game();
        Setup setup = _listener.setup();

        if (playerAlreadyInTheGame(player)) {
            return;
        }

        if (setup.playInTeams() && game.teams().size() > 0) {
            addPlayerToTeamDialog(player, true);
        } else {
            game.addPlayer(player);
            updateScreen();
        }
    }

    public void onPlayerLongClick(MolkkyPlayer player) {
        if (_listener == null || player == null) {
            return;
        }

        userEditDialog(player);
    }

    public void onPlayerRemove(int idx)
    {
        final MolkkyPlayer player = _players_adapter.getVisiblePlayer(idx);
        if (player == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.teamsDeletePlayer);
        builder.setMessage(R.string.teamsDeletePlayerDetail);
        builder.setPositiveButton(R.string.dDelete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (_listener == null) {
                    return;
                }

                Players players = _listener.players();
                MolkkyPlayer p = _listener.players().get(player.name());
                if (p != null) {
                    players.remove(p.name());
                    players.save(getContext());
                    _players_adapter.notifyDataSetFilterChanged();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.dCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                _players_adapter.notifyDataSetFilterChanged();
            }
        });
        builder.show();

    }

    private boolean mixButtonActive(MolkkyGame game)
    {
        int num_of_teams = game.teams().size();
        if (num_of_teams > 1) {
            MolkkyTeam last = game.teams().get(num_of_teams - 1);
            if (last.players().size() == 0) {
                num_of_teams--;
            }
        }

        return (num_of_teams > 1 && ! game.gameStarted());
    }

    public void updateScreen()
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame game = _listener.game();
        if (_teams_adapter != null) {
            _teams_adapter.setGame(game);
            _teams_adapter.cleanupEmptyTeams();
            _teams_adapter.notifyDataSetChanged();
        }
        if (_players_adapter != null) {
            _players_adapter.game(game);
        }
        if (_mix != null) {
            _mix.active(mixButtonActive(game));
        }
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
                            _players_adapter.notifyDataSetFilterChanged();
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

    public void userEditDialog (MolkkyPlayer player) {
        if (_listener == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dEditPlayer);
        LayoutInflater inflater = getLayoutInflater();
        final View dialog_layout = inflater.inflate(R.layout.players_dialog, null);
        final EditText t = dialog_layout.findViewById(R.id.dPlayersName);
        t.setText(player.name());
        final String old_name = player.name();
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
                        _players_adapter.notifyDataSetFilterChanged();
                    }
                }
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

    private void addPlayerToTeamDialog(MolkkyPlayer p, boolean also_new_team)
    {
        if (_listener == null) {
            return;
        }

        final MolkkyGame game = _listener.game();
        final MolkkyPlayer player = new MolkkyPlayer(p);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.dialogNextSetTeam);
        builder.setTitle(getString(R.string.dialogAddPlayerToTeam, p.name()));

        String [] teams = ItemsListDialog.getTeamList(getContext(), game, also_new_team);
        if (game.teams().size() == 0 || (game.teams().size() == 1 && also_new_team)) {
            game.addPlayer(player);
            updateScreen();
            return;
        }

        ListView listView = ItemsListDialog.setItems(builder, getContext(), teams);
        final AlertDialog ad = builder.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == game.teams().size()) {
                    game.addPlayer(player);
                } else {
                    MolkkyTeam t = game.teams().get(position);
                    t.addPlayer(player);
                }
                ad.dismiss();
                updateScreen();
            }
        });
    }

    private ItemTouchHelper.Callback createTeamsItemTouchHelper(final TeamMembersAdapter adapter) {
        return new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                adapter.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.onRemoveItem(viewHolder.getAdapterPosition());
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {

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
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
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
    }

    private ItemTouchHelper.Callback createPlayersItemTouchHelper(final PlayersAdapter adapter) {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.onRemove(viewHolder.getAdapterPosition());
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {

                final int dragFlags = 0;
                final int swipeFlags = ItemTouchHelper.RIGHT;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

        };
    }


    public boolean gameInProgress () {
        if (_listener == null) {
            return true;
        }

        return  _listener.game().gameStarted();
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
     */
    public interface OnFragmentInteractionListener {
        MolkkyGame game();
        Players players();
        void switchTab(int i);
        void startGame();
        void cancelGameStart();
        Setup setup();
    }
}

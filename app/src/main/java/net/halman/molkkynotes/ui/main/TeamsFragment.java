package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
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

import net.halman.molkkynotes.MainActivity;
import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.MolkkyPlayer;
import net.halman.molkkynotes.MolkkyTeam;
import net.halman.molkkynotes.Players;
import net.halman.molkkynotes.PlayersAdapter;
import net.halman.molkkynotes.R;

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

        View mix = result.findViewById(R.id.tMix);
        mix.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (_listener == null) {
                        return;
                    }

                    MolkkyGame game = _listener.game();
                    game.shuffleTeams();
                    updateScreen();
                }
            }
        );


        View addUser = result.findViewById(R.id.tAddUser);
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
            });

            _all_players.setAdapter(_players_adapter);
            _layout_manager = new LinearLayoutManager(getContext());
            _all_players.setLayoutManager(_layout_manager);
        } else {
            _all_players = null;
        }

        updateScreen();
        return result;
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

        int[] ids = {R.id.tTeam1, R.id.tTeam2, R.id.tTeam3, R.id.tTeam4, R.id.tTeam5, R.id.tTeam6, R.id.tTeam7, R.id.tTeam8, R.id.tTeam9, R.id.tTeam10 };
        MolkkyGame game = _listener.game();
        Players players = _listener.players();
        Log.d("A", "User add " + name);

        MolkkyPlayer p = players.get(name);
        game.addPlayer(p);
        for(int a = 0; a < ids.length; a++) {
            UIButton btn = _selected_teams.findViewById(ids[a]);
            if (btn != null) {
                try {
                    MolkkyTeam t = game.teams().get(a);
                    btn.text(t.name());
                    btn.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    btn.text("");
                    btn.setVisibility(View.GONE);
                }
            }
        }
    }


    public void updateScreen()
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame game = _listener.game();
        int[] ids = {R.id.tTeam1, R.id.tTeam2, R.id.tTeam3, R.id.tTeam4, R.id.tTeam5, R.id.tTeam6, R.id.tTeam7, R.id.tTeam8, R.id.tTeam9, R.id.tTeam10 };
        for(int a = 0; a < ids.length; a++) {
            UIButton btn = _selected_teams.findViewById(ids[a]);
            if (btn != null) {
                try {
                    MolkkyTeam t =game.teams().get(a);
                    btn.text(t.name());
                    btn.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    btn.text("");
                    btn.setVisibility(View.GONE);
                }
            }
        }
    }

    public void userAddDialog () {
        if (_listener == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                            players.save();
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
        //void onPlayerAdd(String name);
    }
}

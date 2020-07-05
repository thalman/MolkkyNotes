package net.halman.molkkynotes;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class PlayersAdapter extends
        RecyclerView.Adapter<PlayersAdapter.MyViewHolder> {

    private Players _players;
    private Resources _resources;
    private MolkkyGame _game;
    private ArrayList<MolkkyPlayer> _visible_players = new ArrayList<>();
    private OnPlayerListener _click_listener;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View player_view;
        public OnPlayerListener on_player_click;

        public MyViewHolder(View v, OnPlayerListener l) {
            super(v);

            player_view = v;
            player_view.setOnClickListener(this);
            player_view.setOnLongClickListener(this);
            on_player_click = l;
        }

        @Override
        public void onClick(View view) {
            if (on_player_click != null) {
                TextView tv = view.findViewById(R.id.team);
                if (tv != null) {
                    MolkkyPlayer player = (MolkkyPlayer) tv.getTag();
                    on_player_click.onPlayerClick(player);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (on_player_click != null) {
                TextView tv = view.findViewById(R.id.team);
                if (tv != null) {
                    MolkkyPlayer player = (MolkkyPlayer) tv.getTag();
                    on_player_click.onPlayerLongClick(player);
                }
            }
            return true;
        }

    }

    public PlayersAdapter(Players players, Resources resources, MolkkyGame game, OnPlayerListener l)
    {
        _players = players;
        _click_listener = l;
        _resources = resources;
        _game = game;

        updatePlayerList();
    }

    public  void game(MolkkyGame aGame) {
        _game = aGame;
        notifyDataSetFilterChanged();
    }

    public void updatePlayerList()
    {
        HashMap<String, MolkkyPlayer> playerHashMap = new HashMap<>();

        if (_game != null) {
            for (MolkkyTeam team : _game.teams()) {
                for (MolkkyPlayer player : team.players()) {
                    playerHashMap.put(player.name(), player);
                }
            }
        }

        _visible_players.clear();
        for (int i = 0; i < _players.size(); i++) {
            MolkkyPlayer player = _players.get(i);
            if (! playerHashMap.containsKey(player.name())) {
                _visible_players.add(player);
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlayersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_player, parent, false);
        MyViewHolder vh = new MyViewHolder(v, _click_listener);
        return vh;
    }

    // Replace the  contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MolkkyPlayer p = _visible_players.get(position);
        TextView team = holder.player_view.findViewById(R.id.team);
        TextView score = holder.player_view.findViewById(R.id.score);
        if (p!=null && team != null && score != null) {
            team.setText(p.name());
            team.setTag(p);
            score.setText(_resources.getString(R.string.teamsAverage, p.averageScoreString()));
        }
    }

    @Override
    public int getItemCount() {
        return _visible_players.size();
    }

    public void notifyDataSetFilterChanged()
    {
        updatePlayerList();
        notifyDataSetChanged();
    }

    public interface OnPlayerListener {
        void onPlayerClick(MolkkyPlayer player);
        void onPlayerLongClick(MolkkyPlayer player);
    }
}

package net.halman.molkkynotes;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class PlayersAdapter extends
        RecyclerView.Adapter<PlayersAdapter.MyViewHolder> {

    private Players _players;
    private Resources _resources;
    private OnPlayerListener _click_listener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
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
                    String name;
                    name = tv.getText().toString();
                    Log.d("UI", "Player " +   name);
                    on_player_click.onPlayerClick(name);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (on_player_click != null) {
                TextView tv = view.findViewById(R.id.team);
                if (tv != null) {
                    String name;
                    name = tv.getText().toString();
                    Log.d("UI", "Player " +   name + " long click");
                    on_player_click.onPlayerLongClick(name);
                }
            }
            return true;
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlayersAdapter(Players players, Resources resources, OnPlayerListener l)
    {
        _players = players;
        _click_listener = l;
        _resources = resources;
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
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.playerView.team(mDataset[position]);
        MolkkyPlayer p = _players.get(position);
        TextView team = holder.player_view.findViewById(R.id.team);
        TextView score = holder.player_view.findViewById(R.id.score);
        if (p!=null && team != null && score != null) {
            team.setText(p.name());
            //Resources r = getActivity().getResources();
            score.setText(_resources.getString(R.string.teamsAverage, p.averageScoreString()));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return _players.size();
    }

    public interface OnPlayerListener {
        void onPlayerClick(String name);
        void onPlayerLongClick(String name);
    }
}

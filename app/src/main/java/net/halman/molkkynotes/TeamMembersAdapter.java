package net.halman.molkkynotes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TeamMembersAdapter extends RecyclerView.Adapter<TeamMembersAdapter.TeamMembersViewHolder> {

    private MolkkyGame _game;

    public static class TeamMembersViewHolder extends RecyclerView.ViewHolder {
        public View root;

        public TeamMembersViewHolder(View v) {
            super(v);
            root = v;
        }
    }

    public TeamMembersAdapter(MolkkyGame game) {
        _game = game;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TeamMembersAdapter.TeamMembersViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType)
    {
        View v = null;

        switch (viewType) {
            case 0:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ui_team_separator, parent, false);
                break;
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ui_player, parent, false);
                break;
        }

        TeamMembersAdapter.TeamMembersViewHolder vh = new TeamMembersViewHolder(v);
        return vh;
    }

    private MolkkyPlayer getPlayer(int position)
    {
        int teamidx = 0;
        ArrayList<MolkkyTeam> teams = _game.teams();

        while (true) {
            if (teamidx >= teams.size()) {
                return null;
            }

            MolkkyTeam t = teams.get(teamidx);
            if (position <= t.size()) {
                if (position == 0) {
                    return null;
                }
                return t.members().get(position - 1);
            }

            position -= (t.size() + 1);
            teamidx++;
        }
    }

    private MolkkyTeam getTeam(int position)
    {
        if (position >= 0 && position < _game.teams().size()) {
            return _game.teams().get(position);
        }

        return null;
    }

    private int getTeamIndex(int position)
    {
        int teamidx = 0;
        ArrayList<MolkkyTeam> teams = _game.teams();

        while (true) {
            if (teamidx >= teams.size()) {
                return -1;
            }

            MolkkyTeam t = teams.get(teamidx);
            if (position <= t.size()) {
                return teamidx;
            }

            position -= (t.size() + 1);
            teamidx++;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getPlayer(position) == null) {
            return 0; // title
        }

        return 1; // player
    }

    @Override
    public void onBindViewHolder(TeamMembersViewHolder holder, int position) {
        TextView T = holder.root.findViewById(R.id.team);
        if (T != null) {
            MolkkyTeam t = getTeam(position);
            if (t != null) {
                T.setText(t.name());
                holder.root.setTag(t);
            }

            return;
        }

        T = holder.root.findViewById(R.id.teamSeparatorTextView);
        if (T != null) {
            int idx = getTeamIndex(position);
            if (idx >= 0) {
                T.setText("team " + (idx + 1));
            }
        }
    }

    @Override
    public int getItemCount()
    {
        int items = 0;
        ArrayList<MolkkyTeam> teams = _game.teams();
        items = teams.size();
        for (MolkkyTeam team: teams) {
            items += team.size();
        }

        return items;
    }

    public void onMove(int oldPos, int newPos) {
//        String item = items.get(oldPos);
//        items.remove(oldPos);
//        items.add(newPos, item);
        MolkkyPlayer player = getPlayer(oldPos);
        if (player == null) {
            // not dragging player
            return;
        }

        // determine source and destination team
        int src_team_idx = getTeamIndex(oldPos);
        int dst_team_idx = getTeamIndex(newPos);
        {
            if (src_team_idx == dst_team_idx) {
                MolkkyTeam T = _game.teams().get(src_team_idx);
                MolkkyPlayer P = getPlayer(oldPos);
                if (T.getPlayersIndex(getPlayer(newPos)) == -1) {
                    --dst_team_idx;
                }
                if (dst_team_idx < 0) {
                    return;
                }
            }
        }

        // determine players position inthe team
        MolkkyTeam src_team = _game.teams().get(src_team_idx);
        MolkkyTeam dst_team = _game.teams().get(dst_team_idx);

        if (src_team_idx == dst_team_idx) {
            // move inside team
            int old_player_idx = src_team.getPlayersIndex(player);
            int new_player_idx = src_team.getPlayersIndex(getPlayer(newPos));
            src_team.removePlayer(old_player_idx);
            src_team.addMember(new_player_idx, player);
            notifyItemMoved(oldPos, newPos);
        } else {
            // move to another team
            int old_player_idx = src_team.getPlayersIndex(player);
            int new_player_idx = dst_team.getPlayersIndex(getPlayer(newPos));
            if (new_player_idx == -1) {
                if (newPos < oldPos) {
                    // moving up
                    new_player_idx = dst_team.size();
                } else {
                    // movin down
                    new_player_idx = 0;
                }
            }
            src_team.removePlayer(old_player_idx);
            dst_team.addMember(new_player_idx, player);
            notifyItemMoved(oldPos, newPos);
        }
    }
}

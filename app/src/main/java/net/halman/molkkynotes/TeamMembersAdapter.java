package net.halman.molkkynotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

// credit for improving recycler view https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-6a6f0c422efd

public class TeamMembersAdapter extends RecyclerView.Adapter<TeamMembersAdapter.TeamMembersViewHolder> {
    private Context _context;
    private MolkkyGame _game;
    private TeamMembersListener _listener;

    public static class TeamMembersViewHolder extends RecyclerView.ViewHolder {
        public View root;
        public View drag_handle;

        public TeamMembersViewHolder(View v) {
            super(v);
            root = v;
            drag_handle = v.findViewById(R.id.teamPlayerDragHandle);
        }
    }

    public TeamMembersAdapter(Context context, MolkkyGame game, TeamMembersListener listener)
    {
        _context = context;
        _game = game;
        _listener = listener;
    }

    public void setGame(MolkkyGame game)
    {
        _game = game;
    }

    boolean inTeamSetupMode()
    {
        if (_game != null) {
            return ! _game.gameStarted();
        }

        return false;
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
                        .inflate(R.layout.ui_team_player, parent, false);
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
                return t.players().get(position - 1);
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
    public void onBindViewHolder(final TeamMembersViewHolder holder, int position) {
        if (holder.drag_handle != null) {
            // this is player item
            holder.drag_handle.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && _listener != null) {
                            _listener.onTeamsStartDrag(holder);
                        }

                        return false;
                    }
                }
            );
            TextView T = holder.root.findViewById(R.id.teamPlayerText);
            if (T != null) {
                MolkkyPlayer p = getPlayer(position);
                if (p != null) {
                    String name = p.name();
                    T.setText(p.name());
                    holder.root.setTag(p);
                }

                return;
            }
        } else {
            // team separator
            TextView T = holder.root.findViewById(R.id.teamSeparatorTextView);
            if (T != null) {
                int idx = getTeamIndex(position);
                if (idx >= 0) {
                    T.setText(_context.getResources().getString(R.string.teamsTitle, (idx + 1)));
                }
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

    public void removeTrailingEmptyTeams()
    {
        if (!inTeamSetupMode()) {
            return;
        }

        ArrayList<MolkkyTeam> teams = _game.teams();
        while (teams.size() > 1) {
            MolkkyTeam last = teams.get(teams.size() - 1);
            if (last.players().size() == 0) {
                teams.remove(last);
            } else {
                return;
            }
        }
    }

    public void cleanupEmptyTeams()
    {
        if (!inTeamSetupMode()) {
            return;
        }

        // remove empty teams, put empty team at the end
        boolean notify = false;
        int idx = 0;
        ArrayList<MolkkyTeam> teams = _game.teams();
        while (idx < teams.size() - 1) {
            MolkkyTeam team = teams.get(idx);
            if (team.players().size() == 0) {
                teams.remove(idx);
                notify = true;
            } else {
                ++idx;
            }
        }

        if (teams.size() == 0) {
            teams.add(new MolkkyTeam());
            notify = true;
        }

        MolkkyTeam last = teams.get(teams.size() - 1);
        if (last.players().size() != 0) {
            teams.add(new MolkkyTeam());
            notify = true;
        }

        // reindex IDs
        for (idx = 0; idx < teams.size(); ++idx) {
            teams.get(idx).id(idx + 1);
        }

        if (notify) {
            notifyDataSetChanged();
            if (_listener != null) {
                _listener.onTeamsDataSetChanged();
            }
        }
    }

    public void onMove(int oldPos, int newPos)
    {
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
            src_team.addPlayer(new_player_idx, player);
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
                    // moving down
                    new_player_idx = 0;
                }
            }
            src_team.removePlayer(old_player_idx);
            dst_team.addPlayer(new_player_idx, player);
            notifyItemMoved(oldPos, newPos);
        }
    }

    public void onRemoveItem(int pos)
    {
        MolkkyPlayer player = getPlayer(pos);
        if (player == null) {
            // not dragging player
            // totify to get team separator back
            notifyDataSetChanged();
            return;
        }

        try {
            int team_idx = getTeamIndex(pos);
            MolkkyTeam team = _game.teams().get(team_idx);
            team.removePlayer(player);
            cleanupEmptyTeams();
            notifyDataSetChanged();
            if (_listener != null) {
                _listener.onTeamsDataSetChanged();
            }
        } catch(Exception e) {};
    }

    public interface  TeamMembersListener {
        void onTeamsStartDrag(RecyclerView.ViewHolder viewHolder);
        void onTeamsDataSetChanged();
    }
}

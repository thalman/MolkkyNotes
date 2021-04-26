package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.MolkkyTeam;
import net.halman.molkkynotes.R;

import java.util.ArrayList;

public class ItemsListDialog {
    public static final String[] getTeamList(Context context, MolkkyGame game, boolean include_new_team)
    {
        Resources resources = context.getResources();
        ArrayList<MolkkyTeam> teams = game.teams();
        int size = teams.size();
        if (size > 0 && !game.gameStarted()) {
            // check last team
            if (teams.get(size - 1).players().size() == 0) {
                --size;
            }
        }

        int new_team_space = (include_new_team ? 1 : 0);
        String[] items = new String[size + new_team_space];

        for (int i = 0; i < size; ++i) {
            items[i] = teams.get(i).name();
            if (items[i].length() == 0) {
                items[i] = resources.getString(R.string.teamsTitle, teams.get(i).id());
            }
        }

        if (include_new_team) {
            items[size] = resources.getString(R.string.dialogNewTeam);
        }
        return items;
    }

    public static ListView setItems(AlertDialog.Builder builder, Context context, String[] items) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialog_layout = inflater.inflate(R.layout.items_dialog, null);
        ListView listView = dialog_layout.findViewById(R.id.dialogListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.items_dialog_item, items);
        listView.setAdapter(adapter);
        builder.setView(dialog_layout);
        return listView;
    }
}

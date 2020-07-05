package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.MolkkyTeam;
import net.halman.molkkynotes.R;

import java.util.ArrayList;

public class TeamListDialog {
    public static final AlertDialog.Builder getBuilder(Context context, MolkkyGame game, boolean include_new_team, final OnTeamSelectedListener listener)
    {
        if (context == null || game == null || listener == null) {
            return null;
        }

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
        if (size + new_team_space < 2) {
            listener.onTeamSelected(0);
            return null;
        }

        CharSequence[] items = new CharSequence[size + new_team_space];

        for (int i = 0; i < size; ++i) {
            items[i] = teams.get(i).name();
            if (items[i].length() == 0) {
                items[i] = resources.getString(R.string.teamsTitle, teams.get(i).id());
            }
        }

        if (include_new_team) {
            items[size] = resources.getString(R.string.dialogNewTeam);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MolkkyAlertDialogStyle);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onTeamSelected(which);
                }

                dialog.dismiss();
            }
        });

        return builder;
    }

    public interface OnTeamSelectedListener {
        public void onTeamSelected(int which);
    }
}

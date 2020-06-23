package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

import net.halman.molkkynotes.MolkkyTeam;
import net.halman.molkkynotes.R;

import java.util.ArrayList;

public class TeamListDialog {
    public static final AlertDialog.Builder getBuilder(Context context, ArrayList<MolkkyTeam> teams, boolean include_new_team, final OnTeamSelectedListener listener) {
        Resources resources = context.getResources();
        int size = teams.size() + (include_new_team ? 1 : 0);
        CharSequence[] items = new CharSequence[size];

        for (int i = 0; i < teams.size(); ++i) {
            items[i] = teams.get(i).name();
        }

        if (include_new_team) {
            items[size - 1] = resources.getString(R.string.dialogNewTeam);
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

package net.halman.molkkynotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class Promotion {
    private final static String APP_ID = "net.halman.molkkynotes"; // Package Name
    private boolean _rate_provided = false;
    private boolean _support_provided = false;
    private int _rate_count_down = 3;
    private Context _context;

    public Promotion(Context context)
    {
        _context = context;
        loadConfig(_context);
    }

    private void loadConfig(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("promotion", 0);
        _rate_provided = prefs.getBoolean("rate-provided", false);
        _support_provided = prefs.getBoolean("support-provided", false);
        _rate_count_down = prefs.getInt("rate-count-down", 3);
    }

    private void saveConfig(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("promotion", 0);
        SharedPreferences.Editor editor = prefs.edit();
        if (editor != null) {
            editor.putBoolean("rate-provided", _rate_provided);
            editor.putBoolean("support-provided", _support_provided);
            editor.putInt("rate-count-down", _rate_count_down);
            editor.apply();
        }
    }

    private void rate(final Context context)
    {
        if (_rate_provided) {
            return;
        }

        if (_rate_count_down > 0) {
            _rate_count_down--;
        }

        if (_rate_count_down > 0) {
            saveConfig(context);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.promoteRate);
        builder.setMessage(R.string.promoteRateDetail);
        builder.setPositiveButton(R.string.promoteRateNow, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _rate_provided = true;
                saveConfig(context);
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_ID)));
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.promoteRateLater, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setNeutralButton(R.string.promoteRateNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _rate_provided = true;
                _rate_count_down = 7;
                saveConfig(context);
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                _rate_provided = false;
                _rate_count_down = 3;
                saveConfig(context);
            }
        });

        builder.show();
    }

    private void support(final Context context)
    {
        if (_support_provided) {
            return;
        }

        if (_rate_count_down > 0) {
            _rate_count_down--;
        }

        if (_rate_count_down > 0) {
            saveConfig(context);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.promoteSupport);
        builder.setMessage(R.string.promoteSupportDetail);
        builder.setPositiveButton(R.string.promoteSupportNow, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _rate_provided = true;
                saveConfig(context);
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_ID + "plus")));
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.promoteRateLater, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setNeutralButton(R.string.promoteRateNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _support_provided = true;
                saveConfig(context);
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                _rate_provided = false;
                _rate_count_down = 3;
                saveConfig(context);
            }
        });

        builder.show();
    }

    public void promote()
    {
        if (! _rate_provided) {
            rate(_context);
            return;
        }

        if (! _support_provided) {
            support(_context);
            return;
        }
    }
}

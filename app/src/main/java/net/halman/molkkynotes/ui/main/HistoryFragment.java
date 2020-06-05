package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.halman.molkkynotes.History;
import net.halman.molkkynotes.HistoryAdapter;
import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.R;
import net.halman.molkkynotes.ScoreExport;
import net.halman.molkkynotes.Setup;

import java.io.File;

public class HistoryFragment extends Fragment {
    private HistoryFragment.OnHistoryFragmentInteractionListener _listener;
    private RecyclerView _history_list_view;
    private UIGameRecord _game_record;
    private HistoryAdapter _history_adapter;
    private ImageView _close_button;
    private ImageView _fit_button;
    private ImageView _mail_button;
    private String _current_csv = "";

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GameFragment.
     */
    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View topView = inflater.inflate(R.layout.fragment_history, container, false);
        _history_list_view = topView.findViewById(R.id.historyItemsView);
        _game_record = topView.findViewById(R.id.historyGameRecord);
        _close_button = topView.findViewById(R.id.historyClose);
        _close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCloseHistory();
            }
        });
        _fit_button = topView.findViewById(R.id.historyFit);
        _fit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _game_record.fit();
            }
        });

        _mail_button = topView.findViewById(R.id.historyMail);
        _mail_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMailClick();
            }
        });

        _history_list_view.setVisibility(View.VISIBLE);
        _game_record.setVisibility(View.GONE);
        _close_button.setVisibility(View.GONE);
        _fit_button.setVisibility(View.GONE);
        _mail_button.setVisibility(View.GONE);

        if (_listener != null) {
            History history = _listener.history();
            _history_adapter = new HistoryAdapter(history, new HistoryAdapter.OnHistoryListener() {
                @Override
                public void onHistoryClick(View view) {
                    onHistory(view);
                }

                @Override
                public void onHistoryLongClick(View view) {
                    onHistoryLong(view);
                }
            });

            _history_list_view.setAdapter(_history_adapter);
            RecyclerView.LayoutManager L = new LinearLayoutManager(getContext());
            _history_list_view.setLayoutManager(L);
        }

        _game_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGameRecordClick();
            }
        });

        updateScreen();
        return topView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HistoryFragment.OnHistoryFragmentInteractionListener) {
            _listener = (HistoryFragment.OnHistoryFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHistoryInteractionListener");
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
            if (_listener != null) {
                _current_csv = _listener.historyOpenFile();
            }
            updateScreen();
        } else {
            if (_listener != null) {
                _listener.historySaveStatus(_current_csv);
            }
        }
    }

    public void onCloseHistory()
    {
        if (_listener == null) {
            return;
        }

        _current_csv = "";
        _listener.historySaveStatus(_current_csv);
        updateScreen();
    }

    public void notifyGameSaved()
    {
        if (_listener == null) {
            return;
        }

        _history_adapter.notifyDataSetChanged();
    }

    public void updateScreen() {
        if (_close_button == null) {
            // onCreateView not called yet
            return;
        }

        if (_current_csv.isEmpty()) {
            _close_button.setVisibility(View.GONE);
            _fit_button.setVisibility(View.GONE);
            _mail_button.setVisibility(View.GONE);
            _game_record.setVisibility(View.GONE);
            _history_list_view.setVisibility(View.VISIBLE);
        } else {
            MolkkyGame game = new MolkkyGame();
            game.CSVImport(_current_csv);
            _game_record.game(game);
            _game_record.whatToDraw(UIGameRecord.DRAW_GAME);
            _game_record.setVisibility(View.VISIBLE);
            _game_record.fit();
            _close_button.setVisibility(View.VISIBLE);
            _fit_button.setVisibility(View.VISIBLE);
            _mail_button.setVisibility(View.VISIBLE);
            _history_list_view.setVisibility(View.GONE);
        }
    }

    private void onHistory(View view)
    {
        if (_listener == null) {
            return;
        }

        String file = (String) view.getTag();
        if (! _current_csv.equals(file)) {
            _current_csv = file;
            _listener.historySaveStatus(_current_csv);
            updateScreen();
        }
    }

    private void onHistoryLong(View view) {
        final String item = (String) view.getTag();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.historyDelete);
        builder.setMessage(R.string.historyDeleteDetail);
        builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    File file = new File(item);
                    file.delete();
                    if (_listener != null) {
                        _listener.history().reload();
                    }
                } catch (Exception e) {}
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

    private void onGameRecordClick()
    {
        _game_record.setVisibility(View.GONE);
        _history_list_view.setVisibility(View.VISIBLE);
    }

    private void onMailClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.resultsExportAs);
        builder.setItems(R.array.historyExports, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // export CSV
                        exportCSV();
                        break;
                    case 1:
                        // export JPG
                        exportJPG();
                        break;
                }
            }
        });
        builder.show();
    }

    private void exportCSV()
    {
        if (_current_csv.isEmpty()) {
            return;
        }

        try {
            File file = new File(_current_csv);
            String provider = getContext().getApplicationContext().getPackageName() + ".provider";
            Uri uri = FileProvider.getUriForFile(getContext(), provider, file);

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/csv");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.historyShare)));
        } catch (Exception e) {}
    }

    private void deleteTempContent() {
        try {
            File dir = getContext().getExternalFilesDir("tmp");
            File[] files = dir.listFiles();
            if (files != null) { //some JVMs return null for empty dirs
                for (File f : files) {
                    if (!f.isDirectory()) {
                        f.delete();
                    }
                }
            }
        } catch (Exception e) {};
    }

    private void exportJPG()
    {
        if (_current_csv.isEmpty()) {
            return;
        }

        try {
            deleteTempContent();
            MolkkyGame game = new MolkkyGame();
            game.CSVImport(_current_csv);

            File dir = getContext().getExternalFilesDir("tmp");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, "molkky.jpg");
            ScoreExport exporter = new ScoreExport(getContext());
            exporter.jpeg(game, file.getAbsolutePath());

            String provider = getContext().getApplicationContext().getPackageName() + ".provider";
            Uri uri = FileProvider.getUriForFile(getContext(), provider, file);

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.historyShare)));
        } catch (Exception e) {}
    }

    private void exportPdf()
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            PdfDocument document = new PdfDocument();
        } else {
            // TODO: show warning
        }
    }

    public interface OnHistoryFragmentInteractionListener {
        History history();
        Setup setup();
        String historyOpenFile();
        void historySaveStatus(String file);
    }
}

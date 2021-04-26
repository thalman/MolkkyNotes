package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import net.halman.molkkynotes.History;
import net.halman.molkkynotes.HistoryAdapter;
import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.R;
import net.halman.molkkynotes.ScoreExport;

import java.io.File;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnHistoryListener {
    private HistoryFragment.OnHistoryFragmentInteractionListener _listener;
    private RecyclerView _history_list_view;
    private UIGameRecord _game_record;
    private HistoryAdapter _history_adapter;
    private ImageView _close_button;
    private ImageView _fit_button;
    private ImageView _mail_button;
    private ImageView _trash_button;
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
    public synchronized View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
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

        _trash_button = topView.findViewById(R.id.historyTrash);
        _trash_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTrashClick();
            }
        });

        _history_list_view.setVisibility(View.VISIBLE);
        _game_record.setVisibility(View.GONE);
        _close_button.setVisibility(View.GONE);
        _fit_button.setVisibility(View.GONE);
        _mail_button.setVisibility(View.GONE);
        _trash_button.setVisibility(View.GONE);

        if (_listener != null) {
            History history = _listener.history();
            _history_adapter = new HistoryAdapter(history, this);
            _history_list_view.setAdapter(_history_adapter);
            ItemTouchHelper _history_touch_helper = new ItemTouchHelper(createItemTouchHelper(_history_adapter));
            _history_touch_helper.attachToRecyclerView(_history_list_view);

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
    public synchronized void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HistoryFragment.OnHistoryFragmentInteractionListener) {
            _listener = (HistoryFragment.OnHistoryFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHistoryInteractionListener");
        }
    }

    @Override
    public synchronized void onDetach() {
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

    private ItemTouchHelper.Callback createItemTouchHelper(final HistoryAdapter adapter) {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.onRemove(viewHolder.getAdapterPosition());
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {

                final int dragFlags = 0;
                final int swipeFlags = ItemTouchHelper.LEFT;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

        };
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

    public void onTrashClick()
    {
        if (_listener == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.historyDelete);
        builder.setMessage(R.string.historyDeleteDetail);
        builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    File file = new File(_current_csv);
                    file.delete();
                    if (_listener != null) {
                        _listener.history().reload();
                    }
                    onCloseHistory();
                } catch (Exception ignored) {}
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

    public synchronized void notifyGameSaved()
    {
        if (_listener == null) {
            return;
        }

        if (_history_adapter != null) {
            _history_adapter.notifyDataSetChanged();
        }
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
            _trash_button.setVisibility(View.GONE);
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
            _trash_button.setVisibility(View.VISIBLE);
            _history_list_view.setVisibility(View.GONE);
        }
    }

    public void onHistoryClick(View view)
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

    public void onHistoryRemove(int idx)
    {
        if (_listener == null) {
            return;
        }

        try {
            confirmDelete(_listener.history().getPath(idx));
        } catch (Exception ignored) {}
    }

    public void onHistoryLongClick(View view)
    {
        final String item = (String) view.getTag();

        try {
            confirmDelete(item);
        } catch (Exception ignore) {}
    }

    public void confirmDelete(final String file_name)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.historyDelete);
        builder.setMessage(R.string.historyDeleteDetail);
        builder.setPositiveButton(R.string.dOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    File file = new File(file_name);
                    file.delete();
                    if (_listener != null) {
                        _listener.history().reload();
                    }
                } catch (Exception ignored) {}
            }
        });

        builder.setNegativeButton(R.string.dCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                _history_adapter.notifyDataSetChanged();
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
        builder.setTitle(R.string.historyShare);

        String [] items = getResources().getStringArray(R.array.historyExports);
        ListView listView = ItemsListDialog.setItems(builder, getContext(), items);
        final AlertDialog ad = builder.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // export JPG
                        exportJPG();
                        break;
                    case 1:
                        // export CSV
                        exportCSV();
                        break;
                    case 2:
                        // print
                        printJPG();
                        break;
                }

                ad.dismiss();
            }
        });
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
        } catch (Exception ignored) {}
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
        } catch (Exception ignore) {}
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
            exporter.recordJpeg(game, file.getAbsolutePath());

            String provider = getContext().getApplicationContext().getPackageName() + ".provider";
            Uri uri = FileProvider.getUriForFile(getContext(), provider, file);

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.historyShare)));
        } catch (Exception ignored) {}
    }

    private void printJPG()
    {
        if (_current_csv.isEmpty()) {
            return;
        }

        try {
            deleteTempContent();
            MolkkyGame game = new MolkkyGame();
            game.CSVImport(_current_csv);

            PrintHelper printer = new PrintHelper(getContext());
            printer.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            ScoreExport export = new ScoreExport(getContext());
            Bitmap sheet = export.recordBitmap(game);
            printer.printBitmap("Molkky match record", sheet);
        } catch (Exception ignored) {}
    }

    public interface OnHistoryFragmentInteractionListener {
        History history();
        String historyOpenFile();
        void historySaveStatus(String file);
    }
}

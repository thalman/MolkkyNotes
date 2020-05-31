package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.halman.molkkynotes.History;
import net.halman.molkkynotes.HistoryAdapter;
import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.R;
import net.halman.molkkynotes.Setup;

public class HistoryFragment extends Fragment {
    private HistoryFragment.OnHistoryFragmentInteractionListener _listener;
    private RecyclerView _history_list_view;
    private UIGameRecord _game_record;
    private HistoryAdapter _history_adapter;

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
        if (_listener != null) {
            History history = _listener.history();
            _history_adapter = new HistoryAdapter(history, new HistoryAdapter.OnHistoryListener() {
                @Override
                public void onHistoryClick(View view) {
                    onHistory(view);
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
            updateScreen();
        }
    }

    public void notifyGameSaved()
    {
        if (_listener == null) {
            return;
        }

        _history_adapter.notifyDataSetChanged();
    }

    public void updateScreen() {
        updateHistoryList();
    }

    private void updateHistoryList()
    {
        if (_listener == null) {
            return;
        }

//        File dir = getContext().getExternalFilesDir("history");
//        File[] files = dir.listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                return name.matches(".*\\.csv");
//            }
//        });
//
//        for(File F: files) {
//            TextView t = new TextView(getContext());
//            t.setText(F.toString());
//            // _history_list_view.addView(t);
//        }
//        Log.d("FF", files.toString());
    }

    private void onHistory(View view)
    {
        String item = (String) view.getTag();
        //TODO: save/read setup from CSV
        MolkkyGame game = new MolkkyGame();
        game.setup(_listener.setup());
        game.CSVImport(item);
        _game_record.game(game);
        _game_record.setVisibility(View.VISIBLE);
        _history_list_view.setVisibility(View.GONE);
    }

    private void onGameRecordClick()
    {
        _game_record.setVisibility(View.GONE);
        _history_list_view.setVisibility(View.VISIBLE);
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
    }
}

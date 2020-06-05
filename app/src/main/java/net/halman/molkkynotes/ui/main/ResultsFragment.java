package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.R;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResultsFragment.OnResultsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResultsFragment extends Fragment {
    private OnResultsFragmentInteractionListener _listener;
    private UIButton _next_round = null;
    private UIButton _game_over = null;
    private UIGameRecord _round_score = null;
    private ImageView _fit_score = null;

    public ResultsFragment() {
        // Required empty public constructor
    }

    public static ResultsFragment newInstance() {
        return new ResultsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_results, container, false);
        _next_round = v.findViewById(R.id.resultNextRound);
        _next_round.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onNextRound();
                }
            }
        );

        _game_over = v.findViewById(R.id.resultGameOver);
        _game_over.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onGameOver();
                    }
                }
        );

        _round_score = v.findViewById(R.id.resultRoundScore);
        _round_score.whatToDraw(UIGameRecord.DRAW_CURRENT_ROUND);

        _fit_score = v.findViewById(R.id.resultFit);
        _fit_score.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        _round_score.fit();
                    }
                }
        );

        updateScreen();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnResultsFragmentInteractionListener) {
            _listener = (OnResultsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnResultsFragmentInteractionListener");
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

    public void updateScreen()
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame game = _listener.game();
        _round_score.game(game.gameStarted() ? game : null);

        if (game.roundOver() && game.gameStarted()) {
            _next_round.active(true);
            _game_over.active(true);
        } else {
            _next_round.active(false);
            _game_over.active(false);
        }
    }

    private void roundInProgressInfoDialog()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle).create();
        alertDialog.setTitle(R.string.resultsRoundInProgress);
        alertDialog.setMessage(getString(R.string.resultsRoundInProgressDetail));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dOK),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        alertDialog.show();

    }

    private void onNextRound()
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame g = _listener.game();
        if (! g.roundOver()) {
            roundInProgressInfoDialog();
            return;
        }

        g.startNextRound();
        _listener.switchTab(1);
    }

    private void onGameOver() {
        if (_listener == null) {
            return;
        }

        MolkkyGame g = _listener.game();

        File dir = getContext().getExternalFilesDir("history");
        File path = new File(dir, g.dateAsString() + ".csv");

        g.CSVExport(path.toString());
        _listener.historySaveStatus(path.getAbsolutePath());
        _listener.notifyGameOver();
        _listener.switchTab(3);
        updateScreen();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnResultsFragmentInteractionListener {
        MolkkyGame game();
        void switchTab(int tab);
        void notifyGameOver();
        void historySaveStatus(String file);
    }
}

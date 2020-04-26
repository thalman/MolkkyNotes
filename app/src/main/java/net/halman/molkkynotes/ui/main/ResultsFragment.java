package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.halman.molkkynotes.MainActivity;
import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.MolkkyRound;
import net.halman.molkkynotes.MolkkyTeam;
import net.halman.molkkynotes.R;

import java.util.ArrayList;

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
    private TextView _game_score = null;
    private TextView _round_score = null;

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
                    startNextRound();
                }
            }
        );

        _round_score = v.findViewById(R.id.resultRoundScore);
        _game_score = v.findViewById(R.id.resultGameScore);
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

        MolkkyTeam t;
        MolkkyGame game = _listener.game();

        if (game.roundStarted()) {
            ArrayList<MolkkyTeam> teams = game.roundTeamOrder();
            String text = "";
            for (int i = 0; i < teams.size(); i++) {
                t = teams.get(i);
                text += game.roundTeamScore(t) + " - " + t.name() + "\n          " + game.roundTeamScoreAsString(t) + "\n";
            }

            _round_score.setText(text);
        } else {
            // _round_score.setText("");
        }

        if (game.gameStarted()) {
            ArrayList<MolkkyTeam> teams = game.gameTeamOrder();
            String text = "";
            for (int i = 0; i < teams.size(); i++) {
                t = teams.get(i);
                int score = game.gameTeamScore(t);
                String scoreString = game.gameTeamScoreAsString(t);
                int zeros = game.gameNumberOfZeros(t);
                String zerosString = zeros == 0 ? getString(R.string.resultsNoZeros)
                        : getResources().getQuantityString(R.plurals.resultsZeros, zeros, zeros);

                text += score + " - " + t.name() + "\n          " + scoreString + " "
                        +  zerosString + "\n";
            }

            _game_score.setText(text);
        } else {
            // _game_score.setText("");
        }
    }

    private void roundInProgressInfoDialog()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
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

    private void startNextRound()
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
    }
}

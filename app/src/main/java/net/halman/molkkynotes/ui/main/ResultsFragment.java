package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.halman.molkkynotes.MolkkyGame;
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
    private UIButton _game_over = null;
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

    private void updateRoundScore()
    {
        MolkkyTeam t;
        MolkkyGame game = _listener.game();

        if (game.roundStarted()) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ArrayList<MolkkyTeam> teams = game.roundTeamOrder();
            String text = "";
            int start = 0;

            for (int i = 0; i < teams.size(); i++) {
                t = teams.get(i);

                text = game.roundTeamScore(t) + " - " + t.name() + "\n";
                ssb.append(text);
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.2f), start,start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start += text.length();

                text = "     (" + game.roundTeamScoreAsString(t) + ")\n";
                ssb.append(text);
                start += text.length();

                text = " \n";
                ssb.append(text);
                ssb.setSpan(new RelativeSizeSpan(0.4f), start,start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start += text.length();
            }

            _round_score.setText(ssb, TextView.BufferType.SPANNABLE);
        } else {
            _round_score.setText("");
        }
    }

    private void updateGameScore() {
        MolkkyTeam t;
        MolkkyGame game = _listener.game();

        if (game.gameStarted()) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ArrayList<MolkkyTeam> teams = game.gameTeamOrder();
            String text;
            int start = 0;

            for (int i = 0; i < teams.size(); i++) {
                t = teams.get(i);
                int score = game.gameTeamScore(t);
                String scoreString = game.gameTeamScoreAsString(t);
                int zeros = game.gameNumberOfZeros(t);
                String zerosString = zeros == 0 ? getString(R.string.resultsNoZeros)
                        : getResources().getQuantityString(R.plurals.resultsZeros, zeros, zeros);

                text = score + " - " + t.name() + "\n";
                ssb.append(text);
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.2f), start,start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start += text.length();

                text = "        (" + scoreString + ", " + zerosString + ")\n";
                ssb.append(text);
                start += text.length();

                text = " \n";
                ssb.append(text);
                ssb.setSpan(new RelativeSizeSpan(0.4f), start,start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start += text.length();
            }

            _game_score.setText(ssb, TextView.BufferType.SPANNABLE);
        } else {
            _game_score.setText("");
        }
    }

    public void updateScreen()
    {
        if (_listener == null) {
            return;
        }

        MolkkyTeam t;
        MolkkyGame game = _listener.game();

        updateRoundScore();
        updateGameScore();

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
        g.save(getContext(),g.dateAsString() + ".csv");
        _listener.switchTab(3);
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

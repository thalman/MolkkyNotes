package net.halman.molkkynotes.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.MolkkyHit;
import net.halman.molkkynotes.MolkkyPlayer;
import net.halman.molkkynotes.MolkkyRound;
import net.halman.molkkynotes.MolkkyTeam;
import net.halman.molkkynotes.R;

import java.util.ArrayList;

public class GameFragment extends Fragment {
    private OnGameFragmentInteractionListener _listener;
    private ImageView _setup = null;
    private TextView _current_player = null;
    private TextView _next_player = null;
    private TextView _current_score = null;
    private TextView _current_hit = null;
    private TextView _current_round = null;
    private TextView _setup_molkky = null;
    private LinearLayout _next_player_layout = null;
    private UIButton[] _buttons = null;

    public GameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GameFragment.
     */
    public static GameFragment newInstance() {
        return new GameFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public synchronized View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View topView = inflater.inflate(R.layout.fragment_game, container, false);

        int [] buttons = {R.id.gButton0, R.id.gButton1, R.id.gButton2,
                R.id.gButton3, R.id.gButton4, R.id.gButton5, R.id.gButton6, R.id.gButton7,
                R.id.gButton8, R.id.gButton9, R.id.gButton10, R.id.gButton11, R.id.gButton12,
                R.id.gButtonFoul, R.id.gBack, R.id.gForward};
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(view.getId());
            }
        };

        _buttons = new UIButton[buttons.length];
        for(int i = 0; i < buttons.length; i++) {
            View v = topView.findViewById(buttons[i]);
            v.setOnClickListener(listener);
            _buttons[i] = (UIButton) v;
        }

        _setup = topView.findViewById(R.id.gameSetup);
        _current_player = topView.findViewById(R.id.currentPlayer);
        _next_player = topView.findViewById(R.id.gNextPlayer);
        _next_player_layout = topView.findViewById(R.id.gNextLayout);
        _current_score = topView.findViewById(R.id.currentScore);
        _current_hit = topView.findViewById(R.id.currentPoints);
        _current_round  = topView.findViewById(R.id.gRound);
        _setup_molkky  = topView.findViewById(R.id.setupMolkky);

        updateScreen();
        return topView;
    }

    @Override
    public synchronized void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGameFragmentInteractionListener) {
            _listener = (OnGameFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
            if (_listener == null) {
                return;
            }
            MolkkyGame game = _listener.game();
            if (!game.gameStarted()) {
                if (game.teams().size() > 1) {
                    int cursor = game.roundCursor();
                    game.start();
                    if (cursor != -1) {
                        game.nextHit();
                    }
                }
            }
            updateScreen();
        }
    }

    private String zerosWarning()
    {
        if (_listener == null) {
            return "";
        }

        MolkkyGame game = _listener.game();
        MolkkyTeam team = game.currentTeam();
        switch (game.teamHealth(team)) {
            case MolkkyRound.ZERO:
                return getResources().getQuantityString(R.plurals.resultsZeros, 1, 1);
            case MolkkyRound.TWOZEROS:
                if (game.hit().hit() == MolkkyHit.NOTPLAYED) {
                    Context context = getContext();
                    if (context != null) {
                        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            v.vibrate(300);
                        }
                    }
                }
                return getResources().getQuantityString(R.plurals.resultsZeros, 2, 2);
            default:
                return "";
        }
    }

    private void updateNextPlayer()
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame game = _listener.game();
        int offset = 1;

        MolkkyTeam team = game.nextTeam(offset);
        if (team == null) {
            _next_player.setText("");
            return;
        }

        while (game.teamHealthUpToCursor(team) == MolkkyRound.OUT && offset <= game.teams().size()) {
            offset++;
            team = game.nextTeam(offset);
        }

        ArrayList<MolkkyPlayer> players = game.inTurnTeamMembers(team, offset);
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        int start = 0;

        String text = getString(R.string.gNext);
        ssb.append(text);
        start += text.length();

        int points = game.roundTeamScore(team);
        if (players.size() > 0) {
            text = " " + players.get(0).name() + " (" + points + "/" + (game.goal() - points) + ")";
        } else {
            text = " " + getString(R.string.teamsTitle, team.id()) + " (" + points + "/" + (game.goal() - points) + ")";
        }
        ssb.append(text);
        ssb.setSpan(new RelativeSizeSpan(1.2f), start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // start += text.length();

        if (players.size() > 1) {
            StringBuilder textb = new StringBuilder();
            for (int i = 1; i < players.size(); ++i) {
                textb.append(", ").append(players.get(i).name());
            }

            ssb.append(textb);
            // start += textb.length();
        }
        _next_player.setText(ssb, TextView.BufferType.SPANNABLE);
    }

    private void updateCurrentPlayer()
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame game = _listener.game();
        MolkkyTeam team = game.currentTeam();
        if (team == null || team.size() == 0) {
            _current_player.setText(getString(R.string.teamsTitle, team.id()));
            return;
        }

        ArrayList<MolkkyPlayer> members = game.inTurnTeamMembers(team, 0);
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        int start = 0;

        String text =  members.get(0).name();
        ssb.append(text);
        start += text.length();


        if (members.size() > 1) {
            StringBuilder textb = new StringBuilder();
            for (int i = 1; i < members.size(); ++i) {
                textb.append(", ").append(members.get(i).name());
            }

            ssb.append(textb);
            ssb.setSpan(new RelativeSizeSpan(0.83f), start, start + textb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // start += textb.length();
        }
        _current_player.setText(ssb, TextView.BufferType.SPANNABLE);
    }

    public void updateScreen()
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame game = _listener.game();

        if (game.numberOfValidTeams() < 2) {
            while (game.roundCursor() != -1) {
                game.prevHit();
            }
        }

        if (game.roundCursor() == -1) {
            _setup.setVisibility(View.VISIBLE);
            _current_player.setVisibility(View.GONE);
            _current_score.setVisibility(View.GONE);
            _current_hit.setVisibility(View.GONE);
            _next_player_layout.setVisibility(View.GONE);
            _setup_molkky.setVisibility(View.VISIBLE);
            for (int i = 0; i < _buttons.length - 1; i++) {
                _buttons[i].active(false);
            }
            _buttons[_buttons.length - 1].active(game.teams().size()  > 1);
        } else {
            _setup.setVisibility(View.GONE);
            _current_player.setVisibility(View.VISIBLE);
            _current_score.setVisibility(View.VISIBLE);
            _current_hit.setVisibility(View.VISIBLE);
            _next_player_layout.setVisibility(View.VISIBLE);
            _setup_molkky.setVisibility(View.GONE);
            for (UIButton button : _buttons) {
                button.active(true);
            }

            updateNextPlayer();
            updateCurrentPlayer();

            MolkkyTeam t;
            t = game.currentTeam();
            if (t != null) {
                int points = game.roundTeamScore(t);
                int left = game.goal() - points;
                String text;
                String warning = zerosWarning();
                if (warning.isEmpty()) {
                    text = getString(R.string.gPlayersScore, points, left);
                } else {
                    text = getString(R.string.gPlayersScoreWithWarning, points, left, warning);
                }

                _current_score.setText(text);
                switch (game.hit().hit()) {
                    case MolkkyHit.NOTPLAYED:
                        _current_hit.setText("?");
                        break;
                    case MolkkyHit.LINECROSS:
                        _current_hit.setText(getText(R.string.gFoul));
                        break;
                    default:
                        _current_hit.setText(getString(R.string.gCurrentHit, game.hit().hit()));
                        break;
                }
                _current_round.setText(getString(R.string.gRoundProgress,(game.round() + 1), (game.roundProgress() + 1)));
            } else {
                _current_player.setText("");
                _current_score.setText("");
                _current_hit.setText("");
                _current_round.setText("");
            }
        }
    }

    public void hitChangeDialog (final int new_value)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MolkkyAlertDialogStyle);
        builder.setTitle(R.string.gameChangeTheHit);
        builder.setMessage(R.string.gameChangeTheHitDetail);
        builder.setPositiveButton(getString(R.string.dOK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (_listener == null) {
                    return;
                }

                MolkkyGame game = _listener.game();
                game.hit(new_value);
                updateScreen();
            }
        });

        builder.setNegativeButton(getString(R.string.dCancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    public void setGameHit(int value)
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame game = _listener.game();


        if (game.hit().hit() == MolkkyHit.NOTPLAYED) {
            game.removeTrailingEmptyTeams();
            game.hit(value);
            updateScreen();
            _listener.scheduleForwardMove();
        } else {
            hitChangeDialog(value);
        }
    }

    public void gameStepBack()
    {
        if (_listener == null) {
            return;
        }

        _listener.game().prevHit();
        updateScreen();
    }

    public synchronized void gameStepForward()
    {
        if (_listener == null) {
            return;
        }

        MolkkyGame game = _listener.game();
        if (game.teams().size() < 2) {
            updateScreen();
            return;
        }

        if (game.roundCursor() == -1) {
            game.nextHit();
            updateScreen();
            return;
        }

        if (game.hit().hit() == MolkkyHit.NOTPLAYED) {
            return;
        }

        if (game.roundCursorAtTheEnd() && game.roundOver()) {
            _listener.switchTab(2);
            return;
        }

        game.nextHit();
        updateScreen();
    }

    private void onButtonClick(int button) {
        if (_listener == null) {
            return;
        }

        _listener.cancelForwardMove();
        
        if (button == R.id.gButton0) {
            setGameHit(0);
        } else if (button == R.id.gButton1) {
            setGameHit(1);
        } else if (button == R.id.gButton2) {
            setGameHit(2);
        } else if (button ==  R.id.gButton3) {
            setGameHit(3);
        } else if (button ==  R.id.gButton4) {
            setGameHit(4);
        } else if (button ==  R.id.gButton5) {
            setGameHit(5);
        } else if (button ==  R.id.gButton6) {
            setGameHit(6);
        } else if (button ==  R.id.gButton7) {
            setGameHit(7);
        } else if (button ==  R.id.gButton8) {
            setGameHit(8);
        } else if (button ==  R.id.gButton9) {
            setGameHit(9);
        } else if (button ==  R.id.gButton10) {
            setGameHit(10);
        } else if (button ==  R.id.gButton11) {
            setGameHit(11);
        } else if (button ==  R.id.gButton12) {
            setGameHit(12);
        } else if (button ==  R.id.gButtonFoul) {
            setGameHit(MolkkyHit.LINECROSS);
        } else if (button ==  R.id.gBack) {
            gameStepBack();
        } else if (button ==  R.id.gForward) {
            gameStepForward();
        }
    }

    public interface OnGameFragmentInteractionListener {
        MolkkyGame game();
        void switchTab(int tab);
        void scheduleForwardMove();
        void cancelForwardMove();
    }
}

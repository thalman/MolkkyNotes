package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.halman.molkkynotes.MolkkyGame;
import net.halman.molkkynotes.R;

public class HistoryFragment extends Fragment {
    private HistoryFragment.OnHistoryFragmentInteractionListener _listener;

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

    public interface OnHistoryFragmentInteractionListener {
        MolkkyGame game();
        void switchTab(int tab);
    }
}

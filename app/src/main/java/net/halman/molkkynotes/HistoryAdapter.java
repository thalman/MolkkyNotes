package net.halman.molkkynotes;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HistoryAdapter extends
            RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private History _history;
    private OnHistoryListener _click_listener;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View history_view;
        public OnHistoryListener on_history_click;

        public MyViewHolder(View v, OnHistoryListener l) {
            super(v);

            history_view = v;
            history_view.setOnClickListener(this);
            history_view.setOnLongClickListener(this);
            on_history_click = l;
        }

        @Override
        public void onClick(View view) {
            if (on_history_click != null) {
                on_history_click.onHistoryClick(view);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (on_history_click != null) {
                on_history_click.onHistoryLongClick(view);
            }

            return true;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(History history, OnHistoryListener l) {
        _click_listener = l;
        _history = history;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_history_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v, _click_listener);
        return vh;
    }

    // Replace the  contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String item = _history.getName(position);
        TextView text = holder.history_view.findViewById(R.id.historyNameOfTheGame);
        text.setText(item);
        holder.history_view.setTag(_history.getPath(position));
    }

    // Return the size of dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (_history == null) {
            return 0;
        }

        return _history.size();
    }

    public void onRemove(int idx)
    {
        if (_click_listener != null) {
            _click_listener.onHistoryRemove(idx);
        }
    }

    public interface OnHistoryListener {
        void onHistoryClick(View view);
        void onHistoryLongClick(View view);
        void onHistoryRemove(int idx);
    }
}

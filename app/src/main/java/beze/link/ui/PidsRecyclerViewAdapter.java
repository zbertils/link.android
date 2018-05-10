package beze.link.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import beze.link.Globals;
import beze.link.R;
import beze.link.obd2.ParameterIdentification;

public class PidsRecyclerViewAdapter extends RecyclerView.Adapter<PidsRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private List<ParameterIdentification> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView pidName;
        public TextView pidDescription;
        public CheckBox pidEnabledCheckbox;

        public ViewHolder(View v) {
            super(v);

            pidName = (TextView) v.findViewById(R.id.textViewPidName);
            pidDescription = (TextView) v.findViewById(R.id.textViewPidDescription);
            pidEnabledCheckbox = (CheckBox) v.findViewById(R.id.checkBoxEnablePid);
        }


    }

    public void onClick (View view) {
        ParameterIdentification pid = (ParameterIdentification) view.getTag();
        pid.LogThisPID = ((CheckBox) view).isChecked();

        // if the pid is to be logged then add it to the list, otherwise remove it
        if (pid.LogThisPID) {
            Globals.shownPids.add(pid);
            Globals.appState.LastSelectedPids.add(pid.PID);
        }
        else if (Globals.shownPids.contains(pid)) {
            Globals.shownPids.remove(pid);

            // the pids have to be removed by iteration, there is no removal by value
            // because java thinks any integer value is removal by index
            for (int i = 0; i < Globals.appState.LastSelectedPids.size(); i++) {
                if (Globals.appState.LastSelectedPids.get(i) == pid.PID) {
                    Globals.appState.LastSelectedPids.remove(i);
                }
            }
        }
    }

    public PidsRecyclerViewAdapter(List<ParameterIdentification> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public PidsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.pids_recycler_row, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ParameterIdentification pid = mDataset.get(position);
        holder.pidName.setText(pid.Name);
        holder.pidDescription.setText(pid.Description);

        // the checkbox is somewhat special, it will set the PID state based on user interaction
        holder.pidEnabledCheckbox.setOnClickListener(this);
        holder.pidEnabledCheckbox.setChecked(pid.LogThisPID);
        holder.pidEnabledCheckbox.setTag(pid);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

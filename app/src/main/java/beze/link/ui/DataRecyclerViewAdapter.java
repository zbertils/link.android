package beze.link.ui;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import com.android.beze.link.R;

import beze.link.Globals;
import beze.link.obd2.ParameterIdentification;
import beze.link.obd2.Protocols;

public class DataRecyclerViewAdapter extends RecyclerView.Adapter<DataViewHolder> {

    private static final String TAG = Globals.TAG_BASE + ".DataRecycler";

    private List<ParameterIdentification> mDataset;
    private boolean showPidValue;
    private boolean showGraphs;

    public DataRecyclerViewAdapter(List<ParameterIdentification> myDataset, boolean showPidValue, boolean showGraphs) {
        mDataset = myDataset;
        this.showPidValue = showPidValue;
        this.showGraphs = showGraphs;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.data_recycler_row, parent, false);
        return new DataViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        ParameterIdentification pid = mDataset.get(position);
        holder.pidName.setText(pid.getShortName());

        // if the last decoded value exists then show it
        if (!Double.isNaN(pid.LastDecodedValue()))
        {
            holder.pidDecodedValue.setText(String.format("%.2f", pid.LastDecodedValue()));
        }
        else
        {
            holder.pidDecodedValue.setText("NaN");
        }
        holder.pidUnits.setText(pid.Units);

        // hide the text if the user does not want to see them
        if (!showPidValue || Globals.cable == null) {
            holder.pidValue.setVisibility(View.INVISIBLE);
        }
        else {
            holder.pidValue.setVisibility(View.VISIBLE);
            holder.pidValue.setText(((pid.Header != null && !Protocols.IsCan(Globals.cable.Protocol)) ? pid.Header : "") + pid.Pack(Globals.cable.Protocol));
        }

        if (showGraphs)
        {
            // show the graph, remove previous series, and add the newly updated series
            holder.pidGraph.setVisibility(View.VISIBLE);

            // update the graph value series
            holder.graphSeries.appendData(pid);
        }
        else
        {
            holder.pidGraph.setVisibility(View.GONE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataset != null)
        {
            return mDataset.size();
        }

        return 0;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView view)
    {
        super.onAttachedToRecyclerView(view);
    }
}

package beze.link.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.beze.link.R;

import java.util.List;
import java.util.Map;

import beze.link.obd2.DiagnosticTroubleCode;

public class DtcStatusRecyclerViewAdapter extends RecyclerView.Adapter<DtcStatusRecyclerViewAdapter.ViewHolder> {

    private List<Map.Entry<DiagnosticTroubleCode, String>> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView dtcNumber;
        public TextView dtcDescription;
        public TextView dtcType;

        public ViewHolder(View v) {
            super(v);

            dtcNumber = (TextView) v.findViewById(R.id.textViewDtcNumber);
            dtcDescription = (TextView) v.findViewById(R.id.textViewDtcDescription);
            dtcType = (TextView) v.findViewById(R.id.textViewDtcType);
        }
    }

    public DtcStatusRecyclerViewAdapter(List<Map.Entry<DiagnosticTroubleCode, String>> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DtcStatusRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.dtc_recycler_row, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map.Entry<DiagnosticTroubleCode, String> dtc = mDataset.get(position);
        holder.dtcNumber.setText(dtc.getKey().Code);
        holder.dtcDescription.setText(dtc.getValue());
        holder.dtcType.setText(dtc.getKey().Computer.toString());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

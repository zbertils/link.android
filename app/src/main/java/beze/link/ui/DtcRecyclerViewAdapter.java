package beze.link.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import beze.link.R;
import beze.link.obd2.DiagnosticTroubleCode;
import beze.link.obd2.ParameterIdentification;

public class DtcRecyclerViewAdapter extends RecyclerView.Adapter<DtcRecyclerViewAdapter.ViewHolder> {

    private List<DiagnosticTroubleCode> mDataset;

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

    public DtcRecyclerViewAdapter(List<DiagnosticTroubleCode> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DtcRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.dtc_recycler_row, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DiagnosticTroubleCode dtc = mDataset.get(position);
        holder.dtcNumber.setText(dtc.Code);
        holder.dtcDescription.setText(dtc.Description);
        holder.dtcType.setText(dtc.Type.toString());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

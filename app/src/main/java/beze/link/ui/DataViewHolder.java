package beze.link.ui;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.beze.link.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import beze.link.Globals;
import beze.link.obd2.ParameterIdentification;
import beze.link.util.PidLineGraphSeries;

public class DataViewHolder extends RecyclerView.ViewHolder {

    public TextView pidName;
    public TextView pidValue;
    public TextView pidDecodedValue;
    public TextView pidUnits;
    public GraphView pidGraph;
    public PidLineGraphSeries graphSeries;

    public DataViewHolder(View v) {
        super(v);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Globals.appContext);
        String graphSizeStr = sharedPref.getString(Globals.Preferences.KEY_PREF_GRAPH_SIZES, null);
        int graphSize = 250;
        try
        {
            graphSize = Integer.parseInt(graphSizeStr);
        }
        catch (Exception e)
        {
            Log.w("DataViewHolder", "Could not parse graphSizeStr value " + graphSizeStr);
            graphSize = 250; // this is large
        }

        String graphLengthStr = sharedPref.getString(Globals.Preferences.KEY_PREF_GRAPH_LENGTHS, null);
        int graphLength = 250;
        try
        {
            graphLength = Integer.parseInt(graphLengthStr);
        }
        catch (Exception e)
        {
            Log.w("DataViewHolder", "Could not parse graphLengthStr value " + graphSizeStr);
            graphLength = 250; // this is medium
        }

        pidName = (TextView) v.findViewById(R.id.textViewPidName_Data);
        pidValue = (TextView) v.findViewById(R.id.textViewPidNumber_Data);
        pidDecodedValue = (TextView) v.findViewById(R.id.textViewDecodedPidValue_Data);
        pidUnits = (TextView) v.findViewById(R.id.textViewPidUnits_Data);
        pidGraph = (GraphView) v.findViewById(R.id.data_graph);
        ViewGroup.LayoutParams layoutParams = pidGraph.getLayoutParams();
        layoutParams.height = graphSize;
        pidGraph.setLayoutParams(layoutParams);
        graphSeries = new PidLineGraphSeries(pidGraph, graphLength);
    }
}

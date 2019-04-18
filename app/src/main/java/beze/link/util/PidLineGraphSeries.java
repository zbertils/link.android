package beze.link.util;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import beze.link.obd2.ParameterIdentification;

public class PidLineGraphSeries
{
    private long minXValue;
    private long maxXValue;
    private int maxDataCount;
    private LineGraphSeries<DataPoint> series;
    private GraphView graph;
    private int count;

    public PidLineGraphSeries(GraphView graph, int maxDataCount)
    {
        minXValue = Long.MAX_VALUE;
        maxXValue = Long.MIN_VALUE;
        count = 0;
        this.maxDataCount = maxDataCount;
        series = new LineGraphSeries<>();
        this.graph = graph;

        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    }

    public void appendData(ParameterIdentification pid)
    {
        if (pid.Timestamp < minXValue)
        {
            minXValue = pid.Timestamp;
        }
        if (pid.Timestamp > maxXValue)
        {
            // update the max x value and append the data,
            // appending data requires a new x value be greater than the last
            maxXValue = pid.Timestamp;
            series.appendData(new DataPoint(count, pid.LastDecodedValue()), false, maxDataCount);
            graph.getViewport().setMinX(Math.max(0, count - maxDataCount));
            graph.getViewport().setMaxX(count);
            graph.refreshDrawableState();
            count++;
        }
    }

}

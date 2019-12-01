package beze.link.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.beze.link.R;
import com.hypertrack.hyperlog.HyperLog;

import beze.link.fragments.TestsFragment.OnListFragmentInteractionListener;
import beze.link.obd2.OBDTest;

import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link beze.link.obd2.OBDTest} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TestItemRecyclerViewAdapter extends RecyclerView.Adapter<TestItemRecyclerViewAdapter.ViewHolder> implements View.OnClickListener
{

    private final List<OBDTest> mValues;
//    private final OnListFragmentInteractionListener mListener;

    public TestItemRecyclerViewAdapter(List<OBDTest> items)
    {
        mValues = items;
//        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    public void onClick (View view)
    {
        if (view != null)
        {
            OBDTest test = (OBDTest) view.getTag();
            if (test != null)
            {
                HyperLog.v("TestItemAdapter", "Performing test " + test.testName);
            }
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        if (mValues.size() > position)
        {
            OBDTest test = mValues.get(position);
            holder.mItem = test;
            holder.testName.setText(test.testName);

            holder.runButton.setTag(test);
            holder.runButton.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount()
    {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final Button runButton;
        public final TextView testName;
        public OBDTest mItem;

        public ViewHolder(View view)
        {
            super(view);
            runButton = (Button) view.findViewById(R.id.runButton);
            testName = (TextView) view.findViewById(R.id.testName);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + testName.getText() + "'";
        }
    }
}

package beze.link.obd2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beze.link.Globals;
import beze.link.obd2.ParameterIdentification;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public abstract class OBDTest implements Runnable
{
    public final List<ParameterIdentification> pids;
    public final List<Boolean> results;
    public final List<String> notes;
    public final String testName;

    public OBDTest(String testName)
    {
        this.pids = new ArrayList<>();
        this.results = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.testName = testName;
    }

    public void run()
    {
        if (Globals.cable != null)
        {
            for (ParameterIdentification pid : pids)
            {
                String response = Globals.cable.Communicate(pid);
                results.add(resultIsValid(pid, response));
                notes.add(response);
            }
        }
    }

    protected abstract boolean resultIsValid(ParameterIdentification pid, String response);


}

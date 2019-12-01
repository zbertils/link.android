package beze.link.obd2.tests;

import java.util.ArrayList;

import beze.link.obd2.OBDTest;
import beze.link.obd2.ParameterIdentification;

public class BalanceRatesTest extends OBDTest
{
    protected ParameterIdentification br1_normal_j1850 = new ParameterIdentification(
            "Balance Rate Cylinder 1 - Normal J1850",
            (byte) 0x22,
            (short) 0x162f,
            (byte) 2,
            "((%1$s*256+%2$s)-32768)/64.0",
            "mm³",
            "Engine",
            "GM Enhanced",
            "Balance rate cylinder 1 difference from average, using J1850 header and expected response formula",
            "6C10F1");

    protected ParameterIdentification br1_can_7e0 = new ParameterIdentification(
            "Balance Rate Cylinder 1 - Normal J1850",
            (byte) 0x22,
            (short) 0x162f,
            (byte) 2,
            "((%1$s*256+%2$s)-32768)/64.0",
            "mm³",
            "Engine",
            "GM Enhanced",
            "Balance rate cylinder 1 difference from average, using J1850 header and expected response formula",
            "6C10F1");

    public BalanceRatesTest()
    {
        super("Balance Rates");
        pids.add(br1_normal_j1850);
    }

    protected boolean resultIsValid(ParameterIdentification pid, String response)
    {
        double maxVal = 15;
        double minVal = -15;
        double val = pid.Unpack(response);

        if (val > minVal && val < maxVal)
        {
            return true;
        }

        if (pid == br1_normal_j1850)
        {

        }
        else if (pid == br1_can_7e0)
        {
            if (val > minVal && val < maxVal)
            {
                return true;
            }
        }
        // else unknown pid, fall through to returning false

        return false;
    }

}

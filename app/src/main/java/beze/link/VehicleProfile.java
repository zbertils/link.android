package beze.link;

public class VehicleProfile
{
    private static final String TAG = Globals.TAG_BASE + "VehicleProfile";

    public String vin;
    public short year;
    public String manufacturer;
    public String model;

    public VehicleProfile()
    {
        vin = "";
        year = 0;
        manufacturer = "";
        model = "";
    }

}

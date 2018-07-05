package beze.link.obd2.cables.connections;

public class WifiConnection extends CableConnection
{
    public WifiConnection()
    {
        cableType = Type.Wifi;
    }

    @Override
    public boolean connect()
    {
        return false;
    }

    @Override
    public void close()
    {
    }

    @Override
    public int available()
    {
        return 0;
    }

    @Override
    public boolean isConnected()
    {
        return false;
    }

    @Override
    public int read(byte[] buffer)
    {
        return 0;
    }

    @Override
    public void write(byte[] buffer)
    {

    }
}

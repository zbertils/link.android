package beze.link.obd2.cables.connections;

public class SimulatedConnection extends CableConnection
{
    public SimulatedConnection()
    {
        cableType = Type.USB;
    }

    @Override
    public boolean connect()
    {
        return true;
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
        return true;
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

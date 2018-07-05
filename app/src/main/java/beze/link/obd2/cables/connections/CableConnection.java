package beze.link.obd2.cables.connections;

public abstract class CableConnection
{

    public enum Type
    {
        None,
        Bluetooth,
        Wifi,
        USB
    }

    protected Type cableType;
    public Type getCableType()
    {
        return cableType;
    }

    public abstract boolean connect();
    public abstract void close();
    public abstract int available();
    public abstract boolean isConnected();
    public abstract void write(byte[] buffer);
    public abstract int read(byte[] buffer);

}

package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFileServer extends Remote
{
    public static final String SERVER_NAME_IN_REGISTRY = "CHAT_SERVER";
    public void runCommand(Command cmd) throws RemoteException;
}

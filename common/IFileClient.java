package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFileClient extends Remote
{
    public void receiveResponse(Response res) throws RemoteException;
    public void setID(long id) throws RemoteException;
    public long getID() throws RemoteException;
    public boolean hasID() throws RemoteException;
    public void deleteID() throws RemoteException;
    public void setUserName(String user) throws RemoteException;
    public String getUserName() throws RemoteException;
    public boolean hasUserName() throws RemoteException;
    public void deleteUserName() throws RemoteException;
}

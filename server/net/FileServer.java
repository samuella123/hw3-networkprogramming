package server.net;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.controller.*;


public class FileServer
{
    public static void main(String[] args)
    {
        try
        {
            new FileServer().startRegistry();
            Naming.rebind(Controller.SERVER_NAME_IN_REGISTRY, new Controller());

            System.out.println("Status- Server is up!");
        }
        catch (Exception ex )
        {
            System.out.println("Error- Could not start file server.");
        }
    }

    private void startRegistry() throws RemoteException
    {
        try
        {
            LocateRegistry.getRegistry().list();
        }
        catch (RemoteException noRegistryIsRunning)
        {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
    }
}

package client.view;

import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.*;


public class Interpreter implements Runnable
{
  private final SafeOutput safeOut = new SafeOutput();
  private final Scanner console = new Scanner(System.in);

  private IFileClient clientObject;
  private IFileServer server;

  Interpreter()
  {
    try
    {
      clientObject = new ClientObject();
    }
    catch(Exception ex)
    {
      safeOut.printResult(ex.getMessage());
    }
  }

  @Override
  public void run()
  {
    while (true)
    {
        try
        {
            String entireLine = readNextLine();
            String trimmed = entireLine.trim();
            String[] splited = trimmed.split(" ");
            String cmd = splited[0].toLowerCase();

            if(cmd.equals(CommandType.REGISTER.toString()))
            {
              server.runCommand(new Command(CommandType.REGISTER,splited,(IFileClient) clientObject));
            }
            else if(cmd.equals(CommandType.UNREGISTER.toString()))
            {
              server.runCommand(new Command(CommandType.UNREGISTER,splited,(IFileClient) clientObject));
            }
            else if(cmd.equals(CommandType.LOGIN.toString()))
            {
              if(splited.length<3)
                throw new Exception("Error- Not enough arguments for: " + cmd);

              server.runCommand(new Command(CommandType.LOGIN,splited,(IFileClient) clientObject));
            }
            else if(cmd.equals(CommandType.CONNECT.toString()))
            {
              if(splited.length<2)
                throw new Exception("Error- Not enough arguments for: " + cmd);

              lookForServer(splited[1]);

              server.runCommand(new Command(CommandType.CONNECT,splited,(IFileClient) clientObject));
            }
            else if(cmd.equals(CommandType.QUIT.toString()))
            {
              server.runCommand(new Command(CommandType.QUIT,splited,(IFileClient) clientObject));

              UnicastRemoteObject.unexportObject(clientObject, false);

              System.exit(0);
              return;
            }
            else if(cmd.equals(CommandType.LOGOUT.toString()))
            {
              server.runCommand(new Command(CommandType.LOGOUT,splited,(IFileClient) clientObject));
            }
            else if(cmd.equals(CommandType.UPLOAD.toString()))
            {
              if(splited.length<4)
                throw new Exception("Error- Not enough arguments for: " + cmd);

              String fileName = "clientfiles/" + splited[1];

              File file = new File(fileName);
              String sizeT = Long.toString(file.length());
              List<String> tmp = new ArrayList<String>();
              for(int i=0;i<splited.length;i++)
              {
                tmp.add(splited[i]);
              }
              tmp.add(sizeT);
              splited = new String[tmp.size()];
              splited = tmp.toArray(splited);

			        byte buffer[] = new byte[(int)file.length()];
			        BufferedInputStream input = new BufferedInputStream(new FileInputStream(fileName));
			        input.read(buffer,0,buffer.length);
			        input.close();

              server.runCommand(new Command(CommandType.UPLOAD,splited,(IFileClient) clientObject,buffer));
            }
            else if(cmd.equals(CommandType.DOWNLOAD.toString()))
            {
              if(splited.length<2)
                throw new Exception("Error- Not enough arguments for: " + cmd);

              server.runCommand(new Command(CommandType.DOWNLOAD,splited,(IFileClient) clientObject));
            }
            else if(cmd.equals(CommandType.DELETE.toString()))
            {
              server.runCommand(new Command(CommandType.DELETE,splited,(IFileClient) clientObject));
            }
            else if(cmd.equals(CommandType.SHOWALL.toString()))
            {
              server.runCommand(new Command(CommandType.SHOWALL,splited,(IFileClient) clientObject));
            }
            else if(cmd.equals(CommandType.NOTIFY.toString()))
            {
              server.runCommand(new Command(CommandType.NOTIFY,splited,(IFileClient) clientObject));
            }
            else
            {
              safeOut.println("Error- Command Unknown: " + cmd);
            }
        }
        catch (Exception ex)
        {
            safeOut.println(ex.getMessage());
        }

    }
  }

  private String readNextLine()
  {
      safeOut.printPrompt();
      return console.nextLine();
  }

  private void lookForServer(String host) throws NotBoundException, MalformedURLException, RemoteException
  {
      server = (IFileServer) Naming.lookup("//" + host + "/" + IFileServer.SERVER_NAME_IN_REGISTRY);
  }


  private class ClientObject extends UnicastRemoteObject implements IFileClient
  {
      private long serverId;
      private String userName;
      private boolean iHaveID;
      private boolean iHaveUserName;
      public ClientObject() throws RemoteException
      {
        iHaveID = false;
        serverId = 0;
      }

      @Override
      public void receiveResponse(Response res) throws RemoteException
      {
          if(res.getType()==ResponseType.FILE)
          {
            try
            {
              byte[] filedata = res.getFile();

              String filePath = "clientfiles/" + res.getMessage();

              File file = new File(filePath);
              BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(filePath));
              output.write(filedata,0,filedata.length);
              output.flush();
              output.close();

              String response = "File " + res.getMessage() + " downloaded successfully.";
              safeOut.println(response);
            }
            catch(Exception ex)
            {

            }
          }
          else
          {
            if(res.getType()==ResponseType.NOTIF)
            {
              safeOut.print("\n");
              safeOut.printResult(res.getMessage());
            }
            else
            {
              safeOut.println(res.getMessage());
            }
          }
      }

      @Override
      public void setID(long id) throws RemoteException
      {
        serverId = id;
        iHaveID = true;
      }

      @Override
      public long getID() throws RemoteException
      {
        return serverId;
      }

      @Override
      public boolean hasID() throws RemoteException
      {
        return iHaveID;
      }

      @Override
      public void deleteID() throws RemoteException
      {
        iHaveID = false;
        serverId = 0;
      }

      @Override
      public void setUserName(String user) throws RemoteException
      {
        userName = user;
        iHaveUserName = true;
      }

      @Override
      public String getUserName() throws RemoteException
      {
        return userName;
      }

      @Override
      public boolean hasUserName() throws RemoteException
      {
        return iHaveUserName;
      }

      @Override
      public void deleteUserName() throws RemoteException
      {
        iHaveUserName = false;
        userName = "";
      }
  }
}

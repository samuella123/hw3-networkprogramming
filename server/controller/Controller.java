package server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.*;
import common.*;
import server.model.*;


public class Controller extends UnicastRemoteObject implements IFileServer
{
    private final Random idGenerator = new Random();
    private final Map<Long, IFileClient> clients = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Long> notifications = Collections.synchronizedMap(new HashMap<>());

    private JDBC database;

    public Controller() throws Exception
    {
      database = new JDBC();
    }


    public IFileClient findClient(long id)
    {
        return clients.get(id);
    }

    public void removeClient(long id)
    {
        clients.remove(id);
    }

    public long findSubscriberID(String file)
    {
        return notifications.get(file);
    }

    public void removeSubscription(String file)
    {
        notifications.remove(file);
    }

    @Override
    public void runCommand(Command cmd) throws RemoteException
    {
      try
      {
        if(cmd.getType()==CommandType.CONNECT)  ///////////////CONNECT//////////////
        {
            String welcomeMessage = "You are successfully connected to File Server. Commands:\n>\n>  1) Login <username> <password> \n>  2) Register <username> <password> \n>  3) Unregister <username> <password> \n>  4) Quit \n>";
            IFileClient cc = cmd.getSender();
            cc.receiveResponse(new Response(ResponseType.RESPONSE,welcomeMessage));
        }
        else if(cmd.getType()==CommandType.QUIT)
        {
            IFileClient cc = cmd.getSender();
            if(cc.hasID())
            {
              removeClient(cc.getID());
              cc.deleteID();
              cc.deleteUserName();
            }
            String response = "Successfully logged out.";
            cc.receiveResponse(new Response(ResponseType.RESPONSE,response));
        }
        else if(cmd.getType()==CommandType.UPLOAD)  ///////////////UPLOAD//////////////
        {
            String fileName = cmd.getParams()[1];
            String filePath = "serverfiles/"+cmd.getParams()[1];
            String fileSize = cmd.getParams()[4];

            boolean publicAccess = false;
            boolean readOnly = false;

            if(cmd.getParams()[2].toLowerCase().equals("public"))
            {
              publicAccess = true;
            }
            else if(cmd.getParams()[2].toLowerCase().equals("private"))
            {
              publicAccess = false;
            }
            else
            {
              throw new Exception("Error- Wrong format argument: " + cmd.getParams()[2]);
            }

            if(cmd.getParams()[3].toLowerCase().equals("readonly"))
            {
              readOnly = true;
            }
            else if(cmd.getParams()[3].toLowerCase().equals("open"))
            {
              readOnly = false;
            }
            else
            {
              throw new Exception("Error- Wrong format argument: " + cmd.getParams()[3]);
            }


            String response1 = "File " + cmd.getParams()[1] + " uploaded to the server as a " + cmd.getParams()[2] + " file, successfully.";
            String response2 = "File " + cmd.getParams()[1] + " modified successfully.";
            IFileClient cc = cmd.getSender();

            if(!cc.hasID())
            {
              throw new Exception("Error- You are not logged-in.");
            }

            byte[] filedata = cmd.getFile();
  			    File file = new File(filePath);

            if(!file.exists())
            {
              //add the file to DB
              database.addFile(fileName,cc.getUserName(),fileSize,publicAccess,readOnly);

    			    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(filePath));
    			    output.write(filedata,0,filedata.length);
    		      output.flush();
    			    output.close();


              cc.receiveResponse(new Response(ResponseType.RESPONSE,response1));
            }
            else
            {
              //check user permissions
              //update file in DB
              database.updateFile(fileName,cc.getUserName(),fileSize,publicAccess,readOnly);

              try
              {
                //notify owener if he subscribed
                String notif = "NOTIFICATION- Your file: " + fileName + " modified by: " + cc.getUserName();
                long notifID = findSubscriberID(fileName);
                IFileClient notifClient = findClient(notifID);

                //because the owner may leave the application
                notifClient.receiveResponse(new Response(ResponseType.NOTIF,notif));
              }
              catch(Exception ex) {}


              BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(filePath));
              output.write(filedata,0,filedata.length);
              output.flush();
              output.close();

              cc.receiveResponse(new Response(ResponseType.RESPONSE,response2));
            }
        }
        else if(cmd.getType()==CommandType.DOWNLOAD)  ///////////////DOWNLOAD//////////////
        {
            IFileClient cc = cmd.getSender();
            String fileName = cmd.getParams()[1];
            String filePath = "serverfiles/" + fileName;
            String response = fileName;

            if(!cc.hasID())
            {
              throw new Exception("Error- You are not logged-in.");
            }

            //Check user has the permission
            if(!database.canSeeFile(fileName,cc.getUserName()))
            {
              throw new Exception("Error- You are not allowed to download this file.");
            }

            //notify owner if he subscribed
            try
            {
              String notif = "NOTIFICATION- Your file: " + fileName + " downloaded by: " + cc.getUserName();
              long notifID = findSubscriberID(fileName);
              IFileClient notifClient = findClient(notifID);

              //because the owner may leave the application
              notifClient.receiveResponse(new Response(ResponseType.NOTIF,notif));
            }
            catch(Exception ex) {}

            File file = new File(filePath);
            byte buffer[] = new byte[(int)file.length()];
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(filePath));
            input.read(buffer,0,buffer.length);
            input.close();

            cc.receiveResponse(new Response(ResponseType.FILE,response,buffer));
        }
        else if(cmd.getType()==CommandType.DELETE)  ///////////////DELETE//////////////
        {
            IFileClient cc = cmd.getSender();
            String fileName = cmd.getParams()[1];
            String filePath = "serverfiles/" + fileName;
            String response = "File: " + fileName + " deleted successfully.";

            if(!cc.hasID())
            {
              throw new Exception("Error- You are not logged-in.");
            }

            //Check user has the permission
            //delete from database
            database.deleteFile(fileName, cc.getUserName());

            try
            {
              //notify owner if he is subscribed
              String notif = "NOTIFICATION- Your file: " + fileName + " deleted by: " + cc.getUserName();
              long notifID = findSubscriberID(fileName);
              IFileClient notifClient = findClient(notifID);

              //because the owner may leave the application
              notifClient.receiveResponse(new Response(ResponseType.NOTIF,notif));
            }
            catch(Exception ex) {}

            File file = new File(filePath);

            if(!file.delete())
            {
              throw new Exception("Error- File you entered does not exist.");
            }

            cc.receiveResponse(new Response(ResponseType.RESPONSE,response));
        }
        else if(cmd.getType()==CommandType.LOGIN)  ///////////////LOGIN//////////////
        {
            String username = cmd.getParams()[1];
            String password = cmd.getParams()[2];
            IFileClient cc = cmd.getSender();

            if(cc.hasID())
              throw new Exception("Error- You are already logged-in.");

            //check userpass with database
            if(!database.checkUserPass(username,password))
            {
              throw new Exception("Error- Wrong password.");
            }

            long clientId = idGenerator.nextLong();
            cc.setID(clientId);
            cc.setUserName(username);
            clients.put(clientId, cc);

            String response = "You are logged-in. Commands:\n>\n>  1) Upload <filename> <private/public> <readonly/open> \n>  2) Download <filename> \n>  3) Delete <filename> \n>  4) Showall \n>  5) Notify <filename> \n>  6) Register <username> <password> \n>  6) Unregister <username> <password> \n>  7) Logout \n>  8) Quit \n>";
            cc.receiveResponse(new Response(ResponseType.RESPONSE,response));
        }
        else if(cmd.getType()==CommandType.LOGOUT)  ///////////////LOGOUT//////////////
        {
            IFileClient cc = cmd.getSender();
            if(!cc.hasID())
            {
              throw new Exception("Error- You are not logged-in.");
            }
            removeClient(cc.getID());
            cc.deleteID();
            cc.deleteUserName();
            String response = "Successfully logged out. Commands:\n>\n>  1) Login <username> <password> \n>  2) Register <username> <password> \n>  3) Unregister <username> <password> \n>  4) Quit \n>";
            cc.receiveResponse(new Response(ResponseType.RESPONSE,response));
        }
        else if(cmd.getType()==CommandType.REGISTER) ///////////////REGISTER//////////////
        {
            String username = cmd.getParams()[1];
            String password = cmd.getParams()[2];

            //add userpass to database and check duplicate
            database.addUser(username,password);

            IFileClient cc = cmd.getSender();
            String response = "A new user is registered: " + username;
            cc.receiveResponse(new Response(ResponseType.RESPONSE,response));
        }
        else if(cmd.getType()==CommandType.UNREGISTER)  ///////////////UNREGISTER//////////////
        {
            String username = cmd.getParams()[1];
            String password = cmd.getParams()[2];

            //delete userpass from database
            database.deleteUser(username,password);

            IFileClient cc = cmd.getSender();
            String response = "User is deleted: " + username;
            cc.receiveResponse(new Response(ResponseType.RESPONSE,response));
        }
        else if(cmd.getType()==CommandType.SHOWALL)  ///////////////SHOWALL//////////////
        {
            IFileClient cc = cmd.getSender();
            if(!cc.hasID())
            {
              throw new Exception("Error- You are not logged-in.");
            }

            //show all from database (those available for this user)
            String response = database.showFiles(cc.getUserName());

            cc.receiveResponse(new Response(ResponseType.RESPONSE,response));
        }
        else if(cmd.getType()==CommandType.NOTIFY)  ///////////////NOTIFY//////////////
        {
            IFileClient cc = cmd.getSender();
            String fileName = cmd.getParams()[1];
            String filePath = "serverfiles/" + fileName;
            if(!cc.hasID())
            {
              throw new Exception("Error- You are not logged-in.");
            }

            File file = new File(filePath);
            if(!file.exists())
            {
              throw new Exception("ERROR- There is no such a file on server: " + fileName);
            }

            //check whether the file is for the user or not (owner)
            if(!database.isFileOwner(fileName,cc.getUserName()))
              throw new Exception("You are not allowed to get notified about a file other than yours.");

            //set notify
            removeSubscription(fileName);
            notifications.put(fileName,cc.getID());

            String response = "As long as you are in the application, on any changes or reads of this file, you will be notified.";
            cc.receiveResponse(new Response(ResponseType.RESPONSE,response));
        }
        else
        {
          throw new Exception("Error- Unknown command.");
        }
      }
      catch(Exception ex)
      {
        IFileClient cc = cmd.getSender();
        cc.receiveResponse(new Response(ResponseType.ERROR,ex.getMessage()));
      }
    }

}

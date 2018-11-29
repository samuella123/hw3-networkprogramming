package server.model;

import java.sql.*;
import java.util.*;


public class JDBC
{
    private static final String FILE_TABLE_NAME = "file";
    private static final String USER_TABLE_NAME = "user";

    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Qwerty123";
    private static final String DB_ADDRESS = "jdbc:mysql://localhost:3306/networkprogramming";

    private PreparedStatement createFileStmt;
    private PreparedStatement findAllFilesStmt;
    private PreparedStatement deleteFileStmt;
    private PreparedStatement selectFileStmt;

    private PreparedStatement createUserStmt;
    private PreparedStatement findAllUsersStmt;
    private PreparedStatement deleteUserStmt;
    private PreparedStatement selectUserStmt;


    private Connection connection;

    public JDBC() throws Exception
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(DB_ADDRESS, DB_USERNAME, DB_PASSWORD);
            createTable(FILE_TABLE_NAME);
            createTable(USER_TABLE_NAME);
            Statement stmt = connection.createStatement();
            prepareStatements();
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            throw ex;
        }
    }

    public boolean checkUserPass(String username,String pass) throws SQLException,Exception
    {
      selectUserStmt.setString(1, username);
      ResultSet users = selectUserStmt.executeQuery();
      while (users.next())
      {
        if(pass.equals(users.getString(2)))
          return true;
        else
          return false;
      }
      throw new Exception("ERROR- No user with this username found: " + username);
    }

    public void deleteUser(String username,String pass) throws SQLException,Exception
    {
      if(checkUserPass(username,pass))
      {
        deleteUserStmt.setString(1, username);
        deleteUserStmt.executeUpdate();
      }
      else
      {
        throw new Exception("ERROR- Wrong password for the user: " + username);
      }
    }

    public void addUser(String username,String pass) throws Exception
    {
      try
      {
        createUserStmt.setString(1, username);
        createUserStmt.setString(2, pass);
        createUserStmt.executeUpdate();
      }
      catch(Exception ex)
      {
        throw new Exception("ERROR- This username is token: " + username);
      }

    }

    public void addFile(String name,String owner,String size,boolean access, boolean mwrite) throws SQLException
    {
      createFileStmt.setString(1, name);
      createFileStmt.setString(2, owner);
      createFileStmt.setString(3, size);

      String acc;
      if(access)
        acc = "public";
      else
        acc = "private";

      String mrit;
      if(mwrite)
        mrit = "readonly";
      else
        mrit = "open";

      createFileStmt.setString(4, acc);
      createFileStmt.setString(5, mrit);
      createFileStmt.executeUpdate();
    }

    public void deleteFile(String name, String client) throws SQLException, Exception
    {
      selectFileStmt.setString(1, name);
      ResultSet files = selectFileStmt.executeQuery();

      while (files.next())
      {
        String owner = files.getString(2);
        if(owner.equals(client))
        {
          deleteFileStmt.setString(1, name);
          deleteFileStmt.executeUpdate();
          return;
        }
        else
        {
          if(files.getString(5).equals("open") && files.getString(4).equals("public"))
          {
            deleteFileStmt.setString(1, name);
            deleteFileStmt.executeUpdate();
            return;
          }
          else
          {
            throw new Exception("ERROR- You dont have the permission to delete this file: " + name);
          }
        }
      }

      throw new Exception("ERROR- No file with this name found: " + name);
    }

    public void updateFile(String name,String client,String size,boolean access, boolean mwrite) throws SQLException, Exception
    {
      selectFileStmt.setString(1, name);
      ResultSet files = selectFileStmt.executeQuery();

      while (files.next())
      {
        String owner = files.getString(2);
        if(owner.equals(client))
        {
          deleteFile(name, owner);
          addFile(name,owner,size,access,mwrite);
          return;
        }
        else
        {
          if(files.getString(5).equals("open") && files.getString(4).equals("public"))
          {
            deleteFile(name, owner);
            addFile(name,owner,size,access,mwrite);
            return;
          }
          else
          {
            throw new Exception("ERROR- You dont have the permission to modify this file: " + name);
          }
        }
      }

      throw new Exception("ERROR- No file with this name found: " + name);
    }

    public boolean canSeeFile(String name,String client) throws SQLException,Exception
    {
      selectFileStmt.setString(1, name);
      ResultSet files = selectFileStmt.executeQuery();
      while (files.next())
      {
        String owner = files.getString(2);
        if(owner.equals(client))
        {
          return true;
        }
        else
        {
          if(files.getString(4).equals("public"))
          {
            return true;
          }
          else
          {
            return false;
          }
        }
      }

      throw new Exception("ERROR- No file with this name found: " + name);
    }

    public boolean isFileOwner(String name,String client) throws SQLException,Exception
    {
      selectFileStmt.setString(1, name);
      ResultSet files = selectFileStmt.executeQuery();
      while (files.next())
      {
        String owner = files.getString(2);
        if(owner.equals(client))
        {
          return true;
        }
        else
        {
          return false;
        }
      }
      throw new Exception("ERROR- No file with this name found: " + name);
    }

    public String showFiles(String client) throws SQLException,Exception
    {
      ResultSet files = findAllFilesStmt.executeQuery();
      String res = "";
      while (files.next())
      {
        if(canSeeFile(files.getString(1),client))
          res = res + "name: " + files.getString(1) + ", owner: " + files.getString(2) + ", size: " + files.getString(3) + ", access: " + files.getString(4) + ", mwrite: " + files.getString(5) + "\n";
      }
      return res;
    }

    private void createTable(String tableName) throws SQLException
    {
        if (!tableExists(tableName))
        {
            Statement stmt = connection.createStatement();

            if(tableName.equals(FILE_TABLE_NAME))
              stmt.executeUpdate("create table " + FILE_TABLE_NAME +"(username varchar(255),password varchar(255), PRIMARY KEY (username))");

            if(tableName.equals(USER_TABLE_NAME))
              stmt.executeUpdate("create table " + USER_TABLE_NAME +"(name varchar(255), owner varchar(255), size varchar(255), access varchar(255), mwrite varchar(255), PRIMARY KEY (name))");

        }
    }

    private boolean tableExists(String inTableName) throws SQLException
    {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tableMetaData = metaData.getTables(null, null, null, null);
        while (tableMetaData.next())
        {
            String tableName = tableMetaData.getString(3);
            if (tableName.equalsIgnoreCase(inTableName))
            {
                return true;
            }
        }
        return false;
    }

    private void listAllRows() throws SQLException
    {
        ResultSet files = findAllFilesStmt.executeQuery();
        while (files.next())
        {
            System.out.println("name: " + files.getString(1) + ", owner: " + files.getString(2) + ", size: " + files.getString(3) + ", access: " + files.getString(4) + ", mwrite: " + files.getString(5));
        }
        ResultSet users = findAllUsersStmt.executeQuery();
        while (users.next())
        {
            System.out.println("username: " + users.getString(1) + ", password: " + users.getString(2));
        }
    }

    private void prepareStatements() throws SQLException
    {
        createFileStmt = connection.prepareStatement("INSERT INTO " + FILE_TABLE_NAME + " VALUES (?, ?, ?, ?, ?)");
        deleteFileStmt = connection.prepareStatement("DELETE FROM " + FILE_TABLE_NAME + " WHERE name = ?");
        selectFileStmt = connection.prepareStatement("SELECT * FROM " + FILE_TABLE_NAME + " WHERE name = ?");
        findAllFilesStmt = connection.prepareStatement("SELECT * from " + FILE_TABLE_NAME);

        createUserStmt = connection.prepareStatement("INSERT INTO " + USER_TABLE_NAME + " VALUES (?, ?)");
        deleteUserStmt = connection.prepareStatement("DELETE FROM " + USER_TABLE_NAME + " WHERE username = ?");
        selectUserStmt = connection.prepareStatement("SELECT * from " + USER_TABLE_NAME + " WHERE username = ?");
        findAllUsersStmt = connection.prepareStatement("SELECT * from " + USER_TABLE_NAME);

    }
}

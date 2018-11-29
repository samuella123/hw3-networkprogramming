package common;

import java.io.Serializable;


public class Command implements Serializable
{
    private CommandType type;
    private String[] params;
    private byte[] file;
    private IFileClient sender;
    public Command(CommandType t,String[] ps,IFileClient s)
    {
      type = t;
      params = ps;
      sender = s;
    }
    public Command(CommandType t,String[] ps,IFileClient s,byte[] f)
    {
      type = t;
      params = ps;
      sender = s;
      file = f;
    }
    public CommandType getType()
    {
      return type;
    }
    public String[] getParams()
    {
      return params;
    }
    public IFileClient getSender()
    {
      return sender;
    }
    public byte[] getFile()
    {
      return file;
    }
}

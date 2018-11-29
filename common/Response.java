package common;

import java.io.Serializable;


public class Response implements Serializable
{
    private ResponseType type;
    private String message;
    private byte[] file;
    public Response(ResponseType t,String ms)
    {
      type = t;
      message = ms;
    }
    public Response(ResponseType t,String ms,byte[] f)
    {
      type = t;
      message = ms;
      file = f;
    }
    public ResponseType getType()
    {
      return type;
    }
    public String getMessage()
    {
      return message;
    }
    public byte[] getFile()
    {
      return file;
    }
}

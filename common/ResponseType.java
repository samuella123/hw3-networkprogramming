package common;

public enum ResponseType
{
    NOTIF("notification"),

    FILE("file"),

    ERROR("error"),

    RESPONSE("response");

    private String name;
    private ResponseType(String inp)
    {
      this.name = inp;
    }
    public String toString()
    {
      return this.name;
    }
}

package common;

public enum CommandType
{
    REGISTER("register"),

    UNREGISTER("unregister"),

    LOGIN("login"),

    CONNECT("connect"),

    LOGOUT("logout"),

    UPLOAD("upload"),

    DOWNLOAD("download"),

    DELETE("delete"),

    SHOWALL("showall"),

    QUIT("quit"),

    NOTIFY("notify");

    private String name;
    private CommandType(String inp)
    {
      this.name = inp;
    }
    public String toString()
    {
      return this.name;
    }
}

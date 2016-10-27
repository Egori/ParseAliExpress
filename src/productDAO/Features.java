package productDAO;

public class Features {

    private int id;
    private int pid;
    private String key = "";
    private String value = "";

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
    public int getPid() {
        return pid;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }

}

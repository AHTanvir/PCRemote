package anwar.pcremote.Model;

/**
 * Created by anwar on 9/14/2017.
 */

public class DeviceNameModel {
    private String name;
    private String ip;

    public DeviceNameModel(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}

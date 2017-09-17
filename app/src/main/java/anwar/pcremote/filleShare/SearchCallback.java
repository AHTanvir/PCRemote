package anwar.pcremote.filleShare;

/**
 * Created by anwar on 9/14/2017.
 */

public interface SearchCallback {
    void addDevice(String name, String ip);
    void finish(int position);
}

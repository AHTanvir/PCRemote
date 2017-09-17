package anwar.pcremote.Model;

/**
 * Created by anwar on 7/6/2017.
 */

public class RowItem {
    private int id;
    private int progress;
    private String name;
    private String status;

    public RowItem(int id,String name, String status,int progress) {
        this.id=id;
        this.name = name;
        this.status = status;
        this.progress=progress;
    }

    public RowItem( String name, String status, int progress) {
        this.progress = progress;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

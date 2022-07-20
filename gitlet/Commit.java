package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class Commit implements Serializable, Hashable {

    /** Message of commit. */
    private final String _msg;

    /** Timestamp of commit. */
    private final Date _timeStampDate;

    /** Timestamp MSG of commit. */
    private final String _timeStamp;

    /** Main parent of commit. */
    private final String _parent1;

    /** Secondary parent of commit. */
    private String _parent2 = "";

    /** Map of files to blobs of commit. */
    private TreeMap<String, String> _blobs = new TreeMap<String, String>();

    public String msg() {
        return _msg;
    }

    public String timeStamp() {
        return _timeStamp;
    }

    public Date timeStampDate() {
        return _timeStampDate;
    }

    public String parent1() {
        return _parent1;
    }

    public String parent2() {
        return _parent2;
    }

    public TreeMap<String, String> getBlobs() {
        return _blobs;
    }

    public void addBlob(String fileName, String blobHash) {
        _blobs.put(fileName, blobHash);
    }

    public void setParent2(String parent2) {
        this._parent2 = parent2;
    }

    public Commit(String msg, String parent1) {
        this._msg = msg;
        this._parent1 = parent1;
        Date date;
        SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

        if (this._parent1.equals("")) {
            date = new Date(0);
        } else {
            date = new Date(System.currentTimeMillis());
        }
        this._timeStampDate = date;
        this._timeStamp = fmt.format(_timeStampDate);
    }

    public void copyOver(Commit from) {
        _blobs = new TreeMap<>(from.getBlobs());
    }

    @Override
    public String hash() {
        return Utils.sha1(Utils.serialize(this));
    }

    @Override
    public String toString() {
        String result = "===" + "\n" + "commit " + hash();
        if (!(_parent2.equals(""))) {
            result += ("\n" + "Merge: " + parent1().substring(0, 7) + " "
                    + _parent2.substring(0, 7));
        }
        return result
                + "\n" + "Date: "
                + timeStamp() + " -0800"
                + "\n" + msg() + "\n";
    }
}

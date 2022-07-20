package gitlet;

import java.io.Serializable;

public class Blob implements Serializable, Hashable {

    /** String content of file. */
    private String _content;

    public String content() {
        return _content;
    }

    public Blob(String content) {
        this._content = content;
    }

    @Override
    public String hash() {
        String[] toHash = new String[]{"blob", content()};
        return Utils.sha1((Object[]) toHash);
    }
}

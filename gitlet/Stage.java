package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class Stage implements Serializable {

    /** Staged for removal. */
    private ArrayList<String> _rmStage = new ArrayList<String>();

    /** Staged for addition. */
    private TreeMap<String, String> _addStage = new TreeMap<String, String>();

    public ArrayList<String> rmStage() {
        return _rmStage;
    }

    public TreeMap<String, String> addStage() {
        return _addStage;
    }

    public void addToAddStage(String fileName, String blob) {
        _addStage.put(fileName, blob);
    }

    public void addToRemoveStage(String fileName) {
        _rmStage.add(fileName);
    }

    public void clearStage() {
        _addStage.clear(); _rmStage.clear();
    }
}

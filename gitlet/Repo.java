package gitlet;

import java.io.File;
import java.util.TreeMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Repo {

    /** HEAD commit. */
    private Commit _head;

    /** Stage object. */
    private Stage _stage = new Stage();

    /** Map of branch names to their respective heads. */
    private TreeMap<String, String> _heads = new TreeMap<String, String>();

    /** If merge conflic occured. */
    private boolean _conflict = false;

    /** Main directory of gitlet files. */
    private final File _mainDir = new File("./.gitlet");

    /** Index file. */
    private final File _index = Utils.join(_mainDir, "/index");

    /** Head file pointing to current branch. */
    private final File _headFile = Utils.join(_mainDir, "/HEAD");

    /** Folder containing branches. */
    private final File _headsFolder = Utils.join(_mainDir, "/heads");

    /** Folder containing commits. */
    private final File _comFolder = Utils.join(_mainDir, "/com");

    /** Folder containing blobs. */
    private final File _blobFolder = Utils.join(_mainDir, "/blob");

    /** Folder for remotes. */
    private final File _remotesFile = Utils.join(_mainDir, "/remotes");

    /** Limit before estimation of hash. */
    private int _hashLim = 40;

    /** Map of remotes. */
    private TreeMap<String, File> _remotes = new TreeMap<String, File>();

    public Stage stage() {
        return _stage;
    }

    public Repo() {
        if (_mainDir.exists()) {
            _stage = _index.length() > 0
                    ? Utils.readObject(_index, gitlet.Stage.class)
                    : new Stage();
            File headPath = new File(Utils.readContentsAsString(_headFile));
            _head = hashToCommit(Utils.readContentsAsString(headPath));
            File[] branches = _headsFolder.listFiles();
            if (branches != null) {
                for (File branch : branches) {
                    _heads.put(branch.getName(),
                            Utils.readContentsAsString(branch));
                }
            }
        }
    }

    public void init() {
        if (_mainDir.exists()) {
            throw Utils.error("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }

        Commit firstCommit = new Commit("initial commit", "");
        File master = Utils.join(_headsFolder, "/master");
        File[] commitFilePath = hashToPath(firstCommit.hash(), true);

        try {
            _mainDir.mkdir();
            _index.createNewFile();
            _headsFolder.mkdir();
            _comFolder.mkdir();
            _blobFolder.mkdir();
            _headsFolder.mkdir();
            _remotesFile.createNewFile();
            master.createNewFile();
            commitFilePath[0].mkdir();
            commitFilePath[1].createNewFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Utils.writeObject(commitFilePath[1], firstCommit);
        Utils.writeContents(_headFile, master.getPath());
        Utils.writeContents(master, firstCommit.hash());
    }

    public void add(String fileName) {
        File file = new File("./" + fileName);
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        String content = Utils.readContentsAsString(file);
        Blob blob = new Blob(content);
        File[] blobFilePath = hashToPath(blob.hash(), false);

        if (_head.getBlobs().containsKey(fileName)
            && blob.hash().equals(_head.getBlobs().get(fileName))) {
            _stage.rmStage().remove(fileName);
            Utils.writeObject(_index, _stage);
            return;
        }

        if (!blobFilePath[0].exists()) {
            blobFilePath[0].mkdir();
        }
        tryCreateFile(blobFilePath[1]);

        Utils.writeObject(blobFilePath[1], blob);
        _stage.addToAddStage(fileName, blob.hash());
        Utils.writeObject(_index, _stage);
    }

    public void commit(String msg, String par2) {
        if (msg.equals("")) {
            throw new GitletException("Please enter a commit message.");
        }

        Commit newCommit = new Commit(msg, _head.hash());
        newCommit.copyOver(_head);
        if (!par2.equals("")) {
            newCommit.setParent2(par2);
        }

        if (_index.length() == 0) {
            throw new GitletException("No changes added to the commit.");
        }

        for (Map.Entry<String, String> item : _stage.addStage().entrySet()) {
            if (!newCommit.getBlobs().containsKey(item.getKey())) {
                newCommit.addBlob(item.getKey(), item.getValue());
            } else {
                newCommit.getBlobs().replace(item.getKey(), item.getValue());
            }
        }

        for (String item : _stage.rmStage()) {
            newCommit.getBlobs().remove(item);
        }

        String newCommitHash = newCommit.hash();
        File[] newCommitPath = hashToPath(newCommitHash, true);

        if (!newCommitPath[0].exists()) {
            newCommitPath[0].mkdir();
        }
        tryCreateFile(newCommitPath[1]);

        _stage.clearStage();
        File currBranch = new File(Utils.readContentsAsString(_headFile));
        Utils.writeContents(currBranch, newCommitHash);
        Utils.writeContents(_index, "");
        Utils.writeObject(newCommitPath[1], newCommit);
        _head = newCommit;
    }

    /**
     * @param input = filename or branch name
     * @param branch = true if input is a branch name
     * @param reset = whether triggered by reset
     */
    public void checkout(String input, boolean branch, boolean reset) {
        if (!branch) {
            checkout(_head.hash(), input);
        } else {
            File branchFile = Utils.join(_headsFolder, "/" + input);
            if (!branchFile.exists()) {
                throw new GitletException("No such branch exists.");
            } else if (!reset
                    && branchFile.getPath()
                    .equals(Utils.readContentsAsString(_headFile))) {
                throw new GitletException("No need to "
                        + "checkout the current branch.");
            }
            String commitHash = Utils.readContentsAsString(branchFile);
            Commit newCommit = hashToCommit(commitHash);
            List<String> files = Utils.plainFilenamesIn("./");
            for (String file : files) {
                File realFile = new File("./" + file);
                String fileHash = Utils
                        .sha1(Utils.readContentsAsString(realFile));
                if (!_head.getBlobs().containsKey(file)
                        && newCommit.getBlobs().containsKey(file)
                        && !fileHash.equals(newCommit.getBlobs().get(file))) {
                    throw new GitletException("There is an "
                            + "untracked file in the way; "
                            + "delete it, or add and commit it first.");
                }
            }

            for (String file : files) {
                if (!newCommit.getBlobs().containsKey(file)) {
                    File realFile = new File("./" + file);
                    realFile.delete();
                }
            }

            for (Map.Entry<String, String> entry
                    : newCommit.getBlobs().entrySet()) {
                File file = new File("./" + entry.getKey());
                if (!file.exists()) {
                    tryCreateFile(file);
                }
                String blobHash = newCommit.getBlobs().get(entry.getKey());
                File blobFile = hashToPath(blobHash, false)[1];
                Blob blob = Utils.readObject(blobFile, Blob.class);
                Utils.writeContents(file, blob.content());
            }
            Utils.writeContents(_headFile, branchFile.getPath());
            File headPath = new File(Utils.readContentsAsString(_headFile));
            _head = hashToCommit(Utils.readContentsAsString(headPath));
            _stage.clearStage();
            Utils.writeContents(_index, "");
        }
    }

    public void checkout(String id, String fileName) {
        Commit commit = hashToCommit(id);
        if (!commit.getBlobs().containsKey(fileName)) {
            throw new GitletException("File does not exist in that commit.");
        }
        String blobHash = commit.getBlobs().get(fileName);
        File blobFile = hashToPath(blobHash, false)[1];
        Blob blob = Utils.readObject(blobFile, Blob.class);
        File filePath = new File("./" + fileName);
        if (!filePath.exists()) {
            tryCreateFile(filePath);
        }
        Utils.writeContents(filePath, blob.content());
    }

    public void checkout() {
        File headPath = new File(Utils.readContentsAsString(_headFile));
        checkout(headPath.getName(), true, false);
    }

    public void log() {
        System.out.println(_head);
        String startHash = _head.parent1();
        Commit curr;
        while (!startHash.equals("")) {
            curr = Utils.readObject(Utils.join(_comFolder,
                            startHash.substring(0, 2),
                            startHash.substring(2)),
                    Commit.class);
            System.out.println(curr);
            startHash = curr.parent1();
        }
    }

    public void rm(String fileName) {
        if (!_stage.addStage().containsKey(fileName)
                && !_head.getBlobs().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
        }

        _stage.addStage().remove(fileName);
        if (_head.getBlobs().containsKey(fileName)) {
            _stage.addToRemoveStage(fileName);
            File toRemove = new File("./" + fileName);
            toRemove.delete();
        }
        Utils.writeObject(_index, _stage);
    }

    public void gLog() {
        File[] subs = _comFolder.listFiles();
        for (File sub : subs) {
            for (String fileName : Utils.plainFilenamesIn(sub)) {
                String commitHash = sub.getName() + fileName;
                Commit curr = hashToCommit(commitHash);
                System.out.println(curr);
            }
        }
    }

    public ArrayList<String> find(String message) {
        ArrayList<String> toPrint = new ArrayList<String>();
        File[] subs = _comFolder.listFiles();
        for (File sub : subs) {
            for (String fileName : Utils.plainFilenamesIn(sub)) {
                String commitHash = sub.getName() + fileName;
                Commit curr = hashToCommit(commitHash);
                if (curr.msg().equals(message)) {
                    toPrint.add(curr.hash());
                }
            }
        }
        if (toPrint.size() == 0) {
            throw new GitletException("Found no commit with that message.");
        }
        return toPrint;
    }

    public void status() {
        System.out.println("=== Branches ===");
        List<String> branches = Utils.plainFilenamesIn(_headsFolder);
        Collections.sort(branches);
        File active = new File(Utils.readContentsAsString(_headFile));
        for (String branch : branches) {
            if (branch.equals(active.getName())) {
                System.out.println("*" + branch);
                continue;
            }
            System.out.println(branch);
        }
        System.out.println("\n=== Staged Files ===");
        List<String> addStage = new ArrayList<String>();
        for (Map.Entry<String, String> entry : _stage.addStage().entrySet()) {
            addStage.add(entry.getKey());
        }
        Collections.sort(addStage);
        for (String file : addStage) {
            System.out.println(file);
        }
        System.out.println("\n=== Removed Files ===");
        List<String> rmStage = new ArrayList<String>();
        rmStage.addAll(_stage.rmStage());
        Collections.sort(rmStage);
        for (String file : rmStage) {
            System.out.println(file);
        }
        ArrayList<String> modified = new ArrayList<String>();
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (Map.Entry<String, String> entry : _head.getBlobs().entrySet()) {
            File curr = new File("./" + entry.getKey());
            if (!curr.exists()) {
                if (!rmStage.contains(curr.getName())
                        || addStage.contains(curr.getName())) {
                    modified.add(entry.getKey() + " (deleted)");
                }
                continue;
            }
            String contents = Utils.readContentsAsString(curr);
            if ((_head.getBlobs().containsKey(curr.getName())
                    && !_head.getBlobs()
                    .get(curr.getName()).equals(Utils.sha1("blob" + contents))
                    && !addStage.contains(curr.getName()))
                    || addStage.contains(curr.getName()) && !_stage.addStage()
                    .get(curr.getName()).equals(Utils.sha1(contents))) {
                modified.add(entry.getKey() + " (modified)");
            }
        }
        Collections.sort(modified);
        System.out.println("\n=== Untracked Files ===");
        List<String> untracked = getUntrackedStatus();
        Collections.sort(untracked);
        for (String s : untracked) {
            System.out.println(s);
        }
    }

    public void branch(String name) {
        File branchFile = Utils.join(_headsFolder, "/" + name);
        if (branchFile.exists()) {
            throw new GitletException("A branch with "
                    + "that name already exists.");
        }
        tryCreateFile(branchFile);
        Utils.writeContents(branchFile, _head.hash());
    }

    public void rmBranch(String name) {
        File branchFile = Utils.join(_headsFolder, "/" + name);
        String headPath = Utils.readContentsAsString(_headFile);
        if (!branchFile.exists()) {
            throw new GitletException("A branch "
                    + "with that name does not exist.");
        } else if (branchFile.getPath().equals(headPath)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        branchFile.delete();
    }

    public void reset(String id) {
        if (!hashToPath(id, true)[1].exists()) {
            throw new GitletException("No commit with that id exists.");
        }
        File branchFile = new File(Utils.readContentsAsString(_headFile));
        Utils.writeContents(branchFile, id);
        checkout(branchFile.getName(), true, true);

    }

    public void reset() {
        File branchFile = new File(Utils.readContentsAsString(_headFile));
        checkout(branchFile.getName(), true, true);
    }

    public void merge(String branch) {
        File bFile = Utils.join(_headsFolder, "/" + branch);
        String headPath = Utils.readContentsAsString(_headFile);
        String headBranch = new File(headPath).getName();
        initMergeCheck(bFile, headPath);
        List<String> untracked = getUntracked();

        Commit split = findSplit(bFile);
        String bHash = Utils.readContentsAsString(bFile);
        Commit bHead = hashToCommit(bHash);

        if (mergeEndEarly(split, bFile, bHead)) {
            return;
        }
        mergeSplit(untracked, split, bHead);

        for (Map.Entry<String, String> blob : _head.getBlobs().entrySet()) {
            String bContent = bHead.getBlobs().get(blob.getKey());
            if (split.getBlobs().get(blob.getKey()) == null
                    && !(bContent == null)
                    && !bContent.equals(blob.getValue())) {
                if (untracked.contains(blob.getKey())) {
                    untrackedMerge();
                }
                conflictMerge(blob.getKey(), bContent, blob.getValue());
            }
        }

        for (Map.Entry<String, String> blob : bHead.getBlobs().entrySet()) {
            String hContent = _head.getBlobs().get(blob.getKey());
            if (split.getBlobs().get(blob.getKey()) == null) {
                if (untracked.contains(blob.getKey())) {
                    untrackedMerge();
                }
                if (hContent == null) {
                    checkout(bHead.hash(), blob.getKey());
                    add(blob.getKey());
                } else if (!hContent.equals(blob.getValue())) {
                    conflictMerge(blob.getKey(), hContent, blob.getValue());
                }
            }
        }

        commit(String.format("Merged %s into %s.",
                bFile.getName(), headBranch), bHead.hash());
        if (_conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public void addRemote(String name, String dir) {
        if (_remotes.containsKey(name)) {
            throw Utils.error("A remote with that name already exists.");
        }
        String[] dirWords = dir.split("/");
        String realPath = "";
        for (String jj : dirWords) {
            realPath += (jj + File.separator);
        }
        File remote = new File(realPath + ".gitlet");
        if (remote.exists()) {
            _remotes.put(name, remote);
        }
        Utils.writeContents(_remotesFile, _remotes);
    }

    public void rmRemote(String name) {
        if (!_remotes.containsKey(name)) {
            throw Utils.error("A remote with that name does not exist.");
        }
        _remotes.remove(name);
        Utils.writeContents(_remotesFile, _remotes);
    }

    public void push(String bName, String rBranch) {
        if (_remotes.get(bName) == null) {
            throw Utils.error("Remote directory not found.");
        }
        File rbPath = (File) _remotes.get(bName);
        rbPath = Utils.join(rbPath, "/" + rBranch);

        Commit currCom = _head;
        String toReset = _head.hash();
        File[] rmCom = hashToPath(currCom.hash(), true);
        String dir = rmCom[0].getPath().substring(9);
        String fil = rmCom[1].getPath().substring(9);

        if (!rbPath.exists()) {
            tryCreateFile(rbPath);
            Utils.writeContents(rbPath, currCom.hash());
            File rmComD = Utils.join(rbPath, dir);
            File rmComF = Utils.join(rbPath, fil);
            while (!rmComF.exists()) {
                if (!rmComD.exists()) {
                    tryCreateFile(rmComD);
                }
                tryCreateFile(rmComF);
                Utils.writeObject(rmComF, currCom);
                currCom = hashToCommit(currCom.parent1());
                rmCom = hashToPath(currCom.hash(), true);
                dir = rmCom[0].getPath().substring(9);
                fil = rmCom[1].getPath().substring(9);
                rmComD = Utils.join(rbPath, dir);
                rmComF = Utils.join(rbPath, fil);
            }
        }
    }

    private File[] hashToPath(String hash, boolean commit) {
        String subFolderName = "/" + hash.substring(0, 2);
        File subFolder = commit ? Utils.join(_comFolder, subFolderName)
                : Utils.join(_blobFolder, subFolderName);
        String filePath = "nonexistent";
        if (hash.length() < _hashLim) {
            List<String> allFiles = Utils.plainFilenamesIn(subFolder);
            for (String s : allFiles) {
                if (s.contains(hash.substring(2))) {
                    filePath = "/" + s;
                    break;
                }
            }
        } else {
            filePath = "/" + hash.substring(2);
        }
        File realFile = Utils.join(subFolder, filePath);
        return new File[]{subFolder, realFile};
    }

    private Commit hashToCommit(String hash) {
        File commitPath = hashToPath(hash, true)[1];
        if (!commitPath.exists()) {
            throw Utils.error("No commit with that id exists.");
        }
        return Utils.readObject(commitPath, Commit.class);
    }

    private void tryCreateFile(File path) {
        try {
            path.createNewFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private List<String> getUntracked() {
        File cwd = new File("./");
        List<String> preUntracked = Utils.plainFilenamesIn(cwd);
        List<String> untracked = new ArrayList<String>();
        for (String entry : preUntracked) {
            if (!_head.getBlobs().containsKey(entry)) {
                untracked.add(entry);
            }
        }
        return untracked;
    }

    private List<String> getUntrackedStatus() {
        File cwd = new File("./");
        List<String> preUntracked = Utils.plainFilenamesIn(cwd);
        List<String> untracked = new ArrayList<String>();
        for (String entry : preUntracked) {
            if (!_head.getBlobs().containsKey(entry)
                    && !_stage.addStage().containsKey(entry)
                    && !_stage.rmStage().contains(entry)) {
                untracked.add(entry);
            }
        }
        return untracked;
    }

    private void initMergeCheck(File bFile, String headPath) {
        if (_index.length() > 0) {
            throw Utils.error("You have uncommitted changes");
        } else if (!bFile.exists()) {
            throw Utils.error("A branch with that name does not exist.");
        } else if (headPath.equals(bFile.getPath())) {
            throw Utils.error("Cannot merge a branch with itself.");
        }
    }

    private void conflictMerge(String fileName,
                               String blobCurr, String blobGiven) {
        File file = new File("./" + fileName);
        String currContent, givContent;
        if (blobCurr != null) {
            File pathCurr = hashToPath(blobCurr, false)[1];
            currContent = Utils.readObject(pathCurr, Blob.class).content();
        } else {
            currContent = "";
        }

        if (blobGiven != null) {
            File pathGiven = hashToPath(blobGiven, false)[1];
            givContent = Utils.readObject(pathGiven, Blob.class).content();
        } else {
            givContent = "";
        }

        String newString = "<<<<<<< HEAD" + "\n"
                + currContent + "=======" + "\n" + givContent
                + ">>>>>>>" + "\n";
        Utils.writeContents(file, newString);
        add(fileName);
        _conflict = true;
    }

    private void untrackedMerge() {
        Utils.writeContents(_index, "");
        throw Utils.error("There is an untracked file in the way; delete it"
                + ", or add and commit it first.");
    }

    private Commit findSplit(File bFile) {
        TreeMap<String, Integer> dist = new TreeMap<String, Integer>();
        Commit curr = _head;
        int counter = 0;
        while (curr.parent1() != "") {
            dist.put(curr.hash(), counter);
            if (curr.parent2() != "") {
                dist.put(curr.parent2(), counter + 1);
            }
            counter++;
            curr = hashToCommit(curr.parent1());
        }
        dist.put(curr.hash(), counter);

        String bHash = Utils.readContentsAsString(bFile);
        curr = hashToCommit(bHash);
        Commit bHead = curr;
        Commit split = bHead;
        int smallestDist = Integer.MAX_VALUE;
        while (curr.hash() != "") {
            if (dist.get(curr.hash()) != null
                    && dist.get(curr.hash()) < smallestDist) {
                split = curr;
                smallestDist = dist.get(curr.hash());
            }
            if (dist.get(curr.parent2()) != null
                    && dist.get(curr.parent2()) < smallestDist) {
                split = hashToCommit(curr.parent2());
                smallestDist = dist.get(curr.parent2());
            }
            if (curr.parent1().equals("")) {
                break;
            }
            curr = hashToCommit(curr.parent1());
        }
        return split;
    }

    private boolean mergeEndEarly(Commit split, File bFile, Commit bHead) {
        if (split.hash().equals(bHead.hash())) {
            System.out.println("Given branch "
                    + "is an ancestor of the current branch.");
            return true;
        }

        if (split.hash().equals(_head.hash())) {
            checkout(bFile.getName(), true, true);
            System.out.println("Current branch fast-forwarded.");
            return true;
        }
        return false;
    }

    private void mergeSplit(List<String> untracked,
                            Commit split, Commit bHead) {
        for (Map.Entry<String, String> blob : split.getBlobs().entrySet()) {
            String bContent = bHead.getBlobs().get(blob.getKey());
            String hContent = _head.getBlobs().get(blob.getKey());

            if (hContent != null && bContent != null) {
                if (!hContent.equals(blob.getValue())
                        && bContent.equals(blob.getValue())) {
                    continue;
                } else if (bContent.equals(hContent)) {
                    continue;
                }
            } else if (hContent == null && bContent != null) {
                if (bContent.equals(blob.getValue())) {
                    continue;
                }
            } else if (untracked.contains(blob.getKey())) {
                untrackedMerge();
            }

            if (hContent != null && bContent != null) {
                if (hContent.equals(blob.getValue())) {
                    checkout(bHead.hash(), blob.getKey());
                    add(blob.getKey());
                } else {
                    conflictMerge(blob.getKey(), hContent, bContent);
                }
            } else if (hContent != null) {
                if (hContent.equals(blob.getValue())) {
                    rm(blob.getKey());
                } else {
                    conflictMerge(blob.getKey(), hContent, bContent);
                }
            } else if (bContent != null) {
                conflictMerge(blob.getKey(), hContent, bContent);
            }

        }
    }
}

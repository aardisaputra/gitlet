package gitlet;

import java.io.File;
import java.util.ArrayList;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Austin Nicola Ardisaputra
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        Repo repo = new Repo();
        if (args[0].equals("init")) {
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
            }
            try {
                repo.init();
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }
        File mainDir = new File("./.gitlet");
        if (!mainDir.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
            return;
        }
        if (args[0].equals("log")) {
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
            }
            repo.log();
            return;
        }
        if (args[0].equals("add")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            }
            try {
                repo.add(args[1]);
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }
        if (args[0].equals("commit")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            }
            try {
                repo.commit(args[1], "");
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }
        main2(repo, args);
    }

    public static void main2(Repo repo, String... args) {
        if (args[0].equals("checkout")) {
            try {
                if (args.length == 1) {
                    repo.checkout();
                } else if (args.length == 2) {
                    repo.checkout(args[1], true, false);
                } else if (args.length == 3
                        && args[1].equals("--")) {
                    repo.checkout(args[2], false, false);
                } else if (args.length == 4
                        && args[2].equals("--")) {
                    repo.checkout(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }

        if (args[0].equals("rm")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            }
            repo.rm(args[1]);
            return;
        }
        if (args[0].equals("branch")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            }
            try {
                repo.branch(args[1]);
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }
        main3(repo, args);
    }

    public static void main3(Repo repo, String... args) {
        if (args[0].equals("rm-branch")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            }
            try {
                repo.rmBranch(args[1]);
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }
        if (args[0].equals("global-log")) {
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
            }
            repo.gLog();
            return;
        }
        if (args[0].equals("find")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            }
            ArrayList<String> toPrint = null;
            try {
                toPrint = repo.find(args[1]);
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            for (String line : toPrint) {
                System.out.println(line);
            }
            return;
        }
        if (args[0].equals("reset")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            }
            try {
                repo.reset(args[1]);
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }
        main4(repo, args);
    }

    public static void main4(Repo repo, String... args) {
        if (args[0].equals("status")) {
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
            }
            repo.status();
            return;
        }
        if (args[0].equals("merge")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            }
            try {
                repo.merge(args[1]);
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }
        mainEC(repo, args);

    }

    public static void mainEC(Repo repo, String... args) {
        if (args[0].equals("add-remote")) {
            if (args.length != 3) {
                System.out.println("Incorrect operands.");
            }
            try {
                repo.addRemote(args[1], args[2]);
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }
        if (args[0].equals("rm-remote")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            }
            try {
                repo.rmRemote(args[1]);
            } catch (GitletException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            return;
        }

        System.out.println("No command with that name exists.");
    }
}

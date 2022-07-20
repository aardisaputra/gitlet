package gitlet;
import ucb.junit.textui;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Austin Nicola Ardisaputra
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    /** Init function test. */
    @Test
    public void initTest() {
        Repo repo = new Repo();
        File mainDir = new File("./.gitlet");
        repo.init();
        assertTrue(mainDir.exists());
    }

    @Test
    public void logTest() {
        Repo repo = new Repo();
        repo.log();
    }

    @Test
    public void branchTest() {
        Repo repo = new Repo();
        repo.branch("jj?");
    }

    @Test
    public void treemapTest() {
        "a".equals(null);
    }

    @Test
    public void sepTest() {
        String realPath = "";
        String[] dirWords = new String[]{"jj", "b", "l"};
        for (String jj : dirWords) {
            realPath += (jj + File.separator);
        }
        System.out.println(realPath);
    }
}



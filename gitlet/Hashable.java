package gitlet;

/** An interface describing hashable objects.
 *  @author Austin Nicola Ardisaputra
 */
interface Hashable {
    /** get hash of commit/blob.
     * @return hash value of object */
    String hash();
}

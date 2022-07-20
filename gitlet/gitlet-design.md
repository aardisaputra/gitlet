# Gitlet Design Document
Author: Austin Nicola Ardisaputra

## 1. Classes and Data Structures

### Repo
The main class responsible for most operations.

#### Fields

1. String head: What the HEAD file points to.
2. Stage stage: Staging area.
3. TreeMap<String, String> heads: Branches in the repository.

### Blob
Class object representing the contents of a file. 

#### Fields
1. String content: A string representing the contents of the blob object.

### Commit

A class object that contains log messages, other metadata (commit date, author, etc.), a reference to a tree, and references to parent commits.

#### Fields
1. String log: The commit's log message.
2. String time: The commit's timestamp.
3. TreeMap<String, String> contents: The commit's contents, a mapping of names to blobs.
4. String firstParent: The commit's first parent's filename.
5. String secondParent: The commit's second parent's filename.

### Stage

A class contianing information about the addition stage and removal stage.

#### Fields
1. TreeMap\<String, String> addStage: The staging area for addition.
2. ArrayList\<String> rmStage: Staging area for files to be removed.

## 2. Algorithms

### Main class

1. init(): Initialize a Gitlet repository, unless there is already a Gitlet version-control 
system in the current directory, which should result in an error. It will have a single branch: 
master, which initially points to this initial commit, and master will be the current branch. 
The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970.
2. add(String fileName): Stages the file denoted by fileName for addition unless the current working version 
of the file is identical to the version in the current commit. If the version in the working directory is the 
same as the version added to the stage, remove the file from the stage. 
3. commit(String message): Create new commit with log message denoted by parameter message. By default,
the contents tree of the created object will point to the same blobs as its parent commit's except for files
being staged for addition or removal.
4. rm(String fileName): If the file denoted by fileName is being staged for addition,
unstage it. If not being staged for addition, stage the file for removal.
5. log(): Display information about each commit backwards
, starting at the current head commit, along the commit tree until the initial commit
6. global-log(): Displays information about all commits ever made.
7. find(String message): Prints out the ids of all commits that have the given commit message, one per line.
8. status(): Displays what branches currently exist, and marks the current branch with a *. 
Also displays what files have been staged for addition or removal.
9. checkout(String fileName): Takes the version of the file as it exists in the head commit, the front of the current branch, 
and puts it in the working directory.
10. checkout(String id, String fileName): Takes the version of the file as it exists in the specified commit, the front of the current branch,
and puts it in the working directory.
11. checkout(String branch): Takes all files in the commit at the head of the given branch, and puts them in the working directory, 
overwriting the versions of the files that are already there if they exist.
12. branch(String name): Creates a new branch with the given name, 
and points it at the current head node.
13. rm-branch(String name): Delete the pointer associated with the branch specified
by name.
14. reset(String id): Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. 
Also moves the current branch's head to that commit node.
15. merge(String branch): Merge the given branch to the current branch. If merge conflict occurs,
the user will manually resolve it by editing the file. In the case that two split points exist,
choose the closest one.

## 3. Persistence

All files will be stored in the .gitlet folder.

`java gitlet.Main add fileName`
1. A blob object will be created from the contents of fileName.
2. Get the hash value of the contents and this will be the name of the file within .gitlet that will store the serialized information.
3. If no identical file with the same hash value as its name exists, create a subdirectory
within objects using the first two letters of the hash value and create a new file.
4. Write the serialized content into the file and name it with the hash value of the contents starting
from the 3rd character.
5. Record this data into the addStage field within the Stage object and serialize the Stage object into
the index file within .gitlet.

`java gitlet.Main commit msg`

1. Create a new commit object with a message.
2. Update the commit with all stages denoted in the index file.
3. If no identical file with the same hash value as its name exists, create a subdirectory within objects using the first two letters of the hash value and create a new file.
4. Write the serialized content into the file and name it with the hash value of the contents starting from the 3rd character.
5. Update refs/heads/{current_branch} to contain the new commit hash.

`java gitlet.Main checkout branchName`
1. Update what HEAD points to.

`java gitlet.Main branch branchName`
1. Create a new file in refs/heads/ named branchName. 
2. The content will be the hashcode of the commit that HEAD is pointing to.

`java gitlet.Main rm-branch branchName`
1. Delete the file in refs/heads/ named branchName.

`java gitlet.Main reset id`
1. Change curr branch head to id
2. Change head field.

`java gitlet.Main merge branch`
1. Create a new commit.
2. Change content of file in refs/heads/ named the current branch to point to
the new commit.

## 4. Design Diagram

![](./gitlet-design.jpeg)


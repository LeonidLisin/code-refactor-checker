package ru.etu.sitcenter.counter;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import ru.etu.sitcenter.util.GitUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class RepositoryLineCounter {
    public int countLines(Repository repository, String commitHash) {
        int totalLines = 0;

        try {
            RevCommit latestCommit = GitUtils.getCommitByHash(repository, commitHash);
            TreeWalk treeWalk = new TreeWalk(repository);
            treeWalk.addTree(latestCommit.getTree());
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                String path = treeWalk.getPathString();

                if (!treeWalk.isSubtree() && isCodeFile(path)) {
                    totalLines += countLinesInFile(new File(repository.getWorkTree(), path));
                }
            }
            treeWalk.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return totalLines;
    }


    private static boolean isCodeFile(String path) {
        return path.endsWith(".java") || path.endsWith(".py") || path.endsWith(".js") || path.endsWith(".html");
    }

    private static int countLinesInFile(File file) {
        int lines = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }
}

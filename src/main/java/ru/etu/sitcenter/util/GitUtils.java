package ru.etu.sitcenter.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GitUtils {
    public static Repository openRepository(String repoPath) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            return builder.setGitDir(new File(repoPath + "/.git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RevCommit getCommitByHash(Repository repository, String commitHash) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            return revWalk.parseCommit(repository.resolve(commitHash));
        } catch (Exception e) {
            throw new RuntimeException("Commit with hash " + commitHash + " not found");
        }
    }

    public static RevCommit getLatestCommit(Repository repository) {
        return getCommitByHash(repository, "HEAD");
    }

    public static List<RevCommit> getAllCommits(Repository repository) {
        try (Git git = new Git(repository)) {
            Iterable<RevCommit> commits = git.log().call();
            return StreamSupport.stream(commits.spliterator(), false)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

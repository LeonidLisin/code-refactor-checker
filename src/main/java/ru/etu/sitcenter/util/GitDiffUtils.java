package ru.etu.sitcenter.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import ru.etu.sitcenter.result.DiffResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GitDiffUtils {
    public static Repository openRepository(String repoPath) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.setGitDir(new File(repoPath + "/.git"))
                .readEnvironment()
                .findGitDir()
                .build();
    }

    public static List<RevCommit> getCommits(Repository repository) {
        try (Git git = new Git(repository)) {
            Iterable<RevCommit> commits = git.log().call();
            return StreamSupport.stream(commits.spliterator(), false)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<DiffEntry> getDiffsBetweenCommits(Repository repository,
                                                         RevCommit oldCommit,
                                                         RevCommit newCommit) {
        try (Git git = new Git(repository)) {
            ObjectReader reader = repository.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldCommit.getTree());

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, newCommit.getTree());

            List<DiffEntry> diffs;
            diffs = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();

            return diffs;
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void countChanges(Repository repository,
                                    List<DiffEntry> diffs,
                                    DiffResult diffResult) {
        DiffFormatter diffFormatter = new DiffFormatter(new ByteArrayOutputStream());
        diffFormatter.setRepository(repository);
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
        diffFormatter.setDetectRenames(true);

        int addedLines = 0;
        int deletedLines = 0;

        for (DiffEntry diff : diffs) {
            EditList editList;
            try {
                editList = diffFormatter.toFileHeader(diff).toEditList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (Edit edit : editList) {
                switch (edit.getType()) {
                    case INSERT -> addedLines += edit.getEndB() - edit.getBeginB();
                    case DELETE -> deletedLines += edit.getEndA() - edit.getBeginA();
                    case REPLACE -> {
                        addedLines += edit.getEndB() - edit.getBeginB();
                        deletedLines += edit.getEndA() - edit.getBeginA();
                    }
                }
            }
        }

        int currentAddedLines = diffResult.getAddedLines();
        int currentDeletedLines = diffResult.getDeletedLines();

        diffResult.setAddedLines(currentAddedLines + addedLines);
        diffResult.setDeletedLines(currentDeletedLines + deletedLines);
    }
}

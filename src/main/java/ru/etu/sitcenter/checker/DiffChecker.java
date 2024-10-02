package ru.etu.sitcenter.checker;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import ru.etu.sitcenter.counter.RepositoryLineCounter;
import ru.etu.sitcenter.result.DiffResult;
import ru.etu.sitcenter.result.RefactorResult;
import ru.etu.sitcenter.util.GitUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class DiffChecker {
    private final String beginCommitHash;
    private final Repository repository;
    private final RepositoryLineCounter counter = new RepositoryLineCounter();

    public DiffChecker(Repository repository, String beginCommitHash) {
        this.repository = repository;
        this.beginCommitHash = beginCommitHash;
    }

    public RefactorResult compareCommits() {
        int linesBefore = counter.countLines(repository, beginCommitHash);
        DiffResult diffResult = new DiffResult();
        updateDiffResult(diffResult);

        return new RefactorResult(diffResult, linesBefore);
    }

    private void updateDiffResult(DiffResult diffResult) {
        RevCommit beginCommit = GitUtils.getCommitByHash(repository, beginCommitHash);
        RevCommit latestCommit = GitUtils.getLatestCommit(repository);

        List<DiffEntry> diffs =
                getDiffsBetweenCommits(repository, beginCommit, latestCommit);
        countChanges(repository, diffs, diffResult);
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

        for (DiffEntry diff : diffs) {
            EditList editList = getEdits(diffFormatter, diff);
            updateResultByEdits(diffResult, editList);
        }
    }

    private static void updateResultByEdits(DiffResult diffResult, EditList editList) {
        for (Edit edit : editList) {
            final int addedLines = getAddedLines(edit);
            final int deletedLines = getDeletedLines(edit);
            switch (edit.getType()) {
                case INSERT -> diffResult.increaseAddedLines(addedLines);
                case DELETE -> diffResult.increaseDeletedLines(deletedLines);
                case REPLACE -> diffResult.increaseModifiedLines(addedLines);
            }
        }
    }

    private static EditList getEdits(DiffFormatter diffFormatter, DiffEntry diff) {
        EditList editList;
        try {
            editList = diffFormatter.toFileHeader(diff).toEditList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return editList;
    }

    private static int getDeletedLines(Edit edit) {
        return edit.getEndA() - edit.getBeginA();
    }

    private static int getAddedLines(Edit edit) {
        return edit.getEndB() - edit.getBeginB();
    }
}

package ru.etu.sitcenter.checker;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import ru.etu.sitcenter.result.DiffResult;
import ru.etu.sitcenter.util.GitDiffUtils;
import ru.etu.sitcenter.result.RefactorResult;

import java.io.IOException;
import java.util.List;

public class DiffChecker {
    private final String beginCommitHash;
    private final Repository repository;

    public DiffChecker(String repository, String beginCommitHash) {
        try {
            this.repository = GitDiffUtils.openRepository(repository);
            this.beginCommitHash = beginCommitHash;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RefactorResult compareCommits() {
        List<RevCommit> commits = GitDiffUtils.getCommits(this.repository);
        DiffResult diffResult = new DiffResult();

        updateDiffResult(commits, diffResult);

        return new RefactorResult(diffResult);
    }

    private void updateDiffResult(List<RevCommit> commits, DiffResult diffResult) {
        int beginCommitIndex = getBeginCommitIndex(commits);

        for (int i = 1; i <= beginCommitIndex; i++) {
            RevCommit oldCommit = commits.get(i);
            RevCommit newCommit = commits.get(i - 1);

            List<DiffEntry> diffs =
                    GitDiffUtils.getDiffsBetweenCommits(repository, oldCommit, newCommit);
            GitDiffUtils.countChanges(repository, diffs, diffResult);
        }
    }

    private int getBeginCommitIndex(List<RevCommit> commits) {
        for (int i = 0; i < commits.size(); i++) {
            if (this.beginCommitHash.equals(commits.get(i).getName())) {
                return i;
            }
        }

        throw new RuntimeException("Commit with hash " + beginCommitHash + " not found");
    }
}

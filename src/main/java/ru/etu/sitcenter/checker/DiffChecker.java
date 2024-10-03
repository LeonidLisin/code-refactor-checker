package ru.etu.sitcenter.checker;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import ru.etu.sitcenter.counter.RepositoryLineCounter;
import ru.etu.sitcenter.result.DiffResult;
import ru.etu.sitcenter.result.RefactorResult;
import ru.etu.sitcenter.util.GitUtils;

import java.io.*;
import java.util.List;

public class DiffChecker {
    private final Repository repository;
    private final RevCommit latestCommit;
    private final RevCommit beginCommit;
    private final RepositoryLineCounter counter = new RepositoryLineCounter();

    public DiffChecker(Repository repository, String beginCommitHash) {
        this.repository = repository;
        this.latestCommit = GitUtils.getLatestCommit(repository);
        this.beginCommit = GitUtils.getCommitByHash(repository, beginCommitHash);
    }

    public RefactorResult compareCommits() {
//        int linesBefore = counter.countLines(repository, beginCommitHash);
        DiffResult diffResult = new DiffResult();
        updateDiffResult(diffResult);

        return new RefactorResult(diffResult, 0);
    }

    private void updateDiffResult(DiffResult diffResult) {
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

    public void countChanges(Repository repository,
                             List<DiffEntry> diffs,
                             DiffResult diffResult) {
        DiffFormatter diffFormatter = new DiffFormatter(new ByteArrayOutputStream());
        diffFormatter.setRepository(repository);
        diffFormatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        diffFormatter.setDetectRenames(true);

        for (DiffEntry diff : diffs) {
            EditList editList = getEdits(diffFormatter, diff);
            updateResultByEdits(diffResult, editList, diff.getNewPath());
        }
    }

    private void updateResultByEdits(DiffResult diffResult, EditList editList, String path) {
        for (Edit edit : editList) {
            int beginA = edit.getBeginA(), endA = edit.getEndA();
            int beginB = edit.getBeginB(), endB = edit.getEndB();
            switch (edit.getType()) {
                case INSERT -> updateChangedLines(beginB, endB, diffResult::increaseAddedLines, path);
                case DELETE -> diffResult.increaseDeletedLines(endA - beginA);
                case REPLACE -> updateChangedLines(beginA, endA, diffResult::increaseModifiedLines, path);
            }
        }
    }

    private void updateChangedLines(int begin, int end, Runnable runnable, String path) {
        for (int i = begin; i < end; i++) {
            String lineFromCommit = getLineFromCommit(i, path);
            if (!lineFromCommit.startsWith("import") &&
                    !lineFromCommit.startsWith("package")) {
                runnable.run();
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

    private String getLineFromCommit(int lineIndex, String path) {
        ObjectReader reader = repository.newObjectReader();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(latestCommit.getTree());
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(path));
            if (treeWalk.next()) {
                ObjectId objectId = treeWalk.getObjectId(0);
                byte[] content = reader.open(objectId).getBytes();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)))) {
                    String line;
                    int currentLine = 0;
                    while ((line = br.readLine()) != null) {
                        if (currentLine == lineIndex) {
                            return line;
                        }
                        currentLine++;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "";
    }
}

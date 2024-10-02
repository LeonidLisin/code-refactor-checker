package ru.etu.sitcenter;

import org.eclipse.jgit.lib.Repository;
import ru.etu.sitcenter.checker.DiffChecker;
import ru.etu.sitcenter.result.RefactorResult;
import ru.etu.sitcenter.util.GitUtils;

public class Main {
    public static void main(String[] args) {
        String repositoryPath = "c:/work/test/calendar";
        String beginCommitHash = "0d3df63c357ca187278b3717a44d55c815664dce";
        Repository repository = GitUtils.openRepository(repositoryPath);
        DiffChecker checker = new DiffChecker(repository, beginCommitHash);

        RefactorResult refactorResult = checker.compareCommits();
        double refactorPercentage = refactorResult.getRefactorPercentage();
    }
}
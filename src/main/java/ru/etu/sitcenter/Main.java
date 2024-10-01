package ru.etu.sitcenter;

import ru.etu.sitcenter.checker.DiffChecker;
import ru.etu.sitcenter.result.RefactorResult;

public class Main {
    public static void main(String[] args) {
        String repositoryPath = "c:/work/test/calendar";
        String beginCommitHash = "0d3df63c357ca187278b3717a44d55c815664dce";
        DiffChecker checker = new DiffChecker(repositoryPath, beginCommitHash);
        RefactorResult refactorResult = checker.compareCommits();
    }
}
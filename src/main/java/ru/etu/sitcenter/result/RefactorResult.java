package ru.etu.sitcenter.result;

public class RefactorResult {
    private final DiffResult diffResult;

    public DiffResult getDiffResult() {
        return diffResult;
    }

    public RefactorResult(DiffResult diffResult) {
        this.diffResult = diffResult;
    }
}

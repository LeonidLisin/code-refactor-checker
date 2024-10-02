package ru.etu.sitcenter.result;

public class RefactorResult {
    private final DiffResult diffResult;
    private final Integer linesBefore;

    public DiffResult getDiffResult() {
        return diffResult;
    }

    public RefactorResult(DiffResult diffResult, int linesBefore) {
        this.diffResult = diffResult;
        this.linesBefore = linesBefore;
    }

    public double getRefactorPercentage() {
        return (diffResult.getRefactoredLinesCount()/linesBefore.doubleValue())*100;
    }
}

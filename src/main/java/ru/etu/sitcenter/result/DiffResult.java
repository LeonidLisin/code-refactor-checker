package ru.etu.sitcenter.result;

public class DiffResult {
    private int addedLines;
    private int deletedLines;
    private int modifiedLines;

    public void increaseAddedLines() {
        this.addedLines += 1;
    }

    public void increaseDeletedLines(int deletedLines) {
        this.deletedLines += deletedLines;
    }

    public void increaseModifiedLines() {
        this.modifiedLines += 1;
    }

    public int getRefactoredLinesCount() {
        return this.addedLines + this.deletedLines + this.modifiedLines;
    }
}

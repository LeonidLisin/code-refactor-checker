package ru.etu.sitcenter.result;

public class DiffResult {
    private int addedLines;
    private int deletedLines;
    private int modifiedLines;

    public void increaseAddedLines(int newAddedLines) {
        this.addedLines += newAddedLines;
    }

    public void increaseDeletedLines(int newDeletedLines) {
        this.deletedLines += newDeletedLines;
    }

    public void increaseModifiedLines(int newModifiedLines) {
        this.modifiedLines += newModifiedLines;
    }

    public int getAddedLines() {
        return addedLines;
    }

    public int getDeletedLines() {
        return deletedLines;
    }

    public int getModifiedLines() {
        return modifiedLines;
    }
}

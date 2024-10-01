package ru.etu.sitcenter.result;

public class DiffResult {
    private int addedLines;
    private int deletedLines;

    public void increaseAddedLines(int newAddedLines) {
        this.addedLines += newAddedLines;
    }

    public void increaseDeletedLines(int newDeletedLines) {
        this.deletedLines += newDeletedLines;
    }

    public int getAddedLines() {
        return addedLines;
    }

    public int getDeletedLines() {
        return deletedLines;
    }
}

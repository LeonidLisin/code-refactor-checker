package ru.etu.sitcenter.result;

public class DiffResult {
    private int addedLines;
    private int deletedLines;

    public void setAddedLines(int addedLines) {
        this.addedLines = addedLines;
    }

    public void setDeletedLines(int deletedLines) {
        this.deletedLines = deletedLines;
    }

    public int getAddedLines() {
        return addedLines;
    }

    public int getDeletedLines() {
        return deletedLines;
    }
}

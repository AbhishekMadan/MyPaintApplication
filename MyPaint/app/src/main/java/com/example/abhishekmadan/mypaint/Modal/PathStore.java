package com.example.abhishekmadan.mypaint.Modal;

import android.graphics.Path;

/**
 * Modal class to store the path drawn by the User.
 * The object is later used to draw a line in the DrawingCanvas class.
 */
public class PathStore {

    private Path drawnPath;

    private int pathColor;

    private int pathStroke;

    public PathStore(Path drawnPath, int pathColor, int pathStroke) {
        this.drawnPath = drawnPath;
        this.pathColor = pathColor;
        this.pathStroke = pathStroke;
    }

    public Path getDrawnPath() {
        return drawnPath;
    }

    public void setDrawnPath(Path drawnPath) {
        this.drawnPath = drawnPath;
    }

    public int getPathColor() {
        return pathColor;
    }

    public void setPathColor(int pathColor) {
        this.pathColor = pathColor;
    }

    public int getPathStroke() {
        return pathStroke;
    }

    public void setPathStroke(int pathStroke) {
        this.pathStroke = pathStroke;
    }
}

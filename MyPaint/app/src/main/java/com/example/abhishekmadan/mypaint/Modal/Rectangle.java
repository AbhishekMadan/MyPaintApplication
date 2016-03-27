package com.example.abhishekmadan.mypaint.Modal;

/**
 * Modal class to store the Rectangle dimensions entered by the User.
 * This dimensions are used to draw the rectangle in the DrawingCanvas class.
 */
public class Rectangle extends Shape{

    private int top ;
    private int bottom;
    private int left;
    private int right;

    private int color;


    public Rectangle(int top, int bottom, int left, int right, int color) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.color = color;
    }

    public Rectangle(Rectangle rect){
        this.top = rect.top;
        this.bottom = rect.bottom;
        this.left = rect.left;
        this.right = rect.right;
        this.color = rect.color;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}

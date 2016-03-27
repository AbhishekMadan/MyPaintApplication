package com.example.abhishekmadan.mypaint.Modal;

import android.graphics.Paint;

/**
 * Modal class to store the Circle dimensions entered by the User.
 * This dimensions are used to draw the circle in the DrawingCanvas class.
 */
public class Circle extends Shape{
    private int centerX;
    private int centerY;
    private int radius;

    private int circleColor;

    public Circle(int centerX, int centerY, int radius, int circleColor) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.circleColor = circleColor;
    }


    public Circle(Circle circle){
        this.centerX = circle.getCenterX();
        this.centerY = circle.getCenterY();
        this.radius = circle.getRadius();
        this.circleColor = circle.getCircleColor();
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getCircleColor() {
        return circleColor;
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
    }
}

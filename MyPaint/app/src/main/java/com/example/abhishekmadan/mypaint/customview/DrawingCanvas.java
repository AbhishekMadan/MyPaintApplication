package com.example.abhishekmadan.mypaint.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.abhishekmadan.mypaint.Modal.Circle;
import com.example.abhishekmadan.mypaint.Modal.InputText;
import com.example.abhishekmadan.mypaint.Modal.Oval;
import com.example.abhishekmadan.mypaint.Modal.PathStore;
import com.example.abhishekmadan.mypaint.Modal.Rectangle;
import com.example.abhishekmadan.mypaint.Modal.Shape;
import com.example.abhishekmadan.mypaint.activity.DrawingBoard;
import com.example.abhishekmadan.mypaint.util.Constants;

import java.util.ArrayList;

/**
 * Class which forms the canvas view.
 * All the shapes are drawn on this view.
 */
public class DrawingCanvas extends View {

    private int mCanvasRightBounds;

    private int mCanvasBottomBounds;
    //variables to store the bounds of the canvas
    private int mCanvasLeftBounds = 1;

    private int mCanvasTopBounds = 1;

    private Paint mBrushPaint;

    private Path mBrushPath;

    private Paint mEraserPaint;

    private Context mContext;

    private int xPos;

    private int yPos;

    private static int mCurrentOperation;

    //Moving of view operation
    private static int mStorePrevOperationOnLongPress;

    private static int mIndexOfViewInListToMove;

    private static Shape mShapeToMove;

    //path operations
    private Paint mPathPaint;

    //Circle operations
    private int mCircleRadius;

    private Paint mCirclePaint;

    //Rectangle Operations
    private int mRectangleWidth;

    private int mRectangleHeight;

    //TextBox Operations
    private String mTextBoxText;

    private int mTextBoxSize;

    private Rect mTextBoxBounds;

    private static ArrayList mShapeList;

    //Detection of Gesture
    private GestureDetector mDetectGesture;

    private Vibrator mVibrator;

    //Canvas Background
    private Bitmap mCanvasBackground;

    public DrawingCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initialize();
    }

    public void initialize() {

        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mBrushPaint = new Paint();
        mBrushPaint.setAntiAlias(true);
        changeBrushColor("#000000");
        changeFillStyle(Constants.PAINT_STYLE_STROKE);
        mBrushPaint.setStrokeJoin(Paint.Join.ROUND);
        mBrushPaint.setStrokeCap(Paint.Cap.ROUND);
        mBrushPath = new Path();
        mEraserPaint = new Paint();
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        changeBrushStroke(1);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setColor(Color.parseColor("#ffffff"));
        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        mShapeList = new ArrayList();
        mDetectGesture = new GestureDetector(mContext, new CustomGestureDetector());
        mIndexOfViewInListToMove = -1;
        mShapeToMove = null;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mCanvasBackground!=null){
            mCanvasBackground = Bitmap.createScaledBitmap(mCanvasBackground,canvas.getWidth(),canvas.getHeight(),true);
            canvas.drawBitmap(mCanvasBackground,0f,0f,null);
        }
        for (Object obj : mShapeList) {
            if (obj instanceof PathStore) {
                PathStore path = (PathStore) obj;
                mPathPaint.setColor(path.getPathColor());
                mPathPaint.setStrokeWidth(path.getPathStroke());
                canvas.drawPath(path.getDrawnPath(), mPathPaint);
            } else if (obj instanceof Circle) {
                Circle cir = (Circle) obj;
                mCirclePaint.setColor(cir.getCircleColor());
                canvas.drawCircle(cir.getCenterX(),
                        cir.getCenterY(),
                        cir.getRadius(),
                        mCirclePaint);
            } else if (obj instanceof Rectangle) {
                Rectangle rect = (Rectangle) obj;
                mCirclePaint.setColor(rect.getColor());
                canvas.drawRect(rect.getLeft(), rect.getTop(), rect.getRight(), rect.getBottom(), mCirclePaint);
            } else if (obj instanceof Oval) {
                Oval oval = (Oval) obj;
                mCirclePaint.setColor(oval.getColor());
                canvas.drawOval(new RectF(oval.getLeft(), oval.getTop(), oval.getRight(), oval.getBottom()), mCirclePaint);
            } else if (obj instanceof InputText) {
                InputText text = (InputText) obj;
                mCirclePaint.setColor(text.getTextColor());
                mCirclePaint.setTextSize(text.getTextSize());
                mTextBoxBounds = new Rect();
                mCirclePaint.getTextBounds(text.getTextInput(), 0, text.getTextInput().length(), mTextBoxBounds);
                text.setTextHeight(mTextBoxBounds.height());
                text.setTextWidth(mTextBoxBounds.width());
                canvas.drawText(text.getTextInput(), text.getxLocation(), text.getyLocation(), mCirclePaint);
            }
        }
        if (getCurrentOperation() == Constants.OPERATION_DRAW_PENCIL) {
            canvas.drawPath(mBrushPath, mBrushPaint);
        } else if (getCurrentOperation() == Constants.OPERATION_ERASE) {
            canvas.drawPath(mBrushPath, mEraserPaint);
        }
    }

    /**
     * method to get the new dimensions of the canvas on change of view.
     * @param w is the new width of the canvas
     * @param h is the new height of the canvas
     * @param oldw is the old width of the canvas
     * @param oldh is the new height of the casvas
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasRightBounds = w;
        mCanvasBottomBounds = h;
    }

    /**
     * Motion event to track the touch on the screen and draw the shapes as per the operations
     * reflected by the stored variable mCurrentOperation
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchEvent = event.getAction();
        xPos = (int) event.getX();
        yPos = (int) event.getY();

        mDetectGesture.onTouchEvent(event);
        switch (touchEvent) {
            case MotionEvent.ACTION_DOWN:
                if (mCurrentOperation == Constants.OPERATION_DRAW_PENCIL || mCurrentOperation == Constants.OPERATION_ERASE) {
                    mBrushPath.moveTo(xPos, yPos);
                    mBrushPath.lineTo(xPos, yPos);
                } else if (mCurrentOperation == Constants.OPERATION_DRAW_CIRCLE) {
                    mShapeList.add(new Circle(xPos, yPos, mCircleRadius, mBrushPaint.getColor()));
                    setCurrentOperation(Constants.OPERATION_NO_OPERATION);
                } else if (mCurrentOperation == Constants.OPERATION_DRAW_RECTANGLE) {
                    mShapeList.add(new Rectangle(yPos - ((int) mRectangleHeight / 2),
                            yPos + ((int) mRectangleHeight / 2),
                            xPos - ((int) mRectangleWidth / 2),
                            xPos + ((int) mRectangleWidth / 2),
                            mBrushPaint.getColor()));
                    setCurrentOperation(Constants.OPERATION_NO_OPERATION);
                } else if (mCurrentOperation == Constants.OPERATION_DRAW_OVAL) {
                    mShapeList.add(new Oval(yPos - ((int) mRectangleHeight / 2),
                            yPos + ((int) mRectangleHeight / 2),
                            xPos - ((int) mRectangleWidth / 2),
                            xPos + ((int) mRectangleWidth / 2),
                            mBrushPaint.getColor()));
                    setCurrentOperation(Constants.OPERATION_NO_OPERATION);
                } else if (mCurrentOperation == Constants.OPERATION_INSERT_TEXT) {
                    mShapeList.add(new InputText(mTextBoxText, mTextBoxSize, xPos, yPos, mBrushPaint.getColor(), 0, 0));
                    setCurrentOperation(Constants.OPERATION_NO_OPERATION);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrentOperation == Constants.OPERATION_DRAW_PENCIL || mCurrentOperation == Constants.OPERATION_ERASE) {
                    mBrushPath.lineTo(xPos, yPos);
                } else if (mCurrentOperation == Constants.OPERATION_MOVE_VIEW) {
                    if (mCanvasLeftBounds < xPos && xPos < mCanvasRightBounds && mCanvasTopBounds < yPos && yPos < mCanvasBottomBounds) {
                        if (mShapeToMove instanceof Circle) {
                            ((Circle) mShapeToMove).setCenterX(xPos);
                            ((Circle) mShapeToMove).setCenterY(yPos);

                        } else if (mShapeToMove instanceof Rectangle) {
                            int left = ((Rectangle) mShapeToMove).getLeft();
                            int right = ((Rectangle) mShapeToMove).getRight();
                            int top = ((Rectangle) mShapeToMove).getTop();
                            int bottom = ((Rectangle) mShapeToMove).getBottom();
                            int width = Math.abs(right - left);
                            int height = Math.abs(top - bottom);
                            ((Rectangle) mShapeToMove).setLeft(xPos - (width / 2));
                            ((Rectangle) mShapeToMove).setRight(xPos + (width / 2));
                            ((Rectangle) mShapeToMove).setTop(yPos - (height / 2));
                            ((Rectangle) mShapeToMove).setBottom(yPos + (height / 2));

                        } else if (mShapeToMove instanceof Oval) {
                            int left = ((Oval) mShapeToMove).getLeft();
                            int right = ((Oval) mShapeToMove).getRight();
                            int top = ((Oval) mShapeToMove).getTop();
                            int bottom = ((Oval) mShapeToMove).getBottom();
                            int width = Math.abs(right - left);
                            int height = Math.abs(top - bottom);
                            ((Oval) mShapeToMove).setLeft(xPos - (width / 2));
                            ((Oval) mShapeToMove).setRight(xPos + (width / 2));
                            ((Oval) mShapeToMove).setTop(yPos - (height / 2));
                            ((Oval) mShapeToMove).setBottom(yPos + (height / 2));

                        } else if (mShapeToMove instanceof InputText) {
                            ((InputText) mShapeToMove).setxLocation(xPos);
                            ((InputText) mShapeToMove).setyLocation(yPos);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCurrentOperation == Constants.OPERATION_DRAW_PENCIL) {
                    mShapeList.add(new PathStore(mBrushPath, mBrushPaint.getColor(), (int) mBrushPaint.getStrokeWidth()));
                    mBrushPath = new Path();
                } else if (mCurrentOperation == Constants.OPERATION_ERASE) {
                    mShapeList.add(new PathStore(mBrushPath, mEraserPaint.getColor(), (int) mEraserPaint.getStrokeWidth()));
                    mBrushPath = new Path();
                } else if (mCurrentOperation == Constants.OPERATION_MOVE_VIEW) {
                    mCurrentOperation = mStorePrevOperationOnLongPress;
                    mShapeList.remove(mIndexOfViewInListToMove);
                    mShapeList.add(mShapeToMove);
                    mIndexOfViewInListToMove = -1;
                    mShapeToMove = null;
                }
                break;
        }
        invalidate();
        return true;
    }

    public void drawCircle(int radius) {
        mCircleRadius = radius;
    }

    public void drawRectangle(int width, int height) {
        mRectangleWidth = width;
        mRectangleHeight = height;
    }

    public void createTextBox(String text, int textSize) {
        mTextBoxText = text;
        mTextBoxSize = textSize;
    }

    public void changeFillStyle(int style) {
        switch (style) {
            case Constants.PAINT_STYLE_FILL:
                mBrushPaint.setStyle(Paint.Style.FILL);
                break;
            case Constants.PAINT_STYLE_STROKE:
                mBrushPaint.setStyle(Paint.Style.STROKE);
                break;
        }
    }

    public void changeBrushStroke(int stroke) {
        mBrushPaint.setStrokeWidth(stroke);
        mEraserPaint.setStrokeWidth(stroke * 2);
    }

    public void changeBrushColor(String col) {
        mBrushPaint.setColor(Color.parseColor(col));
    }

    public int getCurrentOperation() {
        return mCurrentOperation;
    }

    public void setCurrentOperation(int operation) {
        if (operation == Constants.OPERATION_DRAW_PENCIL) {
            changeFillStyle(Constants.PAINT_STYLE_STROKE);
        }
        mCurrentOperation = operation;
    }

    public void clearCompleteCanvas() {
        mShapeList.clear();
        invalidate();
    }

    public String getBrushColor() {
        return Integer.toHexString(mBrushPaint.getColor()).toUpperCase().substring(2);
    }

    public void undoPreviousOperation() {
        if (mShapeList != null && mShapeList.size() != 0) {
            mShapeList.remove(mShapeList.size() - 1);
        }else if (mShapeList == null || mShapeList.size() == 0){
            mCanvasBackground =null;
        }
        invalidate();
    }

    public class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private Context mDetectorContext;
        private int xTouchPos;
        private int yTouchPos;

        @Override
        public void onLongPress(MotionEvent e) {

            xTouchPos = (int) e.getX();
            yTouchPos = (int) e.getY();

            if (mShapeList != null && mShapeList.size() > 0) {

                Object obj = mShapeList.get(mShapeList.size() - 1);
                if (obj instanceof Circle) {
                    Circle cir = (Circle) obj;
                    if (isPointInCircle(xTouchPos, yTouchPos, cir)) {
                        mStorePrevOperationOnLongPress = mCurrentOperation;
                        mCurrentOperation = Constants.OPERATION_MOVE_VIEW;
                        mVibrator.vibrate(200);
                        mIndexOfViewInListToMove = mShapeList.size() - 1;
                        mShapeToMove = (Circle) mShapeList.get(mIndexOfViewInListToMove);
                    }
                } else if (obj instanceof Rectangle) {
                    Rectangle rect = (Rectangle) obj;
                    if (isPointInRectangle(xTouchPos, yTouchPos, rect)) {
                        mStorePrevOperationOnLongPress = mCurrentOperation;
                        mCurrentOperation = Constants.OPERATION_MOVE_VIEW;
                        mVibrator.vibrate(200);
                        mIndexOfViewInListToMove = mShapeList.size() - 1;
                        mShapeToMove = (Rectangle) mShapeList.get(mIndexOfViewInListToMove);
                    }
                } else if (obj instanceof Oval) {
                    Oval oval = (Oval) obj;
                    if (isPointInOval(xTouchPos, yTouchPos, oval)) {
                        mStorePrevOperationOnLongPress = mCurrentOperation;
                        mCurrentOperation = Constants.OPERATION_MOVE_VIEW;
                        mVibrator.vibrate(200);
                        mIndexOfViewInListToMove = mShapeList.size() - 1;
                        mShapeToMove = (Oval) mShapeList.get(mIndexOfViewInListToMove);
                    }
                } else if (obj instanceof InputText) {
                    InputText text = (InputText) obj;
                    if (isPointInTextBox(xTouchPos, yTouchPos, text)) {
                        mStorePrevOperationOnLongPress = mCurrentOperation;
                        mCurrentOperation = Constants.OPERATION_MOVE_VIEW;
                        mVibrator.vibrate(200);
                        mIndexOfViewInListToMove = mShapeList.size() - 1;
                        mShapeToMove = (InputText) mShapeList.get(mIndexOfViewInListToMove);
                    }
                }
            }
        }

        /**
         * method in invoked when the user double clicks the shape. This event is used to change
         * the color of the view by opening the color drawer.
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            xTouchPos = (int) e.getX();
            yTouchPos = (int) e.getY();
            if (mShapeList != null && mShapeList.size() > 0) {

                Object obj = mShapeList.get(mShapeList.size() - 1);
                if (obj instanceof Circle) {
                    Circle cir = (Circle) obj;
                    if (isPointInCircle(xTouchPos, yTouchPos, cir)) {
                        DrawingBoard.openColorDrawer();
                        mCurrentOperation = Constants.OPERATION_FILL_VIEW;
                    }
                } else if (obj instanceof Rectangle) {
                    Rectangle rect = (Rectangle) obj;
                    if (isPointInRectangle(xTouchPos, yTouchPos, rect)) {
                        DrawingBoard.openColorDrawer();
                        mCurrentOperation = Constants.OPERATION_FILL_VIEW;
                    }
                } else if (obj instanceof Oval) {
                    Oval oval = (Oval) obj;
                    if (isPointInOval(xTouchPos, yTouchPos, oval)) {
                        DrawingBoard.openColorDrawer();
                        mCurrentOperation = Constants.OPERATION_FILL_VIEW;
                    }
                } else if (obj instanceof InputText) {
                    InputText text = (InputText) obj;
                    if (isPointInTextBox(xTouchPos, yTouchPos, text)) {
                        DrawingBoard.openColorDrawer();
                        mCurrentOperation = Constants.OPERATION_FILL_VIEW;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Method to check if the point at which the user has touched is inside the circle or not.
     * @param xTouch is the x coordinate of the screen where the touch is detected.
     * @param yTouch is the x coordinate of the screen where the touch is detected.
     * @param circle is the circle object to check
     * @return
     */
    public boolean isPointInCircle(int xTouch, int yTouch, Circle circle) {
        int xCenter = circle.getCenterX();
        int yCenter = circle.getCenterY();
        double distanceOfPointFromCenter = 0;
        distanceOfPointFromCenter = Math.sqrt(Math.pow(xCenter - xTouch, 2) + Math.pow(yCenter - yTouch, 2));

        if (distanceOfPointFromCenter > (circle.getRadius()))
            return false;
        else {
            return true;
        }
    }

    /**
     *  Method to check if the point at which the user has touched is inside the rectangle or not.
     * @param xTouch is the x coordinate of the screen where the touch is detected.
     * @param yTouch is the y coordinate of the screen where the touch is detected.
     * @param rectangle is the object to check.
     * @return
     */
    public boolean isPointInRectangle(int xTouch, int yTouch, Rectangle rectangle) {

        int top = rectangle.getTop();
        int bottom = rectangle.getBottom();
        int left = rectangle.getLeft();
        int right = rectangle.getRight();

        if (left < xTouch && xTouch < right) {
            if (top < yTouch && yTouch < bottom) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Method to check if the point at which the user has touched is inside the oval or not.
     * @param xTouch is the x coordinate of the screen where the touch is detected.
     * @param yTouch is the y coordinate of the screen where the touch is detected.
     * @param oval is the object to check.
     * @return
     */
    public boolean isPointInOval(int xTouch, int yTouch, Oval oval) {

        int top = oval.getTop();
        int bottom = oval.getBottom();
        int left = oval.getLeft();
        int right = oval.getRight();

        double relativeX = (((double) left + right) / 2) - xTouch;
        double relativeY = (((double) top + bottom) / 2) - yTouch;

        double majorAxis = (double) Math.abs(right - left) / 2;
        double minorAxis = (double) Math.abs(bottom - top) / 2;

        if ((Math.pow(relativeX, 2) / Math.pow(majorAxis, 2)) + (Math.pow(relativeY, 2) / Math.pow(minorAxis, 2)) < 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method to check if the point at which the user has touched is inside the TextBox or not.
     * @param xTouch is the x coordinate of the screen where the touch is detected.
     * @param yTouch is the y coordinate of the screen where the touch is detected.
     * @param textBox is the object to check.
     * @return
     */
    public boolean isPointInTextBox(int xTouch, int yTouch, InputText textBox) {

        int width = textBox.getTextWidth();
        int heigth = textBox.getTextHeight();
        int xPosition = textBox.getxLocation();
        int yPosition = textBox.getyLocation();

        int top = Math.abs(yPosition - heigth);
        int bottom = yPosition;
        int left = xPosition;
        int right = Math.abs(xPosition + width);

        if (left < xTouch && xTouch < right) {
            if (top < yTouch && yTouch < bottom) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to set the color chosen by the user to the view on which the double tap event
     * was detected.
     */
    public void applyColorToView() {
        if (mCurrentOperation == Constants.OPERATION_FILL_VIEW) {
            Object obj = mShapeList.get(mShapeList.size() - 1);
            if (obj instanceof Circle) {
                Circle circ = (Circle) obj;
                       circ.setCircleColor(mBrushPaint.getColor());
            } else if (obj instanceof Rectangle) {
                Rectangle rect = (Rectangle) obj;
                        rect.setColor(mBrushPaint.getColor());
            } else if (obj instanceof Oval) {
                Oval oval = (Oval) obj;
                       oval.setColor(mBrushPaint.getColor());
            } else if (obj instanceof InputText) {
                InputText text = (InputText) obj;
                text.setTextColor(mBrushPaint.getColor());
            }
            invalidate();
            mCurrentOperation = Constants.OPERATION_NO_OPERATION;
        }
    }


    public void setCanvasBackground(Bitmap background){
        mCanvasBackground = background;
        invalidate();
    }

}

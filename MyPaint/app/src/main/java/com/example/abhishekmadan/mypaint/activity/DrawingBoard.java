package com.example.abhishekmadan.mypaint.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import com.example.abhishekmadan.mypaint.R;
import com.example.abhishekmadan.mypaint.adapter.OperationRecyclerViewAdapter;
import com.example.abhishekmadan.mypaint.service.UploadImageToServer;
import com.example.abhishekmadan.mypaint.util.Constants;
import com.example.abhishekmadan.mypaint.customview.DrawingCanvas;
import com.example.abhishekmadan.mypaint.util.SavePhotoUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;



/**
 * Activity screen holding the top seek bar , the drawing canvas and the
 * tool kit visible at the bottom of the screen.
 */
public class DrawingBoard extends AppCompatActivity implements View.OnClickListener,
        OperationRecyclerViewAdapter.OperationViewHolder.OperationCommunicator,
        SeekBar.OnSeekBarChangeListener {

    //recycler view which show the various tools
    private RecyclerView mOperationRecyclerView;
    //navigation drawer showing the color pallet
    private NavigationView mColorPalletNavView;

    private static DrawerLayout mColorPalletDrawer;

    private SeekBar mBrushStrokeSeeKBar;

    private DrawingCanvas mPaintCanvas;

    private LayoutInflater mInflater;
    //Color tiles visible in drawer
    private ImageView mBlackColor;

    private ImageView mBlueColor;

    private ImageView mGreenColor;

    private ImageView mOrangeColor;

    private ImageView mPinkColor;

    private ImageView mRedColor;

    private ImageView mSkyblueColor;

    private ImageView mVioletColor;

    private ImageView mWhiteColor;

    private ImageView mCurrentColorSelection;

    private Handler mHandler = new Handler();

    private AlertDialog.Builder mBuilder;

    private AlertDialog mDialog;

    public static byte[] sBitmapBytes;

    public static String sEncodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_board);
        init();
    }

    public void init() {

        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mColorPalletDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mOperationRecyclerView = (RecyclerView) findViewById(R.id.oper_recyclerview);
        OperationRecyclerViewAdapter adapter = new OperationRecyclerViewAdapter(DrawingBoard.this, (OperationRecyclerViewAdapter.OperationViewHolder.OperationCommunicator) DrawingBoard.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(DrawingBoard.this, LinearLayoutManager.HORIZONTAL, false);
        mOperationRecyclerView.setLayoutManager(layoutManager);
        mOperationRecyclerView.setAdapter(adapter);
        /*
          getting reference to all the color pallet in the navigation drawer so as to listen to the
          click event and change the color of the brush accordingly
         */
        mColorPalletNavView = (NavigationView) findViewById(R.id.navigation_view);
        mBlackColor = (ImageView) findViewById(R.id.black_color);
        mBlackColor.setOnClickListener(this);
        mBlueColor = (ImageView) findViewById(R.id.blue_color);
        mBlueColor.setOnClickListener(this);
        mGreenColor = (ImageView) findViewById(R.id.green_color);
        mGreenColor.setOnClickListener(this);
        mOrangeColor = (ImageView) findViewById(R.id.orange_color);
        mOrangeColor.setOnClickListener(this);
        mPinkColor = (ImageView) findViewById(R.id.pink_color);
        mPinkColor.setOnClickListener(this);
        mSkyblueColor = (ImageView) findViewById(R.id.skyblue_color);
        mSkyblueColor.setOnClickListener(this);
        mRedColor = (ImageView) findViewById(R.id.red_color);
        mRedColor.setOnClickListener(this);
        mVioletColor = (ImageView) findViewById(R.id.violet_color);
        mVioletColor.setOnClickListener(this);
        mWhiteColor = (ImageView) findViewById(R.id.white_color);
        mWhiteColor.setOnClickListener(this);
        mCurrentColorSelection = (ImageView) findViewById(R.id.current_color_selection);
        /*
          Seek bar to change the stroke width of the brush and the eraser
         */
        mBrushStrokeSeeKBar = (SeekBar) findViewById(R.id.brush_thickness_seek_bar);
        mBrushStrokeSeeKBar.setOnSeekBarChangeListener(this);
        mPaintCanvas = (DrawingCanvas) findViewById(R.id.drawing_canvas);

    }

    /**
     * Method to open the navigation drawer
     */
    public static void openColorDrawer() {
        if (mColorPalletDrawer != null) {
            mColorPalletDrawer.openDrawer(Gravity.LEFT);
        }
    }

    /**
     * Method to close the navigation drawer
     */
    public static void closeColorDrawer() {
        if (mColorPalletDrawer != null) {
            mColorPalletDrawer.closeDrawer(Gravity.LEFT);
        }
    }

    /**
     * Method gets called when the user selects a color from the navigation drawer
     * @param v is the image view clicked
     */
    @Override
    public void onClick(View v) {

        //if current operation is 'Eraser' then avoid the user from changing the color
        if (mPaintCanvas.getCurrentOperation() != Constants.OPERATION_ERASE) {
            ImageView colorView = (ImageView) v;
            mPaintCanvas.changeBrushColor(colorView.getTag().toString());
            ((GradientDrawable) mCurrentColorSelection.getBackground()).setColor(Color.parseColor(colorView.getTag().toString()));
            if (mPaintCanvas.getCurrentOperation() == Constants.OPERATION_FILL_VIEW)
                mPaintCanvas.applyColorToView();

        } else {
            Toast.makeText(DrawingBoard.this, "Operation not allowed with current selection!", Toast.LENGTH_SHORT).show();
        }
        //close the drawer once the user select the color.
        closeColorDrawer();
    }

    /**
     * overridden method of the communicator interface. This method receives a callback when the user
     * clicks one of the tool from the tool kit (recycler view)
     * @param position is the position of the view in the recycler view being clicked
     */
    @Override
    public void getPosition(int position) {

        switch (position) {
            case Constants.OPERATION_DRAW_PENCIL:
                mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_PENCIL);
                mPaintCanvas.changeFillStyle(Constants.PAINT_STYLE_STROKE);
                break;
            case Constants.OPERATION_ERASE:
                mPaintCanvas.setCurrentOperation(Constants.OPERATION_ERASE);
                break;
            case Constants.OPERATION_CLEAR_CANVAS:
                mBuilder = new AlertDialog.Builder(DrawingBoard.this);
                mBuilder.setTitle("New Drawing?");
                mBuilder.setMessage("Start new drawing (you will lose the current drawing)?");
                mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPaintCanvas.clearCompleteCanvas();
                        dialog.cancel();
                    }
                });
                mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                mDialog = mBuilder.create();
                mDialog.show();
                mDialog.setCanceledOnTouchOutside(false);
                break;
            case Constants.OPERATION_UNDO:
                mPaintCanvas.undoPreviousOperation();
                break;
            case Constants.OPERATION_CHOOSE_COLOR:
                openColorDrawer();
                break;
            case Constants.OPERATION_DRAW_CIRCLE:
                mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_CIRCLE);
                mPaintCanvas.changeFillStyle(Constants.PAINT_STYLE_FILL);
                drawCircleOnBoard();
                break;
            case Constants.OPERATION_DRAW_RECTANGLE:
                mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_RECTANGLE);
                mPaintCanvas.changeFillStyle(Constants.PAINT_STYLE_FILL);
                drawRectangleOnBoard();
                break;
            case Constants.OPERATION_DRAW_OVAL:
                mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_OVAL);
                mPaintCanvas.changeFillStyle(Constants.PAINT_STYLE_FILL);
                drawRectangleOnBoard();
                break;
            case Constants.OPERATION_INSERT_TEXT:
                mPaintCanvas.setCurrentOperation(Constants.OPERATION_INSERT_TEXT);
                getTextInputFromUser();
                break;
            case Constants.OPERATION_SAVE_IMAGE:
                initiateSaveOperation();
                break;
/*
            case Constants.OPERATION_SAVE_IMAGE_TO_SERVER:
                initiateServerUpload();
                break;
*/
            case Constants.OPERATION_SET_BACKGROUND:
                loadImageFromGallery();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    /**
     * Method gets called when the user drags the seek bar pointer to a location and the stop value
     * is set as the stroke width of the brush
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Toast.makeText(this, " current stroke : " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
        mPaintCanvas.changeBrushStroke(seekBar.getProgress());
    }

    /**
     * Method to initiate the save operation to the gallery. The method prompts the user
     * for the name of the file with which the bitmap is to be saved.
     */
    public void initiateSaveOperation() {
        mBuilder = new AlertDialog.Builder(DrawingBoard.this);
        mBuilder.setTitle("SAVE IMAGE AS");
        final EditText fileNameInput = new EditText(DrawingBoard.this);
        fileNameInput.setHint(R.string.file_name);
        fileNameInput.setSingleLine(true);
        mBuilder.setView(fileNameInput);
        mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager keyboardManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                keyboardManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                final String fileName = fileNameInput.getText().toString();
                dialog.cancel();

                /*
                  Delay to avoid the clipping of the canvas when we take a bitmap view of the canvas
                 */
                if(fileName.toString().trim().length()>0) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saveImageToGallery(fileName);
                        }
                    }, 1000);
                }else{
                    Toast.makeText(DrawingBoard.this,"Image can't be saved without a name",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        mDialog = mBuilder.create();
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(false);

    }


    /**
     * Method to confirm the save operation and send the bitmap for saving to the class SavePhotoUtil.
     * once the image is saved, the content resolver is notified that a new images
     * has been saved to the memory so that the gallery is refreshed.
     * @param fileName is the name of the file obtained from the user.
     */
    public void saveImageToGallery(final String fileName) {
        mBuilder = new AlertDialog.Builder(DrawingBoard.this);
        mBuilder.setTitle("SAVE");
        mBuilder.setMessage("Save the drawing to Memory?");
        mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPaintCanvas.setDrawingCacheEnabled(true);
                Bitmap bitmap = mPaintCanvas.getDrawingCache();
                String imgSaved = (new SavePhotoUtil(DrawingBoard.this)).saveToGallery(getContentResolver(), bitmap, fileName + ".png", "drawing");
                if (imgSaved != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        File f = new File("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        sendBroadcast(mediaScanIntent);
                    } else {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                    }
                    Toast savedToast = Toast.makeText(getApplicationContext(),
                            "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                    savedToast.show();
                } else {
                    Toast unsavedToast = Toast.makeText(getApplicationContext(),
                            "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                    unsavedToast.show();
                }
                mPaintCanvas.destroyDrawingCache();
                dialog.cancel();
            }
        });

        mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        mDialog = mBuilder.create();
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(false);

    }

    /**
     * Method to get name of the file to save the bitmap with on the server.
     * server used : 000webhost
     */
   /* public void initiateServerUpload() {
        mBuilder = new AlertDialog.Builder(DrawingBoard.this);
        mBuilder.setTitle("UPLOAD IMAGE AS");
        final EditText fileNameInput = new EditText(DrawingBoard.this);
        fileNameInput.setHint(R.string.file_name);
        fileNameInput.setSingleLine(true);
        mBuilder.setView(fileNameInput);
        mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager keyboardManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                keyboardManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                final String fileName = fileNameInput.getText().toString();
                dialog.cancel();
                if( fileName.trim().length()>0){
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadImageToServer(fileName);
                        }
                    }, 1000);
                }else{
                    Toast.makeText(DrawingBoard.this,"Image can't be Uploaded without a name",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        mDialog = mBuilder.create();
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(false);
    }

   */ /**
     * Method to encode the image byte stream in base 64 char steam for transmission
     * @param name is the name of the file
     */
   /* public void uploadImageToServer(final String name) {

        mBuilder = new AlertDialog.Builder(DrawingBoard.this);
        mBuilder.setTitle("Upload to Server?");
        mBuilder.setMessage("Upload the Current Image to Server?");
        mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (isInternetConnectionAvailable()) {
                    mPaintCanvas.setDrawingCacheEnabled(true);
                    Bitmap bitmap = mPaintCanvas.getDrawingCache();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    //byte[] bytes = stream.toByteArray();
                    sBitmapBytes = stream.toByteArray();
                    sEncodedImage = Base64.encodeToString(sBitmapBytes, Base64.DEFAULT);
                    String imageName = name + ".png";
                    Intent intent = new Intent(DrawingBoard.this, UploadImageToServer.class);
                    //intent.putExtra("image", encodedImage);
                    intent.putExtra("image_name", imageName);
                    startService(intent);
                } else {
                    Toast.makeText(DrawingBoard.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
                mPaintCanvas.destroyDrawingCache();
            }
        });
        mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        mDialog = mBuilder.create();
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(false);

    }
*/
    /**
     * Method is invoked when the user chooses the circle tool from the toolkit. The method prompts
     * the user for the radius of the circle as pass this dimension to the canvas view where the circle is
     * drawn on the canvas.
     */
    public void drawCircleOnBoard() {
        mBuilder = new AlertDialog.Builder(DrawingBoard.this);
        mBuilder.setTitle("Parameter!");
        final View view = mInflater.inflate(R.layout.shape_input, null);
        final EditText Circlerad = (EditText) view.findViewById(R.id.circle_radius);
        LinearLayout RectangleWidthLayout = (LinearLayout) view.findViewById(R.id.rectangle_width_layout);
        LinearLayout RectangleHeightLayout = (LinearLayout) view.findViewById(R.id.rectangle_height_layout);
        RectangleHeightLayout.setVisibility(View.GONE);
        RectangleWidthLayout.setVisibility(View.GONE);
        mBuilder.setView(view);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (Circlerad.getText().toString().trim().length() > 0) {
                    mPaintCanvas.drawCircle(Integer.parseInt(Circlerad.getText().toString()));
                    openColorDrawer();
                }else{
                    Toast.makeText(DrawingBoard.this,"Circle radius missing!",Toast.LENGTH_SHORT).show();
                    mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_PENCIL);
                }
            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_PENCIL);
                dialog.cancel();
            }
        });
        mDialog = mBuilder.create();
        mDialog.show();
    }

    /**
     * Method is invoked when the user chooses the draw retangle tool from the toolkit. The method prompts
     * the user to enter the width and the height of the rectangle and passes this dimension to the canvas
     * view where the circle is drawn on the canvas.
     */
    public void drawRectangleOnBoard() {
        mBuilder = new AlertDialog.Builder(DrawingBoard.this);
        if (mPaintCanvas.getCurrentOperation() == Constants.OPERATION_DRAW_RECTANGLE)
            mBuilder.setTitle("Rectangle Dimensions");
        else
            mBuilder.setTitle("Oval Dimensions");
        final View view = mInflater.inflate(R.layout.shape_input, null);
        LinearLayout CircleLayout = (LinearLayout) view.findViewById(R.id.circle_radius_layout);
        CircleLayout.setVisibility(View.GONE);
        final EditText RectWidth = (EditText) view.findViewById(R.id.rectangle_width);
        final EditText RectHeight = (EditText) view.findViewById(R.id.rectangle_heigth);
        mBuilder.setView(view);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (RectWidth.getText().toString().trim().length() > 0 && RectHeight.getText().toString().trim().length() > 0) {
                    mPaintCanvas.drawRectangle(Integer.parseInt(RectWidth.getText().toString()),
                            Integer.parseInt(RectHeight.getText().toString()));
                    openColorDrawer();
                }else{
                    Toast.makeText(DrawingBoard.this,"Dimension missing!",Toast.LENGTH_SHORT).show();
                    mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_PENCIL);
                }

            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_PENCIL);
                dialog.cancel();
            }
        });
        mDialog = mBuilder.create();
        mDialog.show();
    }

    /**
     * Method gets invoked when the user choose the insert text tool from the toolkit. It prompts the
     * user to enter the text,which will be entered in the text box, and the size of the text.
     * This dimensions are then passed to the canvas view to draw.
     */
    public void getTextInputFromUser() {
        mBuilder = new AlertDialog.Builder(DrawingBoard.this);
        mBuilder.setTitle("Create Text Input!");
        final View view = mInflater.inflate(R.layout.text_input_layout, null);
        final EditText text = (EditText) view.findViewById(R.id.input_text);
        final EditText textSize = (EditText) view.findViewById(R.id.text_size);
        mBuilder.setView(view);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (text.getText().toString().trim().length() > 0 && textSize.getText().toString().trim().length() > 0) {
                    mPaintCanvas.createTextBox(text.getText().toString().trim(), Integer.parseInt(textSize.getText().toString().trim()));
                    openColorDrawer();
                }else{
                    mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_PENCIL);
                    Toast.makeText(DrawingBoard.this,"Input missing!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPaintCanvas.setCurrentOperation(Constants.OPERATION_DRAW_PENCIL);
                dialog.cancel();
            }
        });
        mDialog = mBuilder.create();
        mDialog.show();
    }

    /**
     * Method to check if internet connectivity is available so as to upload the image to the server.
     * @return the state of the connection.
     */
    public boolean isInternetConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        } else
            return false;
    }

    /**
     * Method to load a image from the gallery. The method gets invoked when the user selects the open tool
     * from the toolkit.
     */
    public void loadImageFromGallery() {
        /*Intent i = new Intent(
                Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, Constants.RESULT_LOAD_IMAGE);*/
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),Constants.RESULT_LOAD_IMAGE);
    }

    /**
     * Method to receive the image selected by the user from the gallery.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RESULT_LOAD_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                String[] projections = new String[]{MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, projections, null, null, null);
                cursor.moveToFirst();
                String filePath = cursor.getString(0);
                cursor.close();
                Bitmap background = BitmapFactory.decodeFile(filePath);
                mPaintCanvas.setCanvasBackground(background);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

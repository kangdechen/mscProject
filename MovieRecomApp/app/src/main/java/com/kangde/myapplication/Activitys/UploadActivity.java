package com.kangde.myapplication.Activitys;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kangde.myapplication.Bean.Recommendation;
import com.kangde.myapplication.R;
import com.kangde.myapplication.Util.L;
import com.kangde.myapplication.Util.SharedPreferencesUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Environment.getExternalStoragePublicDirectory;

/**
 * on this activity Okhttp3 use to buidld connectino with Server and post recommendation and  files
 */
public class UploadActivity extends AppCompatActivity {

    String filepath = "";
    String filename = "";
    String  username;


    private static final int FILE_SELECT_CODE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_IMAGE_CAPTURE = 2;
    private static final String THUMBNAIL_BITMAP_KEY = "thumbnail.bitmap.key";

    Button button_upload;
    Button button_CF;
    Button buttonPhoto;
    Button get;

    Button button_pic;
    private ImageView mImageView;
    private Bitmap imageBitmap;
    private String mCurrentPhotoPath;
    private  String comment;
    private  String movieName;

    private EditText editText_Moviename;
    private EditText editText_Comment;
    private EditText editText_p;
    RatingBar rb;
    private  float rating;
    private Context context;

    private SharedPreferencesUtil sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_upload);
        View  v = getLayoutInflater().inflate(R.layout.activity_upload,null);
//        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mToolbar.setVisibility(v.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHeader);
        toolbar.setTitle("Recommendation upload");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        startActivity(new Intent(UploadActivity.this,TestActivity.class));

              L.e("return home clicked");

            }
        });

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_IMAGE_CAPTURE);
        }else {
            context = this;
            sp= SharedPreferencesUtil.getInstance(getApplicationContext());

            mImageView = (ImageView) findViewById(R.id.image_view);

            button_upload=(Button)findViewById(R.id.button_Upload);
            button_CF=(Button)findViewById(R.id.button_ChooseFile);
            buttonPhoto=(Button)findViewById(R.id.button_photo);
            get=(Button)findViewById(R.id.button_get) ;

            editText_Moviename = (EditText) findViewById(R.id.editText_movie);
            editText_Comment = (EditText) findViewById(R.id.editText_comment);
            rb=(RatingBar)findViewById(R.id.RatingBar);

            comment =editText_Comment.getText().toString();
            movieName=editText_Moviename.getText().toString();

            filepath="";
            username= sp.get("userName");
            editText_Comment.setText(username);


            button_upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postReview();
                    Upload();
                }
            });
            button_CF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFileChooser();
                }
            });
            get.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("https://www.google.com/search?hl=en&tbm=isch&source=hp&ei=nSaiXqDHNIrr_QbhhbSgCA&q="+editText_Moviename.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });

            buttonPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(context,  "com.kangde.android.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                }
            });
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);

        imageBitmap = bitmap;
    }



    /** 调用文件选择软件来选择文件 **/
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, FILE_SELECT_CODE);
//        try {
//            startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"),
//                    FILE_SELECT_CODE);
//        } catch (android.content.ActivityNotFoundException ex) {
//            // Potentially direct the user to the Market with a Dialog
//            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT)
//                    .show();
//        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:{
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    filepath =picturePath;
                    cursor.close();




                    mImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                    filename=filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);

                    L.e(filename.toString());
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload

                }
                break;
            }
            case REQUEST_IMAGE_CAPTURE :{
                if (resultCode == RESULT_OK){
                    galleryAddPic();
                    setPic();
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public  void postReview()
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         final Recommendation review =new Recommendation();
         review.setUsername(username);
         review.setMovieName(editText_Moviename.getText().toString());
         review.setComment(editText_Comment.getText().toString());
         review.setRating(rb.getRating());
         review.setPic(filename);


        L.e("moviesname "+movieName);
        if (isConnectingToInternet()){
            if (editText_Moviename.getText().toString().isEmpty()) {
                Toast.makeText(UploadActivity.this, "Movie name can't be empty", Toast.LENGTH_SHORT).show();

            }
            else if (filename.isEmpty()) {
                System.out.println("pic cant be empty");
            }
            else if (editText_Comment.getText().toString().isEmpty()) {
                System.out.println("pic cant be empty");
            }

            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Gson gson = new Gson();
                        String json = gson.toJson(review);
                        String address = "http://192.168.1.15:9999/MovieAppServer/ReviewUploadServlet";


                        //HttpConnection.sendOkHttpRequest(address, user, new okhttp3.Callback(){

                        OkHttpClient client=new OkHttpClient();
//

                        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                                , json);
                        Request request = new Request.Builder()
                                .url(address)
                                .post(requestBody)
                                .build();

                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d(MainActivity.TAG, "connectionfaill");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                boolean responseData = Boolean.valueOf(response.body().string());
                                L.e("响应信息： " + responseData);
                                L.e( "res code"+response.code());
                                //  String cookie = response.headers().get("Set-Cookie");

                                // Log.d(TAG, cookie);
                                L.e("res data"+responseData);
                                if (responseData)
                                {
                                    startActivity(new Intent(UploadActivity.this,TestActivity.class));
                                    Looper.prepare();
                                    Toast.makeText(UploadActivity.this, "succeed", Toast.LENGTH_SHORT).show();
                                    Looper.loop();

                                }
                                 else
                                 {
                                     Looper.prepare();
                                     Toast.makeText(UploadActivity.this, "upload Fail", Toast.LENGTH_SHORT).show();
                                     Looper.loop();
                                 }


                            }
                        });

                    }
                }).start();
            }
        }else{
            System.out.println("网络未连接");
        }


    }
    public  void  Upload()
    {
        //上传文件：

        File file = new File(filepath);
       // File file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        if (!file.exists())
        {
            L.e(file.getAbsolutePath()+"not exist");
            return;
        }

        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
               // .addPart(Headers.of(
                 //       "Content-Disposition",
                   //     "form-data; name=\"username\""),
                     //   RequestBody.create(null, "Upload Test"))
                //.addPart(Headers.of(
                  //      "Content-Disposition",
                    //    "form-data; name=\"mFile\"; filename=\"" + filename + "\""), fileBody)
                .addFormDataPart("userename",username)
                .addFormDataPart("file",filename,fileBody)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.1.11:9999/MovieAppServer/UploadHandleServlet")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient =new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                L.e("failure"+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                L.e("onResponse:");
                String res=  response.body().string();
                L.e(res);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_IMAGE_CAPTURE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recreate();
                } else {
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    public void doClick(View view) {
       //String url = "http://cn.bing.com/az/hprichbg/rb/Dongdaemun_ZH-CN10736487148_1920x1080.jpg";
        //String url =""
        //Glide.with(this).load(url).into(mImageView);
    }

}

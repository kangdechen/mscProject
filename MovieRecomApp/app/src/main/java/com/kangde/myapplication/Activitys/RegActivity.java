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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kangde.myapplication.Bean.User;
import com.kangde.myapplication.R;
import com.kangde.myapplication.Util.L;
import com.kangde.myapplication.Util.SharedPreferencesUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class RegActivity extends AppCompatActivity {

    String filepath = "";
    String filename = "";

    private static final int FILE_SELECT_CODE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_IMAGE_CAPTURE = 2;
    private static final String THUMBNAIL_BITMAP_KEY = "thumbnail.bitmap.key";
    private String mCurrentPhotoPath;
    private String baseUrl ="http://192.168.1.15:9999/MovieAppServer/";
    private  String Uname,Upsw;
    private final String u=null;
    private final String p=null;
    private String picName;
    public static final String TAG="RegActivity";
    private EditText name, pswd;

    private ImageView textView_img;
    private CircleImageView circleImageView;
    private Bitmap imageBitmap;
    private Context context;
    private ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

    private Button button_reg;
    private Button button_file;
    private Button button_pic;

    private SharedPreferencesUtil sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_IMAGE_CAPTURE);
        }else {
            picName="";
            context = this;
            name = (EditText) findViewById(R.id.editText_username);
            pswd = (EditText) findViewById(R.id.editText_password);
            button_reg = (Button) findViewById(R.id.button_registerU);
            textView_img =(ImageView)findViewById(R.id.image_view);
            button_pic =(Button)findViewById(R.id.button_ChoosePic);
            button_file=(Button)findViewById(R.id.button_ChooseFile);
//            circleImageView=(CircleImageView)findViewById(R.id.profile_image);
            sp= SharedPreferencesUtil.getInstance(getApplicationContext());
            filepath="";



            button_reg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uname = name.getText().toString();
                    Upsw = pswd.getText().toString();

                    if (isConnectingToInternet()) {

                        register(Uname,Upsw);


                    } else {
                        Log.d(RegActivity.TAG, "connectionfaill");
                        ;
                    }

                }
            });
            button_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFileChooser();
                }
            });
            button_pic.setOnClickListener(new View.OnClickListener() {
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
    initView();

    }


    private  void initView ()
    {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHeader);
        toolbar.setTitle("User Register");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegActivity.this,UserLoginActivity.class));
            }
        });
    }

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
                    cursor.close();



                    filepath =picturePath;
                    textView_img.setImageBitmap(BitmapFactory.decodeFile(picturePath));

                    filename=filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);
                    picName=filename;
                    L.e(filename);
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


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        filename=filename = mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/") + 1);
        picName=filename;
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
        int targetW = textView_img.getWidth();
        int targetH = textView_img.getHeight();

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
        textView_img.setImageBitmap(bitmap);

        imageBitmap = bitmap;
    }

    private void register(final String u, final String p) {

        final User user = new User();
        user.setUsername(u);
        user.setPassword(p);
        user.setImg(picName);
        if (isConnectingToInternet()){
            if (Uname.toString().isEmpty()||Upsw.toString().isEmpty()) {
                System.out.println("password cant be empty");
            }
            if (picName.isEmpty()) {
                System.out.println("pic cant be empty");
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Gson gson = new Gson();
                        String json = gson.toJson(user);
                        String address = "http://192.168.1.15:9999/MovieAppServer/RegisterServlet";


                        //HttpConnection.sendOkHttpRequest(address, user, new okhttp3.Callback(){

                        OkHttpClient client=new OkHttpClient();
//                        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                                .cookieJar(new CookieJar() {
//                                    CookieManager cookieManager = new CookieManager();
//
//                                    @Override
//                                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> cookies) {
//                                        cookieStore.put(httpUrl.host(), cookies);
//                                        //cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
//                                    }
//
//                                    @NotNull
//                                    @Override
//                                    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
//                                        List<Cookie> cookies = cookieStore.get(httpUrl.host());
//                                        return cookies != null ? cookies : new ArrayList<Cookie>();
//                                    }
//                                })
//                                .build();

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
                                    Upload();
                                    Looper.prepare();
                                    Toast.makeText(RegActivity.this, "succeed", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                    startActivity(new Intent(RegActivity.this,UserLoginActivity.class));
                                }
                                 else
                                 {
                                     Looper.prepare();
                                     Toast.makeText(RegActivity.this, "user Already Exist", Toast.LENGTH_SHORT).show();
                                     Looper.loop();
                                 }


                            }
                        });

                    }
                }).start();
            }
        }else{
            System.out.println("not internet connection");
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

    public  void  Upload()
    {
        //上传文件：

        File file = new File(filepath);
        L.e(filepath);
        // File file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        if (!file.exists())
        {
            L.e(file.getAbsolutePath()+"not exist");
            return;
        }
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)

                .addFormDataPart("userename","ss")
                .addFormDataPart("file",filename,fileBody)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.1.15:9999/MovieAppServer/UploadHandleServlet")
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



}

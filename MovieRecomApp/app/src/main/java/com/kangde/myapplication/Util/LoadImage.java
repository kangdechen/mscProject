package com.kangde.myapplication.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *  function that take in imagename and image view parameter
 *  use the okhttp to get the input stream and decode into bitmap
 *  set the image view with bitmap
 */
public class LoadImage {

    private static ImageView imageView ;
    public static  void Download(String picUrl , ImageView view) {

        imageView=view;
        OkHttpClient okHttpClient = new OkHttpClient();


        Request.Builder builder = new Request.Builder();
        String pic=picUrl;
        Request request = builder.url("http://192.168.1.11:9999/MovieAppServer/DownLoadServlet?filename="+pic)
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                //将输入流数据转化为Bitmap位图数据
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                final File file = new File("/mnt/sdcard/picture.jpg");
                file.createNewFile();
                //创建文件输出流对象用来向文件中写入数据
                FileOutputStream out = new FileOutputStream(file);
                //将bitmap存储为jpg格式的图片
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                //刷新文件流
                out.flush();
                out.close();
                final Message msg = Message.obtain();
                msg.obj = bitmap;

                        // Glide.with(this).load(url).into(mImageView);
                        // img.setImageBitmap(file.);
                        imageView.setImageBitmap(bitmap);




            }
        });

    }
}

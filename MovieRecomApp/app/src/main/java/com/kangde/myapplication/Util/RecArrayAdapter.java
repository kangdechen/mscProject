package com.kangde.myapplication.Util;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.kangde.myapplication.Activitys.RecDetailActivity;
import com.kangde.myapplication.Bean.Recommendation;
import com.kangde.myapplication.R;

import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
/*
    ArrayAdapter use to handle the recommendation list to the view
    the example got from module lecture PPT from Moodle

 */
public class RecArrayAdapter extends ArrayAdapter<Recommendation>  {

    private   List<Recommendation> recList; ;
    private  Context context ;
    private MaterialRatingBar ratingBar;

    public RecArrayAdapter(Context context, List<Recommendation> recList) { 
        super(context, 0, recList); 
        this.context = context; 
        this.recList = recList;    }


    public View getView(int position, View convertView, ViewGroup parent) { 
 
        View view = convertView; 
        if(view == null){ 
            view = LayoutInflater.from(context).inflate(R.layout.list_row_layout, parent,false); 
        } 
        final Recommendation rec = recList.get(position);
        Button button =(Button) view.findViewById(R.id.button_detail);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RecDetailActivity.class);
                intent.putExtra("rec",rec);

               context.startActivity(intent);

            }
        });
        TextView name = (TextView) view.findViewById(R.id.editText_uploader); 
        name.setText(rec.getUsername()); 

        TextView moviename = (TextView) view.findViewById(R.id.edit_Show_movie); 
        moviename.setText(rec.getMovieName());

        ratingBar=(MaterialRatingBar) view.findViewById(R.id.RatingBarList);
        int number =(int) rec.getRating();
        ratingBar.setProgress((int)rec.getRating());
       // ImageView imageView=(ImageView)view.findViewById(R.id.Image_Download);
        //String url="http://192.168.1.11:9999/MovieAppServer/DownLoadServlet?filename="+rec.getPic();
          //Glide.with(context).load(url).into(imageView);


         return view;
    } 

}

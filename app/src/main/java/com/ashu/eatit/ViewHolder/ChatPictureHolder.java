package com.ashu.eatit.ViewHolder;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.eatit.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatPictureHolder extends RecyclerView.ViewHolder {
    private Unbinder unbinder;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_time)
    public TextView txt_time;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_email)
    public TextView txt_email;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_chat_message)
    public TextView txt_chat_message;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.profile_image)
    public CircleImageView profile_image;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.img_preview)
    public ImageView img_preview;

    public ChatPictureHolder(@NonNull View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this, itemView);

    }
}

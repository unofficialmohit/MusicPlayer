package com.mg.music;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {
public List<AudioFile> audioFiles;
public List<AudioFile> filteredAudioFiles;
public interface OnItemClickListener {
        void onItemClick(View view, int position);
}
public AudioAdapter(List<AudioFile> audioFiles){
    this.audioFiles=audioFiles;
}

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView titleTextView;
        TextView artistTextView;
        ShapeableImageView artImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView= itemView.findViewById(R.id.titleTextView);
            artistTextView=itemView.findViewById(R.id.artistTextView);
            artImg=itemView.findViewById(R.id.artImgView);
            ShapeAppearanceModel shapeAppearanceModel = new
                    ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, 25) // Set radius for rounded corners
            .build();
            artImg.setShapeAppearanceModel(shapeAppearanceModel);
        }
    }
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audio,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
    AudioFile audioFile=audioFiles.get(position);
    holder.titleTextView.setText(audioFile.getTitle());
    holder.artistTextView.setText(audioFile.getArtist());
    loadAlbumArt(holder.artImg,audioFile.getAlbumArtUri());


    //  direct album art load from storage, heavy on memory laggy ui
    //        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    //        retriever.setDataSource(audioFile.getFilePath());
    //        byte[] albumArt = retriever.getEmbeddedPicture();
    //        if (albumArt != null) {
    //            Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
    //            holder.artImg.setImageBitmap(albumArtBitmap);
    //        } else {
    //            holder.artImg.setImageResource(R.drawable.musicbutton);
    //        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioFiles.size();
    }
    public void loadAlbumArt(ImageView imageView, Uri filePath) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.musicbutton) // Placeholder image while loading
                .error(R.drawable.musicbutton); // Error image if loading fails=
        Glide.with(imageView.getContext())
                .load(filePath)
                .apply(requestOptions)
                .thumbnail(Glide.with(imageView.getContext()).load(filePath))
                .into(imageView);
    }
}


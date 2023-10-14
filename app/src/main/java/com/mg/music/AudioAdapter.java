package com.mg.music;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {
public List<AudioFile> audioFiles;
public interface OnItemClickListener {
        void onItemClick(View view, int position);
}
public AudioAdapter(List<AudioFile> audioFiles){
    this.audioFiles=audioFiles;
}

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView titleTextView;
        TextView artistTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView= itemView.findViewById(R.id.titleTextView);
            artistTextView=itemView.findViewById(R.id.artistTextView);
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


}

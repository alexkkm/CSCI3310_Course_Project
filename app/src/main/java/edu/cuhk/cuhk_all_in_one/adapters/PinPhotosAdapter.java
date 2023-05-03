package edu.cuhk.cuhk_all_in_one.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.datatypes.Photo;

public class PinPhotosAdapter extends RecyclerView.Adapter<PinPhotosAdapter.PinPhotosViewHolder> {

    private List<Photo> photos;

    public PinPhotosAdapter(List<Photo> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PinPhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PinPhotosViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.photo_item, parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull PinPhotosViewHolder holder, int position) {
        holder.setPhoto(photos.get(position));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class PinPhotosViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView photoView;

        public PinPhotosViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.pin_photo);
        }

        void setPhoto(Photo photo) {
            photoView.setImageBitmap(photo.getImage());
        }
    }
}

package edu.cuhk.cuhk_all_in_one.datatypes;

import android.graphics.Bitmap;

public class Photo {
    private Bitmap image;

    public Photo(Bitmap image) {
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }
}

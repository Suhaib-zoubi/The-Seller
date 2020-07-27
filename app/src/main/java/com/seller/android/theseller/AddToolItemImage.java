package com.seller.android.theseller;

import android.graphics.Bitmap;

public class AddToolItemImage {
    public String ImagePath;
    public int Image;
    public Bitmap bitmap;

    AddToolItemImage(String ImagePath, int Image, Bitmap bitmap) {
        this.ImagePath = ImagePath;
        this.Image = Image;
        this.bitmap = bitmap;
    }
}

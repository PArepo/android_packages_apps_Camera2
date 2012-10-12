/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.filtershow.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.android.gallery3d.filtershow.imageshow.GeometryMetadata;

public class ImageFilterGeometry extends ImageFilter {
    private final Bitmap.Config mConfig = Bitmap.Config.ARGB_8888;
    private GeometryMetadata mGeometry = null;
    private static final String LOGTAG = "ImageFilterGeometry";
    private static final boolean LOGV = false;
    private static final int BOTH = 3;
    private static final int VERTICAL = 2;
    private static final int HORIZONTAL = 1;
    private static final int NINETY = 1;
    private static final int ONE_EIGHTY = 2;
    private static final int TWO_SEVENTY = 3;

    public ImageFilterGeometry() {
        mName = "Geometry";
    }

    @Override
    public ImageFilter clone() throws CloneNotSupportedException {
        ImageFilterGeometry filter = (ImageFilterGeometry) super.clone();
        return filter;
    }

    public void setGeometryMetadata(GeometryMetadata m) {
        mGeometry = m;
    }

    native protected void nativeApplyFilterFlip(Bitmap src, int srcWidth, int srcHeight,
            Bitmap dst, int dstWidth, int dstHeight, int flip);

    native protected void nativeApplyFilterRotate(Bitmap src, int srcWidth, int srcHeight,
            Bitmap dst, int dstWidth, int dstHeight, int rotate);

    native protected void nativeApplyFilterCrop(Bitmap src, int srcWidth, int srcHeight,
            Bitmap dst, int dstWidth, int dstHeight, int offsetWidth, int offsetHeight);

    native protected void nativeApplyFilterStraighten(Bitmap src, int srcWidth, int srcHeight,
            Bitmap dst, int dstWidth, int dstHeight, float straightenAngle);

    public Matrix buildMatrix(Bitmap bitmap, boolean rotated) {
        Matrix drawMatrix = new Matrix();
        float dx = bitmap.getWidth() / 2.0f;
        float dy = bitmap.getHeight() / 2.0f;

        Matrix flipper = mGeometry.getFlipMatrix(bitmap.getWidth(), bitmap.getHeight());
        drawMatrix.postConcat(flipper);
        drawMatrix.postTranslate(-dx, -dy);
        drawMatrix.postScale(1.0f / mGeometry.getScaleFactor(), 1.0f / mGeometry.getScaleFactor());
        float angle = (mGeometry.getRotation() + mGeometry.getStraightenRotation());
        drawMatrix.postRotate(angle);
        if (rotated) {
            drawMatrix.postTranslate(dy, dx);
        } else {
            drawMatrix.postTranslate(dx, dy);
        }
        return drawMatrix;
    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, boolean highQuality) {
        // TODO: implement bilinear or bicubic here... for now, just use
        // canvas to do a simple implementation...
        // TODO: and be more memory efficient! (do it in native?)

        Bitmap temp = null;
        float rotation = mGeometry.getRotation();
        boolean rotated = false;
        if (rotation == 0 || rotation % 180 == 0) {
            temp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), mConfig);
        } else {
            temp = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), mConfig);
            rotated = true;
        }

        Matrix drawMatrix = buildMatrix(bitmap, rotated);
        Canvas canvas = new Canvas(temp);
        canvas.drawBitmap(bitmap, drawMatrix, new Paint());
        return temp;
    }

}

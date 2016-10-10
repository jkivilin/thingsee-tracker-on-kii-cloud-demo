/* Copyright 2016 Marko Parttimaa

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.thingsee.tracker.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class ClearTextView extends TextView {

    private Drawable mBackground;

    private Paint mEraserPaint;

    public ClearTextView(Context context) {
        super(context);
        setup();
        mBackground = getBackground();
    }

    public ClearTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
        mBackground = getBackground();
    }

    public ClearTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
        mBackground = getBackground();
    }

    private void setup() {
        mEraserPaint = new Paint();
        mEraserPaint.set(getPaint());
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    public Paint getEraserPaint() {
        return mEraserPaint;
    }

    public void setEraserPaint(Paint eraserPaint) {
        if (eraserPaint != null) {
            mEraserPaint = eraserPaint;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {

            if (mBackground == null) {
                mBackground = getBackground();
            } else {
                setBackgroundDrawable(mBackground);
            }

            if (mBackground != null) {
                mBackground.setBounds(0, 0, w, h);
                carveText();
            }
        }
    }

    private void carveText() {
        if (mBackground != null) {
            Rect rect = mBackground.getBounds();
            if (rect != null && rect.isEmpty() == false) {
                Bitmap background = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(background);
                mBackground.draw(canvas);

                Bitmap text = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                setBackgroundDrawable(null);
                if (getCurrentTextColor() == Color.TRANSPARENT) {
                    setTextColor(Color.BLACK);
                }
                draw(new Canvas(text));

                if (mEraserPaint == null) {
                    setup();
                }
                canvas.drawBitmap(text, 0, 0, mEraserPaint);

                setBackgroundDrawable(new BitmapDrawable(getResources(), background));

                setTextColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (mBackground != null) {
            setBackgroundDrawable(mBackground);
            carveText();
        } else {
            Log.e("ClearTextView", "background cannot be restored.");
        }
    }

    public void clearBackground() {
        super.setBackgroundDrawable(null);
        mBackground = null;
    }

    private void onBackgroundChanged(Drawable d) {
        if (d == null) {
            clearBackground();
        } else {
            if (d.equals(mBackground) == false) {
                mBackground = d;
                carveText();
            }
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        onBackgroundChanged(getBackground());
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        onBackgroundChanged(getBackground());
    }

    @Override
    public void setBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= 16) {
            super.setBackground(background);
            onBackgroundChanged(getBackground());
        } else {
            setBackgroundDrawable(background);
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
        onBackgroundChanged(getBackground());
    }
    public Drawable getFilledBackground() {
        return mBackground;
    }
}

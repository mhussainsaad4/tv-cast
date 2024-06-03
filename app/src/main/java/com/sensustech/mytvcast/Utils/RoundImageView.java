package com.sensustech.mytvcast.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sensustech.mytvcast.R;

@SuppressLint("AppCompatCustomView")
public class RoundImageView extends ImageView {

    public float radius = 22.0f;
    private Path path;
    private RectF rect;

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a=getContext().obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        a.recycle();
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a=getContext().obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        a.recycle();
        init();
    }

    private void init() {
        path = new Path();

    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
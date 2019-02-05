package com.example.aditya.dots1;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

public class ResizeAnimationSmall extends Animation {

    final int targetHeight;
    FrameLayout frameLayout;
    int startHeight;

    public ResizeAnimationSmall(FrameLayout frameLayout, int targetHeight, int startHeight){
        this.frameLayout = frameLayout;
        this.targetHeight = targetHeight;
        this.startHeight = startHeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float newHeight = startHeight - (startHeight - targetHeight) * interpolatedTime;

        frameLayout.getLayoutParams().height = (int) newHeight;
        frameLayout.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}

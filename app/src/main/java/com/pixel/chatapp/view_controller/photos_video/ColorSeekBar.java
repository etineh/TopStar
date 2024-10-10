package com.pixel.chatapp.view_controller.photos_video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.widget.SeekBar;

import com.pixel.chatapp.interface_listeners.ImageListener;

public class ColorSeekBar extends AppCompatSeekBar {

    private Paint paint;
    private int[] colors = new int[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.BLACK};
    private ImageListener imageListener;

    // Setter for color change listener
    public void setImageListener(ImageListener listener) {
        this.imageListener = listener;
    }

    public ColorSeekBar(Context context) {
        super(context);
        init();
    }

    public ColorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        // Add listener to detect changes in SeekBar progress
        setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (imageListener != null && fromUser) {
                    // Calculate color corresponding to SeekBar progress
                    int color = getColorForPosition(progress * getWidth() / getMax());
                    // Notify listener with the new color
                    imageListener.onColorChange(color);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Calculate segment width
        float segmentWidth = (float) width / (colors.length - 1);

        // Draw color gradient with smoothly blended colors
        for (int i = 0; i < colors.length - 1; i++) {
            int startX = (int) (i * segmentWidth);
            int endX = (int) ((i + 1) * segmentWidth);

            int startColor = colors[i];
            int endColor = colors[i + 1];

            paint.setShader(new LinearGradient(startX, 0, endX, 0, startColor, endColor, Shader.TileMode.CLAMP));
            canvas.drawRect(startX, 0, endX, height, paint);
        }
    }

    // Method to calculate color corresponding to SeekBar position
    private int getColorForPosition(float position) {
        float fraction = position / getWidth();
        float colorPosition = fraction * (colors.length - 1);
        int colorIndex = (int) colorPosition;
        float colorFraction = colorPosition % 1;

        int color1 = colors[colorIndex];
        int color2 = colors[Math.min(colorIndex + 1, colors.length - 1)];

        return blendColors(color1, color2, colorFraction);
    }

    // Method to blend colors
    private int blendColors(int color1, int color2, float ratio) {
        float inverseRatio = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRatio);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRatio);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRatio);
        return Color.rgb((int) r, (int) g, (int) b);
    }

}

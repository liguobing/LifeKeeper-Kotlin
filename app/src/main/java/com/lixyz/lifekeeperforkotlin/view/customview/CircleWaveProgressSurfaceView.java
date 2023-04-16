package com.lixyz.lifekeeperforkotlin.view.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.lixyz.lifekeeperforkotlin.R;

import java.text.DecimalFormat;

/**
 * 圆形波浪进度条
 *
 * @author LGB
 */

public class CircleWaveProgressSurfaceView extends SurfaceView {

    private TypedArray typedArray;
    /**
     * 属性 - 圆的半径
     */
    private float circleRadius;
    /**
     * 属性 - 圆的背景色
     */
    private int circleBackground;
    /**
     * 属性 - 深色波浪颜色
     */
    private int darkWaveColor;
    /**
     * 属性 - 浅色波浪颜色
     */
    private int lightWaveColor;
    /**
     * 属性 - 圆外围边框宽度
     */
    private float circleBorderWidth;
    /**
     * 属性 - 文字颜色
     */
    private int textColor;
    /**
     * 属性 - 波浪速度
     */
    private int waveSpeed;
    /**
     * 属性 - 波浪高度
     */
    private float waveHeight;
    /**
     * 属性 - 波浪进度
     */
    private float waveProgress;
    /**
     * 属性 - 文字尺寸
     */
    private float textSize;
    /**
     * 绘制圆边框的画笔
     */
    private Paint circleBorderPaint;
    /**
     * 波浪偏移量
     */
    private float offSet;
    /**
     * 深色波浪画笔
     */
    private Paint darkWavePaint;
    /**
     * 浅色波浪画笔
     */
    private Paint lightWavePaint;

    /**
     * 圆形背景画笔
     */
    private Paint circleBackgroundPaint;
    /**
     * 深色浅色波浪路径
     */
    private Path darkWavePath;
    private Path lightWavePath;
    /**
     * 切割圆形路径
     */
    private Path circleClipPath;
    /**
     * 组件背景色
     */
    private int viewBackground;
    /**
     * 测量文字宽高的 Rect
     */
    private Rect textMeasureRect;
    /**
     * 控制绘制线程
     */
    private boolean waveAnimatorStatus = false;
    /**
     * 线程锁
     */
    private static final Object LOCK = new Object();

    public CircleWaveProgressSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setZOrderOnTop(true);
        initAttrs(context, attrs);
        initDrawTool();
    }


    /**
     * 初始化控件绘制的画笔、路径等
     */
    private void initDrawTool() {
        //圆形边框画笔及属性
        circleBorderPaint = new Paint();
        circleBorderPaint.setStyle(Paint.Style.FILL);
        circleBorderPaint.setAntiAlias(true);
        circleBorderPaint.setStrokeCap(Paint.Cap.ROUND);
        //圆形背景画笔及属性
        circleBackgroundPaint = new Paint();
        circleBackgroundPaint.setColor(circleBackground);
        circleBackgroundPaint.setStyle(Paint.Style.FILL);
        circleBackgroundPaint.setAntiAlias(true);
        //深色波浪画笔及属性
        darkWavePaint = new Paint();
        darkWavePaint.setColor(darkWaveColor);
        darkWavePaint.setAntiAlias(true);
        //深色波浪路径
        darkWavePath = new Path();
        //浅色波浪画笔及属性
        lightWavePaint = new Paint();
        lightWavePaint.setColor(lightWaveColor);
        lightWavePaint.setAntiAlias(true);
        //浅色波浪路径
        lightWavePath = new Path();
        //切割圆形路径
        circleClipPath = new Path();
        //测量文字宽高 Rect
        textMeasureRect = new Rect();
    }

    /**
     * 初始化控件属性
     *
     * @param context 上下文
     * @param attrs   属性集合
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleWaveProgressSurfaceView);
        viewBackground = typedArray.getInt(R.styleable.CircleWaveProgressSurfaceView_view_background, Color.WHITE);
        circleBackground = typedArray.getInt(R.styleable.CircleWaveProgressSurfaceView_circle_background, Color.RED);
        darkWaveColor = typedArray.getInt(R.styleable.CircleWaveProgressSurfaceView_dark_wave_color, Color.BLUE);
        lightWaveColor = typedArray.getInt(R.styleable.CircleWaveProgressSurfaceView_light_wave_color, Color.GRAY);
        circleBorderWidth = typedArray.getDimension(R.styleable.CircleWaveProgressSurfaceView_circle_border_width, 20);
        textColor = typedArray.getInt(R.styleable.CircleWaveProgressSurfaceView_text_color, Color.BLACK);
        waveSpeed = typedArray.getInt(R.styleable.CircleWaveProgressSurfaceView_wave_speed, 2);
        waveHeight = typedArray.getDimension(R.styleable.CircleWaveProgressSurfaceView_wave_height, 0);
        waveProgress = typedArray.getFloat(R.styleable.CircleWaveProgressSurfaceView_wave_progress, 0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 在测量时确定半径，如果没有设置半径，则半径设置为长宽较短的那个值的1/2
        float defaultCircleRadius = widthSize > heightSize ? heightSize / 2F : widthSize / 2F;
        circleRadius = typedArray.getDimension(R.styleable.CircleWaveProgressSurfaceView_circle_radius, defaultCircleRadius);
        textSize = typedArray.getDimension(R.styleable.CircleWaveProgressSurfaceView_text_size, circleRadius / 2);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    private void setWaveProgress(float waveProgress) {
        this.waveProgress = waveProgress;
    }

    private float getWaveProgress() {
        int oneHundred = 100;
        if (waveProgress > oneHundred) {
            waveProgress = 100;
        }
        return waveProgress;
    }

    private float getWaveProgressPercent() {
        return 1 - (waveProgress / 100);
    }

    public void setFinalProgressPercent(float finalProgress) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, finalProgress);
        animator.setDuration(2000);
        animator.setRepeatCount(0);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //构造方法的字符格式这里如果小数不足2位,会以0补足.
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                //format 返回的是字符串
                String p = decimalFormat.format(animation.getAnimatedValue());
                setWaveProgress(Float.parseFloat(p));
            }
        });
        animator.start();
    }

    private void startWaveAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, circleRadius * 2);
        animator.setDuration(waveSpeed * 1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offSet = (float) animation.getAnimatedValue();
            }
        });
        animator.start();
    }

    public void start() {
        waveAnimatorStatus = true;
        startWaveAnimator();

        //设置外围圆圈为渐变颜色，因为需要用到控件的宽高，所以在这里进行初始化
//        LinearGradient linearGradient = new LinearGradient(getWidth() / 2F, 0, 0, getHeight() / 10F, Color.parseColor("#f7daba"),
//                Color.parseColor("#ff0000"), Shader.TileMode.CLAMP);
//        circleBorderPaint.setShader(linearGradient);
        DrawThread drawThread = new DrawThread();

        drawThread.start();
    }

    public void stop() {
        waveAnimatorStatus = false;
    }

    public void waveWait() {
        if (!isWait) {
            isWait = true;
        }
    }

    public void notifyWave() {
        if (isWait) {
            isWait = false;
            synchronized (LOCK) {
                LOCK.notifyAll();
            }
        }
    }

    boolean isWait = false;

    Paint paint = new Paint();

    /**
     * 自定义组件 - 绘制线程
     */
    class DrawThread extends Thread {
        @Override
        public void run() {
            super.run();
            Paint pointPaint = new Paint();
            pointPaint.setStrokeWidth(circleBorderWidth);
            pointPaint.setStrokeCap(Paint.Cap.ROUND);
            pointPaint.setColor(Color.GREEN);

            synchronized (LOCK) {
                circleClipPath.addCircle(getWidth() / 2F, getHeight() / 2F, circleRadius, Path.Direction.CCW);
                SurfaceHolder holder = getHolder();
                while (waveAnimatorStatus) {
                    if (isWait) {
                        try {
                            LOCK.wait();
                            throw new InterruptedException("线程开始等待啦");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Canvas canvas = holder.lockCanvas();
                    try {
                        if (canvas != null) {
                            canvas.drawColor(viewBackground);
                            darkWavePath.reset();
                            lightWavePath.reset();
                            //先画外围进度圆的底色
                            paint.setColor(Color.WHITE);
                            canvas.drawCircle(getWidth() / 2F, getWidth() / 2F, (circleRadius + circleBorderWidth), paint);

                            //画外围圆圈
                            float drawDegree = 360 * (1 - getWaveProgressPercent());
                            if (drawDegree > 360) {
                                drawDegree = 360;
                            }
                            //画外围边框
                            RectF borderArcRect = new RectF(getWidth() / 2F - (circleRadius + circleBorderWidth), getWidth() / 2F - (circleRadius + circleBorderWidth), (getWidth() / 2F - (circleRadius + circleBorderWidth)) + (circleRadius + circleBorderWidth) * 2, (getWidth() / 2F - (circleRadius + circleBorderWidth)) + (circleRadius + circleBorderWidth) * 2);
                            circleBorderPaint.setColor(getColor(drawDegree));
                            canvas.drawArc(borderArcRect, 270, drawDegree, true, circleBorderPaint);
                            //画背景圆
                            canvas.drawCircle(getWidth() / 2F, getWidth() / 2F, circleRadius, circleBackgroundPaint);
                            //切割为圆形
                            if (Build.VERSION.SDK_INT >= 26) {
                                canvas.clipPath(circleClipPath);
                            } else {
                                canvas.clipPath(circleClipPath, Region.Op.REPLACE);
                            }
                            //浅色波浪
                            lightWavePath.moveTo(0 - offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            lightWavePath.quadTo((circleRadius + circleBorderWidth) / 2 - offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent() - waveHeight, (circleRadius + circleBorderWidth) - offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            lightWavePath.quadTo((circleRadius + circleBorderWidth) * 3 / 2 - offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent() + waveHeight, (circleRadius + circleBorderWidth) * 2 - offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            lightWavePath.quadTo((circleRadius + circleBorderWidth) * 5 / 2 - offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent() - waveHeight, (circleRadius + circleBorderWidth) * 3 - offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            lightWavePath.quadTo((circleRadius + circleBorderWidth) * 7 / 2 - offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent() + waveHeight, (circleRadius + circleBorderWidth) * 4 - offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            lightWavePath.lineTo((circleRadius + circleBorderWidth) * 2 + circleBorderWidth * 2, (circleRadius + circleBorderWidth) * 2 + circleBorderWidth * 2);
                            lightWavePath.lineTo(0, (circleRadius + circleBorderWidth) * 2 + circleBorderWidth * 2);
                            lightWavePath.close();
                            canvas.drawPath(lightWavePath, lightWavePaint);
                            //深色波浪
                            darkWavePath.moveTo(-(circleRadius + circleBorderWidth) * 2, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            darkWavePath.quadTo(-(circleRadius + circleBorderWidth) * 3 / 2 + offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent() + waveHeight, -(circleRadius + circleBorderWidth) + offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            darkWavePath.quadTo(-(circleRadius + circleBorderWidth) / 2 + offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent() - waveHeight, 0 + offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            darkWavePath.quadTo((circleRadius + circleBorderWidth) / 2 + offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent() + waveHeight, (circleRadius + circleBorderWidth) + offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            darkWavePath.quadTo((circleRadius + circleBorderWidth) * 3 / 2 + offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent() - waveHeight, (circleRadius + circleBorderWidth) * 2 + offSet, (circleRadius + circleBorderWidth) * 2 * getWaveProgressPercent());
                            darkWavePath.lineTo((circleRadius + circleBorderWidth) * 2 + circleBorderWidth * 2, (circleRadius + circleBorderWidth) * 2 + circleBorderWidth * 2);
                            darkWavePath.lineTo(0, (circleRadius + circleBorderWidth) * 2 + circleBorderWidth * 2);
                            darkWavePath.close();
                            canvas.drawPath(darkWavePath, darkWavePaint);
                            //画文字
                            TextPaint textPaint = new TextPaint();
                            textPaint.setColor(textColor);
                            textPaint.setTextSize(textSize);
                            String drawStr = getWaveProgress() + "%";
                            textPaint.getTextBounds(drawStr, 0, drawStr.length(), textMeasureRect);
                            canvas.drawText(getWaveProgress() + "%", getWidth() / 2F - textMeasureRect.width() / 2F, getHeight() / 2F + textMeasureRect.height() / 2F, textPaint);
                            holder.unlockCanvasAndPost(canvas);
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public int getColor(float val) {
        float one = (255 + 255) / 240;//（255+255）除以最大取值的三分之二
        int r = 0, g = 0, b = 0;
        if (val < 120)//第一个三等分
        {
            r = (int) (one * val);
            g = 255;
        } else if (val >= 120 && val < 240)//第二个三等分
        {
            r = 255;
            g = 255 - (int) ((val - 120) * one);//val减最大取值的三分之一
        } else {
            r = 255;
        }//最后一个三等分
        return Color.rgb(r, g, b);
    }

}
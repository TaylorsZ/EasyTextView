package com.wangdaben.easytextview;
import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.text.NoCopySpan;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.SuperscriptSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;


import androidx.annotation.FloatRange;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.ColorUtils;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ProjectName: EasyTextViewDemo
 * @Package: com.wangdaben.easytextview
 * @ClassName: EasyTextView
 * @Description: java类作用描述
 * @Author: liys
 * @CreateDate: 2022/8/29 11:03 上午
 * @UpdateUser: 更新者
 * @UpdateDate: 2022/8/29 11:03 上午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class EasyTextView  extends AppCompatTextView implements ViewType {
    private static final String TAG = "EasyTextView";

    int normalTextColor = Color.WHITE;
    int pressedTextColor = normalTextColor;
    int selectedTextColor = normalTextColor;
    int disableTextColor = Color.GRAY;

    int strokeColor = Color.TRANSPARENT;
    int normalBackgroundColor = Color.TRANSPARENT;
    int pressedBackgroundColor;
    int disableBackgroundColor;
    int selectedBackgroundColor;

    boolean strokeMode;
    boolean pressWithStrokeMode = true;

    int strokeWidth = 0;

    int radius = 0;
    int topLeftRadius = 0;
    int topRightRadius = 0;
    int bottomLeftRadius = 0;
    int bottomRightRadius = 0;

    int drawableWidth;
    int drawableHeight;
    float drawableScale;

    private int type = ViewType.RECTANGE;
    private boolean urlRegion;
    private OnUrlClickListener mOnUrlClickListener;


    private Runnable applyRunnable = new Runnable() {
        @Override
        public void run() {
            apply();
        }
    };

    /*
     * 正则文本
     * ((http|ftp|https)://)(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?|(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?
     * */
    private String pattern =
            "((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?|(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";
    // 创建 Pattern 对象
    Pattern r = Pattern.compile(pattern);
    // 现在创建 matcher 对象
    Matcher m;
    //记录网址的list
    LinkedList<String> mStringList;
    //记录该网址所在位置的list
    LinkedList<UrlInfo> mUrlInfos;
    int flag = Spanned.SPAN_POINT_MARK;

    public EasyTextView(Context context) {
        this(context, null);
    }

    public EasyTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public EasyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        strokeWidth = dp2px(0.5f);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EasyTextView);
        Drawable background = null;
        background = a.getDrawable(R.styleable.EasyTextView_android_background);
        if (background != null) {
            type = ViewType.RECTANGE;
            setBackground(background);
            a.recycle();
            return;
        } else {
            applyAttrs(context, a);
        }
        a.recycle();

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (getLineCount() > 1) {
                    if (getLineSpacingExtra() == 0 && getLineSpacingMultiplier() == 1) {
                        setLineSpacing(dp2px(1.2f), 1.2f);
                    }
                }
                getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });

    }

    void applyAttrs(Context context, TypedArray a) {
        type = a.getInt(R.styleable.EasyTextView_type, ViewType.RECTANGE);
        normalTextColor = a.getColor(R.styleable.EasyTextView_android_textColor, normalTextColor);
        pressedTextColor = a.getColor(R.styleable.EasyTextView_textPressedColor, normalTextColor);

        disableTextColor = a.getColor(R.styleable.EasyTextView_textDisableColor, ColorUtils.setAlphaComponent(normalTextColor,80));

        strokeColor = a.getColor(R.styleable.EasyTextView_strokeColor, strokeColor);
        normalBackgroundColor = a.getColor(R.styleable.EasyTextView_backgroundColor, normalBackgroundColor);
        pressedBackgroundColor = a.getColor(R.styleable.EasyTextView_backgroundPressedColor, brightnessColor(normalBackgroundColor, DEFAULT_BRIGHTNESS));
        disableBackgroundColor = a.getColor(R.styleable.EasyTextView_backgroundDisableColor, ColorUtils.setAlphaComponent(normalBackgroundColor,80));
        selectedBackgroundColor = a.getColor(R.styleable.EasyTextView_backgroundSelectedColor, normalBackgroundColor);

        strokeMode = a.getBoolean(R.styleable.EasyTextView_strokeMode, false);
        pressWithStrokeMode = a.getBoolean(R.styleable.EasyTextView_pressWithStrokeMode, pressWithStrokeMode);
        strokeWidth = a.getDimensionPixelSize(R.styleable.EasyTextView_custom_stroke_width, strokeWidth);

        radius = a.getDimensionPixelSize(R.styleable.EasyTextView_corner_radius_value, radius);
        topLeftRadius = a.getDimensionPixelSize(R.styleable.EasyTextView_corner_topLeftRadius, topLeftRadius);
        topRightRadius = a.getDimensionPixelSize(R.styleable.EasyTextView_corner_topRightRadius, topRightRadius);
        bottomLeftRadius = a.getDimensionPixelSize(R.styleable.EasyTextView_corner_bottomLeftRadius, bottomLeftRadius);
        bottomRightRadius = a.getDimensionPixelSize(R.styleable.EasyTextView_corner_bottomRightRadius, bottomRightRadius);

        drawableWidth = a.getDimensionPixelSize(R.styleable.EasyTextView_drawableWidth, drawableWidth);
        drawableHeight = a.getDimensionPixelSize(R.styleable.EasyTextView_drawableHeight, drawableHeight);
        drawableScale = a.getFloat(R.styleable.EasyTextView_drawableScale, drawableScale);

        urlRegion = a.getBoolean(R.styleable.EasyTextView_urlRegion, false);

        if (drawableWidth > 0 || drawableHeight > 0 || drawableScale > 0) {
            Drawable[] drawables = getCompoundDrawables();
            if (drawables.length == 4) {
                boolean hasDrawable = false;
                for (Drawable drawable : drawables) {
                    if (drawable != null) {
                        hasDrawable = true;
                        if (drawableWidth > 0 || drawableHeight > 0) {
                            drawable.setBounds(0, 0, drawableWidth > 0 ? drawableWidth : drawable.getIntrinsicWidth(), drawableHeight > 0 ? drawableHeight : drawable.getIntrinsicHeight());
                        } else if (drawableScale > 0) {
                            drawable.setBounds(0, 0, Math.round(drawable.getIntrinsicWidth() * drawableScale), Math.round(drawable.getIntrinsicHeight() * drawableScale));
                        }
                    }
                }
                if (hasDrawable) {
                    setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
                }
            }
        }
        apply();

    }

    //因为可能会set很多参数，因此这里采取11毫秒延迟检查，在16ms内，不影响.
    private void applyValue() {
        removeCallbacks(applyRunnable);
        postDelayed(applyRunnable, 11);
    }


    public void apply() {
        int shape = GradientDrawable.RECTANGLE;
        switch (type) {
            case ViewType.RECTANGE:
                shape = GradientDrawable.RECTANGLE;
                break;
            case ViewType.OVAL:
                shape = GradientDrawable.OVAL;
                break;
        }

        GradientDrawable normalDrawable = generateDrawable(shape, normalBackgroundColor);
        GradientDrawable pressedDrawable = generateDrawable(shape, pressedBackgroundColor);
        if (strokeMode && !pressWithStrokeMode) {
            pressedDrawable.setColor(pressedBackgroundColor);
        }
        GradientDrawable disableDrawable = generateDrawable(shape, disableBackgroundColor);
        GradientDrawable selectedDrawable = generateDrawable(shape, selectedBackgroundColor);

        StateListDrawable backgroundStateListDrawable = new StateListDrawable();
        backgroundStateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        backgroundStateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
        backgroundStateListDrawable.addState(new int[]{-android.R.attr.state_enabled}, disableDrawable);
        backgroundStateListDrawable.addState(new int[]{android.R.attr.state_selected}, selectedDrawable);
        backgroundStateListDrawable.addState(new int[]{}, normalDrawable);

        int[][] textColorState = new int[5][];
        textColorState[0] = new int[]{android.R.attr.state_pressed};
        textColorState[1] = new int[]{android.R.attr.state_focused};
        textColorState[2] = new int[]{-android.R.attr.state_enabled};
        textColorState[3] = new int[]{android.R.attr.state_selected};
        textColorState[4] = new int[]{};

        int[] textColors = {pressedTextColor, pressedTextColor, disableTextColor, selectedTextColor,normalTextColor};
        setBackground(backgroundStateListDrawable);
        setTextColor(new ColorStateList(textColorState, textColors));
    }


    private GradientDrawable generateDrawable(int shape, int color) {
        GradientDrawable result = new GradientDrawable();
        result.setShape(shape);
        if (strokeMode) {
            result.setColor(Color.TRANSPARENT);
        } else {
            result.setColor(color);
        }
        result.setStroke(strokeWidth, strokeColor == Color.TRANSPARENT ? color : strokeColor);

        if (type == RECTANGE) {
            if (radius > 0) {
                result.setCornerRadius(radius);
            } else if (topLeftRadius > 0 || topRightRadius > 0 || bottomLeftRadius > 0 || bottomRightRadius > 0) {
                result.setCornerRadii(new float[]{topLeftRadius,
                        topLeftRadius,
                        topRightRadius,
                        topRightRadius,
                        bottomRightRadius,
                        bottomRightRadius,
                        bottomLeftRadius,
                        bottomLeftRadius
                });
            }
        }
        return result;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        clearPointSpan();


        if (urlRegion) {
            text = recognUrl(text);
            this.setMovementMethod(LinkMovementMethod.getInstance());
        }
        super.setText(text, type);
    }

    private SpannableStringBuilderCompat recognUrl(CharSequence text) {
        mStringList.clear();
        mUrlInfos.clear();

        CharSequence contextText;
        CharSequence clickText;
        text = text == null ? "" : text;
        //以下用于拼接本来存在的spanText
        SpannableStringBuilderCompat span = new SpannableStringBuilderCompat(text);
        ClickableSpan[] clickableSpans = span.getSpans(0, text.length(), ClickableSpan.class);
        if (clickableSpans.length > 0) {
            int start = 0;
            int end = 0;
            for (int i = 0; i < clickableSpans.length; i++) {
                start = span.getSpanStart(clickableSpans[0]);
                end = span.getSpanEnd(clickableSpans[i]);
            }
            //可点击文本后面的内容页
            contextText = text.subSequence(end, text.length());
            //可点击文本
            clickText = text.subSequence(start,
                    end);
        } else {
            contextText = text;
            clickText = null;
        }
        m = r.matcher(contextText);
        //匹配成功
        while (m.find()) {
            //得到网址数
            UrlInfo info = new UrlInfo();
            info.start = m.start();
            info.end = m.end();
            mStringList.add(m.group());
            mUrlInfos.add(info);
        }
        return jointText(clickText, contextText);
    }

    /**
     * 拼接文本
     */
    private SpannableStringBuilderCompat jointText(CharSequence clickSpanText,
                                                   CharSequence contentText) {
        SpannableStringBuilderCompat spanBuilder;
        if (clickSpanText != null) {
            spanBuilder = new SpannableStringBuilderCompat(clickSpanText);
        } else {
            spanBuilder = new SpannableStringBuilderCompat();
        }
        if (mStringList.size() > 0) {
            //只有一个网址
            if (mStringList.size() == 1) {
                String preStr = contentText.toString().substring(0, mUrlInfos.get(0).start);
                spanBuilder.append(preStr);
                String url = mStringList.get(0);
                spanBuilder.append(url, new URLClick(url), flag);
                String nextStr = contentText.toString().substring(mUrlInfos.get(0).end);
                spanBuilder.append(nextStr);
            } else {
                //有多个网址
                for (int i = 0; i < mStringList.size(); i++) {
                    if (i == 0) {
                        //拼接第1个span的前面文本
                        String headStr =
                                contentText.toString().substring(0, mUrlInfos.get(0).start);
                        spanBuilder.append(headStr);
                    }
                    if (i == mStringList.size() - 1) {
                        //拼接最后一个span的后面的文本
                        spanBuilder.append(mStringList.get(i), new URLClick(mStringList.get(i)),
                                flag);
                        String footStr = contentText.toString().substring(mUrlInfos.get(i).end);
                        spanBuilder.append(footStr);
                    }
                    if (i != mStringList.size() - 1) {
                        //拼接两两span之间的文本
                        spanBuilder.append(mStringList.get(i), new URLClick(mStringList.get(i)), flag);
                        String betweenStr = contentText.toString()
                                .substring(mUrlInfos.get(i).end,
                                        mUrlInfos.get(i + 1).start);
                        spanBuilder.append(betweenStr);
                    }
                }
            }
        } else {
            spanBuilder.append(contentText);
        }

        return spanBuilder;
    }

    public int getNormalTextColor() {
        return normalTextColor;
    }

    public EasyTextView setNormalTextColor(int normalTextColor) {
        this.normalTextColor = normalTextColor;
        applyValue();
        return this;
    }

    public int getPressedTextColor() {
        return pressedTextColor;
    }

    public EasyTextView setPressedTextColor(int pressedTextColor) {
        this.pressedTextColor = pressedTextColor;
        applyValue();
        return this;
    }

    public int getDisableTextColor() {
        return disableTextColor;
    }

    public EasyTextView setDisableTextColor(int disableTextColor) {
        this.disableTextColor = disableTextColor;
        applyValue();
        return this;
    }

    public int getNormalBackgroundColor() {
        return normalBackgroundColor;
    }

    public EasyTextView setNormalBackgroundColor(int normalBackgroundColor) {
        this.normalBackgroundColor = normalBackgroundColor;
        pressedBackgroundColor = brightnessColor(normalBackgroundColor, DEFAULT_BRIGHTNESS);
        applyValue();
        return this;
    }

    public int getPressedBackgroundColor() {
        return pressedBackgroundColor;
    }

    public EasyTextView setPressedBackgroundColor(int pressedBackgroundColor) {
        this.pressedBackgroundColor = pressedBackgroundColor;
        applyValue();
        return this;
    }

    public int getDisableBackgroundColor() {
        return disableBackgroundColor;
    }

    public EasyTextView setDisableBackgroundColor(int disableBackgroundColor) {
        this.disableBackgroundColor = disableBackgroundColor;
        applyValue();
        return this;
    }

    public EasyTextView setSelectedBackgroundColor(int selectedBackgroundColor) {
        this.selectedBackgroundColor = selectedBackgroundColor;
        applyValue();
        return this;
    }

    public int getSelectedBackgroundColor() {
        return selectedBackgroundColor;
    }

    public boolean isStrokeMode() {
        return strokeMode;
    }

    public EasyTextView setStrokeMode(boolean strokeMode) {
        this.strokeMode = strokeMode;
        applyValue();
        return this;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public EasyTextView setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        applyValue();
        return this;
    }

    public int getRadius() {
        return radius;
    }

    public EasyTextView setRadius(int radius) {
        this.radius = radius;
        applyValue();
        return this;
    }

    public int getTopLeftRadius() {
        return topLeftRadius;
    }

    public EasyTextView setTopLeftRadius(int topLeftRadius) {
        this.topLeftRadius = topLeftRadius;
        applyValue();
        return this;
    }

    public int getTopRightRadius() {
        return topRightRadius;
    }

    public EasyTextView setTopRightRadius(int topRightRadius) {
        this.topRightRadius = topRightRadius;
        applyValue();
        return this;
    }

    public int getBottomLeftRadius() {
        return bottomLeftRadius;
    }

    public EasyTextView setBottomLeftRadius(int bottomLeftRadius) {
        this.bottomLeftRadius = bottomLeftRadius;
        applyValue();
        return this;
    }

    public int getBottomRightRadius() {
        return bottomRightRadius;
    }

    public EasyTextView setBottomRightRadius(int bottomRightRadius) {
        this.bottomRightRadius = bottomRightRadius;
        applyValue();
        return this;
    }

    public int getType() {
        return type;
    }

    public EasyTextView setType(int type) {
        this.type = type;
        applyValue();
        return this;
    }

    public boolean isUrlRegion() {
        return urlRegion;
    }

    public void setUrlRegion(boolean urlRegion) {
        this.urlRegion = urlRegion;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public EasyTextView setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        applyValue();
        return this;
    }

    @Override
    public void setTextColor(int color) {
        setNormalTextColor(color);
        super.setTextColor(color);
    }

    private static class UrlInfo {
        public int start;
        public int end;
    }

    private class URLClick extends ClickableSpan {
        private String text;

        public URLClick(String text) {
            this.text = text;
        }

        @Override
        public void onClick(View widget) {
            if (mOnUrlClickListener != null) {
                if (mOnUrlClickListener.onUrlClickListener(text)) return;
            }
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(text);
            intent.setData(content_url);
            getContext().startActivity(intent);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(0xff517fae);
            ds.setUnderlineText(false);
        }
    }

    public interface OnUrlClickListener {
        boolean onUrlClickListener(String url);
    }


    public void setLoadingText() {
        setLoadingText("...");
    }

    public void setLoadingText(CharSequence text) {
        setLoadingText(text, 1500);
    }

    public void setLoadingText(CharSequence text, int loopDuration) {
        clearPointSpan();
        SpannableStringBuilderCompat builderCompat = new SpannableStringBuilderCompat(text);
        final int length = builderCompat.length();
        //延迟
        int delay = (int) ((loopDuration / length) * 0.5);
        for (int i = 0; i < length; i++) {
            LoadingPointSpan span = new LoadingPointSpan(delay * i, loopDuration, 0.4f);
            builderCompat.setSpan(span, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        super.setText(builderCompat);
    }

    @Override
    protected void onDetachedFromWindow() {
        clearPointSpan();
        super.onDetachedFromWindow();
    }

    private void stopPointSpan() {
        if (getText() instanceof Spanned) {
            LoadingPointSpan[] span = ((Spanned) getText()).getSpans(0, getText().length(), LoadingPointSpan.class);
            if (span != null) {
                for (LoadingPointSpan loadingPointSpan : span) {
                    loadingPointSpan.stop();
                }
            }
        }
    }

    private void startPointSpan() {
        if (getText() instanceof Spanned) {
            LoadingPointSpan[] span = ((Spanned) getText()).getSpans(0, getText().length(), LoadingPointSpan.class);
            if (span != null) {
                for (LoadingPointSpan loadingPointSpan : span) {
                    loadingPointSpan.start();
                }
            }
        }
    }

    private void clearPointSpan() {
        if (getText() instanceof Spanned) {
            LoadingPointSpan[] span = ((Spanned) getText()).getSpans(0, getText().length(), LoadingPointSpan.class);
            if (span != null) {
                for (LoadingPointSpan loadingPointSpan : span) {
                    loadingPointSpan.clear();
                }
            }
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            startPointSpan();
        } else {
            stopPointSpan();
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        startPointSpan();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        stopPointSpan();
        return super.onSaveInstanceState();
    }

    /**
     * textview小点点span，内封动画（注意内存泄漏）
     */
    class LoadingPointSpan extends SuperscriptSpan implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener, NoCopySpan {
        private int delay;
        private int duration;
        private float maxOffsetRatio;//最高弹跳高度百分比
        private ValueAnimator mValueAnimator;
        private int curOffset;

        LoadingPointSpan(int delay, int duration, float maxOffsetRatio) {
            this.delay = delay;
            this.duration = duration;
            this.maxOffsetRatio = maxOffsetRatio;
        }

        /**
         * 更改baseline,.来改变位置
         *
         * @param tp
         */
        @Override
        public void updateDrawState(TextPaint tp) {
            initAnimate(tp.ascent());
            tp.baselineShift = curOffset;
        }


        /**
         * @param textAscent 文字高度
         */
        private void initAnimate(float textAscent) {
            if (mValueAnimator != null) {
                return;
            }
            curOffset = 0;
            maxOffsetRatio = Math.max(0, Math.min(1.0f, maxOffsetRatio));
            int maxOffset = (int) (textAscent * maxOffsetRatio);
            mValueAnimator = ValueAnimator.ofInt(0, maxOffset);
            mValueAnimator.setDuration(duration);
            mValueAnimator.setStartDelay(delay);
            mValueAnimator.setInterpolator(new PointInterpolator(maxOffsetRatio));
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
            mValueAnimator.addUpdateListener(this);
            mValueAnimator.addListener(this);
            mValueAnimator.start();
        }

        void stop() {
            if (mValueAnimator != null) {
                mValueAnimator.cancel();
            }
        }

        void start() {
            if (mValueAnimator != null) {
                mValueAnimator.start();
            }
        }

        void clear() {
            if (mValueAnimator != null) {
                mValueAnimator.removeAllUpdateListeners();
                mValueAnimator.removeAllListeners();
                mValueAnimator.cancel();
            }
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            curOffset = (int) animation.getAnimatedValue();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (isAttachedToWindow()) {
                    invalidate();
                }
            } else {
                if (getParent() != null) {
                    invalidate();
                }
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            curOffset = 0;
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        /**
         * 时间限制在0~maxOffsetRatio
         */
        private class PointInterpolator implements TimeInterpolator {

            private final float maxOffsetRatio;

            public PointInterpolator(float animatedRange) {
                maxOffsetRatio = Math.abs(animatedRange);
            }

            @Override
            public float getInterpolation(float input) {
                if (input > maxOffsetRatio) {
                    return 0f;
                }
                double radians = (input / maxOffsetRatio) * Math.PI;
                return (float) Math.sin(radians);
            }

        }
    }

    private int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    private final float DEFAULT_BRIGHTNESS = 0.75f;
    /**
     * 根据RGB值判断 深色与浅色
     */
    private boolean isDark(int r, int g, int b) {
        return !(r * 0.299 + g * 0.578 + b * 0.114 >= 192);
    }

    private int brightnessColor(int color, @FloatRange(from = 0f) float brightness) {
        if (color == Color.TRANSPARENT) return color;
        int alpha = Color.alpha(color);
        float[] hslArray = new float[3];

        ColorUtils.colorToHSL(color, hslArray);
        hslArray[2] = hslArray[2] * brightness;

        int result = ColorUtils.HSLToColor(hslArray);
        if (result == Color.BLACK) {
            result = Color.parseColor("#575757");
        } else if (result == Color.WHITE) {
            result = Color.parseColor("#EAEAEA");
        }

        return Color.argb(alpha, Color.red(result), Color.green(result), Color.blue(result));
    }

}


package com.himamis.retex.editor.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.himamis.retex.editor.android.event.ClickListenerAdapter;
import com.himamis.retex.editor.android.event.FocusListenerAdapter;
import com.himamis.retex.editor.android.event.KeyListenerAdapter;
import com.himamis.retex.editor.share.editor.MathFieldInternal;
import com.himamis.retex.editor.share.event.ClickListener;
import com.himamis.retex.editor.share.event.FocusListener;
import com.himamis.retex.editor.share.event.KeyListener;
import com.himamis.retex.editor.share.editor.MathField;
import com.himamis.retex.editor.share.meta.MetaModel;
import com.himamis.retex.editor.share.model.MathFormula;
import com.himamis.retex.renderer.android.FactoryProviderAndroid;
import com.himamis.retex.renderer.android.graphics.ColorA;
import com.himamis.retex.renderer.android.graphics.Graphics2DA;
import com.himamis.retex.renderer.share.ColorUtil;
import com.himamis.retex.renderer.share.TeXFormula;
import com.himamis.retex.renderer.share.TeXIcon;
import com.himamis.retex.renderer.share.platform.FactoryProvider;
import com.himamis.retex.renderer.share.platform.Resource;

import java.io.InputStream;

public class FormulaEditor extends View implements MathField {

    private static MetaModel sMetaModel;

    private TeXIcon mTeXIcon;
    private Graphics2DA mGraphics;
    private MathFieldInternal mMathFieldInternal;

    private float mSize = 20;
    private int mBackgroundColor = Color.TRANSPARENT;
    private ColorA mForegroundColor = new ColorA(android.graphics.Color.BLACK);

    private float mScale;

    public FormulaEditor(Context context) {
        super(context);
        init();
    }

    public FormulaEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttributes(context, attrs, 0);
        init();
    }

    public FormulaEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttributes(context, attrs, defStyleAttr);
        init();
    }

    private void readAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FormulaEditor,
                defStyleAttr, 0);

        try {
            mSize = a.getFloat(R.styleable.FormulaEditor_fe_size, 20);
            mBackgroundColor = a.getColor(R.styleable.FormulaEditor_fe_backgroundColor, android.graphics.Color.TRANSPARENT);
            mForegroundColor = new ColorA(a.getColor(R.styleable.FormulaEditor_fe_foregroundColor, android.graphics.Color.BLACK));
        } finally {
            a.recycle();
        }
    }

    private void init() {
        initFactoryProvider();
        initMetaModel();
        setFocusable(true);
        setFocusableInTouchMode(true);

        mScale = getResources().getDisplayMetrics().scaledDensity;

        mMathFieldInternal = new MathFieldInternal();
        mMathFieldInternal.setSize(mSize * mScale);
        mMathFieldInternal.setMathField(this);
        mMathFieldInternal.setFormula(MathFormula.newFormula(sMetaModel));
    }

    private void initFactoryProvider() {
        if (FactoryProvider.INSTANCE == null) {
            FactoryProvider.INSTANCE = new FactoryProviderAndroid(getContext().getAssets());
        }
    }

    private void initMetaModel() {
        if (sMetaModel == null) {
            sMetaModel = new MetaModel(new Resource().loadResource("Octave.xml"));
        }
    }

    /**
     * Sets the color of the text. Must be called from the UI thread.
     *
     * @param foregroundColor color represented as packed ints
     */
    public void setForegroundColor(int foregroundColor) {
        mForegroundColor = new ColorA(foregroundColor);
        invalidate();
    }

    /**
     * Sets the color of the background. Must be called from the UI thread.
     *
     * @param backgroundColor color represented as packed ints
     */
    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        invalidate();
    }

    @Override
    public void setTeXIcon(TeXIcon icon) {
        mTeXIcon = icon;
    }

    @Override
    public void setFocusListener(FocusListener focusListener) {
        setOnFocusChangeListener(new FocusListenerAdapter(focusListener));
    }

    @Override
    public void setClickListener(ClickListener clickListener) {
        setOnClickListener(new ClickListenerAdapter(clickListener));
    }

    @Override
    public void setKeyListener(KeyListener keyListener) {
        setOnKeyListener(new KeyListenerAdapter(keyListener));
    }

    @Override
    public void repaint() {
        invalidate();
    }

    @Override
    public boolean hasParent() {
        return getParent() != null;
    }

    public void requestViewFocus() {
        requestFocus();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int desiredWidth = mTeXIcon.getIconWidth();
        final int desiredHeight = (int) (Math.max(mSize * mScale, mTeXIcon.getIconHeight()) + 0.5);

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTeXIcon == null) {
            return;
        }

        if (mGraphics == null) {
            mGraphics = new Graphics2DA();
        }
        // draw background
        canvas.drawColor(mBackgroundColor);

        // draw latex
        mGraphics.setCanvas(canvas);
        mTeXIcon.setForeground(mForegroundColor);
        mTeXIcon.paintIcon(null, mGraphics, 0, 0);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        BaseInputConnection fic = new BaseInputConnection(this, false);
        outAttrs.actionLabel = null;
        outAttrs.inputType = InputType.TYPE_NULL;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NEXT;
        return fic;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // show the keyboard so we can enter text
            InputMethodManager imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        }
        return true;
    }
}

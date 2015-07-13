package com.himamis.retex.editor.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import org.scilab.forge.jlatexmath.ColorUtil;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.scilab.forge.jlatexmath.graphics.Graphics2DA;

import com.himamis.retex.editor.android.event.ClickListenerAdapter;
import com.himamis.retex.editor.android.event.FocusListenerAdapter;
import com.himamis.retex.editor.android.event.KeyListenerAdapter;
import com.himamis.retex.editor.share.event.ClickListener;
import com.himamis.retex.editor.share.event.FocusListener;
import com.himamis.retex.editor.share.event.KeyListener;
import com.himamis.retex.editor.share.editor.MathField;

public class FormulaEditor extends View implements MathField {

    private TeXIcon mTeXIcon;
    private Graphics2DA mGraphics;

    public FormulaEditor(Context context) {
        super(context);
    }

    public FormulaEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        if (widthSpecMode == MeasureSpec.UNSPECIFIED && mTeXIcon != null) {
            measuredWidth = mTeXIcon.getIconWidth();
        }
        if (heightSpecMode == MeasureSpec.UNSPECIFIED && mTeXIcon != null) {
            measuredHeight = mTeXIcon.getIconHeight();
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
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
        canvas.drawColor(Color.WHITE);

        // draw latex
        mGraphics.setCanvas(canvas);
        mTeXIcon.setForeground(ColorUtil.BLACK);
        mTeXIcon.paintIcon(null, mGraphics, 0, 0);
    }
}
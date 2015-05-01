package com.micromobs.android.floatlabel;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

@TargetApi(11)
public class FloatLabelEditText
    extends LinearLayout {

    private int mCurrentApiVersion = android.os.Build.VERSION.SDK_INT;
    private int mFocusedColor = android.R.color.black;
    private int mUnFocusedColor = android.R.color.darker_gray;
    private int mFitScreenWidth;
    private int mGravity = Gravity.LEFT;

    private float mTextSizeInSp;
    private CharSequence mHintText;
    private CharSequence mEditText;

    private AttributeSet mAttrs;
    private Context mContext;
    private EditText mEditTextView;
    private TextView mFloatingLabel;

    // -----------------------------------------------------------------------
    // default constructors

    public FloatLabelEditText(Context context) {
        super(context);
        mContext = context;
        initializeView();
    }

    public FloatLabelEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        initializeView();
    }

    public FloatLabelEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mAttrs = attrs;
        initializeView();
    }

    // -----------------------------------------------------------------------
    // public interface

    public EditText getEditText() {
        return mEditTextView;
    }

    public String getText() {
        if (getEditTextString() != null &&
            getEditTextString().toString() != null &&
            getEditTextString().toString().length() > 0) {
            return getEditTextString().toString();
        }
        return "";
    }

    public void setText(CharSequence text) {
        mEditTextView.setText(text);
    }

    public void setHint(String hintText) {
        mHintText = hintText;
        mFloatingLabel.setText(hintText);
        setupEditTextView();
    }

    public void setError(CharSequence text) {
        mEditTextView.setError(text);
    }

    // -----------------------------------------------------------------------
    // private helpers

    private void initializeView() {

        if (mContext == null) {
            return;
        }

        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.floatlabel_edittext, this, true);

        mFloatingLabel = (TextView) findViewById(R.id.floating_label_hint);
        mEditTextView = (EditText) findViewById(R.id.floating_label_edit_text);

        getAttributesFromXmlAndStoreLocally();
        setupEditTextView();
        setupFloatingLabel();
    }

    private void getAttributesFromXmlAndStoreLocally() {
        TypedArray a = mContext.obtainStyledAttributes(mAttrs, R.styleable.FloatLabelEditText);
        if (a == null) {
            return;
        }

        mTextSizeInSp = getScaledFontSize(mEditTextView.getTextSize());

        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            if (attr == R.styleable.FloatLabelEditText_android_hint) {
                mHintText = a.getText(attr);
            } else if (attr == R.styleable.FloatLabelEditText_android_text) {
                mEditText = a.getText(attr);
            } else if (attr == R.styleable.FloatLabelEditText_android_inputType) {
                mEditTextView.setInputType(a.getInt(attr, mEditTextView.getInputType()));
            } else if (attr == R.styleable.FloatLabelEditText_android_imeOptions) {
                mEditTextView.setImeOptions(a.getInt(attr, mEditTextView.getImeOptions()));
            } else if (attr == R.styleable.FloatLabelEditText_android_gravity) {
                mGravity = a.getInt(attr, mGravity);
            } else if (attr == R.styleable.FloatLabelEditText_android_textSize) {
                mTextSizeInSp = getScaledFontSize(a.getDimensionPixelSize(attr,
                        (int) mEditTextView.getTextSize()));
            } else if (attr == R.styleable.FloatLabelEditText_textColorHintFocused) {
                mFocusedColor = a.getColor(attr, mFocusedColor);
            } else if (attr == R.styleable.FloatLabelEditText_textColorHintUnFocused) {
                mUnFocusedColor = a.getColor(attr, mUnFocusedColor);
            } else if (attr == R.styleable.FloatLabelEditText_fitScreenWidth) {
                mFitScreenWidth = a.getInt(attr, 0);
            }
        }
        a.recycle();
    }

    private void setupEditTextView() {

        mEditTextView.setHint(mHintText);
        mEditTextView.setHintTextColor(mUnFocusedColor);
        mEditTextView.setText(mEditText);
        mEditTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSizeInSp);
        mEditTextView.addTextChangedListener(getTextWatcher());

        if (mFitScreenWidth > 0) {
            mEditTextView.setWidth(getSpecialWidth());
        }

        if (mCurrentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            mEditTextView.setOnFocusChangeListener(getFocusChangeListener());
        }
    }

    private void setupFloatingLabel() {
        mFloatingLabel.setText(mHintText);
        mFloatingLabel.setTextColor(mUnFocusedColor);
        mFloatingLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (mTextSizeInSp / 1.3));
        mFloatingLabel.setGravity(mGravity);
        mFloatingLabel.setPadding(mEditTextView.getPaddingLeft(), 0, 0, 0);

        if (getText().length() > 0) {
            showFloatingLabel(false);
        }
    }

    private TextWatcher getTextWatcher() {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && mFloatingLabel.getVisibility() == INVISIBLE) {
                    showFloatingLabel(true);
                } else if (s.length() == 0 && mFloatingLabel.getVisibility() == VISIBLE) {
                    hideFloatingLabel(true);
                }
            }
        };
    }

    private void showFloatingLabel(boolean animate) {
        mFloatingLabel.setVisibility(VISIBLE);
        if (animate)
            mFloatingLabel.startAnimation(AnimationUtils.loadAnimation(getContext(),
                                                                   R.anim.weddingparty_floatlabel_slide_from_bottom));
    }

    private void hideFloatingLabel(boolean animate) {
        mFloatingLabel.setVisibility(INVISIBLE);
        if (animate)
            mFloatingLabel.startAnimation(AnimationUtils.loadAnimation(getContext(),
                                                                   R.anim.weddingparty_floatlabel_slide_to_bottom));
    }

    private OnFocusChangeListener getFocusChangeListener() {
        return new OnFocusChangeListener() {

            ValueAnimator mFocusToUnfocusAnimation
                ,
                mUnfocusToFocusAnimation;

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ValueAnimator lColorAnimation;

                if (hasFocus) {
                    lColorAnimation = getFocusToUnfocusAnimation();
                } else {
                    lColorAnimation = getUnfocusToFocusAnimation();
                }

                lColorAnimation.setDuration(700);
                lColorAnimation.start();
            }

            private ValueAnimator getFocusToUnfocusAnimation() {
                if (mFocusToUnfocusAnimation == null) {
                    mFocusToUnfocusAnimation = getFocusAnimation(mUnFocusedColor, mFocusedColor);
                }
                return mFocusToUnfocusAnimation;
            }

            private ValueAnimator getUnfocusToFocusAnimation() {
                if (mUnfocusToFocusAnimation == null) {
                    mUnfocusToFocusAnimation = getFocusAnimation(mFocusedColor, mUnFocusedColor);
                }
                return mUnfocusToFocusAnimation;
            }
        };
    }

    private ValueAnimator getFocusAnimation(int fromColor, int toColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                                                              fromColor,
                                                              toColor);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mFloatingLabel.setTextColor((Integer) animator.getAnimatedValue());
            }
        });
        return colorAnimation;
    }

    private Editable getEditTextString() {
        return mEditTextView.getText();
    }

    private float getScaledFontSize(float fontSizeFromAttributes) {
        float scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
        return fontSizeFromAttributes / scaledDensity;
    }

    private int getSpecialWidth() {
        float screenWidth = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                                                                                               .getWidth();
        int prevWidth = mEditTextView.getWidth();

        switch (mFitScreenWidth) {
            case 2:
                return (int) Math.round(screenWidth * 0.5);
            default:
                return Math.round(screenWidth);
        }
    }


}

package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.halman.molkkynotes.R;

public class UIButton extends LinearLayout {
    private ImageView _image = null;
    private TextView _text = null;
    private LinearLayout _root = null;
    private float _size = 20.0f;
    private boolean _active = true;

    public UIButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public UIButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public UIButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        // Load attributes
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.ui_button, this);

        _image = (ImageView) this.findViewById(R.id.image);
        _text = (TextView) this.findViewById(R.id.text);
        _root = (LinearLayout) this.findViewById(R.id.uibuttonroot);


        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UIButton);
        Drawable drawable = a.getDrawable(R.styleable.UIButton_image);
        _size = a.getDimension(R.styleable.UIButton_imagesize, 20.0f);
        if (drawable != null) {
            android.view.ViewGroup.LayoutParams layoutParams = _image.getLayoutParams();
            layoutParams.width = (int) _size;
            layoutParams.height = (int) _size;
            _image.setLayoutParams(layoutParams);
            _image.setImageDrawable(drawable);
            _image.setVisibility(VISIBLE);
        } else {
            _image.setVisibility(GONE);
        }

        String T = a.getString(R.styleable.UIButton_text);
        if (T != null) {
            _text.setText(T);
            _text.setVisibility(VISIBLE);
        } else {
            _text.setVisibility(GONE);
        }
    }

    public void text(String txt) {
        if (_text != null) {
            if (txt.equals("")) {
                _text.setVisibility(GONE);
            } else {
                _text.setText(txt);
                _text.setVisibility(VISIBLE);
            }
        }
    }

    public boolean active() {
        return _active;
    }

    public void active(boolean a) {
        if (_active == a) {
            return;
        }

        _active = a;
        Drawable b = _root.getBackground();
        if (_active) {
            _text.setTextColor(getResources().getColor(R.color.colorWhite));
            _image.setImageAlpha(255);
            if (b != null) {
                b.setAlpha(255);
            }
        } else {
            _text.setTextColor(getResources().getColor(R.color.colorGray));
            _image.setImageAlpha(64);

            if (b != null) {
                b.setAlpha(64);
            }
        }
    }

    public String text()
    {
        return _text.getText().toString();
    }
}

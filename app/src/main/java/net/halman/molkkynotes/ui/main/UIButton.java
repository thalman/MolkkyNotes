package net.halman.molkkynotes.ui.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.halman.molkkynotes.R;

public class UIButton extends LinearLayout {
    private ImageView _image = null;
    private TextView _text = null;

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


        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UIButton);
        Drawable drawable = a.getDrawable(R.styleable.UIButton_image);
        if (drawable != null) {
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

    public void image(Drawable d)
    {
        if (_image != null) {
            if (d == null) {
                _image.setVisibility(GONE);
            } else {
                _image.setImageDrawable(d);
                _image.setVisibility(VISIBLE);
            }
        }
    }

    public void text(String txt)
    {
        if (_text != null) {
            if (txt.equals("")) {
                _text.setVisibility(GONE);
            } else {
                _text.setText(txt);
                _text.setVisibility(VISIBLE);
            }
        }
    }

    public String text()
    {
        return _text.getText().toString();
    }
}

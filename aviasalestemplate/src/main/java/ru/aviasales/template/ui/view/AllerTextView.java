package ru.aviasales.template.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by bullhead on 11/03/2018.
 *
 */

public class AllerTextView extends AppCompatTextView{
    public AllerTextView(Context context) {
        super(context);
        setTypeface();
    }

    public AllerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface();
    }

    public AllerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface();
    }
    private void setTypeface(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"font.ttf"));
    }
}

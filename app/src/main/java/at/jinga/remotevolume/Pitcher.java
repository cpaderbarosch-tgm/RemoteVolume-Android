package at.jinga.remotevolume;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Pitcher extends LinearLayout {
    public TextView nameView;
    public VerticalSeekBar sliderView;
    public TextView volumeView;

    final float dp = getContext().getResources().getDisplayMetrics().density;

    public Pitcher(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public Pitcher(Context context, final String name, float level) {
        super(context);
        init();

        nameView.setText(name);
        sliderView.setProgress(Math.round(level));
        volumeView.setText("" + Math.round(level));
    }

    private void init() {
        inflate(getContext(), R.layout.pitcher, this);

        nameView = (TextView) findViewById(R.id.name);
        sliderView = (VerticalSeekBar) findViewById(R.id.slider);
        volumeView = (TextView) findViewById(R.id.volume);
    }
}

package at.jinga.remotevolume;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class PitcherView extends FrameLayout {
    final float dp = getContext().getResources().getDisplayMetrics().density;

    public PitcherView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        View view = initView();
    }

    public PitcherView(Context context, final String name, float level) {
        super(context);

        View pitcher = initView();

        TextView pitcherName = (TextView) pitcher.findViewById(R.id.name);
        VerticalSeekBar seekBar = (VerticalSeekBar) pitcher.findViewById(R.id.seekbar);
        TextView volume = (TextView) pitcher.findViewById(R.id.volume);

        pitcherName.setText(name);
        seekBar.setProgress(Math.round(level));
        volume.setText("" + Math.round(level));
    }

    private View initView() {
        View view = inflate(getContext(), R.layout.pitcher_view, null);
        addView(view);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 0, 10, 0);

        this.setLayoutParams(lp);

        return view;
    }
}

package io.l0neman.fixtouchdelegate;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.chrisbanes.photoview.PhotoView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        FrameLayout parent = findViewById(R.id.fl_parent);
        PhotoView appIcon = findViewById(R.id.pv_touch);

        parent.post(() -> expandViewTouchDelegate(appIcon, parent));
    }

    public static void expandViewTouchDelegate(final View view, final View parent) {
        view.post(() -> {
            Rect bounds = new Rect();
            view.setEnabled(true);
            ((ViewGroup) view.getParent()).getHitRect(bounds);

            bounds.left = 0;
            bounds.top = 0;
            bounds.right = parent.getWidth();
            bounds.bottom = parent.getHeight();

            TouchDelegate touchDelegate = new FixTouchDelegate(bounds, view);

            parent.setTouchDelegate(touchDelegate);
        });
    }
}
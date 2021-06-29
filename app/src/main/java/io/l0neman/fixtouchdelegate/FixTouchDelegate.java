package io.l0neman.fixtouchdelegate;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewConfiguration;

public class FixTouchDelegate extends TouchDelegate {
    private static final String TAG = FixTouchDelegate.class.getSimpleName();
    private View mDelegateView;
    private Rect mBounds;
    private Rect mSlopBounds;
    private boolean mDelegateTargeted;
    private int mSlop;

    public FixTouchDelegate(Rect bounds, View delegateView) {
        super(bounds, delegateView);
        mBounds = bounds;

        mSlop = ViewConfiguration.get(delegateView.getContext()).getScaledTouchSlop();
        mSlopBounds = new Rect(bounds);
        mSlopBounds.inset(-mSlop, -mSlop);
        mDelegateView = delegateView;
    }

    private float mActivePointerId;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        boolean sendToDelegate = false;
        boolean hit = true;
        boolean handled = false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDelegateTargeted = mBounds.contains(x, y);
                sendToDelegate = mDelegateTargeted;

                final int pointerIndex = event.getActionIndex();
                mActivePointerId = event.getPointerId(pointerIndex);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                sendToDelegate = mDelegateTargeted;
                if (sendToDelegate) {
                    Rect slopBounds = mSlopBounds;
                    if (!slopBounds.contains(x, y)) {
                        hit = false;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                sendToDelegate = mDelegateTargeted;
                mDelegateTargeted = false;
                break;
        }
        if (sendToDelegate) {
            MotionEvent obtain = null;
            if (hit) {
                // Offset event coordinates to be inside the target view
                // event.setLocation(mDelegateView.getWidth() / 2, mDelegateView.getHeight() / 2);
                obtain = MotionEvent.obtain(
                        System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        event.getAction(),
                        event.getPointerCount(),
                        getPointerProperties(event),
                        fixPointerCoords(event),
                        event.getMetaState(),
                        event.getButtonState(),
                        event.getXPrecision(),
                        event.getYPrecision(),
                        event.getDeviceId(),
                        event.getEdgeFlags(),
                        event.getSource(),
                        event.getFlags()
                );
            } else {
                // Offset event coordinates to be outside the target view (in case it does
                // something like tracking pressed state)
                int slop = mSlop;
                event.setLocation(-(slop * 2), -(slop * 2));
            }

            if (obtain != null) {
                handled = mDelegateView.dispatchTouchEvent(obtain);
                obtain.recycle();
            } else {
                handled = mDelegateView.dispatchTouchEvent(event);
            }
        }
        return handled;
    }

    private MotionEvent.PointerProperties[] getPointerProperties(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            MotionEvent.PointerProperties pointerProperties = new MotionEvent.PointerProperties();
            event.getPointerProperties(i, pointerProperties);
            properties[i] = pointerProperties;
        }

        return properties;
    }

    private MotionEvent.PointerCoords[] fixPointerCoords(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            event.getPointerCoords(i, coords);
            coords.x = coords.x * 1F / mBounds.width() * mDelegateView.getWidth();
            coords.y = coords.y * 1F / mBounds.width() * mDelegateView.getWidth();
            pointerCoords[i] = coords;
        }

        return pointerCoords;
    }
}

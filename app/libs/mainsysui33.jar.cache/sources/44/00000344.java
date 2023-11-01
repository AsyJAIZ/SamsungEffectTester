package androidx.core.animation;

import android.graphics.Path;
import com.android.settingslib.widget.ActionBarShadowController;

/* loaded from: mainsysui33.jar:androidx/core/animation/PathInterpolator.class */
public class PathInterpolator implements Interpolator {
    public float[] mData;

    public PathInterpolator(float f, float f2, float f3, float f4) {
        initCubic(f, f2, f3, f4);
    }

    public PathInterpolator(Path path) {
        initPath(path);
    }

    public static boolean floatEquals(float f, float f2) {
        return Math.abs(f - f2) < 0.01f;
    }

    public final float getFractionAtIndex(int i) {
        return this.mData[i * 3];
    }

    @Override // androidx.core.animation.Interpolator
    public float getInterpolation(float f) {
        if (f <= ActionBarShadowController.ELEVATION_LOW) {
            return ActionBarShadowController.ELEVATION_LOW;
        }
        if (f >= 1.0f) {
            return 1.0f;
        }
        int i = 0;
        int numOfPoints = getNumOfPoints() - 1;
        while (numOfPoints - i > 1) {
            int i2 = (i + numOfPoints) / 2;
            if (f < getXAtIndex(i2)) {
                numOfPoints = i2;
            } else {
                i = i2;
            }
        }
        float xAtIndex = getXAtIndex(numOfPoints) - getXAtIndex(i);
        if (xAtIndex == ActionBarShadowController.ELEVATION_LOW) {
            return getYAtIndex(i);
        }
        float xAtIndex2 = (f - getXAtIndex(i)) / xAtIndex;
        float yAtIndex = getYAtIndex(i);
        return yAtIndex + (xAtIndex2 * (getYAtIndex(numOfPoints) - yAtIndex));
    }

    public final int getNumOfPoints() {
        return this.mData.length / 3;
    }

    public final float getXAtIndex(int i) {
        return this.mData[(i * 3) + 1];
    }

    public final float getYAtIndex(int i) {
        return this.mData[(i * 3) + 2];
    }

    public final void initCubic(float f, float f2, float f3, float f4) {
        Path path = new Path();
        path.moveTo(ActionBarShadowController.ELEVATION_LOW, ActionBarShadowController.ELEVATION_LOW);
        path.cubicTo(f, f2, f3, f4, 1.0f, 1.0f);
        initPath(path);
    }

    public final void initPath(Path path) {
        this.mData = PathUtils.createKeyFrameData(path, 0.002f);
        int numOfPoints = getNumOfPoints();
        int i = 0;
        float f = 0.0f;
        if (floatEquals(getXAtIndex(0), ActionBarShadowController.ELEVATION_LOW) && floatEquals(getYAtIndex(0), ActionBarShadowController.ELEVATION_LOW)) {
            int i2 = numOfPoints - 1;
            if (floatEquals(getXAtIndex(i2), 1.0f) && floatEquals(getYAtIndex(i2), 1.0f)) {
                float f2 = ActionBarShadowController.ELEVATION_LOW;
                while (true) {
                    float f3 = f2;
                    if (i >= numOfPoints) {
                        return;
                    }
                    float fractionAtIndex = getFractionAtIndex(i);
                    float xAtIndex = getXAtIndex(i);
                    if (fractionAtIndex == f && xAtIndex != f3) {
                        throw new IllegalArgumentException("The Path cannot have discontinuity in the X axis.");
                    }
                    if (xAtIndex < f3) {
                        throw new IllegalArgumentException("The Path cannot loop back on itself.");
                    }
                    i++;
                    f = fractionAtIndex;
                    f2 = xAtIndex;
                }
            }
        }
        throw new IllegalArgumentException("The Path must start at (0,0) and end at (1,1)");
    }
}
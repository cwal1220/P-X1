/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.inspeco.wave;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import java.util.Arrays;

public final class AudioUtils {

    public static int calculateAudioLength(int samplesCount, int sampleRate, int channelCount) {
        return ((samplesCount / channelCount) * 1000) / sampleRate;
    }

    public static short[][] getExtremes(short[] data, int sampleSize) {
        short[][] newData = new short[sampleSize][];
        int groupSize = data.length / sampleSize;

        for (int i = 0; i < sampleSize; i++) {
            short[] group = Arrays.copyOfRange(data, i * groupSize,
                    Math.min((i + 1) * groupSize, data.length));

            // Fin min & max values
            short min = Short.MAX_VALUE, max = Short.MIN_VALUE;
            for (short a : group) {
                min = (short) Math.min(min, a);
                max = (short) Math.max(max, a);
            }
            newData[i] = new short[] { max, min };
        }

        return newData;
    }

    public static float getFontSize(Context ctx, int textAppearance) {
        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(textAppearance, typedValue, true);
        int[] textSizeAttr = new int[] { android.R.attr.textSize };
        TypedArray arr = ctx.obtainStyledAttributes(typedValue.data, textSizeAttr);
        float fontSize = arr.getDimensionPixelSize(0, -1);
        arr.recycle();
        return fontSize;
    }
}

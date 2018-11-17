package com.apps.adrcotfas.goodtime.Statistics.Main;

import android.graphics.Canvas;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Draws the Y-axis labels between and above the grid lines as opposed to the default way
 * of drawing the labels besides the grid lines, to the left.
 */
public class CustomYAxisRenderer extends YAxisRenderer {

    public CustomYAxisRenderer(ViewPortHandler viewPortHandler, YAxis yAxis, Transformer trans) {
        super(viewPortHandler, yAxis, trans);
    }

    @Override
    protected void drawYLabels(Canvas c, float fixedPosition, float[] positions, float offset) {

        // hacky way of getting a valid distance between labels
        float offsetY = 0;
        for (int i = 0; i < positions.length; ++i) {
            if (positions[i] > 0 && positions[i] != Double.POSITIVE_INFINITY) {
                if (i + 2 < positions.length && positions[i + 2] > 0 && positions[i + 2] != Double.POSITIVE_INFINITY) {
                    offsetY = Math.abs(positions[i] - positions[i + 2]) / 2.5f;
                }
            }
        }
        super.drawYLabels(c, fixedPosition * 2 + 20, positions, offset - offsetY);
    }
}

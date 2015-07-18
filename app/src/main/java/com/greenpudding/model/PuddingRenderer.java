package com.greenpudding.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.greenpudding.util.UndirectedWeightedGraph;

import javax.vecmath.Vector2d;

/**
 * Created by forkburn on 2015-07-17.
 */
public class PuddingRenderer {


    // number of nodes
    private int numNodes;

    // the points generated by interpolation, used as start/ending point when
    // drawing curves
    private Vector2d[] interpolatedNodes;


    // the path used for drawing the border line
    private Path path;
    // the paint used for specifying the fill color
    private Paint solidColorPaint;
    private Paint strokePaint;


    private int backgroundColor = Pudding.DEFAULT_BACKGROUND_COLOR;
    private RenderMode renderMode = RenderMode.NORMAL;

    public PuddingRenderer() {
        path = new Path();
        path.setFillType(Path.FillType.WINDING);

        solidColorPaint = new Paint();
        solidColorPaint.setStyle(Paint.Style.FILL);
        solidColorPaint.setAntiAlias(true);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setAntiAlias(true);

        interpolatedNodes = new Vector2d[Pudding.MAX_NODE_NUM];
    }

    public void render(Canvas canvas, PuddingNode[] nodes, UndirectedWeightedGraph stressMap) {
        canvas.drawColor(backgroundColor);

        if (renderMode == RenderMode.NORMAL) {
            renderNormal(canvas, nodes);
        } else if (renderMode == RenderMode.WIREFRAME) {
            renderWireframe(canvas, nodes, stressMap);
        }
    }

    private void renderNormal(Canvas canvas, PuddingNode[] nodes) {
        // calculate position of the control points
        for (int i = 0; i < numNodes - 1; i++) {
            // put the control point between the 2 nodes
            interpolatedNodes[i].interpolate(nodes[i].pos, nodes[i + 1].pos, 0.5f);
        }
        interpolatedNodes[numNodes - 1].interpolate(nodes[numNodes - 1].pos, nodes[0].pos, 0.5f);

        // draw the border line with a path
        path.reset();
        // start at the first point
        path.moveTo((float) interpolatedNodes[0].x, (float) interpolatedNodes[0].y);
        for (int i = 1; i < numNodes; i++) {
            path.quadTo((float) nodes[i].pos.x, (float) nodes[i].pos.y, (float) interpolatedNodes[i].x,
                    (float) interpolatedNodes[i].y);
        }
        path.quadTo((float) nodes[0].pos.x, (float) nodes[0].pos.y, (float) interpolatedNodes[0].x,
                (float) interpolatedNodes[0].y);
        path.close();

        // draw the path to the canvas
        canvas.drawPath(path, solidColorPaint);
    }


    private void renderWireframe(Canvas canvas, PuddingNode[] nodes, UndirectedWeightedGraph stressMap) {
        // for each pair of nodes, draw a line between them
        for (int i = 0; i < numNodes - 1; i++) {
            for (int j = i + 1; j < numNodes; j++) {

                // use the stress between these 2 nodes to determine the color
                // to render their binding
                int strokeColorRed = Color.red(solidColorPaint.getColor()) + 50
                        * Math.abs((int) stressMap.getEdgeWeight(i, j));
                strokeColorRed = (strokeColorRed > 255) ? 255 : strokeColorRed;
                strokeColorRed = (strokeColorRed < 0) ? 0 : strokeColorRed;

                int strokeColorGreen = Color.green(solidColorPaint.getColor()) - 10
                        * Math.abs((int) stressMap.getEdgeWeight(i, j));
                strokeColorGreen = (strokeColorGreen > 255) ? 255 : strokeColorGreen;
                strokeColorGreen = (strokeColorGreen < 0) ? 0 : strokeColorGreen;

                int strokeColorBlue = Color.blue(solidColorPaint.getColor()) + 10
                        * Math.abs((int) stressMap.getEdgeWeight(i, j));
                strokeColorBlue = (strokeColorGreen > 255) ? 255 : strokeColorBlue;
                strokeColorBlue = (strokeColorGreen < 0) ? 0 : strokeColorBlue;

                int strokeColor = Color.rgb(strokeColorRed, strokeColorGreen, strokeColorBlue);
                strokePaint.setColor(strokeColor);
                canvas.drawLine((float) nodes[i].pos.x, (float) nodes[i].pos.y, (float) nodes[j].pos.x,
                        (float) nodes[j].pos.y, strokePaint);
            }
        }

    }


    public final void setColor(int color) {
        solidColorPaint.setColor(color);
    }

    public int getColor() {
        return solidColorPaint.getColor();
    }

    public void setRenderMode(RenderMode mode) {
        renderMode = mode;
    }

    public RenderMode getRenderMode() {
        return renderMode;
    }


    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
        for (int i = 0; i < numNodes; i++) {
            interpolatedNodes[i] = new Vector2d();
        }
    }

}

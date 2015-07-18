package com.greenpudding.model.dragging;

import com.greenpudding.model.PuddingNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

public class DraggingManager {

    // nodes withing this radius will be dragged by pointer
    private  static double dragRadius = 300;


    // map from pointerId to the pointers
    private Map<Integer, Pointer> pointersMap = new LinkedHashMap<>();


    /**
     * Check the node list and select the nodes to be dragged
     *
     * @param pointerId   the pointer that drags nodes
     * @param pointerPosX starting pointer pos
     * @param pointerPosY starting pointer pos
     * @param nodes       the full list of nodes
     */
    public void startDragging(int pointerId, double pointerPosX, double pointerPosY, List<PuddingNode> nodes) {
        Point2d pointerStartPos = new Point2d(pointerPosX, pointerPosY);
        Pointer pointer = new Pointer(pointerStartPos);
        // find the nodes close to the pointer and drag them
        for (int i = 0; i < nodes.size(); i++) {
            double distance = nodes.get(i).pos.distance(pointerStartPos);
            if (distance <= dragRadius) {
                pointer.addDraggedNode(i, nodes.get(i));
            }
        }
        pointersMap.put(pointerId, pointer);
    }

    public void stopDragging(int pointerId) {
        pointersMap.remove(pointerId);
    }

    /**
     * Update the nodes' acceleration due to the drag force. need to set pointer current position
     * before call this
     *
     * @param nodes
     */
    public void drag(List<PuddingNode> nodes) {
        for (Pointer pointer : pointersMap.values()) {
            pointer.drag(nodes);
        }
    }


    public void setPointerCurrentPos(int pointerId, double x, double y) {
        pointersMap.get(pointerId).setPointerCurrentPos(new Point2d(x, y));
    }

    public static double getDragRadius() {
        return dragRadius;
    }

    public static void setDragRadius(double d) {
        dragRadius = d;
    }
}

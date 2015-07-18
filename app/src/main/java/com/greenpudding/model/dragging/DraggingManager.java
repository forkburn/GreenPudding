package com.greenpudding.model.dragging;

import com.greenpudding.model.PuddingNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

public class DraggingManager {

    // nodes withing this radius will be dragged by pointer
    public static double DRAG_RADIUS = 100;


    // map from pointerId to the pointers
    private Map<Integer, Pointer>pointersMap = new HashMap<>();



    public void startDragging(int pointerId, double pointerPosX, double pointerPosY, List<PuddingNode> nodes) {
        Point2d pointerPos = new Point2d();
        pointerPos.set(pointerPosX, pointerPosY);
        Pointer pointer = new Pointer(pointerPos);
        // find the nodes close to the pointer and drag them
        for(int i=0; i<nodes.size();i++) {
            double distance = nodes.get(i).pos.distance(pointerPos);
            if (distance <= DRAG_RADIUS) {
                pointer.addDraggedNode(i,nodes.get(i));
            }
        }
        pointersMap.put(pointerId,pointer);
    }


}

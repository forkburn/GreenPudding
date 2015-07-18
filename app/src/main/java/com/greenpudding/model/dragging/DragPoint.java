package com.greenpudding.model.dragging;

import com.greenpudding.model.PuddingNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * Created by forkburn on 2015-07-18.
 */
public class DragPoint {

    private Point2d pointerPos;

    // ids of the nodes being dragged by this pointer
    private List<Integer> draggedNodeIds = new ArrayList<>();

    // relative position of the nodes
    private Map<Integer, Vector2d> nodeIdToDisplacementMap = new HashMap<>();


    public DragPoint(Point2d pointerPos) {
        this.pointerPos = pointerPos;
    }

    public void addDraggedNode(Integer nodeId, PuddingNode node) {
        draggedNodeIds.add(nodeId);
        Vector2d displacement = new Vector2d();
        displacement.sub(node.pos, pointerPos);
        nodeIdToDisplacementMap.put(nodeId, displacement);
    }
}

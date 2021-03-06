package com.greenpudding.model.dragging;

import com.greenpudding.model.PuddingModel;
import com.greenpudding.model.PuddingNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class Pointer {

    // postion of the pointer when dragging started
    private Point2d pointerStartPos;

    private Point2d pointerCurrentPos = new Point2d();

    // ids of the nodes being dragged by this pointer
    private List<Integer> draggedNodeIds = new ArrayList<>();

    // map from nodeId to position of the dragged nodes when dragging started
    private Map<Integer, Point2d> nodesStartPosMap = new HashMap<>();


    public Pointer(Point2d pointerStartPos) {
        this.pointerStartPos = pointerStartPos;
    }

    public void addDraggedNode(Integer nodeId, PuddingNode node) {
        draggedNodeIds.add(nodeId);

        // store the nodes' starting position
        nodesStartPosMap.put(nodeId, new Point2d(node.pos));
    }

    /**
     * update the acceleration of the nodes, according to the dragging force from pointer
     *
     * @param nodes
     */
    public void drag(List<PuddingNode> nodes) {
        // pointer displacement represents how the pointer moved since touch event
        Vector2d pointerDisplacement = new Vector2d();
        pointerDisplacement.sub(pointerCurrentPos, pointerStartPos);

        // for each node being dragged by this pointer
        for (int nodeId : draggedNodeIds) {
            // nodeTargetPos is the node's original position + mouse drag displacement vector
            Point2d nodeTargetPos = new Point2d();
            nodeTargetPos.add(nodesStartPosMap.get(nodeId), pointerDisplacement);

            PuddingNode node = nodes.get(nodeId);
            Vector2d acceleration = getDraggingAcceleration(node.pos, nodeTargetPos);
            // the further the node is from pointer, the less force it gets
            double nodeDistanceToPointer = pointerCurrentPos.distance(nodeTargetPos);
            acceleration.scale(1 - nodeDistanceToPointer / DraggingManager.getDragRadius());
            node.accel.add(acceleration);
        }
    }

    /**
     * @param draggedPos
     * @param draggingPos
     * @return the acceleration due to dragging force from one point to another
     */
    private Vector2d getDraggingAcceleration(Point2d draggedPos, Point2d draggingPos) {
        // calc the displacement between the points
        Vector2d displacement = new Vector2d();
        displacement.sub(draggingPos, draggedPos);
        Vector2d acceleration = new Vector2d(displacement);
        // accel = force/mass = scale*displacement/mass
        acceleration.scale(PuddingModel.DRAGGING_FORCE_SCALE / PuddingModel.NODE_MASS);
        return acceleration;
    }

    public Point2d getPointerCurrentPos() {
        return pointerCurrentPos;
    }

    public void setPointerCurrentPos(Point2d pointerCurrentPos) {
        this.pointerCurrentPos = pointerCurrentPos;
    }
}

package com.greenpudding.model;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.greenpudding.util.UndirectedWeightedGraph;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class Pudding {
    public static final int DEFAULT_FILL_COLOR = 0xFF9FD867;
    public static final int DEFAULT_BACKGROUND_COLOR = 0xFFFFFFFF;
    public static final double DEFAULT_BINDING_ELASTICITY = 10f;
    public static final double DEFAULT_PINNING_ELASTICITY = 0.1f;
    public static final double DEFAULT_DAMPING_FACTOR = 0.8f;
    public static final int DEFAULT_RADIUS = 100;
    public static final int DEFAULT_NUM_NODES = 12;

    // if distance between 2 nodes is smaller than this, no force will result
    // between the nodes. This prevents float point error when we have a very
    // small denominator
    private static final double DISTANCE_THRESHOLD = 0.01f;
    // mass of each node. used for calculating acceleration
    private static final double NODE_MASS = 1;
    // strength of the force dragging the node by mouse
    private static final double DRAGGING_FORCE_SCALE = 0.5;
    // the nodes representing the mass points.
    private List<PuddingNode> nodes = new LinkedList<>();
    // a 2D array storing the distance between each pair of nodes
    private UndirectedWeightedGraph distanceMap;
    // a 2D array storing the stress on the binding of each pair of nodes
    private UndirectedWeightedGraph stressMap;
    // Physical properties
    // radius, in pixels
    private int radius = DEFAULT_RADIUS;
    // the gravity on the nodes
    private Boolean isGravityEnabled = true;
    private Vector2d gravity = new Vector2d(0, 0);
    // whether nodes are pinned to their spawn position by a force
    private Boolean isPinned = false;
    // the strength of the force pinning a node to its original position
    private double pinningElasticity = DEFAULT_PINNING_ELASTICITY;
    // the strength in the bond between 2 nodes
    private double bindingElasticity = DEFAULT_BINDING_ELASTICITY;
    // the damping factor on the velocity of each node
    private double dampingFactor = DEFAULT_DAMPING_FACTOR;

    // Attributes to facilitate rendering
    private PuddingRenderer renderer;
    // the upper/lower limit of the position to where a node can move to
    private Rect boundingRect;
    // A mapping from pointer ID to their position
    private Map<Integer, Point2d> pointerIdToPosMap = new HashMap<Integer, Point2d>();
    // A mapping from nodeId to pointerId that's dragging the
    // node. If a nodeId maps to null, it means no
    // pointer is dragging the node
    private Map<Integer, Integer> nodeIdToPointerIdMap = new LinkedHashMap<Integer, Integer>();


    public Pudding() {
        renderer = new PuddingRenderer();
        distanceMap = new UndirectedWeightedGraph();
        stressMap = new UndirectedWeightedGraph();
        boundingRect = new Rect();
        setColor(DEFAULT_FILL_COLOR);
        setNumOfNodes(DEFAULT_NUM_NODES);
    }

    public final void setNumOfNodes(int numOfNodes) {
        renderer.setNumNodes(numOfNodes);
        // generate the nodes
        for (int i = 0; i < numOfNodes; i++) {
            nodes.add(new PuddingNode());
            // initially, no node is dragged by mouse
            nodeIdToPointerIdMap.put(i, null);
        }
    }

    public int getNumNodes() {
        return nodes.size();
    }

    /**
     * Regenerate the nodes and recalculate distances between nodes
     */
    public void refreshNodes() {
        // position the nodes
        positionNodesAround((boundingRect.right - boundingRect.left) / 2, (boundingRect.bottom - boundingRect.top) / 2);

        // calculate the distance between node pairs
        updateDistanceMap();
    }

    private void updateDistanceMap() {
        // calculate the distance between pairs of nodes, store them in
        // distanceMap
        for (int i = 0; i < nodes.size() - 1; i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                double distance = nodes.get(i).pos.distance(nodes.get(j).pos);
                distanceMap.setEdgeWeight(i, j, distance);
            }
        }
    }

    /**
     * Put the nodes on a circle around the given point
     */
    private void positionNodesAround(int xPos, int yPos) {
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).pos.x = xPos;
            nodes.get(i).pos.y = yPos;

            // position the nodes along a circle
            nodes.get(i).pos.x += getRadius() * Math.cos(i * 2 * Math.PI / nodes.size());
            nodes.get(i).pos.y += getRadius() * Math.sin(i * 2 * Math.PI / nodes.size());

            // remember the current position as their pinned position
            nodes.get(i).pinnedPos.set(nodes.get(i).pos);
        }
    }

    public int getColor() {
        return renderer.getColor();
    }

    public final void setColor(int color) {
        renderer.setColor(color);
    }

    /**
     * Update the physical status of the pudding
     */
    public void updatePhysics() {
        updateAcceleration();
        updateVelocity();
        updatePosition();
    }

    /**
     * calculate the force imposed on each node, and their acceleration
     * accordingly using Hooke's law. Should be called on each frame.
     */
    private void updateAcceleration() {
        // for each node, calculate the acceleration due to gravity and pinning and dragging
        for (int i = 0; i < nodes.size(); i++) {
            resetAccelerationForNode(i);
            updateAccelerationForNode(i);
        }

        // for each pair of nodes, calculate the acceleration due to the force
        // between nodes
        for (int i = 0; i < nodes.size() - 1; i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                updateBindingForceAccelerationForNodes(i, j);
            }
        }

    }

    private void resetAccelerationForNode(int nodeId) {
        nodes.get(nodeId).accel.set(0, 0);
    }

    private void updateAccelerationForNode(int nodeId) {
        // add the gravity
        if (isGravityEnabled) {
            nodes.get(nodeId).accel.add(gravity);
        }

        if (isPinned) {
            updatePinningForceAccelerationForNode(nodeId);
        }

        updateDraggingForceAccelerationForNode(nodeId);
    }

    private void updatePinningForceAccelerationForNode(int nodeId) {
        // how much has the node deviated from where it's pinned
        Vector2d displacement = new Vector2d(0, 0);
        displacement.sub(nodes.get(nodeId).pos, nodes.get(nodeId).pinnedPos);

        // the force due to the displacement
        double force = -pinningElasticity * displacement.length();

        // the acceleration due to the force
        Vector2d acceleration = new Vector2d(displacement);
        double norm = acceleration.length();

        // use a threshold to prevent weird floating error problem
        if (norm > DISTANCE_THRESHOLD) {
            acceleration.scale(force / norm / NODE_MASS);
            nodes.get(nodeId).accel.add(acceleration);
        }
    }

    private void updateDraggingForceAccelerationForNode(int nodeId) {
        // if the node's being dragged, add the acceleration due to force
        // from being dragged by mouse
        Integer pointerId = nodeIdToPointerIdMap.get(nodeId);
        if (pointerId != null) {
            Point2d pointerPos = pointerIdToPosMap.get(pointerId);
            if (pointerPos != null) {
                Vector2d acceleration = getDraggingAcceleration(nodes.get(nodeId).pos, pointerPos);
                nodes.get(nodeId).accel.add(acceleration);
            }
        }
    }

    private Vector2d getDraggingAcceleration(Point2d draggedPos, Point2d draggingPos) {
        // calc the displacement between the pointer and the node
        Vector2d displacement = new Vector2d(0, 0);
        displacement.sub(draggingPos, draggedPos);
        Vector2d acceleration = new Vector2d(displacement);
        // accel = force/mass = scale*displacement/mass
        acceleration.scale(DRAGGING_FORCE_SCALE / NODE_MASS);
        return acceleration;
    }

    private void updateBindingForceAccelerationForNodes(int nodeId1, int nodeId2) {
        // the current distance between the 2 nodes is
        Vector2d distanceNow = new Vector2d(0, 0);
        distanceNow.sub(nodes.get(nodeId1).pos, nodes.get(nodeId2).pos);

        // how much has the binding between the 2 nodes been stretched
        double stretched = distanceNow.length() - distanceMap.getEdgeWeight(nodeId1, nodeId2);

        // Hooke's law! Note that k = elasticity / length of spring
        double force = bindingElasticity / distanceMap.getEdgeWeight(nodeId1, nodeId2) * stretched;

        // save the force in the stressMap
        stressMap.setEdgeWeight(nodeId1, nodeId2, force);

        // the acceleration is the force / mass
        Vector2d acceleration = distanceNow;
        double norm = acceleration.length();

        // do not allow the divisor to be too small, to prevent floating
        // error
        if (norm < DISTANCE_THRESHOLD) {
            norm = DISTANCE_THRESHOLD;
        }

        acceleration.scale(force / NODE_MASS / norm);

        // apply the force to the 2 nodes and their acceleration is
        // affected
        nodes.get(nodeId2).accel.add(acceleration);
        acceleration.negate();
        nodes.get(nodeId1).accel.add(acceleration);
    }

    /**
     * According to its acceleration, calculate each node's new velocity Should
     * be called on each frame
     */
    private void updateVelocity() {
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).veloc.add(nodes.get(i).accel);
            // apply the damping factor
            nodes.get(i).veloc.scale(dampingFactor);
        }
    }

    /**
     * According to its velocity, update each node's position
     */
    private void updatePosition() {
        for (int i = 0; i < nodes.size(); i++) {

            nodes.get(i).pos.add(nodes.get(i).veloc);

            // if the node is moving out of the valid area
            if (nodes.get(i).pos.x > getBoundingRect().right) {
                nodes.get(i).pos.x = getBoundingRect().right;
                nodes.get(i).veloc.x = 0;
            } else if (nodes.get(i).pos.x < getBoundingRect().left) {
                nodes.get(i).pos.x = getBoundingRect().left;
                nodes.get(i).veloc.x = 0;
            }

            if (nodes.get(i).pos.y > getBoundingRect().bottom) {
                nodes.get(i).pos.y = getBoundingRect().bottom;
                nodes.get(i).veloc.y = 0;
            } else if (nodes.get(i).pos.y < getBoundingRect().top) {
                nodes.get(i).pos.y = getBoundingRect().top;
                nodes.get(i).veloc.y = 0;
            }
        }

    }

    /**
     * Takes a canvas, and draw on to it. Should be called on each frame, after
     * all calculations are done
     *
     * @param canvas
     */
    public void render(Canvas canvas) {
        renderer.render(canvas, nodes, stressMap);
    }


    /**
     * Set the size of the pudding.
     *
     * @param radius
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public void setGravity(double x, double y) {
        this.gravity.set(x, y);
    }

    public Vector2d getGravity() {
        return gravity;
    }

    public void setGravity(Vector2d gravity) {
        this.gravity = gravity;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public double getPinningElasticity() {
        return pinningElasticity;
    }

    public void setPinningElasticity(double pinningElasticity) {
        this.pinningElasticity = pinningElasticity;
    }

    public double getBindingElasticity() {
        return bindingElasticity;
    }

    public void setBindingElasticity(double elasticity) {
        this.bindingElasticity = elasticity;
    }

    public double getDampingFactor() {
        return dampingFactor;
    }

    public void setDampingFactor(double damping) {
        this.dampingFactor = damping;
    }

    public Rect getBoundingRect() {
        return boundingRect;
    }

    public void setBoundingRect(Rect boundingRect) {
        this.boundingRect = boundingRect;
        // reposition the nodes
        positionNodesAround((boundingRect.right - boundingRect.left) / 2, (boundingRect.bottom - boundingRect.top) / 2);
    }

    /**
     * Save the pointer position of the specified pointer
     *
     * @param x
     * @param y
     * @param pointerId
     */
    public void setMousePos(double x, double y, int pointerId) {
        if (pointerIdToPosMap.containsKey(pointerId)) {
            pointerIdToPosMap.get(pointerId).set(x, y);
        } else {
            pointerIdToPosMap.put(pointerId, new Point2d(x, y));
        }
    }

    /**
     * Tells the pudding that a mouse dragging is starting
     *
     * @param x Mouse position where the dragging starts
     * @param y Mouse position where the dragging starts
     */
    public void startDragging(double x, double y, int pointerId) {
        setMousePos(x, y, pointerId);

        // find the node closest to the mousePos
        double minDistanceToMouse = Float.MAX_VALUE;
        double distanceToMouse;
        int nearestNodeId = 0;

        for (int i = 0; i < nodes.size(); i++) {
            distanceToMouse = nodes.get(i).pos.distance(pointerIdToPosMap.get(pointerId));
            if (distanceToMouse < minDistanceToMouse) {
                minDistanceToMouse = distanceToMouse;
                nearestNodeId = i;
            }
        }

        // set that node 's dragging pointer
        nodeIdToPointerIdMap.put(nearestNodeId, pointerId);
    }

    public void stopDragging(int pointerId) {
        // find the node currently being dragged by that pointer
        for (Map.Entry<Integer, Integer> entry : nodeIdToPointerIdMap.entrySet()) {
            Integer nodePointerId = entry.getValue();
            if (nodePointerId != null && nodePointerId == pointerId) {
                entry.setValue(null);
            }
        }
    }

    public Boolean getIsGravityEnabled() {
        return isGravityEnabled;
    }

    public void setIsGravityEnabled(Boolean isGravityEnabled) {
        this.isGravityEnabled = isGravityEnabled;
    }

    public RenderMode getRenderMode() {
        return renderer.getRenderMode();
    }

    public void setRenderMode(RenderMode mode) {
        renderer.setRenderMode(mode);
    }

    public int getBackgroundColor() {
        return renderer.getBackgroundColor();
    }

    public void setBackgroundColor(int backgroundColor) {
        renderer.setBackgroundColor(backgroundColor);
    }


}

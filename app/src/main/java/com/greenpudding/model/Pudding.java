package com.greenpudding.model;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public static final int MAX_NODE_NUM = 100;
    public static final int MIN_NODE_NUM = 3;

    // if distance between 2 nodes is smaller than this, no force will result
    // between the nodes. This prevents float point error when we have a very
    // small denominator
    private static final double DISTANCE_THRESHOLD = 0.01f;
    // mass of each node. used for calculating acceleration
    private static final double NODE_MASS = 1;
    // strength of the force dragging the node by mouse
    private static final double DRAGGING_FORCE_SCALE = 0.5;
    // number of nodes
    private int numNodes;
    // the nodes representing the mass points
    private PuddingNode[] nodes;
    // a 2D array storing the distance between each pair of nodes
    private NodePairMap distanceMap;
    // a 2D array storing the stress on the binding of each pair of nodes
    private NodePairMap stressMap;
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
    // fields to facilitate multi-touch dragging of nodes
    // A mapping between pointer ID and their position
    private Map<Integer, Point2d> pointerPosMap = new HashMap<Integer, Point2d>();
    // A mapping between id of the node and the pointer that's dragging the
    // node. If a node ID 's corresponding pointer ID is null, it means no
    // pointer is dragging the node
    private Map<Integer, Integer> nodePointerMap = new LinkedHashMap<Integer, Integer>();


    public Pudding() {
        renderer = new PuddingRenderer();
        nodes = new PuddingNode[MAX_NODE_NUM];
        distanceMap = new NodePairMap(MAX_NODE_NUM);
        stressMap = new NodePairMap(MAX_NODE_NUM);
        boundingRect = new Rect();
        setColor(DEFAULT_FILL_COLOR);
        setNumOfNodes(DEFAULT_NUM_NODES);
    }

    public final void setNumOfNodes(int numOfNodes) {
        if (numOfNodes >= MIN_NODE_NUM && numOfNodes <= MAX_NODE_NUM) {
            numNodes = numOfNodes;
            renderer.setNumNodes(numOfNodes);
            // generate the nodes
            for (int i = 0; i < numOfNodes; i++) {
                nodes[i] = new PuddingNode();
                // initially, no node is dragged by mouse
                nodePointerMap.put(i, null);
            }
        }
    }

    public int getNumNodes() {
        return numNodes;
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
        for (int i = 0; i < numNodes - 1; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                double distance = nodes[i].pos.distance(nodes[j].pos);
                distanceMap.setEdgeValue(i, j, distance);
            }
        }
    }

    /**
     * Put the nodes on a circle around the given point
     */
    private void positionNodesAround(int xPos, int yPos) {
        for (int i = 0; i < numNodes; i++) {
            nodes[i].pos.x = xPos;
            nodes[i].pos.y = yPos;

            // position the nodes along a circle
            nodes[i].pos.x += getRadius() * Math.cos(i * 2 * Math.PI / numNodes);
            nodes[i].pos.y += getRadius() * Math.sin(i * 2 * Math.PI / numNodes);

            // remember the current position as their pinned position
            nodes[i].pinnedPos.set(nodes[i].pos);
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
    public void calcFrame() {
        calcAcceleration();
        calcVelocity();
        calcPosition();
    }

    /**
     * calculate the force imposed on each node, and their acceleration
     * accordingly using Hooke's law. Should be called on each frame.
     */
    private void calcAcceleration() {
        // for each node, calculate the acceleration due to gravity and pinning
        for (int i = 0; i < numNodes; i++) {
            calcAccelerationForNode(i);
        }

        // for each pair of nodes, calculate the acceleration due to the force
        // between nodes
        for (int i = 0; i < numNodes - 1; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                calcBindingForceAccelerationForNodes(i, j);
            }
        }

    }

    private void calcAccelerationForNode(int nodeId) {
        // clear the acceleration
        nodes[nodeId].accel.set(0, 0);

        // add the gravity
        if (isGravityEnabled) {
            nodes[nodeId].accel.add(gravity);
        }

        // calculate the acceleration due to pinning
        if (isPinned) {
            calcPinningForceAccelerationForNode(nodeId);
        }

        calcDraggingForceAccelerationForNode(nodeId);
    }

    private void calcPinningForceAccelerationForNode(int nodeId) {
        // how much has the node deviated from where it's pinned
        Vector2d displacement = new Vector2d(0, 0);
        displacement.sub(nodes[nodeId].pos, nodes[nodeId].pinnedPos);

        // the force due to the displacement
        double force = -pinningElasticity * displacement.length();

        // the acceleration due to the force
        Vector2d acceleration = new Vector2d(displacement);
        double norm = acceleration.length();

        // use a threshold to prevent weird floating error problem
        if (norm > DISTANCE_THRESHOLD) {
            acceleration.scale(force / norm / NODE_MASS);
            nodes[nodeId].accel.add(acceleration);
        }
    }

    private void calcDraggingForceAccelerationForNode(int nodeId) {
        // if the node's being dragged, add the acceleration due to force
        // from being dragged by mouse
        Integer pointerId = nodePointerMap.get(nodeId);
        if (pointerId != null) {
            Point2d pointerPos = pointerPosMap.get(pointerId);
            if (pointerPos != null) {
                // calc the displacement between the pointer and the node
                Vector2d displacement = new Vector2d(0, 0);
                displacement.sub(pointerPos, nodes[nodeId].pos);

                // the displacement * dragging force factor / mass = force /
                // mass = acceleration
                Vector2d acceleration = new Vector2d(displacement);

                acceleration.scale(DRAGGING_FORCE_SCALE / NODE_MASS);

                nodes[nodeId].accel.add(acceleration);
            }
        }
    }

    private void calcBindingForceAccelerationForNodes(int nodeId1, int nodeId2) {
        // the current distance between the 2 nodes is
        Vector2d distanceNow = new Vector2d(0, 0);
        distanceNow.sub(nodes[nodeId1].pos, nodes[nodeId2].pos);

        // how much has the binding between the 2 nodes been stretched
        double stretched = distanceNow.length() - distanceMap.getEdgeValue(nodeId1, nodeId2);

        // Hooke's law! Note that k = elasticity / length of spring
        double force = bindingElasticity / distanceMap.getEdgeValue(nodeId1, nodeId2) * stretched;

        // save the force in the stressMap
        stressMap.setEdgeValue(nodeId1, nodeId2, force);

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
        nodes[nodeId2].accel.add(acceleration);
        acceleration.negate();
        nodes[nodeId1].accel.add(acceleration);
    }

    /**
     * According to its acceleration, calculate each node's new velocity Should
     * be called on each frame
     */
    private void calcVelocity() {
        for (int i = 0; i < numNodes; i++) {
            nodes[i].veloc.add(nodes[i].accel);
            // apply the damping factor
            nodes[i].veloc.scale(dampingFactor);
        }
    }

    /**
     * According to its velocity, update each node's position
     */
    private void calcPosition() {
        for (int i = 0; i < numNodes; i++) {

            nodes[i].pos.add(nodes[i].veloc);

            // if the node is moving out of the valid area
            if (nodes[i].pos.x > getBoundingRect().right) {
                nodes[i].pos.x = getBoundingRect().right;
                nodes[i].veloc.x = 0;
            } else if (nodes[i].pos.x < getBoundingRect().left) {
                nodes[i].pos.x = getBoundingRect().left;
                nodes[i].veloc.x = 0;
            }

            if (nodes[i].pos.y > getBoundingRect().bottom) {
                nodes[i].pos.y = getBoundingRect().bottom;
                nodes[i].veloc.y = 0;
            } else if (nodes[i].pos.y < getBoundingRect().top) {
                nodes[i].pos.y = getBoundingRect().top;
                nodes[i].veloc.y = 0;
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
     * @param refreshNodes : whether to refresh the position of all nodes
     */
    public void setRadius(int radius, boolean refreshNodes) {
        this.radius = radius;
        if (refreshNodes) {
            refreshNodes();
        }
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
        if (pointerPosMap.containsKey(pointerId)) {
            pointerPosMap.get(pointerId).set(x, y);
        } else {
            pointerPosMap.put(pointerId, new Point2d(x, y));
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

        for (int i = 0; i < numNodes; i++) {
            distanceToMouse = nodes[i].pos.distance(pointerPosMap.get(pointerId));
            if (distanceToMouse < minDistanceToMouse) {
                minDistanceToMouse = distanceToMouse;
                nearestNodeId = i;
            }
        }

        // set that node 's dragging pointer
        nodePointerMap.put(nearestNodeId, pointerId);
    }

    public void stopDragging(int pointerId) {
        // find the node currently being dragged by that pointer
        for (Map.Entry<Integer, Integer> entry : nodePointerMap.entrySet()) {
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

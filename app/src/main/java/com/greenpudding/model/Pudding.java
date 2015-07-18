package com.greenpudding.model;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.greenpudding.model.dragging.DraggingManager;
import com.greenpudding.util.UndirectedWeightedGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Vector2d;

public class Pudding {
    public static final int DEFAULT_FILL_COLOR = 0xFF9FD867;
    public static final int DEFAULT_BACKGROUND_COLOR = 0xFFFFFFFF;
    public static final double DEFAULT_BINDING_ELASTICITY = 10f;
    public static final double DEFAULT_PINNING_ELASTICITY = 0.1f;
    public static final double DEFAULT_DAMPING_FACTOR = 0.8f;
    public static final int DEFAULT_RADIUS = 100;
    public static final int DEFAULT_NUM_NODES = 12;

    // minimal distance between nodes for the elasticity to work. prevents bug due to floating error
    public static final double NODE_DISTANCE_THRESHOLD = 0.01f;
    // mass of each node. used for calculating acceleration
    public static final double NODE_MASS = 1;
    // strength of the force dragging the node by mouse
    public static final double DRAGGING_FORCE_SCALE = 0.5;
    // the nodes representing the mass points.
    private List<PuddingNode> nodes = new LinkedList<>();
    // a 2D array storing the distance between each pair of nodes
    private UndirectedWeightedGraph distanceMap;
    // a 2D array storing the stress on the binding of each pair of nodes
    private UndirectedWeightedGraph forceMap;
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

    private DraggingManager draggingManager;

    public Pudding() {
        renderer = new PuddingRenderer();
        distanceMap = new UndirectedWeightedGraph();
        forceMap = new UndirectedWeightedGraph();
        boundingRect = new Rect();
        setColor(DEFAULT_FILL_COLOR);
        setNumOfNodes(DEFAULT_NUM_NODES);
        draggingManager = new DraggingManager();
    }

    public final void setNumOfNodes(int numOfNodes) {
        renderer.setNumNodes(numOfNodes);
        // regenerate the nodes
        nodes = new ArrayList<>();
        for (int i = 0; i < numOfNodes; i++) {
            nodes.add(new PuddingNode());
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

    /**
     * Put the nodes on a circle around the given point
     */
    private void positionNodesAround(int xPos, int yPos) {
        for (int i = 0; i < nodes.size(); i++) {
            PuddingNode node = nodes.get(i);
            // position the nodes along a circle
            node.pos.x = xPos + getRadius() * Math.cos(i * 2 * Math.PI / nodes.size());
            node.pos.y = yPos + getRadius() * Math.sin(i * 2 * Math.PI / nodes.size());

            // remember the current position as their pinned position
            node.pinnedPos.set(node.pos);
        }
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
        for (int i = 0; i < nodes.size(); i++) {
            // reset acceleration from the calculation in last frame
            resetAccelerationForNode(i);
            // for each node, calculate the acceleration due to gravity and pinning
            updateAccelerationForNode(i);
        }

        // update acceleration due to mouse dragging force
        draggingManager.drag(nodes);

        // for each pair of nodes, calculate the acceleration due to the force between them
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
        if (norm > NODE_DISTANCE_THRESHOLD) {
            acceleration.scale(force / norm / NODE_MASS);
            nodes.get(nodeId).accel.add(acceleration);
        }
    }


    private void updateBindingForceAccelerationForNodes(int nodeId1, int nodeId2) {
        // the current distance between the 2 nodes is
        Vector2d distanceBetween = new Vector2d();
        distanceBetween.sub(nodes.get(nodeId1).pos, nodes.get(nodeId2).pos);

        // how much has the binding between the 2 nodes been stretched
        double stretched = distanceBetween.length() - distanceMap.getEdgeWeight(nodeId1, nodeId2);

        // Hooke's law. force = k * x; k = elasticity / original length of spring
        double force = bindingElasticity / distanceMap.getEdgeWeight(nodeId1, nodeId2) * stretched;

        // save the force in the forceMap
        forceMap.setEdgeWeight(nodeId1, nodeId2, force);

        Vector2d acceleration = distanceBetween;
        double norm = acceleration.length();

        // make sure divisor is not too small. prevent floating error
        if (norm < NODE_DISTANCE_THRESHOLD) {
            norm = NODE_DISTANCE_THRESHOLD;
        }

        // the acceleration vector = force / mass * normalizedDistanceVector
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
        renderer.render(canvas, nodes, forceMap);
    }


    /**
     * Set the size of the pudding.
     *
     * @param radius
     */
    public void setRadius(int radius) {
        // for best effect, set dragging radius to the same as pudding size
        DraggingManager.setDragRadius(radius);
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
    public  void setMousePos(double x, double y, int pointerId) {
        draggingManager.setPointerCurrentPos(pointerId, x, y);
    }

    /**
     * Tells the pudding that a mouse dragging is starting
     *
     * @param x Mouse position where the dragging starts
     * @param y Mouse position where the dragging starts
     */
    public  void startDragging(double x, double y, int pointerId) {
        draggingManager.startDragging(pointerId, x, y, nodes);
    }

    public  void stopDragging(int pointerId) {
        draggingManager.stopDragging(pointerId);
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

package com.greenpudding.lib.model;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * defines the physical attributes of a node in the pudding
 * 
 */
class PuddingNode {

	// current position
	public Point2d pos;

	// pinned position
	public Point2d pinnedPos;

	// current velocity
	public Vector2d veloc;

	// current acceleration
	public Vector2d accel;

	public PuddingNode() {
		pos = new Point2d();
		pinnedPos = new Point2d();
		veloc = new Vector2d();
		accel = new Vector2d();
	}
}
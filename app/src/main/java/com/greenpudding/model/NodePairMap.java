package com.greenpudding.model;

/**
 * This class stores the pair-wise relation between each pair of nodes. A 2D
 * array is used. When nodes with ID x and y has a binding, the information of
 * that binding can be found at the array member [x][y] or [y][x] *
 * 
 * @author Forkburn
 * 
 */

public class NodePairMap {

	private double[][] distance;

	public NodePairMap(int maxNodeNumber) {

		distance = new double[maxNodeNumber][maxNodeNumber];
	}

	public double getEdgeValue(int id1, int id2) {
		return distance[id1][id2];
	}

	public void setEdgeValue(int id1, int id2, double value) {
		distance[id1][id2] = value;
		distance[id2][id1] = value;
	}

}

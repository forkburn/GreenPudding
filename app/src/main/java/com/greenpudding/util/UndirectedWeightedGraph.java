package com.greenpudding.util;


public class UndirectedWeightedGraph {

	private double[][] edgeWeight;

	public UndirectedWeightedGraph(int maxVertices) {

		edgeWeight = new double[maxVertices][maxVertices];
	}

	public double getEdgeWeight(int id1, int id2) {
		return edgeWeight[id1][id2];
	}

	public void setEdgeWeight(int id1, int id2, double value) {
		edgeWeight[id1][id2] = value;
		edgeWeight[id2][id1] = value;
	}

}

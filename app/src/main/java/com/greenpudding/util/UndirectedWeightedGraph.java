package com.greenpudding.util;


import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class UndirectedWeightedGraph {

    private Map<Pair<Integer, Integer>, Double> edgeWeight = new HashMap<>();


    public void setEdgeWeight(int id1, int id2, double value) {
        if (id1 > id2) {
            edgeWeight.put(new Pair(id1, id2), value);
        } else {
            edgeWeight.put(new Pair(id2, id1), value);

        }
    }

    public double getEdgeWeight(int id1, int id2) {
        if (id1 > id2) {
            return edgeWeight.get(new Pair(id1, id2));
        } else {
            return edgeWeight.get(new Pair(id2, id1));

        }
    }

}

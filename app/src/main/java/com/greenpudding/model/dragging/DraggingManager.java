package com.greenpudding.model.dragging;

import java.util.Map;

public class DraggingManager {

    // nodes withing this radius will be dragged by pointer
    public static int DRAG_RADIUS = 100;

    // map from pointerId to the list of pointers
    private Map<Integer, DragPoint>pointersMap;


}

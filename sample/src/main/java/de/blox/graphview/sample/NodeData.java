package de.blox.graphview.sample;

import de.blox.graphview.Node;

public class NodeData {
    public static final int TYPE_AND = 1;
    public static final int TYPE_OR = 2;
    public static final int TYPE_TERM = 3;

    private double val;
    private String text;
    private int type;

    NodeData(String text, int type){
        this.text = text;
        this.type = type;
    }

    NodeData(String text, double val, int type){
        this.val = val;
        this.text = text;
        this.type = type;
    }

    public String getText(){
        return text;
    }

    public int getType() {
        return type;
    }

    public double getVal() {
        return val;
    }
}

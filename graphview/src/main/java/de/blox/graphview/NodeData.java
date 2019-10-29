package de.blox.graphview;

public class NodeData {
    public static final int TYPE_AND = 1;
    public static final int TYPE_OR = 2;
    public static final int TYPE_TERM = 3;

    private double val;
    private String text;

    public String getVar() {
        return var;
    }

    private String var;
    private int type;

    public NodeData(String text, int type){
        this.text = text;
        this.type = type;
        this.var = "no Value";
    }

    public NodeData(String text, double val, int type, String var){
        this.val = val;
        this.text = text;
        this.type = type;
        this.var = var;
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

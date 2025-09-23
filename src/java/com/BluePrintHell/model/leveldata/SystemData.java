package com.BluePrintHell.model.leveldata;

import java.util.ArrayList;
import java.util.List;

public class SystemData {
    private String id;
    private String type; // e.g., "REFERENCE", "NORMAL"
    private double x;
    private double y;
    private List<PortData> inputPorts = new ArrayList<>();
    private List<PortData> outputPorts = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public List<PortData> getInputPorts() { return inputPorts; }
    public void setInputPorts(List<PortData> inputPorts) { this.inputPorts = inputPorts; }
    public List<PortData> getOutputPorts() { return outputPorts; }
    public void setOutputPorts(List<PortData> outputPorts) { this.outputPorts = outputPorts; }
}
package com.BluePrintHell.model.network;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.Port;
import com.fasterxml.jackson.annotation.*;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NormalSystem.class, name = "NormalSystem"),
        @JsonSubTypes.Type(value = ReferenceSystem.class, name = "ReferenceSystem")
})

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public abstract class NetworkSystem {
    protected final String id;
    protected Point2D position;
    protected List<Port> inputPorts = new ArrayList<>();
    protected List<Port> outputPorts = new ArrayList<>();

    protected static final int BUFFER_CAPACITY = 5;
    protected Queue<Packet> packetBuffer = new LinkedList<>();

    protected GameState parentGameState;
    private final List<Packet> justArrivedPackets = new ArrayList<>();

    public NetworkSystem(String id, Point2D position) {
        this.id = id;
        this.position = position;
    }

    @JsonIgnore
    public boolean isBufferEmpty() {
        return packetBuffer.isEmpty();
    }

    public void receivePacket(Packet packet) {
        if (packetBuffer.size() < BUFFER_CAPACITY) {
            packetBuffer.add(packet);
            justArrivedPackets.add(packet);

            if (this instanceof NormalSystem || (this instanceof ReferenceSystem && !this.getInputPorts().isEmpty())) {
                this.getParentGameState().addCoins(packet.getCoinValue());
            }
        } else {
            System.out.println("DEBUG: Packet " + packet.hashCode() + " is lost. Reason: Buffer is full for system " + this.id);
            this.getParentGameState().losePacket(packet); // از متد مرکزی برای از دست دادن پکت استفاده می‌کنیم
        }
    }

    public abstract void update(double deltaTime);

    public void setParentGameState(GameState parentGameState) {
        this.parentGameState = parentGameState;
    }

    @JsonIgnore
    public GameState getParentGameState() {
        return parentGameState;
    }

    public void clearJustArrived() {
        justArrivedPackets.clear();
    }

    protected boolean wasJustArrived(Packet packet) {
        return justArrivedPackets.contains(packet);
    }

    public String getId() { return id; }
    public Point2D getPosition() { return position; }
    public void setPosition(Point2D position) { this.position = position; }
    public List<Port> getInputPorts() { return inputPorts; }
    public List<Port> getOutputPorts() { return outputPorts; }

    @JsonIgnore
    public boolean isFullyConnected() {for (Port port : inputPorts) {
            if (!port.isConnected()) {
                return false;
            }
        }
        // تمام پورت‌های خروجی را چک کن
        for (Port port : outputPorts) {
            if (!port.isConnected()) {
                return false;
            }
        }
        // اگر همه‌ی پورت‌ها متصل بودند
        return true;
    }
}
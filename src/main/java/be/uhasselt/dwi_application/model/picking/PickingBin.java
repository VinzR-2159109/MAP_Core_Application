package be.uhasselt.dwi_application.model.picking;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class PickingBin {
    private Long id;
    private double pos_x;
    private double pos_y;
    private Long partId;

    public PickingBin() {}

    @JdbiConstructor
    public PickingBin(Long id, double x, double y, Long partId) {
        this.id = id;
        this.pos_x = x;
        this.pos_y = y;
        this.partId = partId;
    }

    public PickingBin(double x, double y, Long id) {
        this.pos_x = x;
        this.pos_y = y;
        this.id = id;
    }

    public Long getPartId() {
        return partId;
    }

    public void setPartId(Long partId) {
        this.partId = partId;
    }

    public void setPart(Part part) {
        this.partId = part.getId();
    }

    public void removePart() {
        partId = null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public double getPos_x() {
        return pos_x;
    }

    public double getPos_y() {
        return pos_y;
    }

    public void setPos_x(double pos_x) {
        this.pos_x = pos_x;
    }

    public void setPos_y(double pos_y) {
        this.pos_y = pos_y;
    }

    public void setPosition(double x, double y) {
        this.pos_x = x;
        this.pos_y = y;
    }

    @Override
    public String toString(){
        return String.valueOf(id);
    }
}

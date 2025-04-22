package be.uhasselt.dwi_application.model.workInstruction.picking;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class PickingBin {
    private Long id;

    public PickingBin() {}

    @JdbiConstructor
    public PickingBin(Long id, double x, double y, Long partId) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString(){
        return String.valueOf(id);
    }
}

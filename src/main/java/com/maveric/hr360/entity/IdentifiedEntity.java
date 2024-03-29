package com.maveric.hr360.entity;


import org.springframework.data.annotation.Id;

public abstract class IdentifiedEntity {
    @Id
    protected Long id;

    public void setId(Long id) {
        if (this.id != null) {
            throw new UnsupportedOperationException("ID is already defined");
        }
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}

package com.stockflow.core.entity;

public interface EntityDto<E, D> {
    D fromEntity(E entity);
    D fromEntity(D template, E entity);
    E toEntity();
}
package com.stockflow.core.utils.common;

import com.stockflow.core.entity.EntityDto;

import java.util.List;

public interface GenericCrud <D extends EntityDto<E, D>, E, K> {

    D insert(D d);
    D update(D d);
    void delete(D d);
    List<D> findAll();
    D findById(K d);
}

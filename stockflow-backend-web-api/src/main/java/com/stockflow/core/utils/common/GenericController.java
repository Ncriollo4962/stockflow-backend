package com.stockflow.core.utils.common;

import com.stockflow.core.entity.EntityDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public abstract class GenericController <D extends EntityDto<E, D>, E, K> {

    public GenericController() {
    }

    public abstract GenericCrud<D, E, K> getCrud();

    @PostMapping
    public ResponseEntity<ApiResponse> insert(@RequestBody D d) {
        d = this.getCrud().insert(d);
        URI location = this.getLocation(d);
        return ResponseEntity.created(location).body(ApiResponse.create("Se registró correctamente", d));
    }

    @PutMapping
    public ResponseEntity<ApiResponse> update(@RequestBody D d) {
        d = this.getCrud().update(d);
        return ResponseEntity.ok(ApiResponse.ok("Se actualizó correctamente", d));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> delete(@RequestBody D d) {
        this.getCrud().delete(d);
        return ResponseEntity.ok(ApiResponse.ok("Se eliminó correctamente", ""));
    }

    protected abstract K getPK(D d);

    protected URI getLocation(D d) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{this.getPK(d)}).toUri();
    }
}

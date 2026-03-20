package com.stockflow.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.stockflow.core.dto.DetalleOrdenCompraDto;

class DetalleOrdenCompraServiceImplTest {

    private final DetalleOrdenCompraServiceImpl service = new DetalleOrdenCompraServiceImpl();

    @Nested
    class Unimplemented {
        @Test
        void insert_debeLanzarUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> service.insert(new DetalleOrdenCompraDto()));
        }

        @Test
        void update_debeLanzarUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> service.update(new DetalleOrdenCompraDto()));
        }

        @Test
        void delete_debeLanzarUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> service.delete(1));
        }

        @Test
        void findAll_debeLanzarUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, service::findAll);
        }

        @Test
        void findById_debeLanzarUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> service.findById(1));
        }
    }
}


package com.stockflow.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockflow.core.dto.ProveedorDto;
import com.stockflow.core.entity.Proveedor;
import com.stockflow.core.repository.ProveedorRepository;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceImplTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorServiceImpl service;

    @Nested
    class Unimplemented {
        @Test
        void insert_debeLanzarUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> service.insert(new ProveedorDto()));
        }

        @Test
        void update_debeLanzarUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> service.update(new ProveedorDto()));
        }

        @Test
        void delete_debeLanzarUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> service.delete(1));
        }

        @Test
        void findById_debeLanzarUnsupportedOperationException() {
            assertThrows(UnsupportedOperationException.class, () -> service.findById(1));
        }
    }

    @Nested
    class FindAll {
        @Test
        void debeRetornarListaMapeada() {
            when(proveedorRepository.findAll()).thenReturn(List.of(
                    Proveedor.builder().id(1).codigo("PR1").nombre("Prov 1").build(),
                    Proveedor.builder().id(2).codigo("PR2").nombre("Prov 2").build()));

            List<ProveedorDto> result = service.findAll();

            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }
}


package com.stockflow.core.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.core.entity.*;
import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final UbicacionRepository ubicacionRepository;
    private final InventarioItemRepository inventarioItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final OrdenVentaRepository ordenVentaRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnProperty(prefix = "stockflow.db", name = "initializer.enabled", havingValue = "true")
    @Transactional
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Limpiando base de datos...");
            cleanDatabase();

            log.info("Iniciando carga de datos de prueba...");

            initUsuarios();
            initCategorias();
            initUbicaciones();
            initProveedores();
            initProductos();
            initInventario();
            initOrdenesCompra();
            initOrdenesVenta();
            // initMovimientos();

            log.info("Carga de datos de prueba finalizada.");
        };
    }

    private void cleanDatabase() {
        movimientoInventarioRepository.deleteAll();
        ordenCompraRepository.deleteAll();
        ordenVentaRepository.deleteAll();
        inventarioItemRepository.deleteAll();
        productoRepository.deleteAll();
        proveedorRepository.deleteAll();
        ubicacionRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private void initUsuarios() {
        // if (usuarioRepository.count() > 0) return; // Evitar duplicados de usuarios base si ya existen

        log.info("Creando usuarios...");
        List<Usuario> usuarios = new ArrayList<>();
        
        usuarios.add(Usuario.builder()
                .codigo("USR001")
                .nombre("Admin Principal")
                .email("admin@stockflow.com")
                .contrasena(passwordEncoder.encode("admin123"))
                .rol(com.stockflow.core.enums.EnumCodigoUserRole.ROLE_ADMIN_TI.getCodigo())
                .estado(true)
                .build());

        usuarios.add(Usuario.builder()
                .codigo("USR002")
                .nombre("Vendedor Juan")
                .email("juan@stockflow.com")
                .contrasena(passwordEncoder.encode("user123"))
                .rol(com.stockflow.core.enums.EnumCodigoUserRole.ROLE_VENDEDOR.getCodigo())
                .estado(true)
                .build());

        usuarios.add(Usuario.builder()
                .codigo("USR003")
                .nombre("Almacenero Pedro")
                .email("pedro@stockflow.com")
                .contrasena(passwordEncoder.encode("user123"))
                .rol(com.stockflow.core.enums.EnumCodigoUserRole.ROLE_ALMACENERO.getCodigo())
                .estado(true)
                .build());
                
        usuarios.add(Usuario.builder()
                .codigo("USR004")
                .nombre("Gerente Maria")
                .email("maria@stockflow.com")
                .contrasena(passwordEncoder.encode("user123"))
                .rol(com.stockflow.core.enums.EnumCodigoUserRole.ROLE_GERENTE_ALMACEN.getCodigo())
                .estado(true)
                .build());
                
        usuarios.add(Usuario.builder()
                .codigo("USR005")
                .nombre("Asistente Ana")
                .email("ana@stockflow.com")
                .contrasena(passwordEncoder.encode("user123"))
                .rol(com.stockflow.core.enums.EnumCodigoUserRole.ROLE_ASISTENTE.getCodigo())
                .estado(true)
                .build());

        usuarioRepository.saveAll(usuarios);
    }

    private void initCategorias() {
        // if (categoriaRepository.count() > 0) return;
        log.info("Creando categorias...");
        List<Categoria> categorias = new ArrayList<>();
        
        categorias.add(Categoria.builder().codigo("CAT001").nombre("Herramientas Eléctricas").descripcion("Taladros, amoladoras, sierras").estado(true).build());
        categorias.add(Categoria.builder().codigo("CAT002").nombre("Construcción").descripcion("Cementos, agregados, ladrillos").estado(true).build());
        categorias.add(Categoria.builder().codigo("CAT003").nombre("Gasfitería").descripcion("Tubos, válvulas, accesorios").estado(true).build());
        categorias.add(Categoria.builder().codigo("CAT004").nombre("Electricidad").descripcion("Cables, interruptores, focos").estado(true).build());
        categorias.add(Categoria.builder().codigo("CAT005").nombre("Pinturas").descripcion("Pinturas, solventes, brochas").estado(true).build());

        categoriaRepository.saveAll(categorias);
    }

    private void initUbicaciones() {
        // if (ubicacionRepository.count() > 0) return;
        log.info("Creando ubicaciones...");
        List<Ubicacion> ubicaciones = new ArrayList<>();

        ubicaciones.add(Ubicacion.builder().codigo("UBI001").nombre("Pasillo A Anaquel A1 Estante A1-01").descripcion("Pasillo principal").estado(true).build());
        ubicaciones.add(Ubicacion.builder().codigo("UBI002").nombre("Pasillo B Anaquel B2 Estante B2-01").descripcion("Pasillo secundario").estado(true).build());
        ubicaciones.add(Ubicacion.builder().codigo("UBI003").nombre("Pasillo A Anaquel A1 Estante A1-02").descripcion("Anaquel del Pasillo A").estado(true).build());
        ubicaciones.add(Ubicacion.builder().codigo("UBI004").nombre("Pasillo B Anaquel B2 Estante B2-02").descripcion("Anaquel del Pasillo B").estado(true).build());
        ubicaciones.add(Ubicacion.builder().codigo("UBI005").nombre("Pasillo A Anaquel A1 Estante A1-03").descripcion("Estante del Anaquel 1").estado(true).build());
        ubicaciones.add(Ubicacion.builder().codigo("UBI006").nombre("Pasillo B Anaquel B2 Estante B2-03").descripcion("Estante del Anaquel 2").estado(true).build());

        ubicacionRepository.saveAll(ubicaciones);
    }

    private void initProveedores() {
        // if (proveedorRepository.count() > 0) return;
        log.info("Creando proveedores...");
        List<Proveedor> proveedores = new ArrayList<>();

        proveedores.add(Proveedor.builder().codigo("PROV001").nombre("Tech Supplier Inc").contacto("John Doe").email("contact@tech.com").telefono("555-0101").direccion("Av. Tech 123").estado(true).build());
        proveedores.add(Proveedor.builder().codigo("PROV002").nombre("Home Goods Ltd").contacto("Jane Smith").email("sales@homegoods.com").telefono("555-0102").direccion("Calle Hogar 456").estado(true).build());
        proveedores.add(Proveedor.builder().codigo("PROV003").nombre("Fashion World").contacto("Mike Ross").email("mike@fashion.com").telefono("555-0103").direccion("Fashion Ave 789").estado(true).build());
        proveedores.add(Proveedor.builder().codigo("PROV004").nombre("Toy Kingdom").contacto("Sarah Connor").email("sarah@toys.com").telefono("555-0104").direccion("Play St 101").estado(true).build());
        proveedores.add(Proveedor.builder().codigo("PROV005").nombre("Sporty Life").contacto("Tom Brady").email("tom@sporty.com").telefono("555-0105").direccion("Sport Blvd 202").estado(true).build());

        proveedorRepository.saveAll(proveedores);
    }

    private void initProductos() {
        // if (productoRepository.count() > 0) return;
        log.info("Creando productos...");
        List<Categoria> categorias = categoriaRepository.findAll();
        List<Producto> productos = new ArrayList<>();

        productos.add(Producto.builder().codigo("PROD001").nombre("Taladro Percutor 1/2\"").descripcion("Taladro profesional 750W").categoria(categorias.get(0)).precioCosto(new BigDecimal("180.00")).precioVenta(new BigDecimal("250.00")).cantidadMinima(5).estado(true).build());
        productos.add(Producto.builder().codigo("PROD002").nombre("Cemento Portland 42.5kg").descripcion("Cemento Tipo I para construcción").categoria(categorias.get(1)).precioCosto(new BigDecimal("22.00")).precioVenta(new BigDecimal("28.00")).cantidadMinima(50).estado(true).build());
        productos.add(Producto.builder().codigo("PROD003").nombre("Tubo PVC Desagüe 4\" x 3m").descripcion("Tubo para desagüe clase pesada").categoria(categorias.get(2)).precioCosto(new BigDecimal("15.00")).precioVenta(new BigDecimal("25.00")).cantidadMinima(20).estado(true).build());
        productos.add(Producto.builder().codigo("PROD004").nombre("Cable Eléctrico #12 AWG").descripcion("Rollo de 100m Indeco").categoria(categorias.get(3)).precioCosto(new BigDecimal("150.00")).precioVenta(new BigDecimal("210.00")).cantidadMinima(10).estado(true).build());
        productos.add(Producto.builder().codigo("PROD005").nombre("Pintura Látex Blanca 1GL").descripcion("Pintura lavable mate interior").categoria(categorias.get(4)).precioCosto(new BigDecimal("45.00")).precioVenta(new BigDecimal("65.00")).cantidadMinima(15).estado(true).build());

        productoRepository.saveAll(productos);
    }

    private void initInventario() {
        log.info("Creando inventario...");
        List<Producto> productos = productoRepository.findAll();
        List<Ubicacion> ubicaciones = ubicacionRepository.findAll();
        List<InventarioItem> inventario = new ArrayList<>();

        if (productos.isEmpty() || ubicaciones.isEmpty()) {
            log.warn("No se puede inicializar inventario: faltan productos o ubicaciones.");
            return;
        }

        int cantidad = Math.min(5, Math.min(productos.size(), ubicaciones.size()));
        for (int i = 0; i < cantidad; i++) {
            inventario.add(InventarioItem.builder()
                    .producto(productos.get(i))
                    .ubicacion(ubicaciones.get(i))
                    .lote("LOT-2024-" + (i + 1))
                    .fechaVencimiento(LocalDate.now().plusMonths(6))
                    .cantidad(100)
                    .cantidadReservada(0)
                    .fechaUltimoConteo(LocalDateTime.now())
                    .build());
        }

        inventarioItemRepository.saveAll(inventario);
    }

    private void initOrdenesCompra() {
        log.info("Creando órdenes de compra...");
        List<Proveedor> proveedores = proveedorRepository.findAll();
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Producto> productos = productoRepository.findAll();
        List<OrdenCompra> ordenes = new ArrayList<>();

        if (proveedores.isEmpty() || usuarios.isEmpty() || productos.isEmpty()) {
            log.warn("No se puede inicializar órdenes de compra: faltan dependencias.");
            return;
        }

        int cantidad = Math.min(5, Math.min(proveedores.size(), Math.min(usuarios.size(), productos.size())));
        for (int i = 0; i < cantidad; i++) {
            OrdenCompra orden = OrdenCompra.builder()
                    .numeroOrden("OC-2026-00" + (i + 1))
                    .proveedor(proveedores.get(i))
                    .usuario(usuarios.get(0)) // Admin
                    .fechaOrdenCompra(LocalDateTime.now().minusDays(i * 2))
                    .fechaEntrega(LocalDateTime.now().plusDays(5))
                    .estado(EnumCodigoEstado.APERTURADA.getCodigo())
                    .totalCompra(new BigDecimal("1000.00"))
                    .notas("Nota de prueba " + (i + 1))
                    .detallesOrdenCompra(new ArrayList<>())
                    .build();

            // Agregar detalle
            DetalleOrdenCompra detalle = DetalleOrdenCompra.builder()
                    .ordenCompra(orden)
                    .producto(productos.get(i))
                    .cantidad(10)
                    .precioUnitario(productos.get(i).getPrecioCosto())
                    .cantidadRecibida(0)
                    .estadoDetalle(EnumCodigoEstado.PENDIENTE_RECEPCION.getCodigo())
                    .build();
            
            orden.getDetallesOrdenCompra().add(detalle);
            orden.setTotalCompra(productos.get(i).getPrecioCosto().multiply(new BigDecimal("10"))); // Actualizar total
            
            ordenes.add(orden);
        }

        ordenCompraRepository.saveAll(ordenes);
    }

    private void initOrdenesVenta() {
        log.info("Creando órdenes de venta...");
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Producto> productos = productoRepository.findAll();
        List<OrdenVenta> ordenes = new ArrayList<>();

        if (usuarios.size() < 2 || productos.isEmpty()) { // Necesitamos al menos 2 usuarios (admin y vendedor)
             log.warn("No se puede inicializar órdenes de venta: faltan usuarios o productos.");
             return;
        }

        int cantidad = Math.min(5, productos.size());
        for (int i = 0; i < cantidad; i++) {
            OrdenVenta orden = OrdenVenta.builder()
                    .numeroOrden("OV-2026-00" + (i + 1))
                    .usuario(usuarios.get(1)) // Vendedor
                    .clienteNombre("Cliente " + (i + 1))
                    .clienteEmail("cliente" + (i + 1) + "@email.com")
                    .clienteTelefono("99988877" + i)
                    .direccion("Dirección de entrega " + (i + 1))
                    .fechaVenta(LocalDateTime.now().minusDays(i))
                    .estado(EnumCodigoEstado.APERTURADA.getCodigo())
                    .totalVenta(BigDecimal.ZERO)
                    .detalleOrdenVenta(new ArrayList<>())
                    .build();

            // Agregar detalle
            DetalleOrdenVenta detalle = DetalleOrdenVenta.builder()
                    .ordenVenta(orden)
                    .producto(productos.get(i))
                    .cantidad(2)
                    .precioUnitario(productos.get(i).getPrecioVenta())
                    .cantidadDespachada(0)
                    .estadoDetalle(EnumCodigoEstado.PENDIENTE_DESPACHO.getCodigo())
                    .build();
            
            orden.getDetalleOrdenVenta().add(detalle);
            orden.setTotalVenta(productos.get(i).getPrecioVenta().multiply(new BigDecimal("2")));

            ordenes.add(orden);
        }

        ordenVentaRepository.saveAll(ordenes);
    }
    
    // private void initMovimientos() {
    //     log.info("Creando movimientos de inventario...");
    //     List<Producto> productos = productoRepository.findAll();
    //     List<Usuario> usuarios = usuarioRepository.findAll();
    //     List<Ubicacion> ubicaciones = ubicacionRepository.findAll();
    //     List<MovimientoInventario> movimientos = new ArrayList<>();
        
    //     if (productos.isEmpty() || usuarios.size() < 2 || ubicaciones.isEmpty()) {
    //         log.warn("No se puede inicializar movimientos: faltan dependencias.");
    //         return;
    //     }

    //     int cantidad = Math.min(5, Math.min(productos.size(), ubicaciones.size()));
    //     // Simular movimientos en diferentes meses para el gráfico
    //     for (int i = 0; i < cantidad; i++) {
    //         // Entrada
    //         movimientos.add(MovimientoInventario.builder()
    //                 .producto(productos.get(i))
    //                 .usuario(usuarios.get(0))
    //                 .ubicacion(ubicaciones.get(i)) // Asignar ubicación
    //                 .tipoMovimiento("ENTRADA")
    //                 .cantidad(50)
    //                 .fechaMovimiento(LocalDateTime.now().minusMonths(i)) // Diferentes meses
    //                 .motivo("Compra inicial")
    //                 .referencia("REF-ENT-" + i)
    //                 .build());
            
    //         // Salida
    //         movimientos.add(MovimientoInventario.builder()
    //                 .producto(productos.get(i))
    //                 .usuario(usuarios.get(1))
    //                 .ubicacion(ubicaciones.get(i)) // Asignar ubicación
    //                 .tipoMovimiento("SALIDA")
    //                 .cantidad(10)
    //                 .fechaMovimiento(LocalDateTime.now().minusMonths(i)) // Diferentes meses
    //                 .motivo("Venta mostrador")
    //                 .referencia("REF-SAL-" + i)
    //                 .build());
    //     }
        
    //     movimientoInventarioRepository.saveAll(movimientos);
    // }
}

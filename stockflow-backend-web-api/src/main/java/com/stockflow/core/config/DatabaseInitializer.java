package com.stockflow.core.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
            initMovimientos();

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
        
        categorias.add(Categoria.builder().codigo("CAT001").nombre("Electrónica").descripcion("Dispositivos electrónicos").estado(true).build());
        categorias.add(Categoria.builder().codigo("CAT002").nombre("Hogar").descripcion("Artículos para el hogar").estado(true).build());
        categorias.add(Categoria.builder().codigo("CAT003").nombre("Ropa").descripcion("Prendas de vestir").estado(true).build());
        categorias.add(Categoria.builder().codigo("CAT004").nombre("Juguetes").descripcion("Juguetes para niños").estado(true).build());
        categorias.add(Categoria.builder().codigo("CAT005").nombre("Deportes").descripcion("Artículos deportivos").estado(true).build());

        categoriaRepository.saveAll(categorias);
    }

    private void initUbicaciones() {
        // if (ubicacionRepository.count() > 0) return;
        log.info("Creando ubicaciones...");
        List<Ubicacion> ubicaciones = new ArrayList<>();

        ubicaciones.add(Ubicacion.builder().codigo("UBI001").nombre("Almacén Central").descripcion("Almacén principal").estado(true).build());
        ubicaciones.add(Ubicacion.builder().codigo("UBI002").nombre("Tienda Norte").descripcion("Sucursal Norte").estado(true).build());
        ubicaciones.add(Ubicacion.builder().codigo("UBI003").nombre("Tienda Sur").descripcion("Sucursal Sur").estado(true).build());
        ubicaciones.add(Ubicacion.builder().codigo("UBI004").nombre("Depósito 1").descripcion("Depósito de respaldo").estado(true).build());
        ubicaciones.add(Ubicacion.builder().codigo("UBI005").nombre("Estante A").descripcion("Estantería A en central").estado(true).build());

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

        productos.add(Producto.builder().codigo("PROD001").nombre("Laptop Gamer").descripcion("Laptop de alto rendimiento").categoria(categorias.get(0)).precioCosto(new BigDecimal("1000.00")).precioVenta(new BigDecimal("1500.00")).cantidadMinima(5).estado(true).build());
        productos.add(Producto.builder().codigo("PROD002").nombre("Smartphone X").descripcion("Último modelo").categoria(categorias.get(0)).precioCosto(new BigDecimal("500.00")).precioVenta(new BigDecimal("800.00")).cantidadMinima(10).estado(true).build());
        productos.add(Producto.builder().codigo("PROD003").nombre("Sofá 3 Cuerpos").descripcion("Sofá cómodo").categoria(categorias.get(1)).precioCosto(new BigDecimal("300.00")).precioVenta(new BigDecimal("600.00")).cantidadMinima(2).estado(true).build());
        productos.add(Producto.builder().codigo("PROD004").nombre("Camiseta Polo").descripcion("Camiseta algodón").categoria(categorias.get(2)).precioCosto(new BigDecimal("10.00")).precioVenta(new BigDecimal("25.00")).cantidadMinima(20).estado(true).build());
        productos.add(Producto.builder().codigo("PROD005").nombre("Pelota Fútbol").descripcion("Pelota oficial").categoria(categorias.get(4)).precioCosto(new BigDecimal("15.00")).precioVenta(new BigDecimal("30.00")).cantidadMinima(15).estado(true).build());

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
                    .numeroOrden("OC-2024-00" + (i + 1))
                    .proveedor(proveedores.get(i))
                    .usuario(usuarios.get(0)) // Admin
                    .fechaOrdenCompra(LocalDateTime.now().minusDays(i * 2))
                    .fechaEntrega(LocalDateTime.now().plusDays(5))
                    .estado(EnumCodigoEstado.APERTURADA.name())
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
                    .subtotal(productos.get(i).getPrecioCosto().multiply(new BigDecimal("10")))
                    .build();
            
            orden.getDetallesOrdenCompra().add(detalle);
            orden.setTotalCompra(detalle.getSubtotal()); // Actualizar total
            
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
                    .numeroOrden("OV-2024-00" + (i + 1))
                    .usuario(usuarios.get(1)) // Vendedor
                    .clienteNombre("Cliente " + (i + 1))
                    .clienteEmail("cliente" + (i + 1) + "@email.com")
                    .clienteTelefono("99988877" + i)
                    .direccion("Dirección de entrega " + (i + 1))
                    .fechaVenta(LocalDateTime.now().minusDays(i))
                    .estado(EnumCodigoEstado.PENDIENTE_DESPACHO.name())
                    .totalVenta(BigDecimal.ZERO)
                    .detalleOrdenVenta(new ArrayList<>())
                    .build();

            // Agregar detalle
            DetalleOrdenVenta detalle = DetalleOrdenVenta.builder()
                    .ordenVenta(orden)
                    .producto(productos.get(i))
                    .cantidad(2)
                    .precioUnitario(productos.get(i).getPrecioVenta())
                    .subtotal(productos.get(i).getPrecioVenta().multiply(new BigDecimal("2")))
                    .build();
            
            orden.getDetalleOrdenVenta().add(detalle);
            orden.setTotalVenta(detalle.getSubtotal());

            ordenes.add(orden);
        }

        ordenVentaRepository.saveAll(ordenes);
    }
    
    private void initMovimientos() {
        log.info("Creando movimientos de inventario...");
        List<Producto> productos = productoRepository.findAll();
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Ubicacion> ubicaciones = ubicacionRepository.findAll();
        List<MovimientoInventario> movimientos = new ArrayList<>();
        
        if (productos.isEmpty() || usuarios.size() < 2 || ubicaciones.isEmpty()) {
            log.warn("No se puede inicializar movimientos: faltan dependencias.");
            return;
        }

        int cantidad = Math.min(5, Math.min(productos.size(), ubicaciones.size()));
        // Simular movimientos en diferentes meses para el gráfico
        for (int i = 0; i < cantidad; i++) {
            // Entrada
            movimientos.add(MovimientoInventario.builder()
                    .producto(productos.get(i))
                    .usuario(usuarios.get(0))
                    .ubicacion(ubicaciones.get(i)) // Asignar ubicación
                    .tipoMovimiento("ENTRADA")
                    .cantidad(50)
                    .fechaMovimiento(LocalDateTime.now().minusMonths(i)) // Diferentes meses
                    .motivo("Compra inicial")
                    .referencia("REF-ENT-" + i)
                    .build());
            
            // Salida
            movimientos.add(MovimientoInventario.builder()
                    .producto(productos.get(i))
                    .usuario(usuarios.get(1))
                    .ubicacion(ubicaciones.get(i)) // Asignar ubicación
                    .tipoMovimiento("SALIDA")
                    .cantidad(10)
                    .fechaMovimiento(LocalDateTime.now().minusMonths(i)) // Diferentes meses
                    .motivo("Venta mostrador")
                    .referencia("REF-SAL-" + i)
                    .build());
        }
        
        movimientoInventarioRepository.saveAll(movimientos);
    }
}

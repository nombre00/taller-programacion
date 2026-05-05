-- ##############################################################
-- ## DATOS FIJOS (CATÁLOGOS)
-- ##############################################################

-- ROLES
INSERT INTO Rol (nombre_rol) VALUES ('Admin');
INSERT INTO Rol (nombre_rol) VALUES ('Trabajador');
INSERT INTO Rol (nombre_rol) VALUES ('Cliente');

-- CIUDADES
INSERT INTO Ciudad (nombre_ciudad) VALUES ('Viña del Mar');
INSERT INTO Ciudad (nombre_ciudad) VALUES ('Valparaíso');
INSERT INTO Ciudad (nombre_ciudad) VALUES ('Curauma');
INSERT INTO Ciudad (nombre_ciudad) VALUES ('Quilpué');
INSERT INTO Ciudad (nombre_ciudad) VALUES ('Villa Alemana');

-- CATEGORÍAS DE PRODUCTOS
INSERT INTO CategoriaProducto (nombre_categoria) VALUES ('Hamburguesas');
INSERT INTO CategoriaProducto (nombre_categoria) VALUES ('Combos');
INSERT INTO CategoriaProducto (nombre_categoria) VALUES ('Refrescos');
INSERT INTO CategoriaProducto (nombre_categoria) VALUES ('Acompañamientos');
INSERT INTO CategoriaProducto (nombre_categoria) VALUES ('Niños');

-- ESTADOS DE PEDIDO
INSERT INTO EstadoPedido (nombre_estado) VALUES ('Pendiente de Pago');
INSERT INTO EstadoPedido (nombre_estado) VALUES ('Recibido');
INSERT INTO EstadoPedido (nombre_estado) VALUES ('En preparación');
INSERT INTO EstadoPedido (nombre_estado) VALUES ('En camino');
INSERT INTO EstadoPedido (nombre_estado) VALUES ('Entregado');
INSERT INTO EstadoPedido (nombre_estado) VALUES ('Cancelado');

-- MÉTODOS DE PAGO
INSERT INTO MetodoPago (nombre_metodo) VALUES ('Webpay');
INSERT INTO MetodoPago (nombre_metodo) VALUES ('Efectivo');

-- TIPOS DE ENTREGA
INSERT INTO TipoEntrega (nombre_entrega) VALUES ('Delivery');
INSERT INTO TipoEntrega (nombre_entrega) VALUES ('Retiro en Local');

COMMIT;
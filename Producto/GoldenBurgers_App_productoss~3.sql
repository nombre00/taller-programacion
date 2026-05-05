SET DEFINE OFF;

-- COMBOS (id_categoria = 2)
INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (2, 'Combo Clásica', 'Hamburguesa 120g, doble chedar, pepinillos, salsa Golden, tomate, lechuga, cebolla morada y pepinillos.', 7990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FComboClasica.png?alt=media&token=162762f0-9418-47c2-adea-e7aa701674f9', 1);

-- HAMBURGUESAS (id_categoria = 1)
INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (1, 'Clásica', 'Hamburguesa 120g, doble chedar, pepinillos, salsa Golden, tomate, lechuga, cebolla morada y pepinillos.', 6990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FClasica.PNG?alt=media&token=f64aad4e-a09b-4cb4-9db8-2409b83186c2', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (1, 'Champiñon', 'Hamburguesa 120g, queso mantecoso, champiñones, cebolla caramelizada y Mayonesa.', 8790, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FChampinion.png?alt=media&token=03869d58-00c3-45ad-be83-407f7d2d0e67', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (1, 'Triple Queso', 'Hamburguesa 120g, triple cheddar, ketchup y pepinillos.', 9990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FTripleQueso.png?alt=media&token=ac986191-dec5-4bda-8d9e-be7962bbe359', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (1, 'Golden', 'Hamburguesa 120g, doble cheddar, pepinillos, tocino, salsa golden.', 7990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FGolden.PNG?alt=media&token=95075731-00a5-4c62-9738-215fa308f59b', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (1, 'Bacon BBQ', 'Hamburguesa premium 120gr, doble cheddar, doble bacon, salsa BBQ y cebolla crispy.', 8990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FBaconBBQ.PNG?alt=media&token=5d372314-75fb-43f5-9fbb-775eab2a0d49', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (1, 'Italiana', 'Hamburguesa 120g, Palta, tomate y mayonesa.', 6290, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FItaliana.PNG?alt=media&token=4f0e2591-d762-475a-9ce0-6e528c8a3643', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (1, 'Spicy', 'Hamburguesa premium 120g, cheddar, jalapeños, bacon, cebolla crispy y salsa spicy.', 7790, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FSpicy.png?alt=media&token=4e6a2780-77a0-494d-acf9-d7955a7e2214', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (1, 'Bacon Cheeseburger', 'Hamburguesa 120g, doble cheddar, pepinillos, cebolla, tocino y salsa Golden.', 6990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FBaconCheese.PNG?alt=media&token=02719924-7114-451f-81f4-fc5a5bdc56cf', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (1, 'Cheeseburger', 'Hamburguesa 120g, doble cheddar, pepinillos, cebolla y salsa Golden.', 7890, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FCheeseburger.PNG?alt=media&token=805adca4-bcb4-4fdd-8074-5f1bbee7a361', 1);

-- ACOMPAÑAMIENTOS (id_categoria = 4)
INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (4, 'Papas Golden', 'Papas fritas cubiertas de cheddar y topping de tocino crispy.', 6990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FPapasGolden.png?alt=media&token=0f1a1624-6f47-45a0-a42e-76d3f29a17f0', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (4, 'Chicken Pop', 'Bolitas crujientes de pollo.', 6990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FChickenPop.png?alt=media&token=2757d9b8-9502-49d6-ba36-29745b880800', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (4, 'Papas fritas', 'Palitos de papa frita clásicas.', 1990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FPapasfritas.png?alt=media&token=f103745f-84df-4695-8fec-c8ea03dc298e', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (4, 'Deditos de mozzarella', 'Deditos de queso mozzarella empanizados y fritos.', 2990, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FMozzarellaFingers.png?alt=media&token=df270371-fc08-4023-9a9c-f7e9d62f4d50', 1);

-- REFRESCOS (id_categoria = 3)
INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (3, 'Coca-Cola', 'Coca-Cola original 350 ml.', 1490, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FCocaCola.PNG?alt=media&token=4d845972-9ff2-44e9-9498-ea7e390250c6', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (3, 'Coca-Cola Zero', 'Coca-Cola sin azucar 350 ml.', 1490, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FCocaZero.JPG?alt=media&token=cbaaabad-a733-4177-9559-d050ddfaec7a', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (3, 'Fanta', 'Fanta Original 350 ml.', 1490, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FFanta.JPG?alt=media&token=5b1524c7-96a4-4c51-b17b-178b4c0a5de9', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (3, 'Sprite', 'Bebida refrescante sabor lima 350 ml.', 1490, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FSprite.JPG?alt=media&token=93571a21-602d-4b3e-8d5c-97d035ee6e5c', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (3, 'Jumex', 'Nectar de frutas sabor durazno 350 ml.', 1490, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FJumex.JPG?alt=media&token=bbd16813-4b26-4dd3-848b-a5b8701b8c14', 1);

-- NIÑOS (id_categoria = 5)
INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (5, 'Avocado Kids', 'Hamburguesa con Palta.', 3890, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FAvocadoKids.PNG?alt=media&token=f92d11c8-6d90-465d-b995-daad1b7b4734', 1);

INSERT INTO Producto (id_categoria, nombre_producto, descripcion, precio_base, imagen_url, disponible) 
VALUES (5, 'Play Queso', 'Hamburguesa con queso.', 3890, 
'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FPlayqueso.PNG?alt=media&token=65234a93-d734-47f9-883a-7a6dad45b35c', 1);

COMMIT;
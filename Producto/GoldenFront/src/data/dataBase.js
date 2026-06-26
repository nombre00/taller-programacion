// Este archivo ahora solo exporta el array de productos.
// Ya no se usa 'window.productosDB'. Cualquier componente que necesite
// esta lista, simplemente la importará.

export const productosDB = [
    // --- Combos ---
    {
        id: 1,
        nombre: 'Combo Clásica',
        categoria: 'Combos',
        precio: 7990,
        descripcion: 'Hamburguesa 120g, doble chedar, pepinillos, salsa Golden, tomate, lechuga, cebolla morada y pepinillos.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FComboClasica.png?alt=media&token=162762f0-9418-47c2-adea-e7aa701674f9' // Asegúrate que tus imágenes estén en la carpeta public/img/
    },
    // --- Burgers ---
    {
        id: 2,
        nombre: 'Clásica',
        categoria: 'Burgers',
        precio: 6990,
        descripcion: 'Hamburguesa 120g, doble chedar, pepinillos, salsa Golden, tomate, lechuga, cebolla morada y pepinillos.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FClasica.PNG?alt=media&token=f64aad4e-a09b-4cb4-9db8-2409b83186c2'
    },
    {
        id: 3,
        nombre: 'Champiñon',
        categoria: 'Burgers',
        precio: 8790,
        descripcion: 'Hamburguesa 120g, queso mantecoso, champiñones, cebolla caramelizada y Mayonesa.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FChampinion.png?alt=media&token=03869d58-00c3-45ad-be83-407f7d2d0e67'
    },
    {
        id: 4,
        nombre: 'Triple Queso',
        categoria: 'Burgers',
        precio: 9990,
        descripcion: 'Hamburguesa 120g, triple cheddar, ketchup y pepinillos.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FTripleQueso.png?alt=media&token=ac986191-dec5-4bda-8d9e-be7962bbe359'
    },
    {
        id: 5,
        nombre: 'Golden',
        categoria: 'Burgers',
        precio: 7990,
        descripcion: 'Hamburguesa 120g, doble cheddar, pepinillos, tocino, salsa golden.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FGolden.PNG?alt=media&token=95075731-00a5-4c62-9738-215fa308f59b'
    },
    {
        id: 6,
        nombre: 'Bacon BBQ',
        categoria: 'Burgers',
        precio: 8990,
        descripcion: 'Hamburguesa premium 120gr, doble cheddar, doble bacon, salsa BBQ y cebolla crispy.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FBaconBBQ.PNG?alt=media&token=5d372314-75fb-43f5-9fbb-775eab2a0d49'
    },
    {
        id: 7,
        nombre: 'Italiana',
        categoria: 'Burgers',
        precio: 6290,
        descripcion: 'Hamburguesa 120g, Palta, tomate y mayonesa.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FItaliana.PNG?alt=media&token=4f0e2591-d762-475a-9ce0-6e528c8a3643'
    },
    {
        id: 8,
        nombre: 'Spicy',
        categoria: 'Burgers',
        precio: 7790,
        descripcion: 'Hamburguesa premium 120g, cheddar, jalapeños, bacon, cebolla crispy y salsa spicy.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FSpicy.png?alt=media&token=4e6a2780-77a0-494d-acf9-d7955a7e2214'
    },
    {
        id: 9,
        nombre: 'Bacon Cheeseburger',
        categoria: 'Burgers',
        precio: 6990,
        descripcion: 'Hamburguesa 120g, doble cheddar, pepinillos, cebolla, tocino y salsa Golden.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FBaconCheese.PNG?alt=media&token=02719924-7114-451f-81f4-fc5a5bdc56cf'
    },
    {
        id: 10,
        nombre: 'Cheeseburger',
        categoria: 'Burgers',
        precio: 7890,
        descripcion: 'Hamburguesa 120g, doble cheddar, pepinillos, cebolla y salsa Golden.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FCheeseburger.PNG?alt=media&token=805adca4-bcb4-4fdd-8074-5f1bbee7a361'
    },
    // --- Acompañamientos ---
    {
        id: 11,
        nombre: 'Papas Golden',
        categoria: 'Acompañamientos',
        precio: 6990,
        descripcion: 'Papas fritas cubiertas de cheddar y topping de tocino crispy.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FPapasGolden.png?alt=media&token=0f1a1624-6f47-45a0-a42e-76d3f29a17f0'
    },
    {
        id: 12,
        nombre: 'Chicken Pop',
        categoria: 'Acompañamientos',
        precio: 6990,
        descripcion: 'Bolitas crujientes de pollo.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FChickenPop.png?alt=media&token=2757d9b8-9502-49d6-ba36-29745b880800'
    },
    {
        id: 13,
        nombre: 'Papas fritas',
        categoria: 'Acompañamientos',
        precio: 1990,
        descripcion: 'Palitos de papa frita clásicas.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FPapasfritas.png?alt=media&token=f103745f-84df-4695-8fec-c8ea03dc298e'
    },
    {
        id: 14,
        nombre: 'Deditos de mozzarella',
        categoria: 'Acompañamientos',
        precio: 2990,
        descripcion: 'Deditos de queso mozzarella empanizados y fritos.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FMozzarellaFingers.png?alt=media&token=df270371-fc08-4023-9a9c-f7e9d62f4d50'
    },
    // --- Refrescos ---
    {
        id: 14,
        nombre: 'Coca-Cola',
        categoria: 'Refrescos',
        precio: 1490,
        descripcion: 'Coca-Cola original 350 ml.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FCocaCola.PNG?alt=media&token=4d845972-9ff2-44e9-9498-ea7e390250c6'
    },
    {
        id: 15,
        nombre: 'Coca-Cola Zero',
        categoria: 'Refrescos',
        precio: 1490,
        descripcion: 'Coca-Cola sin azucar 350 ml.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FCocaZero.JPG?alt=media&token=cbaaabad-a733-4177-9559-d050ddfaec7a'
    },
    {
        id: 16,
        nombre: 'Fanta',
        categoria: 'Refrescos',
        precio: 1490,
        descripcion: 'Fanta Original 350 ml.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FFanta.JPG?alt=media&token=5b1524c7-96a4-4c51-b17b-178b4c0a5de9'
    },
    {
        id: 17,
        nombre: 'Sprite',
        categoria: 'Refrescos',
        precio: 1490,
        descripcion: 'Bebida refresacnte sabor lima 350 ml.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FSprite.JPG?alt=media&token=93571a21-602d-4b3e-8d5c-97d035ee6e5c'
    },
    {
        id: 18,
        nombre: 'Jumex',
        categoria: 'Refrescos',
        precio: 1490,
        descripcion: 'Nectar de frutas sabor durazno 350 ml.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FJumex.JPG?alt=media&token=bbd16813-4b26-4dd3-848b-a5b8701b8c14'
    },
    // --- Kids ---
    {
        id: 19,
        nombre: 'Avocado Kids',
        categoria: 'Kids',
        precio: 3890,
        descripcion: 'Hamburguesa con Palta.',
        stock: 5,
        imagen: 'https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2FAvocadoKids.PNG?alt=media&token=f92d11c8-6d90-465d-b995-daad1b7b4734'
    },
    {
        id: 20,
        nombre: 'Play Queso',
        categoria: 'Kids',
        precio: 3890,
        descripcion: 'Hamburguesa con queso.',
        stock: 5,
        imagen: 'https://console.firebase.google.com/u/0/project/goldenburgers-60680/storage/goldenburgers-60680.firebasestorage.app/files/~2Fimg?hl=es-419'
    }
];

// src/database.js

// Datos base de ejemplo
export const usuarios = [
  {
    run: "12345678-9",
    nombre: "Juan",
    apellidos: "Pérez",
    email: "juan@test.com",
    rol: "Admin",
  },
  {
    run: "98765432-1",
    nombre: "María",
    apellidos: "Gómez",
    email: "maria@test.com",
    rol: "Cajero",
  },
  {
    run: "11222333-4",
    nombre: "Pedro",
    apellidos: "Rojas",
    email: "pedro@test.com",
    rol: "Cocinero",
  },
  
];

export const clientes = [
  {
    nombre: "Carlos",
    correo: "carlos@cliente.com",
  },
  {
    nombre: "Ana",
    correo: "ana@cliente.com",
  },
];

-- =====================================================================
-- Caso 6: Administración de parqueos — versión para phpMyAdmin
--
-- Igual que schema.sql pero SIN "DROP/CREATE DATABASE" ni "USE", porque
-- en el servidor compartido del curso el usuario no puede crear bases de
-- datos. Uso: en phpMyAdmin seleccione su base de datos (panel izquierdo)
-- → pestaña "Importar" (o "SQL") → cargue este archivo → Continuar.
-- Borra y vuelve a crear las tablas, así que puede ejecutarse varias veces.
-- =====================================================================

SET NAMES utf8mb4;

DROP TABLE IF EXISTS ingreso;
DROP TABLE IF EXISTS zona;
DROP TABLE IF EXISTS vehiculo;


-- ---------------------------------------------------------------------
-- Tablas
-- ---------------------------------------------------------------------

-- Vehículos autorizados para usar el parqueo.
CREATE TABLE vehiculo (
    placa        VARCHAR(10)  NOT NULL,
    tipo         VARCHAR(15)  NOT NULL,
    marca        VARCHAR(40)  NOT NULL,
    color        VARCHAR(30)  NOT NULL,
    propietario  VARCHAR(80)  NOT NULL,
    categoria    VARCHAR(15) NOT NULL DEFAULT 'PARTICULAR',
    CONSTRAINT pk_vehiculo PRIMARY KEY (placa),
    CONSTRAINT ck_vehiculo_tipo CHECK (tipo IN ('AUTOMOVIL', 'MOTOCICLETA')),
    CONSTRAINT ck_vehiculo_categoria CHECK (categoria IN ('PARTICULAR', 'OFICIAL', 'VISITANTE'))
);

-- Secciones del parqueo; cada una admite un único tipo de vehículo.
CREATE TABLE zona (
    codigo         VARCHAR(10) NOT NULL,
    nombre         VARCHAR(60) NOT NULL,
    tipo_vehiculo  VARCHAR(15) NOT NULL,
    capacidad      INT         NOT NULL,
    CONSTRAINT pk_zona PRIMARY KEY (codigo),
    CONSTRAINT uq_zona_nombre UNIQUE (nombre),
    CONSTRAINT ck_zona_tipo CHECK (tipo_vehiculo IN ('AUTOMOVIL', 'MOTOCICLETA')),
    CONSTRAINT ck_zona_capacidad CHECK (capacidad > 0)
);

-- Estadías: un registro por cada vez que un vehículo entra.
-- fecha_hora_salida = NULL significa que el vehículo sigue dentro.
CREATE TABLE ingreso (
    id                  INT         NOT NULL AUTO_INCREMENT,
    vehiculo_placa      VARCHAR(10) NOT NULL,
    zona_codigo         VARCHAR(10) NOT NULL,
    fecha_hora_entrada  DATETIME    NOT NULL,
    fecha_hora_salida   DATETIME    NULL,
    -- Columna calculada: tiene la placa solo mientras el ingreso está
    -- abierto. Su UNIQUE refuerza RN-02 en la base de datos (un vehículo
    -- no puede tener dos ingresos abiertos). La regla principal se valida
    -- en IngresoService.
    placa_dentro        VARCHAR(10)
        GENERATED ALWAYS AS (CASE WHEN fecha_hora_salida IS NULL THEN vehiculo_placa END) STORED,
    CONSTRAINT pk_ingreso PRIMARY KEY (id),
    CONSTRAINT fk_ingreso_vehiculo FOREIGN KEY (vehiculo_placa)
        REFERENCES vehiculo (placa),
    CONSTRAINT fk_ingreso_zona FOREIGN KEY (zona_codigo)
        REFERENCES zona (codigo),
    CONSTRAINT uq_ingreso_placa_dentro UNIQUE (placa_dentro),
    CONSTRAINT ck_ingreso_fechas CHECK (fecha_hora_salida IS NULL OR fecha_hora_salida >= fecha_hora_entrada)
);

-- Acelera el conteo de vehículos dentro de cada zona (RN-01).
CREATE INDEX ix_ingreso_zona_abierto ON ingreso (zona_codigo, fecha_hora_salida);

-- ---------------------------------------------------------------------
-- Datos de prueba compatibles con espacios reservados
-- Los últimos 2 espacios de cada zona son exclusivos de OFICIAL.
-- Para PARTICULAR y VISITANTE, el límite es MAX(0, capacidad - 2).
-- Los ingresos de prueba se insertan directamente; respetan esta regla.
-- ---------------------------------------------------------------------

INSERT INTO zona (codigo, nombre, tipo_vehiculo, capacidad) VALUES
    ('A', 'Zona A - Automóviles norte', 'AUTOMOVIL',   5),
    ('B', 'Zona B - Automóviles sur',   'AUTOMOVIL',   3),
    ('M', 'Zona M - Motocicletas',      'MOTOCICLETA', 2);

INSERT INTO vehiculo (placa, tipo, marca, color, propietario, categoria) VALUES
    ('BCD123',     'AUTOMOVIL',   'Toyota',  'Blanco', 'Ana Rojas Mora',       'PARTICULAR'),
    ('BRT456',     'AUTOMOVIL',   'Hyundai', 'Gris',   'Luis Solano Vargas',   'PARTICULAR'),
    ('CLM789',     'AUTOMOVIL',   'Nissan',  'Rojo',   'María Jiménez Cruz',   'VISITANTE'),
    ('DFG321',     'AUTOMOVIL',   'Suzuki',  'Azul',   'Carlos Méndez Arias',  'OFICIAL'),
    ('HJK654',     'AUTOMOVIL',   'Kia',     'Negro',  'Sofía Castro León',    'PARTICULAR'),
    ('OFI789',     'AUTOMOVIL',   'Toyota',  'Gris',   'Institución',          'OFICIAL'),
    ('MOT-100200', 'MOTOCICLETA', 'Honda',   'Rojo',   'Diego Brenes Quesada', 'OFICIAL'),
    ('MOT-300400', 'MOTOCICLETA', 'Yamaha',  'Negro',  'Valeria Umaña Soto',  'OFICIAL'),
    ('MOT-500600', 'MOTOCICLETA', 'Bajaj',   'Azul',   'Andrés Porras Vega',  'PARTICULAR');

-- Historial (ingresos ya cerrados)
INSERT INTO ingreso (vehiculo_placa, zona_codigo, fecha_hora_entrada, fecha_hora_salida) VALUES
    ('BCD123',     'A', NOW() - INTERVAL 2 DAY - INTERVAL 5 HOUR, NOW() - INTERVAL 2 DAY - INTERVAL 1 HOUR),
    ('CLM789',     'B', NOW() - INTERVAL 1 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 1 DAY - INTERVAL 3 HOUR),
    ('MOT-500600', 'M', NOW() - INTERVAL 1 DAY - INTERVAL 6 HOUR, NOW() - INTERVAL 1 DAY - INTERVAL 2 HOUR);

-- Ingresos abiertos: A tiene 2/5 (un espacio general libre y dos reservados).
-- M tiene 2/2, ambos OFICIALES. Un PARTICULAR no puede ingresar a M.
-- B queda vacía para probar ingresos de visitantes y particulares.
INSERT INTO ingreso (vehiculo_placa, zona_codigo, fecha_hora_entrada) VALUES
    ('BCD123',     'A', NOW() - INTERVAL 3 HOUR),
    ('BRT456',     'A', NOW() - INTERVAL 90 MINUTE),
    ('MOT-100200', 'M', NOW() - INTERVAL 2 HOUR),
    ('MOT-300400', 'M', NOW() - INTERVAL 45 MINUTE);

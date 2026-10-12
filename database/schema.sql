-- =====================================================================
-- Examen Práctico Integrador — Programación III
-- Caso 6: Administración de parqueos
--
-- Versión preliminar (Etapa 1): base de datos y tablas.
-- Los datos de prueba se agregan en la Etapa 2.
--
-- Ejecutar:  mysql -u root -p < database/schema.sql
-- El nombre de la base de datos debe coincidir con db.url en db.properties.
-- =====================================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS examen_progra3;
CREATE DATABASE examen_progra3
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE examen_progra3;

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
    CONSTRAINT pk_vehiculo PRIMARY KEY (placa),
    CONSTRAINT ck_vehiculo_tipo CHECK (tipo IN ('AUTOMOVIL', 'MOTOCICLETA'))
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

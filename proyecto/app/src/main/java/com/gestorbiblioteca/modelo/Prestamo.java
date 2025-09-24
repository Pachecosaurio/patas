package com.gestorbiblioteca.modelo;

import java.time.LocalDate;

/**
 * Clase modelo para representar un préstamo de libro
 */
public class Prestamo {
    private int id;
    private Usuario usuario;
    private Libro libro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionEsperada;
    private LocalDate fechaDevolucionReal;
    private EstadoPrestamo estado;
    private String observaciones;

    public enum EstadoPrestamo {
        ACTIVO, DEVUELTO, VENCIDO, RENOVADO
    }

    // Constructor vacío
    public Prestamo() {
        this.fechaPrestamo = LocalDate.now();
        this.fechaDevolucionEsperada = LocalDate.now().plusDays(14); // 2 semanas por defecto
        this.estado = EstadoPrestamo.ACTIVO;
    }

    // Constructor completo
    public Prestamo(int id, Usuario usuario, Libro libro, LocalDate fechaDevolucionEsperada) {
        this.id = id;
        this.usuario = usuario;
        this.libro = libro;
        this.fechaPrestamo = LocalDate.now();
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
        this.estado = EstadoPrestamo.ACTIVO;
    }

    // Métodos de negocio
    public boolean isVencido() {
        return estado == EstadoPrestamo.ACTIVO && 
               fechaDevolucionEsperada.isBefore(LocalDate.now());
    }

    public long getDiasRestantes() {
        if (estado != EstadoPrestamo.ACTIVO) return 0;
        return LocalDate.now().until(fechaDevolucionEsperada).getDays();
    }

    public void marcarComoDevuelto() {
        this.estado = EstadoPrestamo.DEVUELTO;
        this.fechaDevolucionReal = LocalDate.now();
        if (libro != null) {
            libro.setDisponible(true);
        }
    }

    public void renovar(int diasAdicionales) {
        if (estado == EstadoPrestamo.ACTIVO) {
            this.fechaDevolucionEsperada = fechaDevolucionEsperada.plusDays(diasAdicionales);
            this.estado = EstadoPrestamo.RENOVADO;
        }
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucionEsperada() {
        return fechaDevolucionEsperada;
    }

    public void setFechaDevolucionEsperada(LocalDate fechaDevolucionEsperada) {
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
    }

    public LocalDate getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "Prestamo{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getNombreCompleto() : "null") +
                ", libro=" + (libro != null ? libro.getTitulo() : "null") +
                ", fechaPrestamo=" + fechaPrestamo +
                ", fechaDevolucionEsperada=" + fechaDevolucionEsperada +
                ", estado=" + estado +
                '}';
    }
}
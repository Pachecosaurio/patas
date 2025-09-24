package com.gestorbiblioteca.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Servicio para generar reportes del sistema de biblioteca
 */
public class ReporteService {
    
    private List<Libro> libros;
    private List<Usuario> usuarios;
    private List<Prestamo> prestamos;

    public ReporteService() {
        this.libros = new ArrayList<>();
        this.usuarios = new ArrayList<>();
        this.prestamos = new ArrayList<>();
    }

    // Setters para inyectar datos
    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    /**
     * Genera reporte de libros más prestados
     */
    public Map<String, Integer> getLibrosMasPrestados() {
        Map<String, Integer> reporte = new HashMap<>();
        
        for (Prestamo prestamo : prestamos) {
            if (prestamo.getLibro() != null) {
                String titulo = prestamo.getLibro().getTitulo();
                reporte.put(titulo, reporte.getOrDefault(titulo, 0) + 1);
            }
        }
        
        return reporte;
    }

    /**
     * Genera reporte de usuarios más activos
     */
    public Map<String, Integer> getUsuariosMasActivos() {
        Map<String, Integer> reporte = new HashMap<>();
        
        for (Prestamo prestamo : prestamos) {
            if (prestamo.getUsuario() != null) {
                String nombre = prestamo.getUsuario().getNombreCompleto();
                reporte.put(nombre, reporte.getOrDefault(nombre, 0) + 1);
            }
        }
        
        return reporte;
    }

    /**
     * Cuenta préstamos por estado
     */
    public Map<Prestamo.EstadoPrestamo, Long> getPrestamosPorEstado() {
        Map<Prestamo.EstadoPrestamo, Long> reporte = new HashMap<>();
        
        for (Prestamo.EstadoPrestamo estado : Prestamo.EstadoPrestamo.values()) {
            long count = prestamos.stream()
                .filter(p -> p.getEstado() == estado)
                .count();
            reporte.put(estado, count);
        }
        
        return reporte;
    }

    /**
     * Obtiene préstamos vencidos
     */
    public List<Prestamo> getPrestamosVencidos() {
        List<Prestamo> vencidos = new ArrayList<>();
        
        for (Prestamo prestamo : prestamos) {
            if (prestamo.isVencido()) {
                vencidos.add(prestamo);
            }
        }
        
        return vencidos;
    }

    /**
     * Cuenta libros por categoría
     */
    public Map<String, Long> getLibrosPorCategoria() {
        Map<String, Long> reporte = new HashMap<>();
        
        for (Libro libro : libros) {
            String categoria = libro.getCategoria() != null ? libro.getCategoria() : "Sin categoría";
            reporte.put(categoria, reporte.getOrDefault(categoria, 0L) + 1);
        }
        
        return reporte;
    }

    /**
     * Obtiene estadísticas generales
     */
    public EstadisticasGenerales getEstadisticasGenerales() {
        long totalLibros = libros.size();
        long librosDisponibles = libros.stream().filter(Libro::isDisponible).count();
        long totalUsuarios = usuarios.size();
        long usuariosActivos = usuarios.stream().filter(Usuario::isActivo).count();
        long prestamosActivos = prestamos.stream()
            .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO).count();
        long prestamosVencidos = getPrestamosVencidos().size();

        return new EstadisticasGenerales(
            totalLibros, librosDisponibles, totalUsuarios, 
            usuariosActivos, prestamosActivos, prestamosVencidos
        );
    }

    /**
     * Clase interna para estadísticas generales
     */
    public static class EstadisticasGenerales {
        private final long totalLibros;
        private final long librosDisponibles;
        private final long totalUsuarios;
        private final long usuariosActivos;
        private final long prestamosActivos;
        private final long prestamosVencidos;

        public EstadisticasGenerales(long totalLibros, long librosDisponibles, 
                                   long totalUsuarios, long usuariosActivos, 
                                   long prestamosActivos, long prestamosVencidos) {
            this.totalLibros = totalLibros;
            this.librosDisponibles = librosDisponibles;
            this.totalUsuarios = totalUsuarios;
            this.usuariosActivos = usuariosActivos;
            this.prestamosActivos = prestamosActivos;
            this.prestamosVencidos = prestamosVencidos;
        }

        // Getters
        public long getTotalLibros() { return totalLibros; }
        public long getLibrosDisponibles() { return librosDisponibles; }
        public long getTotalUsuarios() { return totalUsuarios; }
        public long getUsuariosActivos() { return usuariosActivos; }
        public long getPrestamosActivos() { return prestamosActivos; }
        public long getPrestamosVencidos() { return prestamosVencidos; }
        public long getLibrosPrestados() { return totalLibros - librosDisponibles; }
    }
}
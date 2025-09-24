package com.gestorbiblioteca;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.gestorbiblioteca.modelo.Libro;
import com.gestorbiblioteca.modelo.Usuario;
import com.gestorbiblioteca.modelo.Prestamo;

import java.time.LocalDate;

/**
 * Pruebas unitarias para el sistema de gestión de biblioteca
 */
class AppTest {
    
    @Test
    void testCreacionLibro() {
        Libro libro = new Libro(1, "Test Libro", "Test Autor", "978-123", 
                                "Ficción", "2024", "Descripción test", "/test.jpg");
        
        assertEquals(1, libro.getId());
        assertEquals("Test Libro", libro.getTitulo());
        assertEquals("Test Autor", libro.getAutor());
        assertTrue(libro.isDisponible());
    }
    
    @Test 
    void testCreacionUsuario() {
        Usuario usuario = new Usuario(1, "Juan", "Pérez", "juan@email.com",
                                    "555-1234", "Calle 123", Usuario.TipoUsuario.ESTUDIANTE);
        
        assertEquals(1, usuario.getId());
        assertEquals("Juan Pérez", usuario.getNombreCompleto());
        assertEquals("juan@email.com", usuario.getEmail());
        assertTrue(usuario.isActivo());
        assertEquals(Usuario.TipoUsuario.ESTUDIANTE, usuario.getTipoUsuario());
    }
    
    @Test
    void testCreacionPrestamo() {
        Usuario usuario = new Usuario(1, "Ana", "García", "ana@email.com",
                                    "555-5678", "Av. Principal", Usuario.TipoUsuario.PROFESOR);
        
        Libro libro = new Libro(1, "Cien años de soledad", "García Márquez", 
                                "978-456", "Ficción", "1967", "Novela", "/libro.jpg");
        
        Prestamo prestamo = new Prestamo(1, usuario, libro, LocalDate.now().plusDays(14));
        
        assertEquals(1, prestamo.getId());
        assertEquals(usuario, prestamo.getUsuario());
        assertEquals(libro, prestamo.getLibro());
        assertEquals(Prestamo.EstadoPrestamo.ACTIVO, prestamo.getEstado());
        assertFalse(prestamo.isVencido());
    }
    
    @Test
    void testPrestamoVencido() {
        Usuario usuario = new Usuario(1, "Carlos", "López", "carlos@email.com",
                                    "555-9999", "Plaza Central", Usuario.TipoUsuario.ESTUDIANTE);
        
        Libro libro = new Libro(2, "1984", "George Orwell", "978-789", 
                                "Distopía", "1949", "Novela distópica", "/1984.jpg");
        
        // Crear un préstamo con fecha de devolución en el pasado
        Prestamo prestamo = new Prestamo(2, usuario, libro, LocalDate.now().minusDays(5));
        
        assertTrue(prestamo.isVencido());
        assertTrue(prestamo.getDiasRestantes() < 0);
    }
    
    @Test
    void testDevolucionLibro() {
        Usuario usuario = new Usuario(1, "María", "Rodríguez", "maria@email.com",
                                    "555-7777", "Calle Nueva", Usuario.TipoUsuario.ADMINISTRATIVO);
        
        Libro libro = new Libro(3, "El Quijote", "Cervantes", "978-111", 
                                "Clásicos", "1605", "Obra maestra", "/quijote.jpg");
        
        libro.setDisponible(false); // Marcar como prestado
        Prestamo prestamo = new Prestamo(3, usuario, libro, LocalDate.now().plusDays(7));
        
        // Verificar estado inicial
        assertFalse(libro.isDisponible());
        assertEquals(Prestamo.EstadoPrestamo.ACTIVO, prestamo.getEstado());
        
        // Devolver libro
        prestamo.marcarComoDevuelto();
        
        // Verificar estado después de devolución
        assertTrue(libro.isDisponible());
        assertEquals(Prestamo.EstadoPrestamo.DEVUELTO, prestamo.getEstado());
        assertNotNull(prestamo.getFechaDevolucionReal());
    }
    
    @Test
    void testRenovacionPrestamo() {
        Usuario usuario = new Usuario(1, "Pedro", "Martín", "pedro@email.com",
                                    "555-3333", "Barrio Norte", Usuario.TipoUsuario.EXTERNO);
        
        Libro libro = new Libro(4, "Historia del Arte", "Gombrich", "978-222", 
                                "Arte", "1950", "Historia completa", "/arte.jpg");
        
        LocalDate fechaDevolucionOriginal = LocalDate.now().plusDays(14);
        Prestamo prestamo = new Prestamo(4, usuario, libro, fechaDevolucionOriginal);
        
        // Renovar por 7 días adicionales
        prestamo.renovar(7);
        
        assertEquals(Prestamo.EstadoPrestamo.RENOVADO, prestamo.getEstado());
        assertEquals(fechaDevolucionOriginal.plusDays(7), prestamo.getFechaDevolucionEsperada());
    }
}
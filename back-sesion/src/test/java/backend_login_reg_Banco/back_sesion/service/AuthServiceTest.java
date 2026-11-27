package backend_login_reg_Banco.back_sesion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend_login_reg_Banco.back_sesion.dto.RegisterRequest;
import backend_login_reg_Banco.back_sesion.exception.UsuarioDuplicadoException;
import backend_login_reg_Banco.back_sesion.messaging.UsuarioEventPublisher;
import backend_login_reg_Banco.back_sesion.model.Usuario;
import backend_login_reg_Banco.back_sesion.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository repo;
    @Mock
    private UsuarioEventPublisher publisher;
    @InjectMocks
    private AuthService service;

    private RegisterRequest request() {
        return RegisterRequest.builder().nombre("Ana").rut("12345678-9").gmail("ana@mail.com").build();
    }

    @Test
    void guardaElUsuarioComoUsuarioNoAutorizadoYPublicaElEvento() {
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(7L);
            return u;
        });

        Usuario guardado = service.register(request());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        assertEquals("Usuario", captor.getValue().getPermisos());
        assertFalse(captor.getValue().isAutorizado());
        assertEquals(7L, guardado.getIdUsuario());
        verify(publisher).publicar(guardado);
    }

    @Test
    void rutDuplicadoLanzaExcepcionYNoGuardaNiPublica() {
        when(repo.existsByRut("12345678-9")).thenReturn(true);

        UsuarioDuplicadoException e = assertThrows(UsuarioDuplicadoException.class,
                () -> service.register(request()));

        assertEquals("RUT_DUPLICADO", e.getCodigo());
        verify(repo, never()).save(any());
        verify(publisher, never()).publicar(any());
    }

    @Test
    void gmailDuplicadoLanzaExcepcionYNoGuardaNiPublica() {
        when(repo.existsByGmail("ana@mail.com")).thenReturn(true);

        UsuarioDuplicadoException e = assertThrows(UsuarioDuplicadoException.class,
                () -> service.register(request()));

        assertEquals("GMAIL_DUPLICADO", e.getCodigo());
        verify(repo, never()).save(any());
        verify(publisher, never()).publicar(any());
    }
}

package com.biblioteca.sistema_biblioteca.controller;

import com.biblioteca.sistema_biblioteca.dto.PessoaRequestDTO;
import com.biblioteca.sistema_biblioteca.dto.PessoaResponseDTO;
import com.biblioteca.sistema_biblioteca.dto.PessoaUpdateDTO;
import com.biblioteca.sistema_biblioteca.dto.UsuarioListagemDTO;
import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Usuario;
import com.biblioteca.sistema_biblioteca.model.Role;
import com.biblioteca.sistema_biblioteca.service.UsuarioService;
import com.biblioteca.sistema_biblioteca.service.FuncionarioService;
import com.biblioteca.sistema_biblioteca.repository.RoleRepository;
import com.biblioteca.sistema_biblioteca.repository.StatusUsuarioRepository;

import org.modelmapper.ModelMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final UsuarioService usuarioService;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final StatusUsuarioRepository statusUsuarioRepository;
    private final FuncionarioService funcionarioService;

    // 🔥 CONSTRUTOR ÚNICO
    public PessoaController(
            UsuarioService usuarioService,
            RoleRepository roleRepository,
            ModelMapper modelMapper,
            FuncionarioService funcionarioService,
            StatusUsuarioRepository statusUsuarioRepository
    ) {
        this.usuarioService = usuarioService;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.funcionarioService = funcionarioService;
        this.statusUsuarioRepository = statusUsuarioRepository;
    }

    // ============================================================
    // 🟢 CADASTRO PÚBLICO
    // ============================================================
    @PostMapping
    public ResponseEntity<PessoaResponseDTO> cadastrar(@Valid @RequestBody PessoaRequestDTO dto) {

        Usuario usuario = new Usuario();

        usuario.setUsername(dto.getUsername());
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefone(dto.getTelefone());
        usuario.setCpf(dto.getCpf());
        usuario.setEndereco(dto.getEndereco());
        usuario.setSexo(dto.getSexo());

        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        usuario.setDtNascimento(dto.getDtNascimento().format(formatter));

        usuario.setLimiteSlots(3);
        usuario.setFlagAtivo(false);

        Role userRole = roleRepository.findByNomeIgnoreCase("USUARIO")
                .orElseThrow(() -> new RuntimeException("Role USUARIO não encontrada"));
        usuario.setRole(userRole);

        usuario.setSenha(usuarioService.encodePassword(dto.getSenha()));

        Usuario salvo = usuarioService.salvar(usuario);

        PessoaResponseDTO resp = modelMapper.map(salvo, PessoaResponseDTO.class);
        return ResponseEntity.ok(resp);
    }

    // ============================================================
    // 📋 LISTAGEM — FUNCIONÁRIO OU ADMIN
    // ============================================================
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @GetMapping("/usuarios")
    public List<UsuarioListagemDTO> listarUsuarios() {
        return usuarioService.listarUsuariosCompleto();
    }

    // ============================================================
    // 📋 LISTAGEM — USUARIO A SI MESMO APENAS
    // ============================================================
    @GetMapping("/{username}")
    public ResponseEntity<PessoaResponseDTO> buscarPorUsername(@PathVariable String username) {

        Usuario usuarioLogado = usuarioService.getUsuarioLogado();

        boolean isAdminOuFuncionario =
                usuarioLogado.getRole().getNome().equalsIgnoreCase("ADMIN") ||
                usuarioLogado.getRole().getNome().equalsIgnoreCase("FUNCIONARIO");

        // Usuário comum só pode ver ele mesmo
        if (!isAdminOuFuncionario && !usuarioLogado.getUsername().equals(username)) {
            throw new RegraNegocioException("Você não tem permissão para visualizar outros usuários.");
        }

        Usuario usuario = usuarioService.buscaPorId(username)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        PessoaResponseDTO resp = modelMapper.map(usuario, PessoaResponseDTO.class);
        return ResponseEntity.ok(resp);
    }


    // ============================================================
    // ⛔ BLOQUEAR — ADMIN
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{username}/bloquear")
    public ResponseEntity<?> bloquear(@PathVariable String username) {

        Usuario bloqueado = usuarioService.bloquearUsuario(username);

        return ResponseEntity.ok(
            Map.of(
                "message", "Usuário bloqueado com sucesso",
                "username", bloqueado.getUsername()
            )
        );
    }

    // ============================================================
    // 🔓 DESBLOQUEAR — ADMIN
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{username}/desbloquear")
    public ResponseEntity<?> desbloquear(@PathVariable String username) {

        Usuario desbloqueado = usuarioService.desbloquearUsuario(username);

        return ResponseEntity.ok(
            Map.of(
                "message", "Usuário desbloqueado com sucesso",
                "username", desbloqueado.getUsername()
            )
        );
    }


    // ============================================================
    // ⛔ BLOQUEAR MULTIPLOS — ADMIN
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bloquear-multiplos")
    public ResponseEntity<?> bloquearMultiplos(@RequestBody Map<String, List<String>> body) {

        List<String> usernames = body.get("usernames");

        int total = usuarioService.bloquearUsuariosEmMassa(usernames);

        return ResponseEntity.ok(
            Map.of(
                "message", "Usuários bloqueados com sucesso",
                "total", total
            )
        );
    }

    // ============================================================
    // 🔓 DESBLOQUEAR MULTIPLOS — ADMIN
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/desbloquear-multiplos")
    public ResponseEntity<?> desbloquearMultiplos(@RequestBody Map<String, List<String>> body) {

        List<String> usernames = body.get("usernames");

        int total = usuarioService.desbloquearUsuariosEmMassa(usernames);

        return ResponseEntity.ok(
            Map.of(
                "message", "Usuários desbloqueados com sucesso",
                "total", total
            )
        );
    }




    // ============================================================
    // ❌ DELETAR — ADMIN (agora retorna 200 + JSON)
    // ============================================================
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<?> deletar(@PathVariable String username) {

        usuarioService.deletar(username);

        return ResponseEntity.ok(
            Map.of(
                "message", "Usuário deletado da base de dados",
                "username", username
            )
        );
    }


    // ============================================================
    // 📝 ATUALIZAR DADOS
    // ============================================================
    @PutMapping("/update/{identificador}")
    public ResponseEntity<PessoaResponseDTO> atualizar(
            @PathVariable String identificador,
            @RequestBody PessoaUpdateDTO dto
    ) {
        Usuario atualizado = usuarioService.atualizarUsuario(dto, identificador);
        PessoaResponseDTO resp = modelMapper.map(atualizado, PessoaResponseDTO.class);
        return ResponseEntity.ok(resp);
    }
}


package com.biblioteca.sistema_biblioteca.service;

import com.biblioteca.sistema_biblioteca.exception.RegraNegocioException;
import com.biblioteca.sistema_biblioteca.model.Role;
import com.biblioteca.sistema_biblioteca.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // ==========================================================
    // CRIAR ROLE
    // ==========================================================
    public Role criarRole(Role role) {
        if (role.getNome() == null || role.getNome().isBlank()) {
            throw new RegraNegocioException("O nome da role é obrigatório.");
        }

        boolean existe = roleRepository.findByNomeIgnoreCase(role.getNome()).isPresent();
        if (existe) {
            throw new RegraNegocioException("Já existe uma role com este nome.");
        }

        return roleRepository.save(role);
    }

    // ==========================================================
    // LISTAR TODAS AS ROLES
    // ==========================================================
    public List<Role> listarRoles() {
        return roleRepository.findAll();
    }

    // ==========================================================
    // BUSCAR POR ID
    // ==========================================================
    public Role buscarPorId(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Role não encontrada."));
    }

    // ==========================================================
    // BUSCAR POR NOME
    // ==========================================================
    public Role buscarPorNome(String nome) {
        return roleRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new RegraNegocioException("Role não encontrada: " + nome));
    }

    // ==========================================================
    // ATUALIZAR ROLE
    // ==========================================================
    public Role atualizarRole(Integer id, String novoNome) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Role não encontrada."));

        if (novoNome == null || novoNome.isBlank()) {
            throw new RegraNegocioException("O nome da role é obrigatório.");
        }

        boolean existe = roleRepository.findByNomeIgnoreCase(novoNome).isPresent();
        if (existe && !novoNome.equalsIgnoreCase(role.getNome())) {
            throw new RegraNegocioException("Já existe outra role com este nome.");
        }

        role.setNome(novoNome);
        return roleRepository.save(role);
    }

    // ==========================================================
    // DELETAR ROLE
    // ==========================================================
    public void deletarRole(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Role não encontrada."));

        // regra opcional: impedir remover roles do sistema
        if (role.getId() <= 3) {
            throw new RegraNegocioException("Roles padrão do sistema não podem ser removidas.");
        }

        roleRepository.delete(role);
    }
}

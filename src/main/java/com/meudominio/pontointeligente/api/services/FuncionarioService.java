package com.meudominio.pontointeligente.api.services;

import java.util.Optional;

import com.meudominio.pontointeligente.api.entities.Funcionario;

public interface FuncionarioService {

	Funcionario persistir(Funcionario funcionario);
	
	Optional<Funcionario> buscaPorCpf(String cpf);
	
	Optional<Funcionario> buscaPorEmail(String email);
	
	Optional<Funcionario> buscaPorId(Long id);
}

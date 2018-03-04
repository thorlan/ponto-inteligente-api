package com.meudominio.pontointeligente.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.meudominio.pontointeligente.api.entities.Funcionario;

@Transactional(readOnly = true)
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long>{
	
	public Funcionario findByCpf(String cpf);
	
	public Funcionario findByEmail(String email);
	
	public Funcionario findByCpfOrEmail(String cpf, String email);

}

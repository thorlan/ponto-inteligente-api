package com.meudominio.pontointeligente.api.repositories;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import com.meudominio.pontointeligente.api.entities.Empresa;
import com.meudominio.pontointeligente.api.entities.Funcionario;
import com.meudominio.pontointeligente.api.enums.PerfilEnum;
import com.meudominio.pontointeligente.api.utils.PasswordUtils;

public class PopulaTestes {

	public static final String CPF = "127.429.797-43";
	public static final String EMAIL = "thiagorlandini@hotmail.com";
	public static final String CNPJ = "51463645000100";
	
	public static Empresa obterDadosEmpresa() {
		Empresa empresa = new Empresa();
		empresa.setRazaoSocial("Exemplo de empresa");
		empresa.setCnpj(CNPJ);
		return empresa;
	}
	
	public static Funcionario obterDadosFuncionario(Empresa empresa) throws NoSuchAlgorithmException {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome("Nome de testes");
		funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
		funcionario.setSenha(PasswordUtils.gerarBcrypt("123456"));
		funcionario.setCpf(CPF);
		funcionario.setEmail(EMAIL);
		funcionario.setQtdHorasAlmoco(1F);
		funcionario.setQtdHorasTrabalhoDia(8F);
		funcionario.setValorHora(new BigDecimal(50));
		funcionario.setEmpresa(empresa);
		return funcionario;
	}
}

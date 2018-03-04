package com.meudominio.pontointeligente.api.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.meudominio.pontointeligente.api.entities.Empresa;
import com.meudominio.pontointeligente.api.entities.Funcionario;
import com.meudominio.pontointeligente.api.enums.PerfilEnum;
import com.meudominio.pontointeligente.api.repository.EmpresaRepository;
import com.meudominio.pontointeligente.api.repository.FuncionarioRepository;
import com.meudominio.pontointeligente.api.utils.PasswordUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class FuncionarioRepositoryTest {

	@Autowired
	private FuncionarioRepository funcionarioRepository;
	
	@Autowired
	private EmpresaRepository empresaRepository;
	
	private static final String CPF = "127.429.797-43";
	private static final String EMAIL = "thiagorlandini@hotmail.com";
	private static final String CNPJ = "51463645000100";
	
	
	@Before
	public void setUp() throws Exception{
		Empresa empresa = this.empresaRepository.save(obterDadosEmpresa());
		this.funcionarioRepository.save(obterDadosFuncionario(empresa));
	}
	
	@After
	public final void tearDown() {
		this.empresaRepository.deleteAll();
	}
	
	@Test
	public void testBuscarPorCpf() {
		Funcionario funcionario = this.funcionarioRepository.findByCpf(CPF);
		System.out.println("######## CPF : " + funcionario.getCpf() + " CPF PASSADO : " + CPF + "##############");
		assertEquals(CPF, funcionario.getCpf());
	}
	
	@Test
	public void testBuscarPorEmail() {
		Funcionario funcionario = this.funcionarioRepository.findByEmail(EMAIL);
		assertEquals(EMAIL, funcionario.getEmail());
	}
	
	@Test
	public void testBuscarPorEmaileECpfParaCpfInvalido() {
		Funcionario funcionario = this.funcionarioRepository.findByCpfOrEmail("aaaaa",EMAIL);
		assertNotNull(funcionario);
	}
	
	private Funcionario obterDadosFuncionario(Empresa empresa) throws NoSuchAlgorithmException {
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
	
	private Empresa obterDadosEmpresa() {
		Empresa empresa = new Empresa();
		empresa.setRazaoSocial("Exemplo de empresa");
		empresa.setCnpj(CNPJ);
		return empresa;
	}
	
}

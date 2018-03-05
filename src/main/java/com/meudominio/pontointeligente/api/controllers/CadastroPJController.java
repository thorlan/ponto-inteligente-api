package com.meudominio.pontointeligente.api.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meudominio.pontointeligente.api.dtos.CadastroPJDto;
import com.meudominio.pontointeligente.api.entities.Empresa;
import com.meudominio.pontointeligente.api.entities.Funcionario;
import com.meudominio.pontointeligente.api.enums.PerfilEnum;
import com.meudominio.pontointeligente.api.response.Response;
import com.meudominio.pontointeligente.api.services.EmpresaService;
import com.meudominio.pontointeligente.api.services.FuncionarioService;
import com.meudominio.pontointeligente.api.utils.PasswordUtils;

@RestController
@RequestMapping("/api/cadastrar-pj")
@CrossOrigin(origins = "*")
public class CadastroPJController {

	private static final Logger log = LoggerFactory.getLogger(CadastroPJController.class);

	@Autowired
	private FuncionarioService funcionarioService;

	@Autowired
	private EmpresaService empresaService;

	public CadastroPJController() {

	}

	@PostMapping
	public ResponseEntity<Response<CadastroPJDto>> cadastrar(@Valid @RequestBody CadastroPJDto cadastroPJDto,
			BindingResult bindingResult) {

		log.info("Cadastrando PJ {}", cadastroPJDto.toString());
		Response<CadastroPJDto> response = new Response<CadastroPJDto>();

		validarDadosExistentes(cadastroPJDto, bindingResult);
		Empresa empresa = this.converterDtoParaEmpresa(cadastroPJDto);
		Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPJDto);

		if (bindingResult.hasErrors()) {
			log.error("Erro validando cadastro de PJ {}", bindingResult.getAllErrors());
			bindingResult.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		this.empresaService.persistir(empresa);
		funcionario.setEmpresa(empresa);
		this.funcionarioService.persistir(funcionario);

		response.setData(this.converterCadastroPJDto(funcionario));
		return ResponseEntity.ok(response);

	}

	private CadastroPJDto converterCadastroPJDto(Funcionario funcionario) {
		CadastroPJDto cadastroPJDto = new CadastroPJDto();
		cadastroPJDto.setId(funcionario.getId());
		cadastroPJDto.setNome(funcionario.getNome());
		cadastroPJDto.setEmail(funcionario.getEmail());
		cadastroPJDto.setCpf(funcionario.getCpf());

		cadastroPJDto.setRazaoSocial(funcionario.getEmpresa().getRazaoSocial());
		cadastroPJDto.setCnpj(funcionario.getEmpresa().getCnpj());

		return cadastroPJDto;
	}

	private void validarDadosExistentes(CadastroPJDto cadastroPJDto, BindingResult bindingResult) {
		this.empresaService.buscaPorCnpj(cadastroPJDto.getCnpj()).ifPresent(
				empresa -> bindingResult.addError(new ObjectError("empresa", "CNPJ já registrado em nosso banco.")));

		this.funcionarioService.buscaPorCpf(cadastroPJDto.getCpf()).ifPresent(funcionario -> bindingResult
				.addError(new ObjectError("funcionario", "CPF já registrado em nosso banco")));

		this.funcionarioService.buscaPorEmail(cadastroPJDto.getEmail()).ifPresent(funcionario -> bindingResult
				.addError(new ObjectError("funcionario", "EMAIL já registrado em nosso banco")));

	}

	private Funcionario converterDtoParaFuncionario(@Valid CadastroPJDto cadastroPJDto) {
		Funcionario funcionario = new Funcionario();
		funcionario.setCpf(cadastroPJDto.getCpf());
		funcionario.setEmail(cadastroPJDto.getEmail());
		funcionario.setNome(cadastroPJDto.getNome());
		funcionario.setPerfil(PerfilEnum.ROLE_ADMIN);
		funcionario.setSenha(PasswordUtils.gerarBcrypt(cadastroPJDto.getSenha()));

		return funcionario;
	}

	private Empresa converterDtoParaEmpresa(@Valid CadastroPJDto cadastroPJDto) {
		Empresa empresa = new Empresa();
		empresa.setCnpj(cadastroPJDto.getCnpj());
		empresa.setRazaoSocial(cadastroPJDto.getRazaoSocial());

		return empresa;
	}

}

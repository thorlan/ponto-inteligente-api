package com.meudominio.pontointeligente.api.controllers;

import java.math.BigDecimal;
import java.util.Optional;

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

import com.meudominio.pontointeligente.api.dtos.CadastroPFDto;
import com.meudominio.pontointeligente.api.entities.Empresa;
import com.meudominio.pontointeligente.api.entities.Funcionario;
import com.meudominio.pontointeligente.api.enums.PerfilEnum;
import com.meudominio.pontointeligente.api.response.Response;
import com.meudominio.pontointeligente.api.services.EmpresaService;
import com.meudominio.pontointeligente.api.services.FuncionarioService;
import com.meudominio.pontointeligente.api.utils.PasswordUtils;

@RestController
@RequestMapping("/api/cadastrar-pf")
@CrossOrigin(origins = "*")
public class CadastroPFController {

	private static final Logger log = LoggerFactory.getLogger(CadastroPFController.class);
	
	@Autowired
	private EmpresaService empresaService;
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	public CadastroPFController() {
		
	}
	
	@PostMapping
	public ResponseEntity<Response<CadastroPFDto>> cadastrar(@Valid @RequestBody CadastroPFDto cadastroPFDto, BindingResult bindingResult){
		
		log.info("Cadastrando PF {}", cadastroPFDto.toString());
		Response<CadastroPFDto> response = new Response<CadastroPFDto>();

		validarDadosExistentes(cadastroPFDto, bindingResult);
		Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPFDto);

		if (bindingResult.hasErrors()) {
			log.error("Erro validando cadastro de PF {}", bindingResult.getAllErrors());
			bindingResult.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		Optional<Empresa> empresa = this.empresaService.buscaPorCnpj(cadastroPFDto.getCnpj());
		empresa.ifPresent(emp -> funcionario.setEmpresa(emp));
		this.funcionarioService.persistir(funcionario);

		response.setData(this.converterCadastroPFDto(funcionario));
		return ResponseEntity.ok(response);

	}

	private CadastroPFDto converterCadastroPFDto(Funcionario funcionario) {
		CadastroPFDto cadastroPFDto = new CadastroPFDto();
		cadastroPFDto.setId(funcionario.getId());
		cadastroPFDto.setNome(funcionario.getNome());
		cadastroPFDto.setEmail(funcionario.getEmail());
		cadastroPFDto.setCpf(funcionario.getCpf());
		cadastroPFDto.setCnpj(funcionario.getEmpresa().getCnpj());
		
		funcionario.getQtdHorasAlmocoOpt().ifPresent(
				qtdHorasAlmoco -> cadastroPFDto.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(
				qtdHorasTrabalho -> cadastroPFDto.setQtdHorasTRabalhoDia(Optional.of(Float.toString(qtdHorasTrabalho))));
		funcionario.getValorHoraOpt().ifPresent(
				valorHora -> cadastroPFDto.setValorHora(Optional.of(valorHora.toString())));
		
		return cadastroPFDto;
	}

	private void validarDadosExistentes(@Valid CadastroPFDto cadastroPFDto, BindingResult bindingResult) {
		
		Optional<Empresa> empresa = this.empresaService.buscaPorCnpj(cadastroPFDto.getCnpj());
		if (!empresa.isPresent()) {
			bindingResult.addError(new ObjectError("empresa", "Empresa não cadastrada"));
		}

		this.funcionarioService.buscaPorCpf(cadastroPFDto.getCpf()).ifPresent(funcionario -> bindingResult
				.addError(new ObjectError("funcionario", "CPF já registrado em nosso banco")));
		
		this.funcionarioService.buscaPorEmail(cadastroPFDto.getEmail()).ifPresent(funcionario -> bindingResult
				.addError(new ObjectError("funcionario", "EMAIL já registrado em nosso banco")));
		
	}

	private Funcionario converterDtoParaFuncionario(@Valid CadastroPFDto cadastroPFDto) {
		Funcionario funcionario = new Funcionario();
		funcionario.setCpf(cadastroPFDto.getCpf());
		funcionario.setEmail(cadastroPFDto.getEmail());
		funcionario.setNome(cadastroPFDto.getNome());
		funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
		funcionario.setSenha(PasswordUtils.gerarBcrypt(cadastroPFDto.getSenha()));
		
		cadastroPFDto.getQtdHorasAlmoco().ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
		cadastroPFDto.getQtdHorasTRabalhoDia().ifPresent(qtdHorasTrabalho -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabalho)));
		cadastroPFDto.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));
		
		return funcionario;
		
		
	}	
}

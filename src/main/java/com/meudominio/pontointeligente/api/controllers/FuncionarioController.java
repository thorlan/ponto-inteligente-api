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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meudominio.pontointeligente.api.dtos.FuncionarioDto;
import com.meudominio.pontointeligente.api.entities.Funcionario;
import com.meudominio.pontointeligente.api.response.Response;
import com.meudominio.pontointeligente.api.services.FuncionarioService;
import com.meudominio.pontointeligente.api.utils.PasswordUtils;

@RestController
@RequestMapping("/api/funcionarios")
@CrossOrigin(origins ="*")
public class FuncionarioController {
	
	private static final Logger log = LoggerFactory.getLogger(FuncionarioController.class);
	
	@Autowired
	private FuncionarioService funcionarioService;
	
	public FuncionarioController() {
		
	}
	
	@PutMapping(value = "/{id}")
	public ResponseEntity<Response<FuncionarioDto>> atualizar(@PathVariable("id") Long id, @Valid @RequestBody FuncionarioDto funcionarioDto, BindingResult bindingResult){
		
		log.info("Atualizando funcionario: {}", funcionarioDto.toString());
		Response<FuncionarioDto> response = new Response<FuncionarioDto>();
		
		Optional<Funcionario> funcionario = this.funcionarioService.buscaPorId(id);
		if (!funcionario.isPresent()) {
			bindingResult.addError(new ObjectError("funcionario", "Funcionário não encontrado"));
		}
		
		this.atualizarDadosFuncionario(funcionario.get(), funcionarioDto, bindingResult);
		
		if (bindingResult.hasErrors()) {
			log.error("Erro validando funcionário: {}", bindingResult.getAllErrors());
			bindingResult.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		this.funcionarioService.persistir(funcionario.get());
		response.setData(this.converterParaFuncionarioDto(funcionario.get()));
		
		return ResponseEntity.ok(response);
	 
	}

	private FuncionarioDto converterParaFuncionarioDto(Funcionario funcionario) {
		FuncionarioDto funcionarioDto = new FuncionarioDto();
		System.out.println("################################### " + funcionario.getEmail() + "######################################");
		funcionarioDto.setEmail(funcionario.getEmail());
		funcionarioDto.setId(funcionario.getId());
		funcionarioDto.setNome(funcionario.getNome());
		
		funcionario.getQtdHorasAlmocoOpt().ifPresent(qtHorasAlmoco -> 
				funcionarioDto.setQtdHorasAlmoco( Optional.of(Float.toString(qtHorasAlmoco))));
		
		funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(qtHorasTrabalhoDia -> 
				funcionarioDto.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtHorasTrabalhoDia))));
		
		funcionario.getValorHoraOpt().ifPresent(valorHora ->
				funcionarioDto.setValorHora(Optional.of(valorHora.toString())));
		
		return funcionarioDto;
	}

	private void atualizarDadosFuncionario(Funcionario funcionario, @Valid FuncionarioDto funcionarioDto,
			BindingResult bindingResult) {
		
		funcionario.setNome(funcionarioDto.getNome());
		
		if (!funcionario.getEmail().equals(funcionarioDto.getEmail())) {
			this.funcionarioService.buscaPorEmail(funcionarioDto.getEmail())
					.ifPresent(func -> bindingResult.addError(new ObjectError("funcionario", "Email já existente")));
			funcionario.setEmail(funcionarioDto.getEmail());
		}
		
		funcionario.setQtdHorasAlmoco(null);
		funcionarioDto.getQtdHorasAlmoco()
				.ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
		
		funcionario.setQtdHorasTrabalhoDia(null);
		funcionarioDto.getQtdHorasTrabalhoDia().ifPresent(qtHorasTrabalhoDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtHorasTrabalhoDia)));
		
		funcionario.setValorHora(null);
		funcionarioDto.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));
		
		if ( funcionarioDto.getSenha().isPresent()) {
			funcionario.setSenha(PasswordUtils.gerarBcrypt(funcionarioDto.getSenha().get()));
		}
		
		
	}

}

package com.meudominio.pontointeligente.api.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.meudominio.pontointeligente.api.dtos.LancamentoDto;
import com.meudominio.pontointeligente.api.entities.Funcionario;
import com.meudominio.pontointeligente.api.entities.Lancamento;
import com.meudominio.pontointeligente.api.enums.TipoEnum;
import com.meudominio.pontointeligente.api.response.Response;
import com.meudominio.pontointeligente.api.services.FuncionarioService;
import com.meudominio.pontointeligente.api.services.LancamentoService;

@RestController
@RequestMapping("/api/lancamento")
@CrossOrigin("*")
public class LancamentoController {

	private static final Logger log = LoggerFactory.getLogger(LancamentoController.class);
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private LancamentoService lancamentoService;

	@Autowired
	private FuncionarioService funcionarioService;

	@Value("${paginacao.qtd_por_pagina}")
	private int qtdPorPagina;

	public LancamentoController() {

	}

	@GetMapping(value = "/funcionario/{funcionarioId}")
	public ResponseEntity<Response<Page<LancamentoDto>>> listarPorFuncionarioId(
			@PathVariable("funcionarioId") Long funcionarioId, @RequestParam(value = "pag", defaultValue = "0") int pag,
			@RequestParam(value = "ord", defaultValue = "id") String ord,
			@RequestParam(value = "dir", defaultValue = "DESC") String dir) {

		log.info("Buscando lançamentos por ID do funcionario: {}, página: {}", funcionarioId, pag);
		Response<Page<LancamentoDto>> response = new Response<Page<LancamentoDto>>();

		PageRequest pageRequest = new PageRequest(pag, this.qtdPorPagina, Direction.valueOf(dir), ord);
		Page<Lancamento> lancamentos = this.lancamentoService.buscarPorFuncionarioId(funcionarioId, pageRequest);
		Page<LancamentoDto> lancamentosDto = lancamentos.map(lancamento -> this.converterLancamentoDto(lancamento));

		response.setData(lancamentosDto);
		return ResponseEntity.ok(response);
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<Response<LancamentoDto>> listarPorId(@PathVariable("id") Long id) {
		log.info("Buscando lançamento pela ID : {}", id);
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);

		if (!lancamento.isPresent()) {
			log.info("Não foi possível encontrar o lançamento com a ID : {}", id);
			response.getErrors().add("Lançamento não encontrado para o id " + id);
			return ResponseEntity.badRequest().body(response);
		}
		response.setData(this.converterLancamentoDto(lancamento.get()));
		return ResponseEntity.ok(response);

	}

	@PostMapping
	public ResponseEntity<Response<LancamentoDto>> adicionar(@Valid @RequestBody LancamentoDto lancamentoDto,
			BindingResult bindingResult) throws ParseException {
		log.info("Adicionando lançamento : {}", lancamentoDto.toString());
		Response<LancamentoDto> response = new Response<LancamentoDto>();

		validarFuncionario(lancamentoDto, bindingResult);
		Lancamento lancamento = this.converterDtoParaLancamento(lancamentoDto, bindingResult);

		if (bindingResult.hasErrors()) {
			log.info("Ocorreu um erro ao validar o lancamento!");
			log.error("Erro validando lançamento {}", bindingResult.getAllErrors());
			bindingResult.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		log.info("Tentando persistir o lancamento!");
		lancamento = this.lancamentoService.persistir(lancamento);
		log.info("Lançamento persistido com sucesso!");
		response.setData(this.converterLancamentoDto(lancamento));
		log.info("Lançamento convertido para DTO com sucesso!");
		return ResponseEntity.ok(response);
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<Response<LancamentoDto>> atualizar(@PathVariable("id") Long id,
			@Valid @RequestBody LancamentoDto lancamentoDto, BindingResult bindingResult) throws ParseException {

		log.info("Atualizando o lancamento : {}", lancamentoDto.toString());
		Response<LancamentoDto> response = new Response<LancamentoDto>();
		validarFuncionario(lancamentoDto, bindingResult);
		lancamentoDto.setId(Optional.of(id));
		Lancamento lancamento = this.converterDtoParaLancamento(lancamentoDto, bindingResult);

		if (bindingResult.hasErrors()) {
			log.error("Erro validando lançamento {}", bindingResult.getAllErrors());
			bindingResult.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		lancamento = this.lancamentoService.persistir(lancamento);
		response.setData(this.converterLancamentoDto(lancamento));
		return ResponseEntity.ok(response);

	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Response<String>> remover(@PathVariable("id") Long id) {
		log.info("Removendo lancamento de ID {}", id);
		Response<String> response = new Response<>();

		Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);

		if (!lancamento.isPresent()) {
			log.error("Lancamento não encontrado para o ID {}", id);
			response.getErrors().add("Erro ao remover lançamento. Registro não encontrado para o ID " + id);
			return ResponseEntity.badRequest().body(response);
		}

		this.lancamentoService.remover(id);
		return ResponseEntity.ok(response);
	}

	private void validarFuncionario(@Valid LancamentoDto lancamentoDto, BindingResult bindingResult) {

		if (lancamentoDto.getFuncionarioId() == null) {
			bindingResult.addError(new ObjectError("funcionario", "Funcionário não informado"));
			return;
		}

		log.info("Validando funcionário id {}: ", lancamentoDto.getFuncionarioId());
		Optional<Funcionario> funcionario = this.funcionarioService.buscaPorId(lancamentoDto.getFuncionarioId());
		if (!funcionario.isPresent()) {
			bindingResult.addError(new ObjectError("funcionario", "Funcionário não encontrado. ID inexistente"));
		}
		
		log.info("Funcionário validado");
	}

	private LancamentoDto converterLancamentoDto(Lancamento lancamento) {
		
		LancamentoDto lancamentoDto = new LancamentoDto();

		lancamentoDto.setId(Optional.of(lancamento.getId()));

		lancamentoDto.setData(this.dateFormat.format(lancamento.getData()));
		lancamentoDto.setTipo(lancamento.getTipo().toString());
		lancamentoDto.setDescricao(lancamento.getDescricao());
		lancamentoDto.setLocalizacao(lancamento.getLocalizacao());
		lancamentoDto.setFuncionarioId(lancamento.getFuncionario().getId());

		return lancamentoDto;
	}

	private Lancamento converterDtoParaLancamento(LancamentoDto lancamentoDto, BindingResult bindingResult)
			throws ParseException {
		Lancamento lancamento = new Lancamento();

		log.info("Convertando DTO para Lancamento");
		
		if (lancamentoDto.getId().isPresent()) {
			Optional<Lancamento> lanc = this.lancamentoService.buscarPorId(lancamentoDto.getId().get());
			if (lanc.isPresent()) {
				lancamento = lanc.get();
				log.info("Lançamento foi encontrado!");
			} else {
				bindingResult.addError(new ObjectError("lancamento", "Lançamento não encontrado"));
				log.info("Lançamento não encontrado");
			}
		} else {
			log.info("Tentando setar o lancamento para o usuario");
			lancamento.setFuncionario(new Funcionario());
			lancamento.getFuncionario().setId(lancamentoDto.getFuncionarioId());
			log.info("Setamos o lançamento para o usuario de ID {}", lancamento.getFuncionario().getId());
		}
		lancamento.setLocalizacao(lancamentoDto.getLocalizacao());
		lancamento.setDescricao(lancamentoDto.getDescricao());
		lancamento.setData(this.dateFormat.parse(lancamentoDto.getData()));

		if (EnumUtils.isValidEnum(TipoEnum.class, lancamentoDto.getTipo())) {
			lancamento.setTipo(TipoEnum.valueOf(lancamentoDto.getTipo()));
			log.info("Enum passado é válido!");
		} else {
			bindingResult.addError(new ObjectError("tipo", "Tipo inválido"));
		}
		log.info("Retornando o lancamento no converterDtoParaLancamento");
		return lancamento;
	}

}

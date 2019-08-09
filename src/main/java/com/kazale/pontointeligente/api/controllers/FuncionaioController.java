package com.kazale.pontointeligente.api.controllers;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
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

import com.kazale.pontointeligente.api.dtos.FuncionarioDto;
import com.kazale.pontointeligente.api.entities.Funcionario;
import com.kazale.pontointeligente.api.response.Response;
import com.kazale.pontointeligente.api.services.FuncionarioService;
import com.kazale.pontointeligente.api.utils.PasswordUtils;

@RestController
@RequestMapping("api/funcionarios")
@CrossOrigin(origins = "*")
public class FuncionaioController {
	private static final Logger log = LoggerFactory.getLogger(FuncionaioController.class);
	
	@Autowired
	FuncionarioService funcionarioService;

	public FuncionaioController() {
		
	}
	
	
	/**
	 * Atualiza os dados de um funcionário.
	 * 
	 * @param id
	 * @param FuncionarioDto
	 * @param result
	 * @return ResponseEntity<Response<FuncionarioDto>>
	 * @throws NoSuchAlgorithmException
	 */
	@PutMapping(value = "/{id}")
	public ResponseEntity<Response<FuncionarioDto>> atualizar(@PathVariable("id") Long id,
			@Valid @RequestBody FuncionarioDto FuncionarioDto, BindingResult result) throws NoSuchAlgorithmException {
		log.info("Atualizando funcionário: {}", FuncionarioDto.toString());
		Response<FuncionarioDto> response = new Response<FuncionarioDto>();

		Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(id);
		if (!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Erro - Funcionário não encontrado."));
		}

		this.atualizarDadosFuncionario(funcionario.get(), FuncionarioDto, result);

		if (result.hasErrors()) {
			log.error("Erro validando funcionário: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		this.funcionarioService.persisitir(funcionario.get());
		response.setData(this.converterFuncionarioDto(funcionario.get()));

		return ResponseEntity.ok(response);
	}

	/**
	 * Atualiza os dados do funcionário com base nos dados encontrados no DTO.
	 * 
	 * @param funcionario
	 * @param FuncionarioDto
	 * @param result
	 * @throws NoSuchAlgorithmException
	 */
	private void atualizarDadosFuncionario(Funcionario funcionario, FuncionarioDto FuncionarioDto, BindingResult result)
			throws NoSuchAlgorithmException {
		funcionario.setNome(FuncionarioDto.getNome());

		if (!funcionario.getEmail().equals(FuncionarioDto.getEmail())) {
			this.funcionarioService.buscarPorEmail(FuncionarioDto.getEmail())
					.ifPresent(func -> result.addError(new ObjectError("email", "Email já existente.")));
			funcionario.setEmail(FuncionarioDto.getEmail());
		}

		funcionario.setQtdHorasAlmoco(null);
		FuncionarioDto.getQtdHorasAlmoco()
				.ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));

		funcionario.setQtdHorasTrabalhoDia(null);
		FuncionarioDto.getQtdHorasTrabalhoDia()
				.ifPresent(qtdHorasTrabDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabDia)));

		funcionario.setValorHora(null);
		FuncionarioDto.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));

		if (FuncionarioDto.getSenha().isPresent()) {
			funcionario.setSenha(PasswordUtils.gerarBCrypt(FuncionarioDto.getSenha().get()));
		}
	}

	/**
	 * Retorna um DTO com os dados de um funcionário.
	 * 
	 * @param funcionario
	 * @return FuncionarioDto
	 */
	private FuncionarioDto converterFuncionarioDto(Funcionario funcionario) {
		FuncionarioDto FuncionarioDto = new FuncionarioDto();
		FuncionarioDto.setId(funcionario.getId());
		FuncionarioDto.setEmail(funcionario.getEmail());
		FuncionarioDto.setNome(funcionario.getNome());
		funcionario.getQtdHorasAlmocoOpt().ifPresent(
				qtdHorasAlmoco -> FuncionarioDto.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(
				qtdHorasTrabDia -> FuncionarioDto.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasTrabDia))));
		funcionario.getValorHoraOpt()
				.ifPresent(valorHora -> FuncionarioDto.setValorHora(Optional.of(valorHora.toString())));

		return FuncionarioDto;
	}
	
	
	
	

}

package com.cotefacilfood.pagamentos.service;

import com.cotefacilfood.pagamentos.dto.PagamentoDTO;
import com.cotefacilfood.pagamentos.enums.Status;
import com.cotefacilfood.pagamentos.httpClient.PedidoClient;
import com.cotefacilfood.pagamentos.model.Pagamento;
import com.cotefacilfood.pagamentos.repository.PagamentoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PedidoClient pedidoClient;

    @Transactional(readOnly = true)
    public Page<PagamentoDTO> findAll(Pageable pageable) {
        Page<Pagamento> page = repository.findAll(pageable);
        return page.map(PagamentoDTO::new);
//        return repository.findAll(pageable).map(p -> modelMapper.map(p, PagamentoDTO.class));
    }

    @Transactional(readOnly = true)
    public PagamentoDTO findById(Long id) {
        Pagamento entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException());
        return new PagamentoDTO(entity);
//        return modelMapper.map(entity, PagamentoDTO.class);
    }

    @Transactional
    public PagamentoDTO insert(PagamentoDTO dto) {
        Pagamento entity = modelMapper.map(dto, Pagamento.class);
        entity.setStatus(Status.CRIADO);
        repository.save(entity);
        return modelMapper.map(entity, PagamentoDTO.class);
    }

    @Transactional
    public PagamentoDTO update(PagamentoDTO dto, Long id) {
        Pagamento entity = modelMapper.map(dto, Pagamento.class);
        entity.setId(dto.getId());
        repository.save(entity);
        return modelMapper.map(entity, PagamentoDTO.class);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void confirmarPagamento(Long id){
        Optional<Pagamento> pagamento = repository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO);
        repository.save(pagamento.get());
        pedidoClient.atualizarPagamento(pagamento.get().getPedidoId());
    }

    public void alterarStatus(Long id) {
        Optional<Pagamento> pagamento = repository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        repository.save(pagamento.get());
    }
}

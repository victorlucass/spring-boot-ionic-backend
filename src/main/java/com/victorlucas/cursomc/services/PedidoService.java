package com.victorlucas.cursomc.services;

import com.victorlucas.cursomc.domain.Pedido;
import com.victorlucas.cursomc.domain.enums.EstadoPagamento;
import com.victorlucas.cursomc.domain.itemPedido.ItemPedido;
import com.victorlucas.cursomc.domain.pagamentos.PagamentoComBoleto;
import com.victorlucas.cursomc.exceptions.ObjectNotFoundException;
import com.victorlucas.cursomc.repositories.ItemPedidoRepository;
import com.victorlucas.cursomc.repositories.PagamentoRepository;
import com.victorlucas.cursomc.repositories.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private BoletoService boletoService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    public Pedido findById(Integer id){
        Optional<Pedido> pedidoSelect = pedidoRepository.findById(id);

        return pedidoSelect.orElseThrow(
                () -> new ObjectNotFoundException("Pedido não encontrado.")
        );
    }

    @Transactional
    public Pedido save(Pedido pedido) {
        pedido.setId(null);
        pedido.setInstante(new Date());
        pedido.getPagamento().setEstadoPagamento(EstadoPagamento.PENDENTE);
        pedido.getPagamento().setPedido(pedido);
        if (pedido.getPagamento() instanceof PagamentoComBoleto){
            PagamentoComBoleto bol = (PagamentoComBoleto) pedido.getPagamento();
            boletoService.preencherPagamentoComBoleto(bol, pedido.getInstante());
        }
        pedido = pedidoRepository.save(pedido);
        pagamentoRepository.save(pedido.getPagamento());
        for(ItemPedido ip : pedido.getItens()){
            ip.setDesconto(0.0);
            ip.setPreco(produtoService.findById(ip.getProduto().getId()).getPreco());
            ip.setPedido(pedido);
        }
        itemPedidoRepository.saveAll(pedido.getItens());
        return pedido;
    }
}

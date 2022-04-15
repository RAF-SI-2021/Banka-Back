package rs.edu.raf.banka.berza.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.requests.OrderRequest;
import rs.edu.raf.banka.berza.service.impl.BerzaService;

@RestController
@RequestMapping("/api/berza")
public class BerzaController {

    private final BerzaService berzaService;


    public BerzaController(BerzaService berzaService){
        this.berzaService = berzaService;
    }

    @PostMapping(value = "/order", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> makeOrder(@RequestBody OrderRequest orderRequest){
        berzaService.makeOrder(orderRequest.getBerzaId() , orderRequest.getUserId(), orderRequest.getHartijaOdVrednostiId(),
                orderRequest.getHartijaOdVrednostiTip(), orderRequest.getKolicina(), orderRequest.getAkcija(),
                orderRequest.getLimitValue(), orderRequest.getStopValue(), orderRequest.isAllOrNoneFlag(), orderRequest.isMarginFlag());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/order-status/{idBerza}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrderStatus(@PathVariable Long idBerza){
        return ResponseEntity.ok(berzaService.getOrderStatus(idBerza));
    }

}

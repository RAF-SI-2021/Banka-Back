package rs.edu.raf.banka.berza.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AkcijeDto {

    private Long idHartijeOdVrednosti;
    private String oznakaHartije;
    private Double cena;
    private Double promenaIznos;
    private Long volume;
}

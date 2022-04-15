package rs.edu.raf.banka.berza.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuturesUgovoriDto {

    private Long idHartijeOdVrednosti;
    private String oznakaHartije;
    private Double cena;
    private Double promenaIznos;
    private Long volume;
    private Double maintenanceMargin;

}

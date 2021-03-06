package rs.edu.raf.banka.racun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KapitalPoTipuHartijeDto {
    Long id;
    String oznakaHartije;
    String berza;
    Long kolicinaUVlasnistvu;
    Double cena;
    Double vrednostRSD;
    Double vrednost;
    Double kupljenoZa;
    Double profit;
    String kodValute;
}

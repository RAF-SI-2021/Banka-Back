package rs.edu.raf.banka.berza.requests;

import lombok.Data;

@Data
public class OrderRequest {

    private Long berzaId;
    private Long userId;
    private Long hartijaOdVrednostiId;
    private String hartijaOdVrednostiTip;
    private Integer kolicina;
    private String akcija;
    private Integer limitValue;
    private Integer stopValue;
    private boolean allOrNoneFlag;
    private boolean marginFlag;

}

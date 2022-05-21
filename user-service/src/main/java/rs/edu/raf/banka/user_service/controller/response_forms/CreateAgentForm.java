package rs.edu.raf.banka.user_service.controller.response_forms;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateAgentForm {
    String username;
    String ime;
    String prezime;
    String email;
    String jmbg;
    String brTelefon;
    Double limit;
    boolean needsSupervisorPermission;
}

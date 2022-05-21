package rs.edu.raf.banka.user_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
public class Agent extends AbstractUser{

    private Double limit;
    private Double limitUsed;
    private boolean needsSupervisorPermission;

    public Agent(
            String username,
            String ime,
            String prezime,
            String email,
            String jmbg,
            String brTelefon,
            String password,
            String otpSeecret,
            boolean aktivan,
            boolean requiresOtp,
            Role role,
            Double limit,
            boolean needsSupervisorPermission
    ){
        super(username, ime, prezime, email, jmbg, brTelefon, password, otpSeecret, aktivan, requiresOtp, role);
        this.limit = limit;
        this.limitUsed = 0.0;
        this.needsSupervisorPermission = needsSupervisorPermission;
    }
}

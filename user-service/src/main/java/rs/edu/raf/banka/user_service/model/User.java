package rs.edu.raf.banka.user_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class User extends AbstractUser{

    public User(String username, String ime, String prezime, String email, String jmbg, String brTelefon, String password, String otpSeecret, boolean aktivan, boolean requiresOtp, Role role) {
        super(username, ime, prezime, email, jmbg, brTelefon, password, otpSeecret, aktivan, requiresOtp, role);
    }

    public User(String username, String password){
        super(username, password);
    }

    public User(String username, String password, String otpSeecret){
        super(username, password, otpSeecret);
    }
}

package si.banka.korisnicki_servis.controller.response_forms;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChangePasswordForm {
    String newPassword;
}

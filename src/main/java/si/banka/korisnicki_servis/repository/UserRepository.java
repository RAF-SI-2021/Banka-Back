package si.banka.korisnicki_servis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.korisnicki_servis.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u from User u where u.email=:email")
    User findByEmail(String email);

    User findByUsername(String username);
}

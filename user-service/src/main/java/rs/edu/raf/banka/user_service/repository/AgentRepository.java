package rs.edu.raf.banka.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.user_service.model.Agent;

public interface AgentRepository extends JpaRepository<Agent, Long> {
}

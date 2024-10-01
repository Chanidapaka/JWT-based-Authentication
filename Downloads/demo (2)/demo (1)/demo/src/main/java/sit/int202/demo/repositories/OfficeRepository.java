package sit.int202.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int202.demo.entities.Office;

public interface OfficeRepository extends JpaRepository<Office, String> {
}
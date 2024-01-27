package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thainguyen.domain.Payment;

@Repository
public interface TransactionRepository extends JpaRepository<Payment, Long> {
}

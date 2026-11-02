package edu.dvc.repository;

import edu.dvc.domain.ResourceItem;
import edu.dvc.domain.ResourceStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceItemRepository extends JpaRepository<ResourceItem, Long> {
    long countByStatus(ResourceStatus status);
    List<ResourceItem> findByStatus(ResourceStatus status);
    List<ResourceItem> findAllByOrderByNameAsc();
}

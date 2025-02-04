package com.app.backend.domain.group.repository;

import com.app.backend.domain.group.entity.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupRepositoryCustom {

    Optional<Group> findByIdAndDisabled(Long id, Boolean disabled);

    List<Group> findAllByDisabled(Boolean disabled);

    Page<Group> findAllByDisabled(Boolean disabled, Pageable pageable);

    List<Group> findAllByNameContainingAndDisabled(String name, Boolean disabled);

    Page<Group> findAllByNameContainingAndDisabled(String name, Boolean disabled, Pageable pageable);

    List<Group> findAllByCategory_Name(String categoryName);

    Page<Group> findAllByCategory_Name(String categoryName, Pageable pageable);

    List<Group> findAllByCategory_NameAndDisabled(String categoryName, Boolean disabled);

    Page<Group> findAllByCategory_NameAndDisabled(String categoryName, Boolean disabled, Pageable pageable);

    List<Group> findAllByCategory_NameAndNameContainingAndDisabled(String categoryName, String name, Boolean disabled);

    Page<Group> findAllByCategory_NameAndNameContainingAndDisabled(String categoryName,
                                                                   String name,
                                                                   Boolean disabled,
                                                                   Pageable pageable);

}

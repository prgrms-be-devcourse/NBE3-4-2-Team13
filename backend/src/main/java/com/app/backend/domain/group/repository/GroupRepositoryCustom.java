package com.app.backend.domain.group.repository;

import com.app.backend.domain.group.entity.Group;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupRepositoryCustom {

    List<Group> findAllByRegion(String province, String city, String town, Boolean disabled);

    Page<Group> findAllByRegion(String province, String city, String town, Boolean disabled, Pageable pageable);

    List<Group> findAllByNameContainingAndRegion(String name, String province, String city, String town,
                                                 Boolean disabled);

    Page<Group> findAllByNameContainingAndRegion(String name, String province, String city, String town,
                                                 Boolean disabled, Pageable pageable);

}

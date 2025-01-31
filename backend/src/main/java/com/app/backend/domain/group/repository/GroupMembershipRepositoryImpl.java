package com.app.backend.domain.group.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupMembershipRepositoryImpl implements GroupMembershipRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

}

package com.app.backend.domain.group.repository;

import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.QGroup;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 목록 조회
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param disabled - 활성화 여부(Soft Delete 상태)
     * @return 모임 목록
     */
    @Override
    public List<Group> findAllByRegion(final String province,
                                       final String city,
                                       final String town,
                                       @NotNull final Boolean disabled) {
        QGroup group = QGroup.group;
        return jpaQueryFactory.selectFrom(group)
                              .where(getRegionCondition(province, city, town, group).and(group.disabled.eq(disabled)))
                              .fetch();
    }

    /**
     * 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 페이징 목록 조회
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param disabled - 활성화 여부(Soft Delete 상태)
     * @param pageable - 페이징 객체
     * @return 모임 페이징 목록
     */
    @Override
    public Page<Group> findAllByRegion(final String province,
                                       final String city,
                                       final String town,
                                       @NotNull final Boolean disabled,
                                       @NotNull final Pageable pageable) {
        QGroup group = QGroup.group;
        List<Group> content = jpaQueryFactory.selectFrom(group)
                                             .where(getRegionCondition(province, city, town, group)
                                                            .and(group.disabled.eq(disabled)))
                                             .orderBy(getSortCondition(pageable, group))
                                             .offset(pageable.getOffset())
                                             .limit(pageable.getPageSize())
                                             .fetch();
        JPAQuery<Long> count = jpaQueryFactory.select(group.count())
                                              .from(group)
                                              .where(getRegionCondition(province, city, town, group));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }

    /**
     * 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 목록 조회
     *
     * @param name     - 모임 이름
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param disabled - 활성화 여부(Soft Delete 상태)
     * @return 모임 목록
     */
    @Override
    public List<Group> findAllByNameContainingAndRegion(final String name,
                                                        final String province,
                                                        final String city,
                                                        final String town,
                                                        @NotNull final Boolean disabled) {
        QGroup group = QGroup.group;
        return jpaQueryFactory.selectFrom(group)
                              .where(group.name.contains(name)
                                               .and(getRegionCondition(province, city, town, group))
                                               .and(group.disabled.eq(disabled)))
                              .fetch();
    }

    /**
     * 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 페이징 목록 조회
     *
     * @param name     - 모임 이름
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param disabled - 활성화 여부(Soft Delete 상태)
     * @param pageable - 페이징 객체
     * @return 모임 페이징 목록
     */
    @Override
    public Page<Group> findAllByNameContainingAndRegion(final String name,
                                                        final String province,
                                                        final String city,
                                                        final String town,
                                                        @NotNull final Boolean disabled,
                                                        @NotNull final Pageable pageable) {
        QGroup group = QGroup.group;
        List<Group> content = jpaQueryFactory.selectFrom(group)
                                             .where(group.name.contains(name)
                                                              .and(getRegionCondition(province, city, town, group))
                                                              .and(group.disabled.eq(disabled)))
                                             .orderBy(getSortCondition(pageable, group))
                                             .offset(pageable.getOffset())
                                             .limit(pageable.getPageSize())
                                             .fetch();
        JPAQuery<Long> count = jpaQueryFactory.select(group.count())
                                              .from(group)
                                              .where(group.name.contains(name)
                                                               .and(getRegionCondition(province, city, town, group)));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }

    /**
     * 카테고리명, 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 목록 조회
     *
     * @param categoryName - 카테고리명
     * @param name         - 모임 이름
     * @param province     - 시/도
     * @param city         - 시/군/구
     * @param town         - 읍/면/동
     * @param disabled     - 활성화 여부(Soft Delete 상태)
     * @return 모임 목록
     */
    @Override
    public List<Group> findAllByCategoryAndNameContainingAndRegion(final String categoryName,
                                                                   final String name,
                                                                   final String province,
                                                                   final String city,
                                                                   final String town,
                                                                   @NotNull final Boolean disabled) {
        QGroup group = QGroup.group;
        return jpaQueryFactory.selectFrom(group)
                              .where(categoryName != null && !categoryName.isBlank()
                                     ? group.category.name.eq(categoryName)
                                     : Expressions.TRUE,
                                     name != null && !name.isBlank()
                                     ? group.name.contains(name)
                                     : Expressions.TRUE,
                                     getRegionCondition(province, city, town, group),
                                     group.disabled.eq(disabled))
                              .fetch();
    }

    /**
     * 카테고리명, 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 페이징 목록 조회
     *
     * @param categoryName - 카테고리명
     * @param name         - 모임 이름
     * @param province     - 시/도
     * @param city         - 시/군/구
     * @param town         - 읍/면/동
     * @param disabled     - 활성화 여부(Soft Delete 상태)
     * @return 모임 페이징 목록
     */
    @Override
    public Page<Group> findAllByCategoryAndNameContainingAndRegion(final String categoryName,
                                                                   final String name,
                                                                   final String province,
                                                                   final String city,
                                                                   final String town,
                                                                   @NotNull final Boolean disabled,
                                                                   @NotNull final Pageable pageable) {
        QGroup group = QGroup.group;
        List<Group> content = jpaQueryFactory.selectFrom(group)
                                             .where(categoryName != null && !categoryName.isBlank()
                                                    ? group.category.name.eq(categoryName)
                                                    : Expressions.TRUE,
                                                    name != null && !name.isBlank()
                                                    ? group.name.contains(name)
                                                    : Expressions.TRUE,
                                                    getRegionCondition(province, city, town, group),
                                                    group.disabled.eq(disabled))
                                             .orderBy(getSortCondition(pageable, group))
                                             .offset(pageable.getOffset())
                                             .limit(pageable.getPageSize())
                                             .fetch();
        JPAQuery<Group> query = jpaQueryFactory.selectFrom(group)
                                               .where(categoryName != null && !categoryName.isBlank()
                                                      ? group.category.name.eq(categoryName)
                                                      : Expressions.TRUE,
                                                      name != null && !name.isBlank()
                                                      ? group.name.contains(name)
                                                      : Expressions.TRUE,
                                                      getRegionCondition(province, city, town, group),
                                                      group.disabled.eq(disabled))
                                               .orderBy(getSortCondition(pageable, group))
                                               .offset(pageable.getOffset())
                                               .limit(pageable.getPageSize());
        System.out.println("query.toString() = " + query.toString());
        JPAQuery<Long> count = jpaQueryFactory.select(group.count())
                                              .from(group)
                                              .where(categoryName != null && !categoryName.isBlank()
                                                     ? group.category.name.eq(categoryName)
                                                     : Expressions.TRUE,
                                                     name != null && !name.isBlank()
                                                     ? group.name.contains(name)
                                                     : Expressions.TRUE,
                                                     getRegionCondition(province, city, town, group),
                                                     group.disabled.eq(disabled));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }

    //============================== 내부 메서드 ==============================//

    /**
     * 검색할 지역(시/도, 시/군/구, 읍/면/동)에 따라 BooleanExpression 생성
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param group    - QGroup
     * @return BooleanExpression
     */
    private BooleanExpression getRegionCondition(final String province,
                                                 final String city,
                                                 final String town,
                                                 @NotNull final QGroup group) {
        BooleanExpression expression = Expressions.TRUE;

        if (StringUtils.hasText(province))
            expression = expression.and(group.province.eq(province));

        if (StringUtils.hasText(city))
            expression = expression.and(group.city.eq(city));

        if (StringUtils.hasText(town))
            expression = expression.and(group.town.eq(town));

        return expression;
    }

    /**
     * 페이징 객체(Pageable)에 포함된 정렬 조건에 따라 OrderSpecifier[] 생성
     *
     * @param pageable - 페이징 객체
     * @param group    - QGroup
     * @return OrderSpecifier[]
     */
    private OrderSpecifier<?>[] getSortCondition(@NotNull final Pageable pageable, @NotNull final QGroup group) {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        if (!pageable.getSort().isEmpty()) {
            pageable.getSort().forEach(order -> {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

                switch (order.getProperty()) {
                    case "name":
                        orderSpecifiers.add(new OrderSpecifier(direction, group.name));
                        break;
                    case "recruitStatus":
                        orderSpecifiers.add(new OrderSpecifier(direction, group.recruitStatus));
                        break;
                    case "maxRecruitCount":
                        orderSpecifiers.add(new OrderSpecifier(direction, group.maxRecruitCount));
                        break;
                    case "createdAt":
                        orderSpecifiers.add(new OrderSpecifier(direction, group.createdAt));
                        break;
                    case "modifiedAt":
                        orderSpecifiers.add(new OrderSpecifier(direction, group.modifiedAt));
                        break;
                    default:
                        break;
                }
            });
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

}

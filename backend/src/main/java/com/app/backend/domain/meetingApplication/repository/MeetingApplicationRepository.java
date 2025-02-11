package com.app.backend.domain.meetingApplication.repository;

import com.app.backend.domain.meetingApplication.entity.MeetingApplication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingApplicationRepository extends JpaRepository<MeetingApplication, Long> {

    List<MeetingApplication> findByGroupIdAndDisabled(Long groupId, boolean disabled);

    Optional<MeetingApplication> findByGroupIdAndIdAndDisabled(Long groupId, Long meetingApplicationId,
                                                               boolean disabled);

    Optional<MeetingApplication> findByIdAndDisabled(Long id, boolean disabled);

    Optional<MeetingApplication> findByGroup_IdAndMember_IdAndDisabled(Long groupId, Long memberId, boolean disabled);
}

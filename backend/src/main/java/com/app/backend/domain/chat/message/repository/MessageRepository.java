package com.app.backend.domain.chat.message.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.app.backend.domain.chat.message.entity.Message;

public interface MessageRepository extends MongoRepository<Message, ObjectId>, MessageRepositoryCustom {
}

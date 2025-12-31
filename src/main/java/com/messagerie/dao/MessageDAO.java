package com.messagerie.dao;

import com.messagerie.model.Message;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageDAO extends GenericDAO<Message> {
    List<Message> findByUserId(int userId);
    List<Message> findRecent(int limit);
    List<Message> findBetweenDates(LocalDateTime start, LocalDateTime end);
    List<Message> findLastMessages(int count);
}
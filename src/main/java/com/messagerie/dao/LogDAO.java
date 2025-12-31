package com.messagerie.dao;

import com.messagerie.model.Log;
import java.util.List;

public interface LogDAO extends GenericDAO<Log> {
    List<Log> findByUserId(int userId);
    List<Log> findByType(String type);
    List<Log> findRecent(int limit);
}